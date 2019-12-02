package com.peiel.im.ws;

import com.peiel.im.ws.handler.WebSocketHandle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019/12/2
 */
@Slf4j
@Component
public class WebSocketServer {

    @PostConstruct
    public void start() throws InterruptedException {
        log.info("WebSocketServer - Starting...");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializerHandler());
        final ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(8080)).sync();
        new Thread(() -> {
            try {
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        log.info("WebSocketServer - Start completed");
    }

    private static class ChannelInitializerHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            //websocket协议本身是基于http协议的，所以这边也要使用http解编码器
            pipeline.addLast(new HttpServerCodec());
            //以块的方式来写的处理器
            pipeline.addLast(new ChunkedWriteHandler());
            //netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
            pipeline.addLast(new HttpObjectAggregator(1024 * 1024 * 1024));
            pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true));
            //websocket定义了传递数据的6中frame类型
            pipeline.addLast(new WebSocketHandle());
        }
    }

}
