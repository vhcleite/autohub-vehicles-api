package com.fiap.autohub.autohub_vehicles_api.infrastructure.messaging.listeners;

// Removido: import com.amazonaws.services.lambda.runtime.events.SQSEvent;

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

import java.util.UUID;

@Component
public class SaleCreatedListener {

    private static final Logger logger = LoggerFactory.getLogger(SaleCreatedListener.class);

    private final VehicleServicePort vehicleService;
    private final SNSEventPublisher eventPublisher;

    public SaleCreatedListener(VehicleServicePort vehicleService,
                               SNSEventPublisher eventPublisher) {
        this.vehicleService = vehicleService;
        this.eventPublisher = eventPublisher;
    }

    public void handleSaleCreatedEvent(SaleCreatedEvent event) {
        if (event == null || event.data() == null || event.data().saleId() == null || event.data().vehicleId() == null) {
            logger.error("handleSaleCreatedEvent called with invalid event data.");
            return;
        }

        UUID saleId = event.data().saleId();
        UUID vehicleId = event.data().vehicleId();
        String messageId = event.eventId().toString();

        logger.info("Processing SaleCreatedEvent for saleId: {}, vehicleId: {}", saleId, vehicleId);

        try {
            vehicleService.reserveVehicle(vehicleId, saleId, event.data().price());

            logger.info("Successfully processed SaleCreatedEvent for vehicleId: {}", vehicleId);

        } catch (OptimisticLockingException | VehicleUpdateForbiddenException | VehicleNotFoundException e) {
            String reason = e.getMessage();
            logger.warn("HANDLED business error processing SaleCreatedEvent for saleId {}: {}", saleId, reason);

            try {
                VehicleReservationFailedEvent failureEvent = new VehicleReservationFailedEvent(saleId, vehicleId, reason);
                eventPublisher.publishVehicleReservationFailed(failureEvent);
                logger.info("Published VehicleReservationFailed event for saleId {}", saleId);
            } catch (Exception publishEx) {
                logger.error("CRITICAL: Failed to publish VehicleReservationFailed event for saleId {} after processing failure. Message ID: {}",
                        saleId, messageId, publishEx);
            }

        } catch (Exception e) {
            logger.error("Unexpected error processing SaleCreatedEvent for vehicleId {} (Message ID: {}): {}",
                    vehicleId, messageId, e.getMessage(), e);
            throw new RuntimeException("Unexpected vehicle reservation processing failed for message ID: " + messageId, e);
        }
    }
}
