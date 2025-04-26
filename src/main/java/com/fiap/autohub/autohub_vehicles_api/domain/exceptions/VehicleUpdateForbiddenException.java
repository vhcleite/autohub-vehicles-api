package com.fiap.autohub.autohub_vehicles_api.domain.exceptions;

public class VehicleUpdateForbiddenException extends RuntimeException {
    public VehicleUpdateForbiddenException(String message) {
        super(message);
    }
}