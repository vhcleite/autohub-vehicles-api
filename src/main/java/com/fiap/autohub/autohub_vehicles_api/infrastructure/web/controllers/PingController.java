package com.fiap.autohub.autohub_vehicles_api.infrastructure.web.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    private static final Logger logger = LoggerFactory.getLogger(PingController.class);

    @GetMapping("/vehicles/ping")
    public String ping() {
        logger.info("Ping endpoint invoked!");
        return "pong-2";
    }
}
