package com.fiap.autohub.autohub_vehicles_api.domain.ports.in;

import com.fiap.autohub.autohub_vehicles_api.domain.commands.CreateVehicleCommand;
import com.fiap.autohub.autohub_vehicles_api.domain.commands.UpdateVehicleCommand;
import com.fiap.autohub.autohub_vehicles_api.domain.entities.Vehicle;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.entities.VehiclePersistenceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleServicePort {
    Vehicle createVehicle(CreateVehicleCommand command, String ownerId);

    Vehicle updateVehicle(UUID id, UpdateVehicleCommand command, String ownerId);

    Optional<Vehicle> findVehicleById(UUID id);

    Page<Vehicle> findAvailableVehicles(Specification<VehiclePersistenceEntity> spec, Pageable pageable); // Usa Specification + Pageable

    Page<Vehicle> findSoldVehicles(Specification<VehiclePersistenceEntity> spec, Pageable pageable);

    List<Vehicle> findVehiclesByOwner(String ownerId); // Lista simples por owner

    void deleteVehicleLogically(UUID id, String ownerId);

    Vehicle reserveVehicle(UUID id, UUID saleId, BigDecimal price);

    Vehicle markVehicleAsSold(UUID id);

    void unreserveVehicle(UUID id); // Compensação
}