package com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "vehicle_audit_log")
public class VehicleAuditLogPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "change_timestamp", nullable = false)
    private OffsetDateTime changeTimestamp;

    @Column(name = "operation_type", nullable = false, length = 10)
    private String operationType;

    @Column(name = "changed_by_user_id")
    private String changedByUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    // Mapeia para o tipo JSONB do Postgres (requer dependência hibernate-types se não for padrão)
    @Column(name = "vehicle_data_snapshot", columnDefinition = "jsonb", nullable = false)
    private String vehicleDataSnapshot;

    public VehicleAuditLogPersistenceEntity() {
        this.changeTimestamp = OffsetDateTime.now(ZoneOffset.UTC); // Default timestamp
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public OffsetDateTime getChangeTimestamp() {
        return changeTimestamp;
    }

    public void setChangeTimestamp(OffsetDateTime changeTimestamp) {
        this.changeTimestamp = changeTimestamp;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(String changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getVehicleDataSnapshot() {
        return vehicleDataSnapshot;
    }

    public void setVehicleDataSnapshot(String vehicleDataSnapshot) {
        this.vehicleDataSnapshot = vehicleDataSnapshot;
    }
}