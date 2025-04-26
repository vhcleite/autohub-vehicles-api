package com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.autohub.autohub_vehicles_api.domain.entities.Vehicle;
import com.fiap.autohub.autohub_vehicles_api.domain.entities.VehicleStatus;
import com.fiap.autohub.autohub_vehicles_api.domain.exceptions.VehicleNotFoundException;
import com.fiap.autohub.autohub_vehicles_api.domain.ports.out.VehicleRepositoryPort;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.entities.VehicleAuditLogPersistenceEntity;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.entities.VehiclePersistenceEntity;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.mappers.VehiclePersistenceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PostgresVehicleRepositoryAdapter implements VehicleRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(PostgresVehicleRepositoryAdapter.class);

    private final JpaVehicleRepository jpaRepository;
    private final JpaVehicleAuditLogRepository auditLogRepository; // Injetar Repositório de Auditoria
    private final VehiclePersistenceMapper mapper;
    private final ObjectMapper objectMapper; // Injetar ObjectMapper

    public PostgresVehicleRepositoryAdapter(JpaVehicleRepository jpaRepository,
                                            JpaVehicleAuditLogRepository auditLogRepository,
                                            VehiclePersistenceMapper mapper,
                                            ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.auditLogRepository = auditLogRepository;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Vehicle save(Vehicle vehicle) {
        boolean isCreating = vehicle.id() == null;
        String operationType = isCreating ? "CREATE" : "UPDATE";

        VehiclePersistenceEntity entityToSave = mapper.toPersistenceEntity(vehicle);

        // Garante versão inicial se for criação
        if (isCreating && entityToSave.getVersion() == null) {
            entityToSave.setVersion(0L);
        }
        // @CreationTimestamp/@UpdateTimestamp cuidam das datas

        VehiclePersistenceEntity savedEntity = jpaRepository.save(entityToSave); // save é suficiente aqui

        // Salva o log de auditoria
        saveAuditLogInternal(savedEntity, operationType, savedEntity.getOwnerId());

        return mapper.toDomainEntity(savedEntity);
    }

    @Override
    @Transactional
    public void deleteLogically(Vehicle vehicle) {
        VehiclePersistenceEntity entityToDelete = jpaRepository.findById(vehicle.id())
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found for logical delete: " + vehicle.id()));

        entityToDelete.setStatus(VehicleStatus.DELETED);
        // A transação ao fazer commit fará o UPDATE. @Version e @UpdateTimestamp serão atualizados.

        // Salva o log de auditoria DEPOIS da modificação (antes do commit)
        saveAuditLogInternal(entityToDelete, "DELETE", entityToDelete.getOwnerId());
    }


    private void saveAuditLogInternal(VehiclePersistenceEntity entity, String operation, String userId) {
        try {
            String snapshot = objectMapper.writeValueAsString(entity);

            VehicleAuditLogPersistenceEntity logEntry = new VehicleAuditLogPersistenceEntity();
            logEntry.setVehicleId(entity.getId());
            logEntry.setOperationType(operation);
            logEntry.setChangedByUserId(userId);
            logEntry.setVehicleDataSnapshot(snapshot);

            auditLogRepository.save(logEntry);

        } catch (JsonProcessingException e) {
            logger.error("CRITICAL: Failed to serialize vehicle snapshot for audit log during {} operation. Vehicle ID: {}", operation, entity.getId(), e);
        } catch (Exception e) {
            logger.error("CRITICAL: Failed to save audit log during {} operation for vehicle ID: {}", operation, entity.getId(), e);
        }
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomainEntity);
    }

    @Override
    public Optional<Vehicle> findByIdAndOwnerId(UUID id, String ownerId) {
        return jpaRepository.findByIdAndOwnerId(id, ownerId).map(mapper::toDomainEntity);
    }

    @Override
    public Page<Vehicle> findAll(Specification<VehiclePersistenceEntity> spec, Pageable pageable) {
        Page<VehiclePersistenceEntity> entityPage = jpaRepository.findAll(spec, pageable);
        return entityPage.map(mapper::toDomainEntity);
    }

    @Override
    public List<Vehicle> findByOwnerId(String ownerId) {
        return jpaRepository.findByOwnerId(ownerId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}