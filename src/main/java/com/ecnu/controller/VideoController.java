package com.ecnu.controller;

import com.ecnu.mapper.ImageMapper;
import com.ecnu.mapper.PersonMapper;
import com.ecnu.mapper.UserMapper;
import com.ecnu.mapper.VideoMapper;
import com.ecnu.pojo.User;
import com.ecnu.pojo.Video;
import com.ecnu.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
// 视频的控制类
public class VideoController {

    @Autowired
    VideoMapper videoMapper;
    @Autowired
    PersonMapper personMapper;
    @Autowired
    ImageMapper imageMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    Utils utils;

    // 视频存储的根目录
    @Value("${videoPath}")
    private String videoPath;
    @Value("${objPath}")
    private String objPath;
    @Value("${posePath}")
    private String posePath;
    @Value("${facePath}")
    private String facePath;
    @Value("${returnPath}")
    private String returnPath;
    @Value("${txtPath}")
    private String txtPath;

    // 查询所有视频并跳转到视频页面展示
    @RequestMapping("/video")
    public String showVideo(Model model) {
        Collection<Video> videos = videoMapper.selectAllVideo();
        model.addAttribute("videos", videos);
        return "video/video";
    }

    // 查找视频
    @RequestMapping("/selectVideo")
    public String selectVideo(@RequestParam("selectType") String selectType,
                              @RequestParam("content") String content,
                              Model model) {
        Collection<Video> videos;
        if (selectType.equals("teacher")) { // 按教师查
            videos = videoMapper.selectVideoByTeacher(content);
        } else { // 按上课地点查
            videos = videoMapper.selectVideoByPlace(content);
        }
        model.addAttribute("videos", videos);
        return "video/video";
    }

    // 跳转到添加视频页面
    @RequestMapping("/toAddVideoPage")
    public String toAddVideoPage() {
        return "video/addVideo";
    }

    // 跳转到更新页面，利用携带的视频id信息查出视频，并在视频更新页面显示原数据
    @RequestMapping("/toUpdateVideoPage")
    public String toUpdateVideoPage(@RequestParam("VideoId") String videoId, Model model) {
        // 查出原来的数据
        Video video = videoMapper.selectVideoById(videoId);
        model.addAttribute("video", video);
        model.addAttribute("courseDate", video.getCourseDate());
        return "video/updateVideo";
    }


    // 将视频文件写入到指定位置，并将视频信息添加到数据库，并返回状态信息
    //@Async // 当前方法为异步方法
    @RequestMapping("/addVideo")
    public String addVideo(@RequestParam("file") MultipartFile file,
                           @RequestParam("teacher") String teacher,
                           @RequestParam("place") String place,
                           @RequestParam("courseDate") String courseDate,
                           Model model) {
        // 若文件为空
        if (file.isEmpty()) {
            model.addAttribute("addVideoState", "视频添加失败！");
            return "video/addVideo";
        }
        utils.makeDirs(videoPath);
        String[] ext = file.getOriginalFilename().split("\\."); // 视频文件后缀名，点需要转义
        String videoId = UUID.randomUUID().toString();
        String videoName = videoId + "." + ext[ext.length - 1];
        System.out.println("videoPath: " + videoPath);
        System.out.println(videoPath + "/" + videoName);
        File dest = new File(videoPath + "/" + videoName);
        try {
            System.out.println("video add");
            // 写入视频文件
            file.transferTo(dest);
            System.out.println("文件写入成功！");
            // 往数据库添加视频信息
            Video video = new Video();
            video.setVideoId(videoId);
            video.setPath(videoName);
            video.setTeacher(teacher);
            video.setPlace(place);
            video.setCourseDate(courseDate);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String uploadDate = dateFormat.format(new Date()).replace(' ', 'T');
            video.setUploadDate(uploadDate);
            video.setState(false);
            // 获取当前登录用户
            String curUser = SecurityContextHolder.getContext().getAuthentication().getName();
            video.setOwner(curUser);
            videoMapper.addVideo(video);
            // 使用多线程进行视频的处理，并将结果信息保存到数据库
            ThreadController threadController = new ThreadController();
            threadController.asynTask(videoId);
            model.addAttribute("addVideoState", "视频添加成功！");
        } catch (IOException e) {
            System.out.println("出错了！！！！！！！！！");
            System.out.println(e);
            model.addAttribute("addVideoState", "视频添加失败！");
        }
        return "video/addVideo";
    }

    // 更新视频信息
    @RequestMapping("/updateVideo")
    public String updateVideo(@RequestParam("videoId") String videoId,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam("teacher") String teacher,
                              @RequestParam("place") String place,
                              @RequestParam("courseDate") String courseDate,
                              Model model) {
        Video video = videoMapper.selectVideoById(videoId);
        if (!file.isEmpty()) { // 如果更新了视频文件
            try {
                utils.makeDirs(videoPath);
                // 写入视频文件
                File dest = new File(videoPath + "/" + videoId);
                file.transferTo(dest);
                // 更新视频信息
                video.setTeacher(teacher);
                video.setPlace(place);
                video.setCourseDate(courseDate);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String uploadDate = dateFormat.format(new Date()).replace(' ', 'T');
                video.setUploadDate(uploadDate);
                videoMapper.updateVideo(video);
                model.addAttribute("updateVideoState", "视频信息更新成功！");
            } catch (IOException e) {
                System.out.println(e);
                model.addAttribute("updateVideoState", "视频信息更新失败！");
            }
        } else {
            // 只更新视频信息
            video.setTeacher(teacher);
            video.setPlace(place);
            video.setCourseDate(courseDate);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String uploadDate = dateFormat.format(new Date()).replace(' ', 'T');
            video.setUploadDate(uploadDate);
            videoMapper.updateVideo(video);
            model.addAttribute("updateVideoState", "视频信息更新成功！");
        }
        model.addAttribute("video", video);
        return "video/updateVideo";
    }

    // 删除视频以及对应的人物、图片信息
    @RequestMapping("/deleteVideo")
    // 开启事务支持
    @Transactional
    public String deleteVideo(@RequestParam("videoId") String videoId, Model model) {
        String curUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectUserByUsername(curUser);
        Video video = videoMapper.selectVideoById(videoId);
        // 如果当前用户不是管理员用户，并且不是当前视频的所有者，则不能删除
        if (user.getRole().contains("admin") || curUser.equals(video.getOwner())) {
            // 删除视频及其对应的人物和图片
            String tmpObjPath = objPath + "/" + videoId;
            String tmpPosePath = posePath + "/" + videoId;
            String tmpFacePath = facePath + "/" + videoId;
            String tmpTxtPath = txtPath + "/" + videoId;
            String tmpReturnPath = returnPath + "/" + videoId + ".txt";

            utils.deleteFile(utils.searchFiles(new File(videoPath), videoId).get(0));
            String[] paths = {tmpObjPath, tmpPosePath, tmpFacePath, tmpTxtPath, tmpReturnPath};
            for (String path : paths) {
                File file = new File(path);
                utils.deleteFile(file);
            }
            // 删除数据库信息
            videoMapper.deleteVideo(videoId);
            personMapper.deletePersonByVideoId(videoId);
            imageMapper.deleteImageByVideoId(videoId);
            model.addAttribute("deleteVideoState", "删除视频成功！");
        } else {
            model.addAttribute("deleteVideoState", "普通用户只能删除自己上传的视频！");
        }
        Collection<Video> videos = videoMapper.selectAllVideo();
        model.addAttribute("videos", videos);
        return "video/video";
    }
}
