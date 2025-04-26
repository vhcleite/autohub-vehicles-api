package com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.mappers;

import com.fiap.autohub.autohub_vehicles_api.domain.entities.Vehicle;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.entities.VehiclePersistenceEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehiclePersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VehiclePersistenceEntity toPersistenceEntity(Vehicle vehicle);

    @InheritInverseConfiguration
    Vehicle toDomainEntity(VehiclePersistenceEntity entity);

}