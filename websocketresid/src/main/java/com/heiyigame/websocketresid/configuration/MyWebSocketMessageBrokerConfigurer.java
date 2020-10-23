package com.heiyigame.websocketresid.configuration;

import com.heiyigame.websocketresid.interceptor.AuthWebSocketHandlerDecoratorFactory;
import com.heiyigame.websocketresid.interceptor.MyHandShakeInterceptor;
import com.heiyigame.websocketresid.interceptor.MyPrincipalHandshakeHandler;
import com.heiyigame.websocketresid.reactiveutil.RedisReactiveUtil;
import com.heiyigame.websocketresid.utils.LogUtil;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.Assert;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

/**
 * @author admin
 */
/** 此注解表示使用STOMP协议来传输基于消息代理的消息，此时可以在@Controller类中使用@MessageMapping*/
@Configuration
@EnableWebSocketMessageBroker
public class MyWebSocketMessageBrokerConfigurer implements WebSocketMessageBrokerConfigurer {
    @Autowired
    MyHandShakeInterceptor myHandShakeInterceptor;
    @Autowired
    private MyPrincipalHandshakeHandler myDefaultHandshakeHandler;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private AuthWebSocketHandlerDecoratorFactory myWebSocketHandlerDecoratorFactory;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /**
         * 注册 Stomp的端点
         * addEndpoint：添加STOMP协议的端点。这个HTTP URL是供WebSocket或SockJS客户端访问的地址
         * withSockJS：指定端点使用SockJS协议
         */
        registry.addEndpoint("/websocket-rabbitmq")
                /* 添加允许跨域访问*/
                .setAllowedOrigins("*")
                /* 添加自定义拦截 */
                .addInterceptors(myHandShakeInterceptor)
                .setHandshakeHandler(myDefaultHandshakeHandler)
                .withSockJS();//支持js访问
    }


    /**
     * 配置消息代理
     * 使用RabbitMQ做为消息代理，替换默认的Simple Broker
     * 启动简单Broker，消息的发送的地址符合配置的前缀来的消息才发送到这个broker
     * 第一行代码启用了STOMP代理中继（broker relay）功能，并将其目的地前缀设置为“/topic”和“/queue”。这样的话，Spring就能知道所有目的地前缀为“/topic”或“/queue”的消息都会发送到STOMP代理中
     * 应用的前缀设置为“/app”。所有目的地以“/app”打头的消息都将会路由到带有@MessageMapping注解的方法中，而不会发布到代理队列或主题中
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /**
         * 配置消息代理
         * 启动简单Broker，消息的发送的地址符合配置的前缀来的消息才发送到这个broker
         * 第一行代码启用了STOMP代理中继（broker relay）功能，并将其目的地前缀设置为“/topic”和“/queue”。这样的话，Spring就能知道所有目的地前缀为“/topic”或“/queue”的消息都会发送到STOMP代理中
         */
//        registry.enableSimpleBroker("/topic","/queue");

        registry.enableStompBrokerRelay("/exchange","/topic","/queue","/amq/queue")
                .setRelayHost("111.230.94.160")
                .setClientLogin("wangyuheiyi")
                .setClientPasscode("f5XocXKtUJFth17PS8bS")
                .setSystemLogin("wangyuheiyi")
                .setSystemPasscode("f5XocXKtUJFth17PS8bS")
                .setSystemHeartbeatSendInterval(5000)
                .setSystemHeartbeatReceiveInterval(4000);
    }



    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        ChannelInterceptor interceptor = new ChannelInterceptor(){
            @Override
            public boolean preReceive(MessageChannel channel) {
                LogUtil.mygame.info("myChannelInterceptorAdapter: preReceive");
                return true;
            }

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                LogUtil.mygame.info("myChannelInterceptorAdapter: preSend");
                Assert.notNull(message, "Message must not be null");
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                Assert.notNull(accessor, "accessor must not be null");
                if(!accessor.isHeartbeat()){
                    StompCommand command = accessor.getCommand();
                    Assert.notNull(command, "command must not be null");
                    //检测用户订阅内容（防止用户订阅不合法频道）
                    switch (command.getMessageType()){
                        case SUBSCRIBE:
                            LogUtil.mygame.info(this.getClass().getCanonicalName() + " 用户订阅目的地=" + accessor.getDestination());
                            break;
                        default:
                            LogUtil.mygame.error("Unexpected value: " + command.getMessageType());
                            break;
                    }
                }
                return message;
            }

            @Override
            public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
                LogUtil.mygame.info("myChannelInterceptorAdapter: afterSendCompletion");
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                Assert.notNull(accessor, "accessor must not be null");
                if(!accessor.isHeartbeat()){
                    StompCommand command = accessor.getCommand();
                    Assert.notNull(command, "command must not be null");
//                    Message<String> resmessage=new Message();
                    switch (command.getMessageType()){
                        case SUBSCRIBE:
                            LogUtil.mygame.info(this.getClass().getCanonicalName() + " 订阅消息发送成功");
                            /** 第一个参数是交换机，第二个参数是交换机和queue绑定的key**/
                            //amqpTemplate.convertAndSend("mytest","getMqResponse","消息发送成功");
                            break;
                        case MESSAGE:
                            LogUtil.mygame.info(this.getClass().getCanonicalName() + " 心跳消息");
                           // amqpTemplate.convertAndSend("mytest","getMqResponse","消息发送心跳成功");
                            break;
                        case DISCONNECT:
                            LogUtil.mygame.info(this.getClass().getCanonicalName() + "用户断开连接成功");
                           // amqpTemplate.convertAndSend("mytest","getMqResponse","{'msg':'用户断开连接成功'}");
                            break;
                        default:
                            LogUtil.mygame.error("Unexpected value: " + command.getMessageType());
                            break;
                    }
                }else{
                    LogUtil.mygame.info(this.getClass().getCanonicalName() + " 心跳消息");
                    //amqpTemplate.convertAndSend("mytest","getMqResponse","消息发送心跳成功");
                }
            }
        };
        registration.interceptors(interceptor);
    }

    /**
     * 这时实际spring weboscket集群的新增的配置，用于获取建立websocket时获取对应的sessionid值
     * @param registration
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(myWebSocketHandlerDecoratorFactory);
//        super.configureWebSocketTransport(registration);

//        registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
//            @Override
//            public WebSocketHandler decorate(final WebSocketHandler handler) {
//                return new WebSocketHandlerDecorator(handler) {
//                    @Override
//                    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
//                        // 客户端与服务器端建立连接后，此处记录谁上线了
//                        String username = session.getPrincipal().getName();
//                        LogUtil.mygame.info("online: " + username);
//                        super.afterConnectionEstablished(session);
//                    }
//
//                    @Override
//                    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
//                        // 客户端与服务器端断开连接后，此处记录谁下线了
//                        String username = session.getPrincipal().getName();
//                        LogUtil.mygame.info("offline: " + username);
//                        super.afterConnectionClosed(session, closeStatus);
//                    }
//                };
//            }
//        });
    }
}
