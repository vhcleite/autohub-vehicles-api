package com.fiap.autohub.autohub_vehicles_api.infrastructure.messaging.listeners; // Ajuste o pacote

// Removido: import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import com.fiap.autohub.autohub_vehicles_api.domain.events.ChargeCreationFailedEvent;
import com.fiap.autohub.autohub_vehicles_api.domain.events.ChargeExpiredEvent;
import com.fiap.autohub.autohub_vehicles_api.domain.events.PaymentCompletedEvent;
import com.fiap.autohub.autohub_vehicles_api.domain.events.PaymentFailedEvent;
import com.fiap.autohub.autohub_vehicles_api.domain.ports.in.VehicleServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Listener (agora chamado pelo VehicleEventConsumer) para eventos relacionados
 * ao resultado do pagamento (Completed, Failed, ChargeCreationFailed, ChargeExpired).
 */
@Component
public class PaymentEventsListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventsListener.class);

    private final VehicleServicePort vehicleService;

    public PaymentEventsListener(VehicleServicePort vehicleService) {
        this.vehicleService = vehicleService;
    }


    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        if (event == null || event.data() == null || event.getVehicleId() == null) {
            log.error("handlePaymentCompletedEvent called with invalid event data.");
            return;
        }
        UUID vehicleId = event.getVehicleId();
        log.info("Processing PaymentCompletedEvent for vehicleId: {}", vehicleId);
        try {
            vehicleService.markVehicleAsSold(vehicleId);
        } catch (Exception e) {
            log.error("Failed to process PaymentCompletedEvent for vehicleId {}: {}", vehicleId, e.getMessage(), e);
            throw new RuntimeException("Failed to mark vehicle as sold for vehicleId: " + vehicleId, e);
        }
    }

    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        if (event == null || event.data() == null || event.getVehicleId() == null) {
            log.error("handlePaymentFailedEvent called with invalid event data.");
            return;
        }
        UUID vehicleId = event.getVehicleId();
        log.warn("Processing PaymentFailedEvent for vehicleId: {}. Unreserving.", vehicleId);
        try {
            vehicleService.unreserveVehicle(vehicleId);
        } catch (Exception e) {
            log.error("Failed to process PaymentFailedEvent for vehicleId {}: {}", vehicleId, e.getMessage(), e);
            throw new RuntimeException("Failed to unreserve vehicle after payment failure for vehicleId: " + vehicleId, e);
        }
    }

    public void handleChargeCreationFailedEvent(ChargeCreationFailedEvent event) {
        if (event == null || event.data() == null || event.getVehicleId() == null) {
            log.error("handleChargeCreationFailedEvent called with invalid event data.");
            return;
        }
        UUID vehicleId = event.getVehicleId();
        log.warn("Processing ChargeCreationFailedEvent for vehicleId: {}. Unreserving.", vehicleId);
        try {
            vehicleService.unreserveVehicle(vehicleId);
        } catch (Exception e) {
            log.error("Failed to process ChargeCreationFailedEvent for vehicleId {}: {}", vehicleId, e.getMessage(), e);
            throw new RuntimeException("Failed to unreserve vehicle after charge creation failure for vehicleId: " + vehicleId, e);
        }
    }

    public void handleChargeExpiredEvent(ChargeExpiredEvent event) {
        if (event == null || event.data() == null || event.getVehicleId() == null) {
            log.error("handleChargeExpiredEvent called with invalid event data.");
            return;
        }
        UUID vehicleId = event.getVehicleId();
        log.warn("Processing ChargeExpiredEvent for vehicleId: {}. Unreserving.", vehicleId);
        try {
            vehicleService.unreserveVehicle(vehicleId);
        } catch (Exception e) {
            log.error("Failed to process ChargeExpiredEvent for vehicleId {}: {}", vehicleId, e.getMessage(), e);
            throw new RuntimeException("Failed to unreserve vehicle after charge expiration for vehicleId: " + vehicleId, e);
        }
    }
}
