package com.peiel.im.ws.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.peiel.im.ws.WebsocketServerMsgParser;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019/12/2
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class WebSocketHandle extends SimpleChannelInboundHandler<Object> {

    @Autowired
    private WebsocketServerMsgParser parser;

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) msg).text();
            log.info("收到消息：" + text);
            String resp = parser.parse(ctx.channel(), text);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(resp));
        } else if (msg instanceof BinaryWebSocketFrame) {
            log.info("收到二进制消息：" + ((BinaryWebSocketFrame) msg).content().readableBytes());
            BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame(Unpooled.buffer().writeBytes("xxx".getBytes()));
            ctx.channel().writeAndFlush(binaryWebSocketFrame);
        }
    }

}
