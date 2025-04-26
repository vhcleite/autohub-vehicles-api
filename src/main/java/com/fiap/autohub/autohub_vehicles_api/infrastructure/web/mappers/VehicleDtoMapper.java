package com.fiap.autohub.autohub_vehicles_api.infrastructure.web.mappers;

import com.fiap.autohub.autohub_vehicles_api.domain.commands.CreateVehicleCommand;
import com.fiap.autohub.autohub_vehicles_api.domain.commands.UpdateVehicleCommand;
import com.fiap.autohub.autohub_vehicles_api.domain.entities.Vehicle;
import com.fiap.autohub.autohub_vehicles_api.domain.entities.VehicleStatus;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.web.dtos.CreateVehicleRequestDto;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.web.dtos.UpdateVehicleRequestDto;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.web.dtos.VehicleResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface VehicleDtoMapper {

    CreateVehicleCommand toCreateCommand(CreateVehicleRequestDto dto);

    @Mapping(target = "expectedVersion", source = "version")
    UpdateVehicleCommand toUpdateCommand(UpdateVehicleRequestDto dto);

    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "offsetToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "offsetToString")
    VehicleResponseDto toResponseDto(Vehicle vehicle);

    @Named("statusToString")
    default String statusToString(VehicleStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("offsetToString")
    default String offsetToString(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null;
    }
}