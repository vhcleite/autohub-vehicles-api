package com.fiap.autohub.autohub_vehicles_api.domain.entities;

public enum VehicleStatus {
    AVAILABLE, // Disponível para venda
    RESERVED,  // Reservado durante processo de compra
    SOLD,      // Vendido
    DELETED    // Deleção lógica
}