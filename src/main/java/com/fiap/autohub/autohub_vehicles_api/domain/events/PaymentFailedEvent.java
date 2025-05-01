package com.fiap.autohub.autohub_vehicles_api.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento: Falha no Pagamento (Recebido da Charges API).
 */
public record PaymentFailedEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType,
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source, // "charges-api"
        @JsonProperty("data") PfData data
) {
    public record PfData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("vehicle_id") UUID vehicleId,
            @JsonProperty("charge_id") String chargeId,
            @JsonProperty("reason") String reason
    ) {
    }

    // Métodos utilitários
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
