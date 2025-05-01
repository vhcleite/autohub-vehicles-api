package com.fiap.autohub.autohub_vehicles_api.infrastructure.messaging.consumers; // Verifique o pacote

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.autohub.autohub_vehicles_api.domain.events.*;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.messaging.listeners.PaymentEventsListener;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.messaging.listeners.SaleCreatedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Componente central que recebe eventos SQS e os roteia para os listeners apropriados
 * com base no eventType. Chamado pelo @Bean Consumer<SQSEvent>.
 */
@Component
public class VehicleEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(VehicleEventConsumer.class);

    private final SaleCreatedListener saleCreatedListener;
    private final PaymentEventsListener paymentEventsListener;
    private final ObjectMapper objectMapper;

    public VehicleEventConsumer(SaleCreatedListener saleCreatedListener,
                                PaymentEventsListener paymentEventsListener,
                                ObjectMapper objectMapper) {
        this.saleCreatedListener = saleCreatedListener;
        this.paymentEventsListener = paymentEventsListener;
        this.objectMapper = objectMapper;
    }

    /**
     * Ponto de entrada principal para processar eventos SQS.
     *
     * @param sqsEvent O evento SQS recebido.
     */
    public void consumeEvent(SQSEvent sqsEvent) {
        if (sqsEvent == null || sqsEvent.getRecords() == null) {
            log.warn("Received null or empty SQSEvent in VehicleEventConsumer.");
            return;
        }
        log.info("Processing SQS event with {} record(s) in VehicleEventConsumer.", sqsEvent.getRecords().size());
        List<SQSEvent.SQSMessage> messages = sqsEvent.getRecords();

        for (SQSEvent.SQSMessage message : messages) {
            String messageId = message.getMessageId();
            String messageBody = message.getBody();
            log.debug("Processing message ID: {}, Body: {}", messageId, messageBody);

            try {
                JsonNode rootNode = objectMapper.readTree(messageBody);
                String eventType = rootNode.path("event_type").asText(null);

                if (eventType == null) {
                    log.error("Received message (ID: {}) without 'eventType' field: {}", messageId, messageBody);
                    throw new IllegalArgumentException("Missing eventType in message body for message ID: " + messageId);
                }

                log.info("Routing event (Message ID: {}) based on eventType: {}", messageId, eventType);

                // Roteamento baseado no eventType
                switch (eventType) {
                    case "SaleCreated":
                        SaleCreatedEvent sce = objectMapper.readValue(messageBody, SaleCreatedEvent.class);
                        // Chama o método do listener original, mas sem a anotação SQSListener
                        saleCreatedListener.handleSaleCreatedEvent(sce);
                        break;

                    case "PaymentCompleted":
                        PaymentCompletedEvent pce = objectMapper.readValue(messageBody, PaymentCompletedEvent.class);
                        // Chama o método do listener original
                        paymentEventsListener.handlePaymentCompletedEvent(pce);
                        break;

                    case "PaymentFailed":
                        PaymentFailedEvent pfe = objectMapper.readValue(messageBody, PaymentFailedEvent.class);
                        paymentEventsListener.handlePaymentFailedEvent(pfe);
                        break;

                    case "ChargeCreationFailed":
                        ChargeCreationFailedEvent ccfe = objectMapper.readValue(messageBody, ChargeCreationFailedEvent.class);
                        paymentEventsListener.handleChargeCreationFailedEvent(ccfe);
                        break;

                    case "ChargeExpired":
                        ChargeExpiredEvent cee = objectMapper.readValue(messageBody, ChargeExpiredEvent.class);
                        paymentEventsListener.handleChargeExpiredEvent(cee);
                        break;

                    default:
                        log.warn("Received unhandled eventType '{}' for message ID: {}", eventType, messageId);
                        break;
                }
                log.debug("Finished processing message ID: {} for eventType: {}", messageId, eventType);

            } catch (JsonProcessingException e) {
                log.error("Failed to parse message body (Message ID: {}): {}", messageId, messageBody, e);
                throw new RuntimeException("Message parsing failed for message ID: " + messageId, e);
            } catch (Exception e) {
                log.error("Failed to process message (Message ID: {}): {}", messageId, messageBody, e);
                throw new RuntimeException("Message processing failed for message ID: " + messageId, e);
            }
        }
        log.info("Finished processing batch of {} message(s) in VehicleEventConsumer.", messages.size());
    }
}
