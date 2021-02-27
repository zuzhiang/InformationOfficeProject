package com.ecnu;

import com.ecnu.controller.ThreadController;
import com.ecnu.controller.VideoController;
import com.ecnu.controller.VisualController;
import com.ecnu.mapper.*;
import com.ecnu.pojo.*;
import com.ecnu.utils.Utils;
import net.minidev.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileFilter;
import java.sql.SQLOutput;
import java.sql.Timestamp;
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

    public double getExpScore(int x, List<Integer> list) {
        int size = list.size();
        double max = list.get(size - 1);
        double mid;
        if (size % 2 == 1) {
            mid = list.get((size - 1) / 2);
        } else {
            mid = (list.get(size / 2) + list.get(size / 2 - 1)) / 2.0;
        }
        return 3.0 * Math.exp((x - mid) / (max - mid) * Math.log(5.0 / 3.0));
    }

    public double getMeanScore(double x, List<Double> list) {
        int size = list.size();
        double min = list.get(0), max = list.get(size - 1);
        double sum = 0;
        for (Double itm : list) {
            sum += itm;
        }
        double avg = sum / size;
        if (x == avg) {
            return 3;
        } else if (x < avg) {
            return 3 - 2.0 * (avg - x) / (avg - min);
        } else {
            return 3 + 2.0 * (x - avg) / (max - avg);
        }
    }

    @Test
    public void getNewInfo() {
        double phoneCoef = 0.1, laptopCoef = 0.1, bookCoef = 0.1, handCoef = 0.1, bowCoef = 0.1, leanCoef = 0.1;

        System.out.println(personMapper);
        List<Person> personList = personMapper.selectAllPerson();
        List<Integer> expList = new ArrayList<Integer>();
        for (Person person : personList) {
            expList.add(person.getPlayPhoneNum());
            expList.add(person.getPlayLaptopNum());
            expList.add(person.getReadBookNum());
            expList.add(person.getRaiseHandNum());
            expList.add(person.getBowNum());
            expList.add(person.getLeanNum());
        }
        Collections.sort(expList);
        List<Double> meanList = new ArrayList<Double>();
        for (Person person : personList) {
            double phoneScore = getExpScore(person.getPlayPhoneNum(), expList), laptopScore = getExpScore(person.getPlayLaptopNum(), expList);
            double bookScore = getExpScore(person.getReadBookNum(), expList), handScore = getExpScore(person.getRaiseHandNum(), expList);
            double bowScore = getExpScore(person.getBowNum(), expList), leanScore = getExpScore(person.getLeanNum(), expList);
            meanList.add(phoneScore);
            meanList.add(laptopScore);
            meanList.add(bookScore);
            meanList.add(handScore);
            meanList.add(bowScore);
            meanList.add(leanScore);
            person.setPlayPhoneScore(phoneScore);
            person.setPlayLaptopScore(laptopScore);
            person.setReadBookScore(bookScore);
            person.setRaiseHandScore(handScore);
            person.setBowScore(bowScore);
            person.setLeanScore(leanScore);
        }
        Collections.sort(meanList);
        for (Person person : personList) {
            person.setPlayPhoneScore(getMeanScore(person.getPlayPhoneScore(), meanList));
            person.setPlayLaptopScore(getMeanScore(person.getPlayLaptopScore(), meanList));
            person.setReadBookScore(getMeanScore(person.getReadBookScore(), meanList));
            person.setRaiseHandScore(getMeanScore(person.getRaiseHandScore(), meanList));
            person.setBowScore(getMeanScore(person.getBowScore(), meanList));
            person.setLeanScore(getMeanScore(person.getLeanScore(), meanList));

            person.setInvestment(phoneCoef * person.getPlayPhoneScore() + laptopCoef * person.getPlayLaptopScore() + bookCoef * person.getReadBookScore() + handCoef * person.getRaiseHandScore() + bowCoef * person.getBowScore() + leanCoef * person.getLeanScore());
            person.setDivorce(phoneCoef * person.getPlayPhoneScore() + laptopCoef * person.getPlayLaptopScore() + bookCoef * person.getReadBookScore() + handCoef * person.getRaiseHandScore() + bowCoef * person.getBowScore() + leanCoef * person.getLeanScore());
            person.setListen(phoneCoef * person.getPlayPhoneScore() + laptopCoef * person.getPlayLaptopScore() + bookCoef * person.getReadBookScore() + handCoef * person.getRaiseHandScore() + bowCoef * person.getBowScore() + leanCoef * person.getLeanScore());
            person.setCommunication(phoneCoef * person.getPlayPhoneScore() + laptopCoef * person.getPlayLaptopScore() + bookCoef * person.getReadBookScore() + handCoef * person.getRaiseHandScore() + bowCoef * person.getBowScore() + leanCoef * person.getLeanScore());
            personMapper.updatePerson(person);
        }
    }

    @Test
    void justTest() {
        Timestamp timestamp=new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp.toString().substring(0,19));
        System.out.println(Timestamp.valueOf(timestamp.toString().substring(0,19)));
    }

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

    @Test
    void updatePersonTable() {
        Collection<Video> videos = videoMapper.selectAllVideo();
        for (Video video : videos) {
            System.out.println("viede id: " + video.getVideoId());
            // if (!video.getVideoId().equals("0d834201-1fef-4d52-96c4-305ed0ca747b")) {
            //    continue;
            //}
            Collection<Person> persons = personMapper.selectPersonByVideoId(video.getVideoId());
            for (Person person : persons) {
                int phone = 0, laptop = 0, book = 0, hand = 0, bow = 0, lean = 0;
                Collection<Image> images = imageMapper.selectImageByPersonId(person.getPersonId());
                for (Image image : images) {
                    phone += image.isPlayPhoneExist() ? 1 : 0;
                    laptop += image.isPlayLaptopExist() ? 1 : 0;
                    book += image.isReadBookExist() ? 1 : 0;
                    hand += image.isRaiseHandExist() ? 1 : 0;
                    bow += image.isBowExist() ? 1 : 0;
                    lean += image.isLeanExist() ? 1 : 0;
                }
                person.setPlayPhoneNum(phone);
                person.setPlayLaptopNum(laptop);
                person.setReadBookNum(book);
                person.setRaiseHandNum(hand);
                person.setBowNum(bow);
                person.setLeanNum(lean);
                personMapper.updatePerson(person);
                // System.out.println(person.getName() + "  phone: " + phone + "  laptop: " + laptop + "  book: " + book + "  hand: " + hand + "  bow: " + bow + "  lean: " + lean);
            }
        }
    }
}

