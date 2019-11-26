package com.peiel.im.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.peiel.im.exception.UserNotFoundException;
import com.peiel.im.exception.UserPasswdErrorException;
import com.peiel.im.mapper.ContactMapper;
import com.peiel.im.mapper.UserMapper;
import com.peiel.im.model.ContactDO;
import com.peiel.im.model.MessageContactVO;
import com.peiel.im.model.UserDO;
import com.peiel.im.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019-11-25
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper mapper;
    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private ContactMapper contactMapper;

    @Override
    public UserDO login(String name, String passwd) {
        List<UserDO> userDOS = mapper.selectList(Wrappers.lambdaQuery(new UserDO())
                .eq(UserDO::getName, name));
        if (userDOS == null || userDOS.size() == 0) {
            throw new UserNotFoundException(500, "用户不存在");
        }
        UserDO userDO = userDOS.get(0);
        if (!userDO.getPasswd().equals(passwd)) {
            throw new UserPasswdErrorException(500, "密码错误");
        }
        return userDO;
    }

    @Override
    public List<UserDO> getAllUsersExcept(UserDO loginUserDO) {
        List<UserDO> userDOS = mapper.selectList(Wrappers.lambdaQuery(new UserDO())
                .eq(UserDO::getStatus, 1));
        userDOS.remove(loginUserDO);
        return userDOS;
    }

    @Override
    public MessageContactVO getContacts(UserDO loginUserDO) {
        List<ContactDO> list = contactMapper.getContactListByUserId(loginUserDO.getId());

        if (list.isEmpty()) {
            return new MessageContactVO();
        }

        Long totalUnread = 0L;
        String total = template.opsForValue().get(loginUserDO.getId() + "_T");
        totalUnread = total != null ? Long.valueOf(total) : totalUnread;

        MessageContactVO vo = new MessageContactVO();
        vo.setUserId(loginUserDO.getId());
        vo.setName(loginUserDO.getName());
        vo.setImgUrl(loginUserDO.getImgUrl());
        vo.setTotalUnread(totalUnread);

        list.forEach(contactDO -> {
            Object unread = template.opsForHash().get(vo.getUserId() + "_KEY", contactDO.getOtherUserId() + "_S");
            MessageContactVO.ContactInfo info = vo.new ContactInfo();
            info.setUserId(contactDO.getOtherUserId());
            info.setName(contactDO.getOtherUserName());
            info.setImgUrl(contactDO.getOtherImgUrl());
            info.setType(contactDO.getType());
            info.setMid(contactDO.getMsgId());
            info.setContent(contactDO.getContent());
            info.setHisUnread(unread != null ? Long.parseLong((String) unread) : 0L);
            vo.appendContactInfo(info);
        });

        return vo;
    }

    @Override
    public UserDO selectById(Long userId) {
        return mapper.selectById(userId);
    }

}
