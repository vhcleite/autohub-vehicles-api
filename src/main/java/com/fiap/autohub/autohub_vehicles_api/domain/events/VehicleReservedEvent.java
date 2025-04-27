package com.fiap.autohub.autohub_vehicles_api.domain.events;

import java.util.UUID;

public record VehicleReservedEvent(
        UUID saleId,
        UUID vehicleId
) {
}
