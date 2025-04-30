package com.fiap.autohub.autohub_vehicles_api.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Evento publicado quando um veículo é reservado com sucesso.
 */
public record VehicleReservedEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType, // "VehicleReserved"
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source, // "vehicles-api"
        @JsonProperty("data") EventData data
) {
    // Construtor para facilitar a criação a partir da lógica de negócio
    public VehicleReservedEvent(UUID saleId, UUID vehicleId) {
        this(
                UUID.randomUUID(),
                "VehicleReserved",
                OffsetDateTime.now(ZoneOffset.UTC),
                "vehicles-api",
                new EventData(saleId, vehicleId)
        );
    }

    /**
     * Dados específicos para o evento VehicleReserved.
     */
    public record EventData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("vehicle_id") UUID vehicleId
    ) {
    }

    public UUID getSaleId() {
        return (data != null) ? data.saleId() : null;
    }

    public UUID getVehicleId() {
        return (data != null) ? data.vehicleId() : null;
    }
}
