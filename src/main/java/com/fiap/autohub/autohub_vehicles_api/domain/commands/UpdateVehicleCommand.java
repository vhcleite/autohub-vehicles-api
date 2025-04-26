package com.fiap.autohub.autohub_vehicles_api.domain.commands;

import java.math.BigDecimal;

// Dados que PODEM ser atualizados (nuláveis indicam não alteração)
public record UpdateVehicleCommand(
        String make,
        String model,
        Integer year,
        String color,
        BigDecimal price,
        String description,
        Long expectedVersion // Para optimistic locking
) {
}