package com.fiap.autohub.autohub_vehicles_api;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.messaging.consumers.VehicleEventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

@SpringBootApplication
public class AutohubVehiclesApiApplication {

    private static final Logger log = LoggerFactory.getLogger(AutohubVehiclesApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AutohubVehiclesApiApplication.class, args);
    }

    @Bean
    public Consumer<SQSEvent> vehicleEventsConsumer(VehicleEventConsumer consumerLogic) {
        log.info("Creating vehicleEventsConsumer bean for SQS profile.");
        return consumerLogic::consumeEvent;
    }
}
