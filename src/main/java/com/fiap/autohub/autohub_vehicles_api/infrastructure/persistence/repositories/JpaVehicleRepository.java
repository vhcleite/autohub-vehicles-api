package com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.repositories;

import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.entities.VehiclePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaVehicleRepository extends JpaRepository<VehiclePersistenceEntity, UUID>, JpaSpecificationExecutor<VehiclePersistenceEntity> {

    Optional<VehiclePersistenceEntity> findByIdAndOwnerId(UUID id, String ownerId);

    List<VehiclePersistenceEntity> findByOwnerId(String ownerId);
}