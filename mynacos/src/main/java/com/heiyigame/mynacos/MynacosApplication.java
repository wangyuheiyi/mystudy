package com.heiyigame.mynacos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MynacosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MynacosApplication.class, args);
    }

}
