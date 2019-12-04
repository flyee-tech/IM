package com.peiel.im.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.peiel.im.mapper.ContactMapper;
import com.peiel.im.mapper.MsgIndexMapper;
import com.peiel.im.mapper.MsgMapper;
import com.peiel.im.model.*;
import com.peiel.im.service.MessageService;
import com.peiel.im.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019-11-26
 */
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private UserService userService;
    @Autowired
    private MsgMapper msgMapper;
    @Autowired
    private MsgIndexMapper msgIndexMapper;
    @Autowired
    private RedisTemplate template;
    @Autowired
    private ContactMapper contactMapper;

    @Override
    public MessageContactVO getContacts(Long userId) {
        UserDO user = userService.selectById(userId);
        return userService.getContacts(user);
    }

    @Override
    public List<MessageVO> queryOneByOneMsg(Long userId, Long otherUserId) {
        // 清除未读数
        Object old_unread = template.opsForHash().get(userId + "_KEY", otherUserId + "_S");
        template.opsForHash().put(userId + "_KEY", otherUserId + "_S", 0);
        template.opsForValue().decrement(userId + "_T", old_unread != null ? Long.parseLong(old_unread + "") : 0);

        List<MsgIndexDO> list = msgIndexMapper.selectList(Wrappers.lambdaQuery(new MsgIndexDO())
                .eq(MsgIndexDO::getOwnerUserId, userId)
                .eq(MsgIndexDO::getOtherUserId, otherUserId)
                .eq(MsgIndexDO::getStatus, 1)
                .orderByAsc(MsgIndexDO::getCreatedDate));

        List<MessageVO> voList = new ArrayList<>();
        list.forEach(msgIndex -> {
            MsgDO msg = msgMapper.selectById(msgIndex.getMsgId());
            UserDO owner = userService.selectById(msgIndex.getOwnerUserId());
            UserDO other = userService.selectById(msgIndex.getOtherUserId());
            voList.add(MessageVO.builder().msgIndex(msgIndex).msg(msg).ownerUser(owner).otherUser(other).build());
        });

        return voList;
    }

    @Override
    public List<MessageVO> queryIncrOneByOneMsg(Long userId, Long otherUserId, Long lastMsgId) {
        // 触发未读数清除操作
        Object old_unread = template.opsForHash().get(userId + "_KEY", otherUserId + "_S");
        template.opsForHash().put(userId + "_KEY", otherUserId + "_S", 0);
        template.opsForValue().decrement(userId + "_T", old_unread != null ? Long.parseLong(old_unread + "") : 0);

        List<MsgIndexDO> list = msgIndexMapper.selectList(Wrappers.lambdaQuery(new MsgIndexDO())
                .eq(MsgIndexDO::getOwnerUserId, userId)
                .eq(MsgIndexDO::getOtherUserId, otherUserId)
                .eq(MsgIndexDO::getStatus, 1)
                .gt(MsgIndexDO::getMsgId, lastMsgId)
                .orderByAsc(MsgIndexDO::getCreatedDate));

        List<MessageVO> voList = new ArrayList<>();
        list.forEach(msgIndex -> {
            MsgDO msg = msgMapper.selectById(msgIndex.getMsgId());
            UserDO owner = userService.selectById(msgIndex.getOwnerUserId());
            UserDO other = userService.selectById(msgIndex.getOtherUserId());
            voList.add(MessageVO.builder().msgIndex(msgIndex).msg(msg).ownerUser(owner).otherUser(other).build());
        });

        return voList;
    }

    @Override
    public MessageVO sendMsg(Long userId, Long otherUserId, String content) {
        // 保存消息表
        MsgDO msg = MsgDO.builder().content(content).build();
        msgMapper.insert(msg);
        // 保存消息索引表
        MsgIndexDO msgIndex = MsgIndexDO.builder().msgId(msg.getId()).ownerUserId(userId).otherUserId(otherUserId).type(0).build();
        msgIndexMapper.insert(msgIndex);
        msgIndexMapper.insert(MsgIndexDO.builder().msgId(msg.getId()).ownerUserId(otherUserId).otherUserId(userId).type(1).build());
        // 更新最近联系人表
        List<ContactDO> list = contactMapper.selectList(Wrappers.lambdaQuery(new ContactDO())
                .or(i -> i.eq(ContactDO::getOwnerUserId, userId).eq(ContactDO::getOtherUserId, otherUserId))
                .or(i -> i.eq(ContactDO::getOwnerUserId, otherUserId).eq(ContactDO::getOtherUserId, userId))
        );
        if (list.size() > 0) {
            list.forEach(contactDO -> contactMapper.updateById(ContactDO.builder().id(contactDO.getId()).msgId(msg.getId()).build()));
        } else {
            contactMapper.insert(ContactDO.builder().ownerUserId(userId).otherUserId(otherUserId).msgId(msg.getId()).type(0).build());
            contactMapper.insert(ContactDO.builder().ownerUserId(otherUserId).otherUserId(userId).msgId(msg.getId()).type(1).build());
        }

        // 新增总未读和单未读
        template.opsForValue().increment(otherUserId + "_T");
        template.opsForHash().increment(otherUserId + "_KEY", userId + "_S", 1);
        return MessageVO.builder()
                .msg(msg)
                .msgIndex(msgIndex)
                .ownerUser(userService.selectById(userId))
                .otherUser(userService.selectById(otherUserId))
                .build();
    }

}
