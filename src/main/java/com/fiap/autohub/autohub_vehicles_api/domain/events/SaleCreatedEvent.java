package com.fiap.autohub.autohub_vehicles_api.domain.events;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleCreatedEvent(
        UUID saleId,
        UUID vehicleId,
        String userId,
        BigDecimal price
) {
}
