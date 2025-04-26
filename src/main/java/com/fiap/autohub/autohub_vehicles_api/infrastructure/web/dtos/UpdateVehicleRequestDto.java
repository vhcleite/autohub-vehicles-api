package com.fiap.autohub.autohub_vehicles_api.infrastructure.web.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Dados que podem ser atualizados em um veículo. Envie apenas os campos que deseja alterar.")
public class UpdateVehicleRequestDto {

    @Schema(description = "Nova marca do veículo", example = "Volkswagen")
    private String make;

    @Schema(description = "Novo modelo do veículo", example = "Nivus")
    private String model;

    @Min(value = 1950, message = "Year must be after 1950")
    @Max(value = 2026, message = "Year cannot be in the future")
    @Schema(description = "Novo ano de fabricação/modelo", example = "2021")
    private Integer year;

    @Schema(description = "Nova cor do veículo", example = "Branco")
    private String color;

    @Positive(message = "Price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Price format invalid")
    @Schema(description = "Novo preço do veículo", example = "95000.00")
    private BigDecimal price;

    @Schema(description = "Nova descrição do veículo", example = "Todas revisões feitas")
    private String description;

    @NotNull(message = "Expected version cannot be null for update") // Crucial para optimistic locking
    @Schema(description = "Versão esperada do registro (para controle de concorrência)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long version;

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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}