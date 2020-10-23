package com.heiyigame.websocketresid;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebsocketresidApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketresidApplication.class, args);
    }
}
