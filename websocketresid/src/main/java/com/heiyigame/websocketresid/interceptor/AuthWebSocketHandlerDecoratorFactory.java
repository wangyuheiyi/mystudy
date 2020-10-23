package com.heiyigame.websocketresid.interceptor;

import com.heiyigame.websocketresid.reactiveutil.RedisReactiveUtil;
import com.heiyigame.websocketresid.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.security.Principal;

/**
 * @author admin
 */
@Component
public class AuthWebSocketHandlerDecoratorFactory implements WebSocketHandlerDecoratorFactory {
    @Autowired
    RedisReactiveUtil redisReactiveUtil;
    @Override
    public WebSocketHandler decorate(final WebSocketHandler webSocketHandler) {
        return new WebSocketHandlerDecorator(webSocketHandler) {
            @Override
            public void afterConnectionEstablished(final WebSocketSession webSocketSession) throws Exception {
                // 客户端与服务器端建立连接后，此处记录谁上线了
                Principal principal = webSocketSession.getPrincipal();
                if(principal != null){
                    String username = principal.getName();
                    String websocketSessionId=webSocketSession.getId();
                    LogUtil.mygame.info("websocket online: " + username + " session " + websocketSessionId);
                    redisReactiveUtil.set(username, websocketSessionId).subscribe(s-> {
                        if (s) {
                            LogUtil.mygame.info("save redis key: " + username + " valeu " + websocketSessionId);
                        }else{
                            LogUtil.mygame.info("save redis error key: " + username + " valeu " + websocketSessionId);
                        }
                    });
                }
                super.afterConnectionEstablished(webSocketSession);
            }

//            @Override
//            public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
//
//            }
//
//            @Override
//            public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
//
//            }

            @Override
            public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
                // 客户端与服务器端断开连接后，此处记录谁下线了
                Principal principal = webSocketSession.getPrincipal();
                if(principal != null){
                    String username = webSocketSession.getPrincipal().getName();
                    LogUtil.mygame.info("websocket offline: " + username);
                    redisReactiveUtil.del(username).subscribe(s-> LogUtil.mygame.info("del redis key: " + username + " valeu " + s));
                }
                super.afterConnectionClosed(webSocketSession, closeStatus);
            }

            @Override
            public boolean supportsPartialMessages() {
                return false;
            }
        };
    }

//    @Override
//    public WebSocketHandler decorate(final WebSocketHandler handler) {
//        return new WebSocketHandlerDecorator(handler) {
//            @Override
//            public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
//                // 客户端与服务器端建立连接后，此处记录谁上线了
//                String username = session.getPrincipal().getName();
//                String websocketSessionId=session.getId();
//                LogUtil.mygame.info("online: " + username+" websocketSessionId："+websocketSessionId);
//                redisReactiveUtil.set(username, websocketSessionId).subscribe(s-> {
//                    if (s) {
//                        LogUtil.mygame.info("save redis key: " + username + " valeu " + websocketSessionId);
//                    }else{
//                        LogUtil.mygame.info("save redis error key: " + username + " valeu " + websocketSessionId);
//                    }
//                });
//                super.afterConnectionEstablished(session);
//            }
//
//            @Override
//            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
//                // 客户端与服务器端断开连接后，此处记录谁下线了
//                String username = session.getPrincipal().getName();
//                LogUtil.mygame.info("offline: " + username);
//                super.afterConnectionClosed(session, closeStatus);
//            }
//        };
//    }
}
