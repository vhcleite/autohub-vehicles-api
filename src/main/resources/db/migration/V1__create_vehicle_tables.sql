-- Script Flyway V1: Cria as tabelas iniciais para veículos

-- Tabela vehicles
CREATE TABLE vehicles (
    id UUID PRIMARY KEY,
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    color VARCHAR(50) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    owner_id VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para otimizar buscas e ordenações comuns
CREATE INDEX idx_vehicles_status_price ON vehicles (status, price);
CREATE INDEX idx_vehicles_make ON vehicles (make);
CREATE INDEX idx_vehicles_model ON vehicles (model);
CREATE INDEX idx_vehicles_year ON vehicles (year);
CREATE INDEX idx_vehicles_owner_id ON vehicles (owner_id);
CREATE INDEX idx_vehicles_status ON vehicles (status);

-- Tabela vehicle_audit_log
CREATE TABLE vehicle_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    vehicle_id UUID NOT NULL,
    change_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operation_type VARCHAR(10) NOT NULL, -- 'CREATE', 'UPDATE', 'DELETE'
    changed_by_user_id VARCHAR(255),
    vehicle_data_snapshot JSONB NOT NULL -- Snapshot do registro completo
);

-- Índice para buscar histórico por veículo
CREATE INDEX idx_vehicle_audit_log_vehicle_id ON vehicle_audit_log (vehicle_id);
-- Índice para buscar por timestamp
CREATE INDEX idx_vehicle_audit_log_timestamp ON vehicle_audit_log (change_timestamp);
