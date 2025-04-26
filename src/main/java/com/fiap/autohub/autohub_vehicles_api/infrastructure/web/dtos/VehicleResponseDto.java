package com.fiap.autohub.autohub_vehicles_api.infrastructure.web.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Representa os dados de um veículo na resposta da API")
public class VehicleResponseDto {

    @Schema(description = "ID único do veículo", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID id;

    @Schema(description = "Marca do veículo", example = "Volkswagen")
    private String make;

    @Schema(description = "Modelo do veículo", example = "Gol")
    private String model;

    @Schema(description = "Ano de fabricação/modelo", example = "2020")
    private Integer year;

    @Schema(description = "Cor do veículo", example = "Prata")
    private String color;

    @Schema(description = "Preço do veículo", example = "55000.90")
    private BigDecimal price;

    @Schema(description = "Descrição opcional do veículo", example = "Único dono, baixa km")
    private String description;

    @Schema(description = "Status atual do veículo", example = "AVAILABLE")
    private String status; // Retorna como String para o cliente

    @Schema(description = "ID do proprietário do veículo (Cognito Sub)", example = "a1b2c3d4-...")
    private String ownerId;

    @Schema(description = "Versão atual do registro (para controle de concorrência)", example = "0")
    private Long version;

    @Schema(description = "Data e hora de criação do registro (ISO 8601 UTC)", example = "2025-04-21T02:09:08.123Z")
    private String createdAt; // Retorna como String formatada

    @Schema(description = "Data e hora da última atualização (ISO 8601 UTC)", example = "2025-04-21T02:10:15.456Z")
    private String updatedAt; // Retorna como String formatada

    // Construtor Padrão, Getters e Setters
    public VehicleResponseDto() {
    }

    // Getters e Setters para todos os campos...
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}