package com.fiap.autohub.autohub_vehicles_api.domain.events; // Ajuste o pacote se necessário

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Evento publicado quando ocorre uma falha ao tentar reservar um veículo.
 * Segue o padrão de Envelope.
 */
public record VehicleReservationFailedEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType, // "VehicleReservationFailed"
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source, // "vehicles-api"
        @JsonProperty("data") EventData data
) {
    // Construtor para facilitar a criação a partir da lógica de negócio
    public VehicleReservationFailedEvent(UUID saleId, UUID vehicleId, String reason) {
        this(
                UUID.randomUUID(),
                "VehicleReservationFailed",
                OffsetDateTime.now(ZoneOffset.UTC),
                "vehicles-api",
                new EventData(saleId, vehicleId, reason)
        );
    }

    /**
     * Dados específicos para o evento VehicleReservationFailed.
     */
    public record EventData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("vehicle_id") UUID vehicleId,
            @JsonProperty("reason") String reason // Motivo da falha
    ) {
    }

    // Métodos utilitários (opcional, mas pode ser útil para consumidores)
    public UUID getSaleId() {
        return (data != null) ? data.saleId() : null;
    }

    public UUID getVehicleId() {
        return (data != null) ? data.vehicleId() : null;
    }

    public String getReason() {
        return (data != null) ? data.reason() : null;
    }
}
