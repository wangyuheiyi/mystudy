package com.heiyigame.mynetty;

import com.heiyigame.mynetty.handler.MyWebsocketClientHandler;
import com.heiyigame.mynetty.handler.MyWebsocketHandler;
import com.heiyigame.mynetty.handler.NettyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.URISyntaxException;


@SpringBootTest
@Slf4j
class MynettyApplicationTests {
    @Test
    void connectWebsocketTest(){
        EventLoopGroup group=new NioEventLoopGroup();
        try {
            URI websocketURI = new URI("ws://10.2.100.61:8000/mytest");
            HttpHeaders httpHeaders = new DefaultHttpHeaders();
            //进行握手
            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(websocketURI, WebSocketVersion.V13, (String)null, true,httpHeaders);
            Bootstrap websocketBoot=new Bootstrap();
            websocketBoot.option(ChannelOption.SO_KEEPALIVE,true)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .group(group)
                    .channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                //有连接到达时会创建一个channel
                protected void initChannel(SocketChannel ch) throws Exception {
                    // webSocket协议本身是基于http协议的，所以这边也要使用http编解码器
                    ch.pipeline()
                            .addLast(new HttpClientCodec())
                            //netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
                            .addLast(new HttpObjectAggregator(64*1024))
                            .addLast("hookedHandler",new MyWebsocketClientHandler(handshaker));
                }
            });
            Channel channel = websocketBoot.connect(websocketURI.getHost(), websocketURI.getPort()).sync().channel();
            MyWebsocketClientHandler handler = (MyWebsocketClientHandler)channel.pipeline().get("hookedHandler");
            log.info("客户端建立连接 握手!!");
            ChannelFuture f =handshaker.handshake(channel);
            f.addListener((ChannelFuture futureListener) ->
            {
                if (futureListener.isSuccess()) {
                    log.info("websocket客户端连接成功!");

                } else {
                    log.info("websocket客户端连接失败!");
                }

            });
            f.sync();
            channel.closeFuture().sync();



        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }
    @Test
    void connectNettyTest(){
        Bootstrap bootstrap = new Bootstrap();
        String serverIp="10.2.129.100";
        int serverPort=8000;
        //创建reactor 线程组
        EventLoopGroup workerLoopGroup = new NioEventLoopGroup();
        try{
            //1 设置reactor 线程组
            bootstrap.group(workerLoopGroup);
            //2 设置nio类型的channel
            bootstrap.channel(NioSocketChannel.class);
            //3 设置监听端口
            bootstrap.remoteAddress(serverIp, serverPort);
            //4 设置通道的参数
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            //5 装配子通道流水线
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                //有连接到达时会创建一个channel
                protected void initChannel(SocketChannel ch) throws Exception {
                    // pipeline管理子通道channel中的Handler
                    // 向子channel流水线添加一个handler处理器
                    ch.pipeline().addLast(NettyClientHandler.INSTANCE);
                }
            });
            ChannelFuture f = bootstrap.connect();
            f.addListener((ChannelFuture futureListener) ->
            {
                if (futureListener.isSuccess()) {
                    log.info("EchoClient客户端连接成功!");

                } else {
                    log.info("EchoClient客户端连接失败!");
                }

            });

            // 阻塞,直到连接完成
            f.sync();
            Channel channel = f.channel();
            ByteBuf buffer = channel.alloc().buffer();
            buffer.writeInt(500);
            channel.writeAndFlush(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 优雅关闭EventLoopGroup，
            // 释放掉所有资源包括创建的线程
            workerLoopGroup.shutdownGracefully();
        }
    }

    @Test
    void contextLoads() {
        ByteBuf buffer= Unpooled.buffer();
        buffer.writeInt(500);
//        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInitializer<SocketChannel>() {
//                //有连接到达时会创建一个channel
//                protected void initChannel(SocketChannel ch) throws Exception {
//                    // pipeline管理子通道channel中的Handler
//                    // 向子channel流水线添加一个handler处理器
//                    ch.pipeline().addLast(NettyClientHandler.INSTANCE);
//                }
//        });
        EmbeddedChannel channel=new EmbeddedChannel(
                MyWebsocketHandler.getInstance()
        );
        channel.writeInbound(buffer);
        channel.finish();
//        assertTrue(channel.writeInbound(buffer));
//        assertTrue(channel.finish());
        channel.close();

    }

}
