package com.ecnu.controller;

import com.ecnu.mapper.PersonMapper;
import com.ecnu.pojo.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;

@Controller
public class PersonController {

    @Autowired
    PersonMapper personMapper;

    // 根据视频id查询所有人物，并在人物页面显示
    @RequestMapping("/person/{videoId}")
    public String showPerson(@PathVariable("videoId") String videoId, Model model) {
        Collection<Person> persons = personMapper.selectPersonByVideoId(videoId);
        model.addAttribute("persons", persons);
        return "video/person";
    }
}
