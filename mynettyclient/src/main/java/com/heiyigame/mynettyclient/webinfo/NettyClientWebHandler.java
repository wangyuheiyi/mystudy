package com.heiyigame.mynettyclient.webinfo;

import com.heiyigame.mynettyclient.bean.ResInfoBean;
import com.heiyigame.mynettyclient.service.NettyClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class NettyClientWebHandler {
    @Autowired
    NettyClientService nettyClientService;

    /**
     * ch
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> initClientConnect(ServerRequest serverRequest){
        Mono<ResInfoBean> resinfo=nettyClientService.initClient();
        return ok().contentType(MediaType.APPLICATION_STREAM_JSON).
                body(resinfo,ResInfoBean.class);
    }
}
