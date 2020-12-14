package com.ecnu.controller;

import com.ecnu.mapper.ImageMapper;
import com.ecnu.mapper.PersonMapper;
import com.ecnu.mapper.UserMapper;
import com.ecnu.mapper.VideoMapper;
import com.ecnu.pojo.Image;
import com.ecnu.pojo.Person;
import com.ecnu.pojo.User;
import com.ecnu.pojo.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@Controller
public class ImageController {

    @Autowired
    UserMapper userMapper;
    @Autowired
    VideoMapper videoMapper;
    @Autowired
    PersonMapper personMapper;
    @Autowired
    ImageMapper imageMapper;

    // 根据人物id查询所有图片，并在图片页面显示
//    @RequestMapping("/image/{id}")
//    public String showImage(@PathVariable("id") String id, Model model) {
//        Collection<Image> images = imageMapper.selectImageByPersonId(id);
//        model.addAttribute("images", images);
//        return "video/image";
//    }

    @RequestMapping("/toImagePage/{personId}")
    public String showImage(@PathVariable("personId") String personId, Model model) {
        Collection<Image> images = imageMapper.selectImageByPersonId(personId);
        model.addAttribute("images", images);
        return "image/image";
    }

    // 跳转到更新页面，利用携带的图片id信息查出图片，并在图片更新页面显示原数据
    @RequestMapping("/toUpdateImagePage")
    public String toUpdateImagePage(@RequestParam("imageId") String imageId,
                                    Model model) {
        // 查出原来的数据
        Image image = imageMapper.selectImageByImageId(imageId);
        model.addAttribute("image", image);
        return "image/updateImage";
    }

    // 更新图片信息
    @RequestMapping("/updateImage")
    // 开启事务支持
    @Transactional
    public String updateImage(@RequestParam("imageId") String imageId,
                              @RequestParam("oldPlayPhoneExist") String oldPlayPhoneExist,
                              @RequestParam("oldPlayLaptopExist") String oldPlayLaptopExist,
                              @RequestParam("oldReadBookExist") String oldReadBookExist,
                              @RequestParam("oldRaiseHandExist") String oldRaiseHandExist,
                              @RequestParam("oldBowExist") String oldBowExist,
                              @RequestParam("oldLeanExist") String oldLeanExist,

                              @RequestParam("handExist") String handExist,
                              @RequestParam("phoneExist") String phoneExist,
                              @RequestParam("laptopExist") String laptopExist,
                              @RequestParam("bookExist") String bookExist,
                              @RequestParam("playPhoneExist") String playPhoneExist,
                              @RequestParam("playLaptopExist") String playLaptopExist,
                              @RequestParam("readBookExist") String readBookExist,
                              @RequestParam("raiseHandExist") String raiseHandExist,
                              @RequestParam("bowExist") String bowExist,
                              @RequestParam("leanExist") String leanExist,
                              Model model) {
        Image image = imageMapper.selectImageByImageId(imageId);
        // 更新图片信息
        image.setHandExist(Boolean.parseBoolean(handExist));
        image.setPhoneExist(Boolean.parseBoolean(phoneExist));
        image.setLaptopExist(Boolean.parseBoolean(laptopExist));
        image.setBookExist(Boolean.parseBoolean(bookExist));
        image.setPlayPhoneExist(Boolean.parseBoolean(playPhoneExist));
        image.setPlayLaptopExist(Boolean.parseBoolean(playLaptopExist));
        image.setReadBookExist(Boolean.parseBoolean(readBookExist));
        image.setRaiseHandExist(Boolean.parseBoolean(raiseHandExist));
        image.setBowExist(Boolean.parseBoolean(bowExist));
        image.setLeanExist(Boolean.parseBoolean(leanExist));
        imageMapper.updateImage(image);

        // 更新图片对应的人物信息
        Person person = personMapper.selectPersonByPersonId(image.getPersonId());
        person.setPlayPhoneNum(person.getPlayPhoneNum() - Integer.parseInt(oldPlayPhoneExist) + (playPhoneExist.equals("true") ? 1 : 0));
        person.setPlayLaptopNum(person.getPlayLaptopNum() - Integer.parseInt(oldPlayLaptopExist) + (playLaptopExist.equals("true") ? 1 : 0));
        person.setReadBookNum(person.getReadBookNum() - Integer.parseInt(oldReadBookExist) + (readBookExist.equals("true") ? 1 : 0));
        person.setRaiseHandNum(person.getRaiseHandNum() - Integer.parseInt(oldRaiseHandExist) + (raiseHandExist.equals("true") ? 1 : 0));
        person.setBowNum(person.getBowNum() - Integer.parseInt(oldBowExist) + (bowExist.equals("true") ? 1 : 0));
        person.setLeanNum(person.getLeanNum() - Integer.parseInt(oldLeanExist) + (leanExist.equals("true") ? 1 : 0));
        personMapper.updatePerson(person);

        model.addAttribute("updateImageState", "图片信息更新成功！");
        model.addAttribute("image", image);
        return "image/updateImage";
    }

    // 删除人物信息
    @RequestMapping("/deleteImage")
    // 开启事务支持
    @Transactional
    public String deleteImage(@RequestParam("imageId") String imageId, Model model) {
        String curUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectUserByUsername(curUser);
        Image image = imageMapper.selectImageByImageId(imageId);
        Video video = videoMapper.selectVideoById(image.getVideoId());
        // 如果当前用户不是管理员用户，并且不是当前视频的所有者，则不能删除
        if (user.getRole().contains("admin") || curUser.equals(video.getOwner())) {
            // 删除图片数据库信息，并更新相应的人物数据库信息
            imageMapper.deleteImageByImageId(imageId);
            Person person = personMapper.selectPersonByPersonId(image.getPersonId());
            person.setPlayPhoneNum(person.getPlayPhoneNum() - (image.isPlayPhoneExist() ? 1 : 0));
            person.setPlayLaptopNum(person.getPlayLaptopNum() - (image.isPlayLaptopExist() ? 1 : 0));
            person.setReadBookNum(person.getReadBookNum() - (image.isReadBookExist() ? 1 : 0));
            person.setRaiseHandNum(person.getRaiseHandNum() - (image.isRaiseHandExist() ? 1 : 0));
            person.setBowNum(person.getBowNum() - (image.isBowExist() ? 1 : 0));
            person.setLeanNum(person.getLeanNum() - (image.isLeanExist() ? 1 : 0));
            personMapper.updatePerson(person);

            model.addAttribute("deleteImageState", "删除图片信息成功！");
        } else {
            model.addAttribute("deleteImageState", "普通用户只能在自己上传的课程下进行删除！");
        }
        Collection<Image> images = imageMapper.selectImageByPersonId(image.getPersonId());
        model.addAttribute("images", images);
        return "image/image";
    }
}
