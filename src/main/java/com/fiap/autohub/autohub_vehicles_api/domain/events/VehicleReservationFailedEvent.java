package com.fiap.autohub.autohub_vehicles_api.domain.events;

import java.util.UUID;

public record VehicleReservationFailedEvent(
        UUID saleId,
        UUID vehicleId,
        String reason
) {
}
