package com.ecnu;

import com.ecnu.controller.ThreadController;
import com.ecnu.controller.VideoController;
import com.ecnu.mapper.ImageMapper;
import com.ecnu.mapper.PersonMapper;
import com.ecnu.mapper.VideoMapper;
import com.ecnu.pojo.Image;
import com.ecnu.pojo.Person;
import com.ecnu.pojo.Video;
import com.ecnu.utils.Utils;
import net.minidev.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

@SpringBootTest
class ProjectApplicationTests {

    @Autowired
    VideoMapper videoMapper;
    @Autowired
    ImageMapper imageMapper;
    @Autowired
    PersonMapper personMapper;
    @Autowired
    Utils utils;

    @Test
    void updateDataBase() {

        String videoId = "9caee8ab-8d4a-4820-a82c-05220f32431a";

        ThreadController threadController = new ThreadController();
        // 往数据库添加视频信息
        threadController.addPersonImageInfo(videoId); // 往数据库中增加Person和Image信息
        System.out.println("person and image");
        // 将视频的state改为true，表示已经处理完
        Video video = videoMapper.selectVideoById(videoId);
        video.setState(true);
        videoMapper.updateVideo(video);
        System.out.println("task has finished....");
    }

    @Test
    void deleteFile() {

        String videoId = "8b0e7a5f-a708-469c-be7b-67fed9f12a70";
        // 删除不必要的文件，以节省空间
        String filePath = "/home/jchen/zuzhiang/images/" + videoId;
        String facePath = "/home/jchen/zuzhiang/results/face/" + videoId;
        String jsonPath = "/home/jchen/zuzhiang/results/pose/" + videoId + "/json/sep-json";
        String jsonFile = "/home/jchen/zuzhiang/results/pose/" + videoId + "/json/alphapose-results.json";
        String[] paths = {filePath, facePath, jsonPath, jsonFile};
        for (String path : paths) {
            System.out.println("delete: " + path);
            File file = new File(path);
            utils.deleteFile(file);
        }
    }

    @Test
    void updateImage() {
        Collection<Image> images = imageMapper.selectAllImage();
        int i = 0;
        for (Image image : images) {
            try {
                i += 1;
                String path = image.getObjPath();
                String tmp = path.split("\\.")[0];
                String[] lst = tmp.split("_");
                String time = lst[lst.length - 2] + lst[lst.length - 1];
                image.setTime(time);
                imageMapper.updateImage(image);
                System.out.println(time + "   " + i);
            } catch (Exception e) {
                System.out.println("wrong!!!!!!!!!!!");
                throw e;
            }
        }
        System.out.println("\n\nEnd");
    }
}

