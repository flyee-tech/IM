package com.peiel.im.controller;

import com.alibaba.fastjson.JSON;
import com.peiel.im.model.MessageContactVO;
import com.peiel.im.model.MessageVO;
import com.peiel.im.service.MessageService;
import com.peiel.im.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019-11-26
 */
@Slf4j
@RestController
public class MessageController {

    // 1. 轮询查询联系人列表和消息总未读数
    // 2. 发送消息
    // 3. 查询单聊聊天信息
    // 4. 轮询查询单聊递增聊天信息

    @Autowired
    private MessageService messageService;

    @GetMapping("queryContacts")
    public String queryContacts(@RequestParam Long userId) {
        MessageContactVO vo = messageService.getContacts(userId);
        return JSON.toJSONString(vo);
    }

    @GetMapping("queryOneByOneMsg")
    public String queryOneByOneMsg(@RequestParam Long userId, @RequestParam Long otherUserId) {
        List<MessageVO> voList = messageService.queryOneByOneMsg(userId, otherUserId);
        return JSON.toJSONString(voList);
    }

    @GetMapping("queryIncrOneByOneMsg")
    public String queryIncrOneByOneMsg(@RequestParam Long userId, @RequestParam Long otherUserId, @RequestParam Long lastMsgId) {
        List<MessageVO> voList = messageService.queryIncrOneByOneMsg(userId, otherUserId, lastMsgId);
        return JSON.toJSONString(voList);
    }

    @PostMapping("sendMsg")
    public String sendMsg(@RequestParam Long userId, @RequestParam Long otherUserId, @RequestParam String content) {
        MessageVO vo = messageService.sendMsg(userId, otherUserId, content);
        return JSON.toJSONString(vo);
    }

}
