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
import java.util.List;

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
        List<Image> images = imageMapper.selectImageByPersonId(personId);
        model.addAttribute("images", images);
        model.addAttribute("videoId", images.get(0).getVideoId());
        return "image/image";
    }


    // 更新图片信息
    @RequestMapping("/updateImage")
    // 开启事务支持
    @Transactional
    public String updateImage(@RequestParam("imageIdList") String[] imageIdList,
                              @RequestParam("oldPlayPhoneExistList") String[] oldPlayPhoneExistList,
                              @RequestParam("oldPlayLaptopExistList") String[] oldPlayLaptopExistList,
                              @RequestParam("oldReadBookExistList") String[] oldReadBookExistList,
                              @RequestParam("oldRaiseHandExistList") String[] oldRaiseHandExistList,
                              @RequestParam("oldBowExistList") String[] oldBowExistList,
                              @RequestParam("oldLeanExistList") String[] oldLeanExistList,

                              @RequestParam("playPhoneExistList") String[] playPhoneExistList,
                              @RequestParam("playLaptopExistList") String[] playLaptopExistList,
                              @RequestParam("readBookExistList") String[] readBookExistList,
                              @RequestParam("raiseHandExistList") String[] raiseHandExistList,
                              @RequestParam("bowExistList") String[] bowExistList,
                              @RequestParam("leanExistList") String[] leanExistList,
                              Model model) {
        int tmpPlayPhoneNum = 0, tmpPlayLaptopNum = 0, tmpReadBookNum = 0, tmpRaiseHandNum = 0, tmpBowNum = 0, tmpLeanNum = 0;
//        System.out.println("\n\nold:\nphone: " + oldPlayPhoneExistList.length + "  laptop: " + oldPlayLaptopExistList.length + "  book: " + oldReadBookExistList.length +
//                "  hand: " + oldRaiseHandExistList.length + "  bow: " + oldBowExistList.length + "  lean: " + oldLeanExistList.length);
//        System.out.println("new:\nphone: " + playPhoneExistList.length + "  laptop: " + playLaptopExistList.length + "  book: " + readBookExistList.length +
//                "  hand: " + raiseHandExistList.length + "  bow: " + bowExistList.length + "  lean: " + leanExistList.length);
        for (int i = 0; i < imageIdList.length; i++) {
            Image image = imageMapper.selectImageByImageId(imageIdList[i]);
            // 更新图片信息
            image.setPlayPhoneExist(Boolean.parseBoolean(playPhoneExistList[i]));
            image.setPlayLaptopExist(Boolean.parseBoolean(playLaptopExistList[i]));
            image.setReadBookExist(Boolean.parseBoolean(readBookExistList[i]));
            image.setRaiseHandExist(Boolean.parseBoolean(raiseHandExistList[i]));
            image.setBowExist(Boolean.parseBoolean(bowExistList[i]));
            image.setLeanExist(Boolean.parseBoolean(leanExistList[i]));
            imageMapper.updateImage(image);
//            System.out.println("\n");
//            System.out.println("phone: " + oldPlayPhoneExistList[i] + "  " + playPhoneExistList[i]);
//            System.out.println("laptop: " + oldPlayLaptopExistList[i] + "  " + playLaptopExistList[i]);
//            System.out.println("book: " + oldReadBookExistList[i] + "  " + readBookExistList[i]);
//            System.out.println("hand: " + oldRaiseHandExistList[i] + "  " + raiseHandExistList[i]);
//            System.out.println("bow: " + oldBowExistList[i] + "  " + bowExistList[i]);
//            System.out.println("lean: " + oldLeanExistList[i] + "  " + leanExistList[i]);
            tmpPlayPhoneNum += Integer.parseInt(oldPlayPhoneExistList[i]) - (playPhoneExistList[i].equals("true") ? 1 : 0);
            tmpPlayLaptopNum += Integer.parseInt(oldPlayLaptopExistList[i]) - (playLaptopExistList[i].equals("true") ? 1 : 0);
            tmpReadBookNum += Integer.parseInt(oldReadBookExistList[i]) - (readBookExistList[i].equals("true") ? 1 : 0);
            tmpRaiseHandNum += Integer.parseInt(oldRaiseHandExistList[i]) - (raiseHandExistList[i].equals("true") ? 1 : 0);
            tmpBowNum += Integer.parseInt(oldBowExistList[i]) - (bowExistList[i].equals("true") ? 1 : 0);
            tmpLeanNum += Integer.parseInt(oldLeanExistList[i]) - (leanExistList[i].equals("true") ? 1 : 0);
        }
        System.out.println("phone: " + tmpPlayPhoneNum + "  laptop: " + tmpPlayLaptopNum + "  book: " + tmpReadBookNum + "  hand: " + tmpRaiseHandNum + "  bow: " + tmpBowNum + "  lean: " + tmpLeanNum);
        // 更新图片对应的人物信息
        Person person = personMapper.selectPersonByPersonId(imageMapper.selectImageByImageId(imageIdList[0]).getPersonId());
        System.out.println("before: \n" + person);
        person.setPlayPhoneNum(person.getPlayPhoneNum() - tmpPlayPhoneNum);
        person.setPlayLaptopNum(person.getPlayLaptopNum() - tmpPlayLaptopNum);
        person.setReadBookNum(person.getReadBookNum() - tmpReadBookNum);
        person.setRaiseHandNum(person.getRaiseHandNum() - tmpRaiseHandNum);
        person.setBowNum(person.getBowNum() - tmpBowNum);
        person.setLeanNum(person.getLeanNum() - tmpLeanNum);
        System.out.println("after: \n" + person);
        personMapper.updatePerson(person);
        List<Image> images = imageMapper.selectImageByPersonId(person.getPersonId());
        model.addAttribute("images", images);
        model.addAttribute("videoId", images.get(0).getVideoId());
        model.addAttribute("updateImageState", "图片信息更新成功！");
        return "image/image";
    }

    /*
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
        List<Image> images = imageMapper.selectImageByPersonId(image.getPersonId());
        model.addAttribute("images", images);
        model.addAttribute("videoId", image.getVideoId());
        return "image/image";
    }
    */

    // 删除人物信息
    @RequestMapping("/deleteImage")
    // 开启事务支持
    @Transactional
    public String deleteImage(@RequestParam("imageId") String[] imageIdList, Model model) {
        String curUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectUserByUsername(curUser);
        Image image = imageMapper.selectImageByImageId(imageIdList[0]);
        Video video = videoMapper.selectVideoById(image.getVideoId());
        // 如果当前用户不是管理员用户，并且不是当前视频的所有者，则不能删除
        if (user.getRole().contains("admin") || curUser.equals(video.getOwner())) {
            // 删除图片数据库信息，并更新相应的人物数据库信息
            Person person = personMapper.selectPersonByPersonId(image.getPersonId());
            int tmpPlayPhoneNum = 0, tmpPlayLaptopNum = 0, tmpReadBookNum = 0, tmpRaiseHandNum = 0, tmpBowNum = 0, tmpLeanNum = 0;
            for (String imageId : imageIdList) {
                Image curImage=imageMapper.selectImageByImageId(imageId);
                imageMapper.deleteImageByImageId(imageId);
                tmpPlayPhoneNum += curImage.isPlayPhoneExist() ? 1 : 0;
                tmpPlayLaptopNum += curImage.isPlayLaptopExist() ? 1 : 0;
                tmpReadBookNum += curImage.isReadBookExist() ? 1 : 0;
                tmpRaiseHandNum += curImage.isRaiseHandExist() ? 1 : 0;
                tmpBowNum += curImage.isBowExist() ? 1 : 0;
                tmpLeanNum += curImage.isLeanExist() ? 1 : 0;
            }
            person.setPlayPhoneNum(person.getPlayPhoneNum() - tmpPlayPhoneNum);
            person.setPlayLaptopNum(person.getPlayLaptopNum() - tmpPlayLaptopNum);
            person.setReadBookNum(person.getReadBookNum() - tmpReadBookNum);
            person.setRaiseHandNum(person.getRaiseHandNum() - tmpRaiseHandNum);
            person.setBowNum(person.getBowNum() - tmpBowNum);
            person.setLeanNum(person.getLeanNum() - tmpLeanNum);
            personMapper.updatePerson(person);
            model.addAttribute("deleteImageState", "删除图片信息成功！");
        } else {
            model.addAttribute("deleteImageState", "普通用户只能在自己上传的课程下进行删除！");
        }
        List<Image> images = imageMapper.selectImageByPersonId(image.getPersonId());
        model.addAttribute("images", images);
        model.addAttribute("videoId", image.getVideoId());
        return "image/image";
    }
}

