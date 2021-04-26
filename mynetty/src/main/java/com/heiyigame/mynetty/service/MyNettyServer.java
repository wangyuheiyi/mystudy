package com.heiyigame.mynetty.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

/**
 * @author wangyuheiyi
 */
@Component
@Slf4j
public class MyNettyServer {
    private EventLoopGroup bossGroup;

    private EventLoopGroup workGroup;
    /**
     * 端口号
     */
    @Value("${mynetty.port}")
    private int port;

    /**
     * 启动
     * @throws InterruptedException
     */
    private void start() throws InterruptedException {
        //主线程组，用于接收客户端的链接，但不做任何处理
        bossGroup = new NioEventLoopGroup();
        //定义从线程组，主线程组会把任务转给从线程组进行处理
        workGroup = new NioEventLoopGroup();
        //启动类
        ServerBootstrap bootstrap = new ServerBootstrap();
        // bossGroup辅助客户端的tcp连接请求, workGroup负责与客户端之前的读写操作
        bootstrap.group(bossGroup, workGroup);
        // 设置NIO类型的channel NIO双向通道
        bootstrap.channel(NioServerSocketChannel.class);
        // 设置监听端口
        bootstrap.localAddress(new InetSocketAddress(port));
        /*
         * option是设置 bossGroup，childOption是设置workerGroup
         * netty 默认数据包传输大小为1024字节, 设置它可以自动调整下一次缓冲区建立时分配的空间大小，避免内存的浪费    最小  初始化  最大 (根据生产环境实际情况来定)
         * 使用对象池，重用缓冲区
         */
        bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64, 10496, 1048576));
        bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64, 10496, 1048576));
        // 连接到达时会创建一个通道 初始化器，chanel注册后会执行里面相应的初始化方法
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
            // 流水线管理通道中的处理程序（Handler），用来处理业务
            // webSocket协议本身是基于http协议的，所以这边也要使用http编解码器
            ch.pipeline().addLast(new HttpServerCodec());
            }
        });

        //启动
        //绑定端口，并设置为同步方式，是一个异步的chanel
        //配置完成，开始绑定server，通过调用sync同步方法阻塞直到绑定成功
        ChannelFuture channelFuture = bootstrap.bind().sync();

        log.info("Server started and listen on:{}", channelFuture.channel().localAddress());

        /*
         * 关闭
         * 获取某个客户端所对应的chanel，关闭并设置同步方式
         * 对关闭通道进行监听
         * 这里阻塞了后续其他类的加载 所有需要一个新线程启动
         */
        channelFuture.channel().closeFuture().sync();
    }
    /**
     * 在程序关闭前
     * 释放资源
     */
    @PreDestroy
    public void destroy() throws InterruptedException {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().sync();
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully().sync();
        }
    }

    /**
     * 在创建Bean时运行
     * 需要开启一个新的线程来执行netty server 服务器 不要阻塞其他类的加载
     */
    @PostConstruct()
    public void init() {
        new Thread(() -> {
            try {
                start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
