package com.heiyigame.mynetty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class MyWebsocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static class SingletonContainer{
        private static MyWebsocketHandler instance = new MyWebsocketHandler();
    }
    public static MyWebsocketHandler getInstance(){
        return SingletonContainer.instance;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        log.info("服务器 收到[" + channelHandlerContext.channel().remoteAddress() + "]消息：" + textWebSocketFrame.text());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerAdded:"+ctx.channel().id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerRemoved:"+ctx.channel().id().asLongText());
    }
}
