package com.peiel.im.ws.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019/12/2
 */
@Slf4j
public class WebSocketHandle extends SimpleChannelInboundHandler<Object> {

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("channelRead0()");
        if (msg instanceof TextWebSocketFrame) {
            log.info("收到消息：" + ((TextWebSocketFrame) msg).text());
        } else if (msg instanceof BinaryWebSocketFrame) {
            log.info("收到二进制消息：" + ((BinaryWebSocketFrame) msg).content().readableBytes());
            BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame(Unpooled.buffer().writeBytes("xxx".getBytes()));
            ctx.channel().writeAndFlush(binaryWebSocketFrame);
        }
    }

}
