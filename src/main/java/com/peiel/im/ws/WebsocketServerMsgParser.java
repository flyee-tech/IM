package com.peiel.im.ws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.peiel.im.Constants;
import com.peiel.im.model.MessageVO;
import com.peiel.im.service.MessageService;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Peiel
 * @version V1.0
 * @date 2019/12/3
 */
@Slf4j
@Component
public class WebsocketServerMsgParser {

    final static ConcurrentHashMap<Long, Channel> map = new ConcurrentHashMap<>();

    @Autowired
    private MessageService messageService;

    public String parse(Channel channel, String text) {
        JSONObject jsonText;
        try {
            jsonText = JSON.parseObject(text);
        } catch (Exception e) {
            e.printStackTrace();
            return "Not support message type, please use json!";
        }
        Integer type = jsonText.getInteger("type");
        JSONObject data = jsonText.getJSONObject("data");
        if (type.equals(Constants.WS_MES_TYPE_CONNECT)) {
            Long userId = data.getLong("uid");
            map.put(userId, channel);
            return "{\"type\":" + type + ",\"status\":\"success\"}";
        }
        if (type.equals(Constants.WS_MES_TYPE_QUERY)) {
            Long userId = data.getLong("userId");
            Long otherUserId = data.getLong("otherUserId");
            List<MessageVO> list = messageService.queryOneByOneMsg(userId, otherUserId);
            JSONObject resp = new JSONObject();
            resp.put("type", type);
            resp.put("data", list);
            return resp.toJSONString();
        }
        //"data": {"userId":' + sender_id + ',"otherUserId":' + recipient_id + ', "content":"' + msg_content + '","msgType":1  }}';
        if (type.equals(Constants.WS_MES_TYPE_SEND)) {
            Long userId = data.getLong("userId");
            Long otherUserId = data.getLong("otherUserId");
            String content = data.getString("content");
            MessageVO messageVO = messageService.sendMsg(userId, otherUserId, content);
            JSONObject resp = new JSONObject();
            resp.put("type", Constants.WS_MES_TYPE_REICIVE);
            resp.put("data", messageVO);
            receiveMsg(otherUserId, resp.toJSONString());
            resp.put("type", type);
            return resp.toJSONString();
        }
        return null;
    }

    public void receiveMsg(Long userId, String content) {
        Channel channel = map.get(userId);
        if (channel != null && channel.isActive() && channel.isWritable()) {
            channel.writeAndFlush(new TextWebSocketFrame(content));
        } else {
            log.warn("ws sendMsg not found user connect, userId: {}, content: {}", userId, content);
        }
    }

}
