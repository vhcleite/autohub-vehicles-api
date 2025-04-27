package com.fiap.autohub.autohub_vehicles_api.infrastructure.messaging.publishers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.autohub.autohub_vehicles_api.domain.events.VehicleReservationFailedEvent;
import com.fiap.autohub.autohub_vehicles_api.domain.events.VehicleReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.HashMap;
import java.util.Map;

@Component
public class SNSEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SNSEventPublisher.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final String topicArn;

    public SNSEventPublisher(SnsClient snsClient,
                             ObjectMapper objectMapper,
                             @Value("${sns.topic.main-event-bus-arn}") String topicArn) { // Pega ARN do application.yml
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.topicArn = topicArn;
    }

    public void publishVehicleReserved(VehicleReservedEvent event) {
        publishEvent(event, "VehicleReserved");
    }

    public void publishVehicleReservationFailed(VehicleReservationFailedEvent event) {
        publishEvent(event, "VehicleReservationFailed");
    }

    private void publishEvent(Object eventPayload, String eventType) {
        try {
            String messageBody = objectMapper.writeValueAsString(eventPayload);
            logger.info("Publishing event type '{}' to SNS topic {}: {}", eventType, topicArn, messageBody);

            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("eventType", MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(eventType)
                    .build());

            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(messageBody)
                    .messageAttributes(messageAttributes)
                    .build();

            snsClient.publish(publishRequest);
            logger.info("Event type '{}' published successfully.", eventType);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize event payload for type {}: {}", eventType, eventPayload, e);
            throw new RuntimeException(e.getMessage());
        } catch (SnsException e) {
            logger.error("Failed to publish event type '{}' to SNS topic {}: {}", eventType, topicArn, e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
}