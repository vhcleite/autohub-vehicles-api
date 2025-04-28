package com.fiap.autohub.autohub_vehicles_api;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.messaging.listeners.SaleCreatedListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

@SpringBootApplication
public class AutohubVehiclesApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutohubVehiclesApiApplication.class, args);
    }


    @Bean
    public Consumer<SQSEvent> saleCreatedConsumer(SaleCreatedListener listener) {
        return listener::handleSaleCreatedEvent;
    }
}
