package com.peiel.im.controller;


import com.peiel.im.Constants;
import com.peiel.im.exception.UserNotFoundException;
import com.peiel.im.exception.UserPasswdErrorException;
import com.peiel.im.model.MessageContactVO;
import com.peiel.im.model.UserDO;
import com.peiel.im.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping(path = "/")
    public String welcomePage(HttpSession session) {
        if (session.getAttribute(Constants.SESSION_KEY) != null) {
            return "index";
        } else {
            return "login";
        }
    }

    @PostMapping(path = "/login")
    public String login(@RequestParam String userName, @RequestParam String password, Model model, HttpSession session) {
        try {
            UserDO loginUserDO = userService.login(userName, password);
            model.addAttribute("loginUserDO", loginUserDO);
            session.setAttribute(Constants.SESSION_KEY, loginUserDO);

            List<UserDO> otherUserDOS = userService.getAllUsersExcept(loginUserDO);
            model.addAttribute("otherUserDOS", otherUserDOS);

            MessageContactVO contactVO = userService.getContacts(loginUserDO);
            model.addAttribute("contactVO", contactVO);
            return "index";

        } catch (UserNotFoundException e1) {
            model.addAttribute("errormsg", userName + ": 该用户不存在！");
            return "login";
        } catch (UserPasswdErrorException e2) {
            model.addAttribute("errormsg", "密码输入错误！");
            return "login";
        }
    }

//    @RequestMapping(path = "/ws")
//    public String ws(Model model, HttpSession session) {
//        UserDO loginUser = (UserDO)session.getAttribute(Constants.SESSION_KEY);
//        model.addAttribute("loginUser", loginUser);
//        List<UserDO> otherUsers = userService.getAllUsersExcept(loginUser);
//        model.addAttribute("otherUsers", otherUsers);
//
//        MessageContactVO contactVO = userService.getContacts(loginUser);
//        model.addAttribute("contactVO", contactVO);
//        return "index_ws";
//    }
//

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(Constants.SESSION_KEY);
        return "redirect:/";
    }

}