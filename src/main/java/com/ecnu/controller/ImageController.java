package com.ecnu.controller;

import com.ecnu.mapper.ImageMapper;
import com.ecnu.pojo.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@Controller
public class ImageController {

    @Autowired
    ImageMapper imageMapper;

    // 根据人物id查询所有图片，并在图片页面显示
//    @RequestMapping("/image/{id}")
//    public String showImage(@PathVariable("id") String id, Model model) {
//        Collection<Image> images = imageMapper.selectImageByPersonId(id);
//        model.addAttribute("images", images);
//        return "video/image";
//    }

    @RequestMapping("/toImagePage")
    public String showImage(@RequestParam("personId") String personId, Model model) {
        Collection<Image> images = imageMapper.selectImageByPersonId(personId);
        model.addAttribute("images", images);
        return "video/image";
    }
}
