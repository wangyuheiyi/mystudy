package com.heiyigame.mynacos.webinfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
/**
 * @author admin
 */
@Configuration
public class RouterConfig {
    @Autowired
    private MyNacosController wbTestInfoClt;
    @Bean
    public RouterFunction<ServerResponse> timerRouter() {
        return route(GET("/init").and(accept(MediaType.APPLICATION_JSON)), wbTestInfoClt::initInfo);
    }
}
