package com.peiel.im.service;

import com.peiel.im.model.MessageContactVO;
import com.peiel.im.model.MessageVO;

import java.util.List;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019-11-26
 */
public interface MessageService {

    MessageContactVO getContacts(Long userId);


    List<MessageVO> queryOneByOneMsg(Long userId, Long otherUserId);

    List<MessageVO> queryIncrOneByOneMsg(Long userId, Long otherUserId, Long lastMsgId);

    MessageVO sendMsg(Long userId, Long otherUserId, String content);
}
