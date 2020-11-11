package com.heiyigame.websocketgateway.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class TestController {
    public Mono<ServerResponse> initInfo(ServerRequest serverRequest){
        Mono<String> initInfo= Mono.just("wangyu");
        return ok().contentType(MediaType.APPLICATION_STREAM_JSON).body(initInfo,String.class);
    }
}
