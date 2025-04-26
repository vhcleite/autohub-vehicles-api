package com.fiap.autohub.autohub_vehicles_api.domain.ports.out;

import com.fiap.autohub.autohub_vehicles_api.domain.entities.Vehicle;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.entities.VehiclePersistenceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepositoryPort {
    Vehicle save(Vehicle vehicle); // Create ou Update

    Optional<Vehicle> findById(UUID id);

    Optional<Vehicle> findByIdAndOwnerId(UUID id, String ownerId); // Para update/delete seguro

    Page<Vehicle> findAll(Specification<VehiclePersistenceEntity> spec, Pageable pageable); // Para listagem filtrada/ordenada

    List<Vehicle> findByOwnerId(String ownerId);

    void deleteLogically(Vehicle vehicle); // Recebe entidade para auditoria fácil

    boolean existsById(UUID id); // Útil para verificar antes de deletar
}