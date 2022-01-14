package com.heiyigame.mynetty.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * 入站处理继承SimpleChannelInboundHandler 可以自动释放Bytebuf
 * 否则需要自己手动调用向下传递的方法 由netty在流水线最后默认加入的TailHandler自动释放
 */
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
