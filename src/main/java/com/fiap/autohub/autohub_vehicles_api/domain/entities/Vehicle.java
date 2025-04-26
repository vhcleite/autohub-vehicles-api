package com.fiap.autohub.autohub_vehicles_api.domain.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Vehicle(
        UUID id,
        String make,
        String model,
        Integer year,
        String color,
        BigDecimal price,
        String description,
        VehicleStatus status,
        String ownerId, // Cognito Sub
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}