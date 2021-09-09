package com.heiyigame.mynetty.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyWebsocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private WebSocketClientHandshaker webSocketClientHandshaker;
    private ChannelPromise handshakeFuture;

    public MyWebsocketClientHandler(WebSocketClientHandshaker webSocketClientHandshaker){
        this.webSocketClientHandshaker=webSocketClientHandshaker;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.handshakeFuture = ctx.newPromise();
    }

    /**
     * 当客户端主动链接服务端的链接后，调用此方法
     *
     * @param channelHandlerContext ChannelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) {
//        log.info("客户端建立连接 握手");
//        Channel channel = channelHandlerContext.channel();
//        // 握手
//        webSocketClientHandshaker.handshake(channel);
    }

    public ChannelPromise handshakeFuture() {
        return this.handshakeFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        log.info("服务器返回收到[" + channelHandlerContext.channel().remoteAddress() + "]消息：" + msg.toString());
        FullHttpResponse response;
        Channel ch = channelHandlerContext.channel();
        if (!this.webSocketClientHandshaker.isHandshakeComplete()) {
            try {
                response = (FullHttpResponse)msg;
                //握手协议返回，设置结束握手
                this.webSocketClientHandshaker.finishHandshake(ch, response);
                //设置成功
                this.handshakeFuture.setSuccess();
                log.info("WebSocket Client connected! response headers[sec-websocket-extensions]:{}"+response.headers());

                TextWebSocketFrame frame = new TextWebSocketFrame("我是文本");
                channelHandlerContext.channel().writeAndFlush(frame).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if(channelFuture.isSuccess()){
                            log.info("text send success");
                        }else{
                            log.info("text send failed  "+channelFuture.cause().getMessage());
                        }
                    }
                });
            } catch (WebSocketHandshakeException var7) {
                FullHttpResponse res = (FullHttpResponse)msg;
                String errorMsg = String.format("WebSocket Client failed to connect,status:%s,reason:%s", res.status(), res.content().toString(CharsetUtil.UTF_8));
                this.handshakeFuture.setFailure(new Exception(errorMsg));
            }
        }else{
            WebSocketFrame frame = (WebSocketFrame)msg;
            if (frame instanceof TextWebSocketFrame) {
                TextWebSocketFrame textFrame = (TextWebSocketFrame)frame;
                log.info("TextWebSocketFrame:" +textFrame.text());
            }else if (frame instanceof CloseWebSocketFrame) {
                log.info("receive close frame");
                ch.close();
            }
        }
    }
}
