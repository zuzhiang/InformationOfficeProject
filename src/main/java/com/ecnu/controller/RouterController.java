package com.ecnu.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class RouterController {

    // 主页面的映射
    @RequestMapping({"/", "index", "main"})
    public String index(HttpSession session) {
        // 获取当前登录用户名，并在页面显示
        String loginUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (loginUser == "anonymousUser") {
            loginUser = "未登录";
        }
        session.setAttribute("loginUser", loginUser);
        return "index";
    }

    // 登录页面的映射
    @RequestMapping("/login")
    public String login() {
        return "login";
    }

}
