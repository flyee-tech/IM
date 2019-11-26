package com.peiel.im;

import com.alibaba.fastjson.JSON;
import com.peiel.im.mapper.UserMapper;
import com.peiel.im.model.UserDO;
import com.peiel.im.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class ImApplicationTests {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    @Test
    void contextLoads() {
    }

    @Test
    public void testUserMapper() {
        UserDO userDO = userMapper.selectById(1);
        log.info(JSON.toJSONString(userDO));
    }

    @Test
    public void testUserService() {
        UserDO user = userService.login("peiel", "123");
        List<UserDO> otherUser = userService.getAllUsersExcept(user);
        log.info(JSON.toJSONString(otherUser));
    }

}
