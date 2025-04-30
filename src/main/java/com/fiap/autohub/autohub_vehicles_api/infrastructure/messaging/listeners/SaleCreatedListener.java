package com.fiap.autohub.autohub_vehicles_api.infrastructure.messaging.listeners;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.autohub.autohub_vehicles_api.domain.events.SaleCreatedEvent;
import com.fiap.autohub.autohub_vehicles_api.domain.events.VehicleReservationFailedEvent;
import com.fiap.autohub.autohub_vehicles_api.domain.exceptions.OptimisticLockingException;
import com.fiap.autohub.autohub_vehicles_api.domain.exceptions.VehicleNotFoundException;
import com.fiap.autohub.autohub_vehicles_api.domain.exceptions.VehicleUpdateForbiddenException;
import com.fiap.autohub.autohub_vehicles_api.domain.ports.in.VehicleServicePort;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.messaging.publishers.SNSEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SaleCreatedListener {

    private static final Logger logger = LoggerFactory.getLogger(SaleCreatedListener.class);

    private final VehicleServicePort vehicleService;
    private final ObjectMapper objectMapper;
    private final SNSEventPublisher eventPublisher;

    public SaleCreatedListener(VehicleServicePort vehicleService, ObjectMapper objectMapper, SNSEventPublisher eventPublisher) {
        this.vehicleService = vehicleService;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    public void handleSaleCreatedEvent(SQSEvent sqsEvent) {
        if (sqsEvent == null || sqsEvent.getRecords() == null) {
            logger.warn("Received null or empty SQSEvent in vehicles-api.");
            return;
        }
        logger.info("Received SQS event with {} record(s) in vehicles-api.", sqsEvent.getRecords().size());
        List<SQSEvent.SQSMessage> messages = sqsEvent.getRecords();

        for (SQSEvent.SQSMessage message : messages) {
            String messageId = message.getMessageId();
            String messageBody = message.getBody();
            logger.debug("Processing message ID: {}, Body: {}", messageId, messageBody);
            SaleCreatedEvent event = null;

            try {
                // Desserializa o CORPO da mensagem SQS para o evento SaleCreatedEvent
                event = objectMapper.readValue(messageBody, SaleCreatedEvent.class);
                if (event == null || event.data() == null || event.data().saleId() == null || event.data().vehicleId() == null) {
                    logger.error("Failed to parse essential data (saleId, vehicleId) from message body (Message ID: {}): {}", messageId, messageBody);
                    throw new JsonProcessingException("Parsed event or its data/ids are null") {
                    }; // Lança exceção para indicar falha no parsing
                }

                logger.info("Processing SaleCreatedEvent for saleId: {}, vehicleId: {}", event.data().saleId(), event.data().vehicleId());

                // Chama o serviço com os IDs corretos extraídos de event.data()
                vehicleService.reserveVehicle(event.data().vehicleId(), event.data().saleId());

                logger.info("Successfully processed SaleCreatedEvent for vehicleId: {}", event.data().vehicleId());

            } catch (OptimisticLockingException | VehicleUpdateForbiddenException | VehicleNotFoundException e) {
                // Erros de negócio esperados durante a reserva
                String reason = e.getMessage();
                UUID saleId = (event != null && event.data() != null) ? event.data().saleId() : null;
                UUID vehicleId = (event != null && event.data() != null) ? event.data().vehicleId() : null;

                logger.warn("HANDLED business error processing SaleCreatedEvent for saleId {}: {}", saleId, reason);

                // Publica o evento de falha
                if (saleId != null && vehicleId != null) {
                    try {
                        VehicleReservationFailedEvent failureEvent = new VehicleReservationFailedEvent(saleId, vehicleId, reason);
                        eventPublisher.publishVehicleReservationFailed(failureEvent);
                        logger.info("Published VehicleReservationFailed event for saleId {}", saleId);
                    } catch (Exception publishEx) {
                        logger.error("CRITICAL: Failed to publish VehicleReservationFailed event for saleId {} after processing failure. SQS Message (ID: {}) MAY BE ACKED!",
                                saleId, messageId, publishEx);
                        // Considerar relançar a exceção original 'e' aqui se a publicação da falha for crítica
                        // throw new RuntimeException("Vehicle reservation failed AND failed to publish failure event", e);
                    }
                } else {
                    logger.error("Cannot publish failure event because saleId or vehicleId is null from message ID: {}", messageId);
                }
                // NÃO relança a exceção 'e' aqui para dar ACK na mensagem original, conforme sua preferência

            } catch (JsonProcessingException e) {
                logger.error("Failed to parse SaleCreatedEvent message body (Message ID: {}): {}", messageId, messageBody, e);
                // Relança para a mensagem ir para DLQ
                throw new RuntimeException("Message parsing failed for message ID: " + messageId, e);
            } catch (Exception e) {
                // Erros inesperados durante o processamento
                UUID vehicleId = (event != null && event.data() != null) ? event.data().vehicleId() : null;
                logger.error("Unexpected error processing SaleCreatedEvent for vehicleId {} (Message ID: {}): {}",
                        vehicleId, messageId, e.getMessage(), e);
                // Relança para a mensagem ir para DLQ
                throw new RuntimeException("Unexpected vehicle reservation processing failed for message ID: " + messageId, e);
            }
        }
        logger.info("Finished processing batch of {} message(s) in vehicles-api.", messages.size());
    }
}