package com.fiap.autohub.autohub_vehicles_api.infrastructure.web.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreateVehicleRequestDto {

    @NotBlank(message = "Make cannot be blank")
    @Schema(description = "Marca do veículo", example = "Volkswagen", requiredMode = Schema.RequiredMode.REQUIRED)
    private String make;

    @NotBlank(message = "Model cannot be blank")
    @Schema(description = "Modelo do veículo", example = "Gol", requiredMode = Schema.RequiredMode.REQUIRED)
    private String model;

    @NotNull(message = "Year cannot be null")
    @Min(value = 1950, message = "Year must be after 1950")
    @Max(value = 2026, message = "Year cannot be in the future") // Ajuste o ano máximo
    @Schema(description = "Ano de fabricação/modelo", example = "2020", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer year;

    @NotBlank(message = "Color cannot be blank")
    @Schema(description = "Cor do veículo", example = "Prata", requiredMode = Schema.RequiredMode.REQUIRED)
    private String color;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    @Digits(integer = 10, fraction = 2, message = "Price format invalid")
    @Schema(description = "Preço do veículo", example = "55000.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "Descrição opcional do veículo", example = "Único dono, baixa km")
    private String description;

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
}