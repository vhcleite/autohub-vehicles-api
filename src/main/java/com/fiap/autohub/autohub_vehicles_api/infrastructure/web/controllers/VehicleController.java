package com.fiap.autohub.autohub_vehicles_api.infrastructure.web.controllers;

import com.fiap.autohub.autohub_vehicles_api.domain.commands.CreateVehicleCommand;
import com.fiap.autohub.autohub_vehicles_api.domain.commands.UpdateVehicleCommand;
import com.fiap.autohub.autohub_vehicles_api.domain.entities.Vehicle;
import com.fiap.autohub.autohub_vehicles_api.domain.ports.in.VehicleServicePort;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.entities.VehiclePersistenceEntity;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.specifications.VehicleSpecification;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.web.dtos.CreateVehicleRequestDto;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.web.dtos.ErrorResponse;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.web.dtos.UpdateVehicleRequestDto;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.web.dtos.VehicleResponseDto;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.web.mappers.VehicleDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Vehicle Management", description = "Endpoints para gerenciamento de veículos")
public class VehicleController {

    private final VehicleServicePort vehicleService;
    private final VehicleDtoMapper mapper;

    public VehicleController(VehicleServicePort vehicleService, VehicleDtoMapper mapper) {
        this.vehicleService = vehicleService;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo veículo para venda", description = "Cria um novo registro de veículo associado ao usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Veículo cadastrado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados faltando ou inválidos)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado (JWT inválido ou ausente)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VehicleResponseDto> createVehicle(
            @Valid @RequestBody CreateVehicleRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String ownerId = jwt.getSubject();
        CreateVehicleCommand command = mapper.toCreateCommand(requestDto);
        Vehicle createdVehicle = vehicleService.createVehicle(command, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseDto(createdVehicle));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um veículo existente", description = "Permite ao proprietário atualizar os dados de um veículo que esteja disponível para venda.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados inválidos)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Proibido (usuário não é dono ou status não permite atualização)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado para este usuário", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de versão (modificação concorrente)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VehicleResponseDto> updateVehicle(
            @Parameter(description = "ID do veículo a ser atualizado", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String ownerId = jwt.getSubject();
        UpdateVehicleCommand command = mapper.toUpdateCommand(requestDto);
        Vehicle updatedVehicle = vehicleService.updateVehicle(id, command, ownerId);
        return ResponseEntity.ok(mapper.toResponseDto(updatedVehicle));
    }

    @GetMapping("/available")
    @Operation(summary = "Lista veículos disponíveis para venda", description = "Retorna uma lista paginada de veículos com status AVAILABLE, com opções de filtro e ordenação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de veículos disponíveis retornada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação/ordenação inválidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<VehicleResponseDto>> listAvailableVehicles(
            @Parameter(description = "Filtrar por marca", required = false, in = ParameterIn.QUERY) @RequestParam Optional<String> make,
            @Parameter(description = "Filtrar por modelo", required = false, in = ParameterIn.QUERY) @RequestParam Optional<String> model,
            @Parameter(description = "Filtrar por ano mínimo", required = false, in = ParameterIn.QUERY) @RequestParam Optional<Integer> minYear,
            @Parameter(description = "Filtrar por ano máximo", required = false, in = ParameterIn.QUERY) @RequestParam Optional<Integer> maxYear,
            @Parameter(description = "Filtrar por cor", required = false, in = ParameterIn.QUERY) @RequestParam Optional<String> color,
            @Parameter(description = "Filtrar por preço mínimo", required = false, in = ParameterIn.QUERY) @RequestParam Optional<BigDecimal> minPrice,
            @Parameter(description = "Filtrar por preço máximo", required = false, in = ParameterIn.QUERY) @RequestParam Optional<BigDecimal> maxPrice,
            @ParameterObject @PageableDefault(size = 10, sort = "price", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Specification<VehiclePersistenceEntity> spec = Specification.where(VehicleSpecification.hasMake(make.orElse(null)))
                .and(VehicleSpecification.hasModel(model.orElse(null)))
                .and(VehicleSpecification.hasYearGreaterThanOrEqualTo(minYear.orElse(null)))
                .and(VehicleSpecification.hasYearLessThanOrEqualTo(maxYear.orElse(null)))
                .and(VehicleSpecification.hasColor(color.orElse(null)))
                .and(VehicleSpecification.hasPriceGreaterThanOrEqualTo(minPrice.orElse(null)))
                .and(VehicleSpecification.hasPriceLessThanOrEqualTo(maxPrice.orElse(null)));

        Page<Vehicle> vehiclePage = vehicleService.findAvailableVehicles(spec, pageable);
        Page<VehicleResponseDto> responseDtoPage = vehiclePage.map(mapper::toResponseDto);
        return ResponseEntity.ok(responseDtoPage);
    }

    @GetMapping("/sold")
    @Operation(summary = "Lista veículos vendidos", description = "Retorna uma lista paginada de veículos com status SOLD.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de veículos vendidos retornada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação/ordenação inválidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<VehicleResponseDto>> listSoldVehicles(
            @Parameter(description = "Filtrar por marca", required = false) @RequestParam Optional<String> make,
            @ParameterObject @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Specification<VehiclePersistenceEntity> spec = Specification.where(VehicleSpecification.hasMake(make.orElse(null)));
        Page<Vehicle> vehiclePage = vehicleService.findSoldVehicles(spec, pageable);
        Page<VehicleResponseDto> responseDtoPage = vehiclePage.map(mapper::toResponseDto);
        return ResponseEntity.ok(responseDtoPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um veículo por ID", description = "Retorna os detalhes de um veículo específico pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Veículo encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleResponseDto> getVehicleById(
            @Parameter(description = "ID do veículo (UUID)", required = true) @PathVariable UUID id) {
        return vehicleService.findVehicleById(id)
                .map(mapper::toResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-vehicles")
    @Operation(summary = "Lista os veículos cadastrados pelo usuário logado", description = "Retorna a lista de veículos associados ao usuário autenticado (exceto os deletados logicamente).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de veículos retornada", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VehicleResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<VehicleResponseDto>> getMyVehicles(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String ownerId = jwt.getSubject();
        List<Vehicle> vehicles = vehicleService.findVehiclesByOwner(ownerId);
        List<VehicleResponseDto> responseDtos = vehicles.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove (logicamente) um veículo", description = "Marca um veículo como 'DELETED'. Somente o proprietário pode fazer isso e apenas se o veículo estiver disponível.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Veículo marcado como deletado com sucesso", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Proibido (usuário não é dono ou status não permite exclusão)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado para este usuário", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "ID do veículo a ser deletado", required = true) @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        String ownerId = jwt.getSubject();
        vehicleService.deleteVehicleLogically(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}