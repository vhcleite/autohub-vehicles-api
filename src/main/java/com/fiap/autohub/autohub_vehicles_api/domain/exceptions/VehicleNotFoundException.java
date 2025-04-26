package com.fiap.autohub.autohub_vehicles_api.domain.exceptions;

import java.util.UUID;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(String message) {
        super(message);
    }

    public VehicleNotFoundException(UUID id) {
        super("Vehicle not found with id: " + id);
    }

    public VehicleNotFoundException(UUID id, String ownerId) {
        super("Vehicle not found with id: " + id + " for owner: " + ownerId);
    }
}