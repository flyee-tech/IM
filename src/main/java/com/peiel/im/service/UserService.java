package com.peiel.im.service;

import com.peiel.im.model.MessageContactVO;
import com.peiel.im.model.UserDO;

import java.util.List;

/**
 * @author Peiel
 * @version V1.0
 * @date 2019-11-25
 */
public interface UserService {

    UserDO login(String name, String passwd);

    List<UserDO> getAllUsersExcept(UserDO loginUserDO);

    MessageContactVO getContacts(UserDO loginUserDO);

    UserDO selectById(Long userId);
}
