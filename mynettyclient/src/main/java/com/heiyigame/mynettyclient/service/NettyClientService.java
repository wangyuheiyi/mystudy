package com.heiyigame.mynettyclient.service;

import com.heiyigame.mynettyclient.bean.ResInfoBean;
import com.heiyigame.mynettyclient.bean.RoleBean;
import com.heiyigame.mynettyclient.handler.MyWebsocketClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author admin
 */
@Service
@Slf4j
public class NettyClientService {
    /**
     * 端口号
     */
    @Value("${mynetty.host}")
    private String host;

    /** 工作线程*/
    private EventLoopGroup group;
    private RoleBean roleBean=new RoleBean();
    private Channel channel;

    private void nettyClientConnect(){
        group=new NioEventLoopGroup();
        ChannelFuture channelFuture =null;
        try {
            URI websocketURI = new URI(host);
            HttpHeaders httpHeaders = new DefaultHttpHeaders();
            //握手对象
            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(websocketURI, WebSocketVersion.V13, (String)null, true,httpHeaders);
            Bootstrap websocketBoot=new Bootstrap();
            websocketBoot.option(ChannelOption.SO_KEEPALIVE,true)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .group(group)
                    .channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                //有连接到达时会创建一个channel
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // webSocket协议本身是基于http协议的，所以这边也要使用http编解码器
                    ch.pipeline()
                            .addLast(new HttpClientCodec())
                            //netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
                            .addLast(new HttpObjectAggregator(64*1024))
                            .addLast("hookedHandler",new MyWebsocketClientHandler(handshaker));
                }
            });
            channelFuture = websocketBoot.connect(websocketURI.getHost(), websocketURI.getPort()).sync();
            channel=channelFuture.channel();
            log.info("客户端建wesocket立连接 握手!!");
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

        }catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    @PreDestroy
    public void destroy() throws InterruptedException {
        ChannelFuture closeFuture=channel.closeFuture().sync();
        closeFuture.addListener((ChannelFuture futureListener) ->
        {
            if (futureListener.isSuccess()) {
                log.info("websocket客户端连接成功!");
            } else {
                log.info("websocket客户端连接失败!");
            }
            if (group != null) {
                group.shutdownGracefully().sync();
            }
        });
    }

//    @PostConstruct()
    public Mono<ResInfoBean> initClient(){
        nettyClientConnect();
        return Mono.just(new ResInfoBean(1,"wanyu",null));
    }

}
