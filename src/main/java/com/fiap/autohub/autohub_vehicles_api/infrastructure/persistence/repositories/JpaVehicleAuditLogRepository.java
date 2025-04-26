// Pacote: infrastructure.persistence.repositories
package com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.repositories;

import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.entities.VehicleAuditLogPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaVehicleAuditLogRepository extends JpaRepository<VehicleAuditLogPersistenceEntity, Long> {
}