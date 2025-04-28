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

    public void handleSaleCreatedEvent(SQSEvent messagePayload) {
        SaleCreatedEvent event = null;
        logger.info("Received potential SaleCreatedEvent message: {}", messagePayload);
        List<SQSEvent.SQSMessage> messages = messagePayload.getRecords();
        for (SQSEvent.SQSMessage message : messages) {
            try {
                event = objectMapper.readValue(message.getBody(), SaleCreatedEvent.class);
                logger.info("Processing SaleCreatedEvent for saleId: {}, vehicleId: {}", event.saleId(), event.vehicleId());

                vehicleService.reserveVehicle(event.vehicleId(), event.saleId());

                logger.info("Successfully processed SaleCreatedEvent for vehicleId: {}", event.vehicleId());

            } catch (OptimisticLockingException | VehicleUpdateForbiddenException | VehicleNotFoundException e) {
                logger.warn("HANDLED business error processing SaleCreatedEvent for saleId {}: {}",
                        (event != null ? event.saleId() : "unknown"), e.getMessage());

                VehicleReservationFailedEvent failureEvent = new VehicleReservationFailedEvent(
                        event.saleId(),
                        event.vehicleId(),
                        e.getMessage()
                );
                eventPublisher.publishVehicleReservationFailed(failureEvent);
                logger.info("Published VehicleReservationFailed event for saleId {}", event.saleId());

            } catch (JsonProcessingException e) {
                logger.error("Failed to parse SaleCreatedEvent message: {}", messagePayload, e);
                throw new RuntimeException("Message parsing failed", e);
            } catch (Exception e) {
                logger.error("Failed to process SaleCreatedEvent for vehicleId {}: {}",
                        extractVehicleId(message.getBody()),
                        e.getMessage());
                throw new RuntimeException("Vehicle reservation processing failed", e); // Relança para DLQ
            }
        }

    }

    private String extractVehicleId(String payload) {
        try {
            return objectMapper.readTree(payload).path("vehicleId").asText("unknown");
        } catch (Exception e) {
            return "unknown";
        }
    }
}