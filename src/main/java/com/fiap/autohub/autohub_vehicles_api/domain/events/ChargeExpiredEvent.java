package com.fiap.autohub.autohub_vehicles_api.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento: Cobrança Expirada (Recebido da Charges API ou Timeout Lambda).
 */
public record ChargeExpiredEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType, // "ChargeExpired"
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source,
        @JsonProperty("data") CeData data
) {
    public record CeData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("vehicle_id") UUID vehicleId,
            @JsonProperty("charge_id") String chargeId
    ) {
    }

    public UUID getSaleId() {
        return (data != null) ? data.saleId() : null;
    }

    public String getChargeId() {
        return (data != null) ? data.chargeId() : null;
    }

    public UUID getVehicleId() {
        return (data != null) ? data.vehicleId() : null;
    }
}