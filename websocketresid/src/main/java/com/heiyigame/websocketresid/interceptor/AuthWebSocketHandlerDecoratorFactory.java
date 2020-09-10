package com.heiyigame.websocketresid.interceptor;

import com.shanggame.websocketresid.reactiveutil.RedisReactiveUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.security.Principal;

/**
 * @author admin
 */
@Component
@Slf4j
public class AuthWebSocketHandlerDecoratorFactory implements WebSocketHandlerDecoratorFactory {
    @Autowired
    RedisReactiveUtil redisReactiveUtil;
    @Override
    public WebSocketHandler decorate(WebSocketHandler webSocketHandler) {
        return new WebSocketHandlerDecorator(webSocketHandler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
                // 客户端与服务器端建立连接后，此处记录谁上线了
                Principal principal = webSocketSession.getPrincipal();
                if(principal != null){
                    String username = principal.getName();
                    log.info("websocket online: " + username + " session " + webSocketSession.getId());
                    redisReactiveUtil.set(username, webSocketSession.getId());
                    redisReactiveUtil.get(username).subscribe(s-> log.info("websocket online: " + username + " session "+s));
                }
                super.afterConnectionEstablished(webSocketSession);
            }

            @Override
            public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {

            }

            @Override
            public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {

            }

            @Override
            public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
                // 客户端与服务器端断开连接后，此处记录谁下线了
                Principal principal = webSocketSession.getPrincipal();
                if(principal != null){
                    String username = webSocketSession.getPrincipal().getName();
                    log.info("websocket offline: " + username);
                    redisReactiveUtil.del(username);
                }
                super.afterConnectionClosed(webSocketSession, closeStatus);
            }

            @Override
            public boolean supportsPartialMessages() {
                return false;
            }
        };
    }
}
