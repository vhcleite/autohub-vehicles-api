package com.fiap.autohub.autohub_vehicles_api.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento: Falha na Criação da Cobrança (Recebido da Charges API).
 */
public record ChargeCreationFailedEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType, // "ChargeCreationFailed"
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source, // "charges-api"
        @JsonProperty("data") CcfData data
) {
    public record CcfData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("vehicle_id") UUID vehicleId,
            @JsonProperty("reason") String reason
    ) {
    }

    public UUID getSaleId() {
        return (data != null) ? data.saleId() : null;
    }

    public String getReason() {
        return (data != null) ? data.reason() : null;
    }

    public UUID getVehicleId() {
        return (data != null) ? data.vehicleId() : null;
    }
}
