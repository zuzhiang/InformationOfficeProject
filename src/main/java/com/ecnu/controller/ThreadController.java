package com.ecnu.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ecnu.config.SpringContextConfig;
import com.ecnu.mapper.ImageMapper;
import com.ecnu.mapper.PersonMapper;
import com.ecnu.mapper.VideoMapper;
import com.ecnu.pojo.Image;
import com.ecnu.pojo.Person;
import com.ecnu.pojo.Video;
import com.ecnu.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.UUID;

@Controller
public class ThreadController {

    VideoMapper videoMapper = SpringContextConfig.getBean(VideoMapper.class);
    PersonMapper personMapper = SpringContextConfig.getBean(PersonMapper.class);
    ImageMapper imageMapper = SpringContextConfig.getBean(ImageMapper.class);

    @Autowired
    Utils utils;

    private String returnPath = "/home/jchen/zuzhiang/results/return";

    // 读取返回的json文件，并将信息添加到Person和Image表中
    public void addPersonImageInfo(String videoId) {
        //String videoName = "1127";
        //returnPath = "C:\\Data\\1.Work\\02.Code\\zhaochun\\results\\return";
        String jsonPath = returnPath + "/" + videoId + ".txt";
        System.out.println("returnPath: " + returnPath);
        System.out.println("jsonPath: " + jsonPath);
        String s = utils.readJsonFile(jsonPath);
        JSONObject jobj = JSON.parseObject(s);
        for (String number : jobj.keySet()) { // 遍历每个学号
            JSONObject personInfo = (JSONObject) jobj.get(number);
            Person person = new Person();
            String personId = UUID.randomUUID().toString();
            person.setPersonId(personId);
            person.setVideoId(videoId);
            person.setNumber(number.split("_")[0]);
            person.setName(personInfo.getString("name"));
            person.setPlayPhoneNum(personInfo.getInteger("playphone_num"));
            person.setPlayLaptopNum(personInfo.getInteger("playlaptop_num"));
            person.setReadBookNum(personInfo.getInteger("readbook_num"));
            person.setRaiseHandNum(personInfo.getInteger("raisehand_num"));
            person.setBowNum(personInfo.getInteger("bow_num"));
            person.setLeanNum(personInfo.getInteger("lean_num"));
            personMapper.addPerson(person);

            JSONArray object = personInfo.getJSONArray("object");
            JSONArray pose = personInfo.getJSONArray("pose");
            for (int i = 0; i < object.size(); i++) {
                JSONObject objImg = (JSONObject) object.get(i);
                JSONObject poseImg = (JSONObject) pose.get(i);
                Image image = new Image();
                image.setImageId(UUID.randomUUID().toString());
                image.setVideoId(videoId);
                image.setPersonId(personId);
                image.setObjPath((String) objImg.get("address"));
                image.setPosePath((String) poseImg.get("address"));
                image.setHandExist((int) objImg.get("hand_exist") == 1 ? true : false);
                image.setPhoneExist((int) objImg.get("phone_exist") == 1 ? true : false);
                image.setLaptopExist((int) objImg.get("laptop_exist") == 1 ? true : false);
                image.setBookExist((int) objImg.get("book_exist") == 1 ? true : false);
                image.setPlayPhoneExist((int) objImg.get("playphone_exist") == 1 ? true : false);
                image.setPlayLaptopExist((int) objImg.get("playlaptop_exist") == 1 ? true : false);
                image.setReadBookExist((int) objImg.get("readbook_exist") == 1 ? true : false);
                image.setRaiseHandExist((int) poseImg.get("raisehand_exist") == 1 ? true : false);
                image.setBowExist((int) poseImg.get("bow_exist") == 1 ? true : false);
                image.setLeanExist((int) poseImg.get("lean_exist") == 1 ? true : false);
                String path = image.getObjPath();
                String tmp = path.split("\\.")[0];
                String[] lst = tmp.split("_");
                String time = lst[lst.length - 2] + lst[lst.length - 1];
                image.setTime(time);
                // image.setTime((String) objImg.get("time"));
                imageMapper.addImage(image);
            }
        }
    }

    // 使用多线程异步处理视频，并将信息添加到数据库
    public String asynTask(String videoId) {
        try {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    VideoHandleController videoHandleController = new VideoHandleController();
                    System.out.println("start asyntask.......");
                    try {
                        // 视频处理：切分图片、姿态检测、物品识别、人脸识别、计算分数等
                        videoHandleController.videoHandle(videoId, videoMapper.selectVideoById(videoId).getPlace());
                        System.out.println("视频处理完毕！");
                        // 往数据库添加视频信息
                        addPersonImageInfo(videoId); // 往数据库中增加Person和Image信息
                        System.out.println("person and image");
                        // 将视频的state改为true，表示已经处理完
                        Video video = videoMapper.selectVideoById(videoId);
                        video.setState(true);
                        videoMapper.updateVideo(video);

                        System.out.println("task has finished....");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }


}
