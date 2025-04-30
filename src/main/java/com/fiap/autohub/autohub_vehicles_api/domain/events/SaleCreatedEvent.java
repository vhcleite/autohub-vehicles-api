package com.fiap.autohub.autohub_vehicles_api.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representa o evento SaleCreated recebido pela Vehicles API.
 */
public record SaleCreatedEvent(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("event_type") String eventType,
        @JsonProperty("timestamp") OffsetDateTime timestamp,
        @JsonProperty("source") String source,
        @JsonProperty("data") SaleData data // Objeto interno com os dados específicos
) {

    /**
     * Classe interna para os dados específicos da venda.
     */
    public record SaleData(
            @JsonProperty("sale_id") UUID saleId,
            @JsonProperty("vehicle_id") UUID vehicleId,
            @JsonProperty("buyer_user_id") String buyerUserId,
            @JsonProperty("seller_user_id") String sellerUserId,
            @JsonProperty("price") BigDecimal price
    ) {
    }
}