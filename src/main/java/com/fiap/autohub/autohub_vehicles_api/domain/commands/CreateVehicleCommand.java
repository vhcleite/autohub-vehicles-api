package com.fiap.autohub.autohub_vehicles_api.domain.commands;

import java.math.BigDecimal;

// Dados necessários para criar um veículo NOVO
public record CreateVehicleCommand(
        String make,
        String model,
        Integer year,
        String color,
        BigDecimal price,
        String description // Opcional?
) {
}