package com.fiap.autohub.autohub_vehicles_api.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento: Pagamento Concluído (Recebido da Charges API).
 */
public record PaymentCompletedEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType, // "PaymentCompleted"
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source, // "charges-api"
        @JsonProperty("data") PcData data
) {
    public record PcData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("vehicle_id") UUID vehicleId,
            @JsonProperty("charge_id") String chargeId,
            @JsonProperty("paid_at") OffsetDateTime paidAt
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
