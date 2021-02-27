package com.ecnu.controller;

import com.ecnu.mapper.PersonMapper;
import com.ecnu.pojo.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class RouterController {

    @Autowired
    PersonMapper personMapper;

    // 主页面的映射
    @RequestMapping({"/", "index", "main"})
    public String index(HttpSession session, Model model) {
        // 获取当前登录用户名，并在页面显示
        String loginUser = SecurityContextHolder.getContext().getAuthentication().getName();
        if (loginUser.equals("anonymousUser")) {
            loginUser = "未登录";
        }
        session.setAttribute("loginUser", loginUser);

        // 获取饼图所需要的信息
        List<Person> personList = personMapper.selectAllPerson();
        int tmpPlayPhoneNum = 0, tmpPlayLaptopNum = 0, tmpReadBookNum = 0, tmpRaiseHandNum = 0, tmpBowNum = 0, tmpLeanNum = 0;
        for (Person curPerson : personList) {
            tmpPlayPhoneNum += curPerson.getPlayPhoneNum();
            tmpPlayLaptopNum += curPerson.getPlayLaptopNum();
            tmpReadBookNum += curPerson.getReadBookNum();
            tmpRaiseHandNum += curPerson.getRaiseHandNum();
            tmpBowNum += curPerson.getBowNum();
            tmpLeanNum += curPerson.getLeanNum();
        }
        model.addAttribute("tmpPlayPhoneNum", tmpPlayPhoneNum);
        model.addAttribute("tmpPlayLaptopNum", tmpPlayLaptopNum);
        model.addAttribute("tmpReadBookNum", tmpReadBookNum);
        model.addAttribute("tmpRaiseHandNum", tmpRaiseHandNum);
        model.addAttribute("tmpBowNum", tmpBowNum);
        model.addAttribute("tmpLeanNum", tmpLeanNum);
        return "index";
    }

    // 登录页面的映射
    @RequestMapping("/login")
    public String login() {
        return "login";
    }

}
