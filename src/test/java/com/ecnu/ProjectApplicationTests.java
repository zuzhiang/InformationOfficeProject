package com.ecnu;

import com.ecnu.controller.VideoController;
import com.ecnu.mapper.ImageMapper;
import com.ecnu.mapper.PersonMapper;
import com.ecnu.mapper.VideoMapper;
import com.ecnu.pojo.Image;
import com.ecnu.pojo.Person;
import com.ecnu.pojo.Video;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

@SpringBootTest
class ProjectApplicationTests {

    @Autowired
    VideoMapper videoMapper;
    @Autowired
    ImageMapper imageMapper;
    @Autowired
    PersonMapper personMapper;

    @Test
    void contextLoads() {
        List<Video> videos = videoMapper.selectAllVideo();
        System.out.println("selectAllVideo");
        for (Video item : videos) {
            System.out.println(item);
        }
    }

    @Test
    void testVideo() {

        System.out.println("selectVideoById");
        System.out.println(videoMapper.selectVideoById("1"));

        Video video = new Video();
        String video_id = UUID.randomUUID().toString();
        video.setVideoId(video_id);
        video.setPlace("111");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String courseDate = dateFormat.format(new Date()).replace(' ', 'T');
        video.setCourseDate(courseDate);
        System.out.println("addVideo");
        videoMapper.addVideo(video);

        List<Video> videos = videoMapper.selectAllVideo();
        System.out.println("selectAllVideo");
        for (Video item : videos) {
            System.out.println(item);
        }

        video.setVideoId(video_id);
        video.setPlace("222");
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        courseDate = dateFormat.format(new Date()).replace(' ', 'T');
        video.setCourseDate(courseDate);
        System.out.println("updateVideo");
        videoMapper.updateVideo(video);

        videos = videoMapper.selectAllVideo();
        System.out.println("selectAllVideo");
        for (Video item : videos) {
            System.out.println(item);
        }

        System.out.println("deleteVideo");
        videoMapper.deleteVideo(video_id);

        videos = videoMapper.selectAllVideo();
        System.out.println("selectAllVideo");
        for (Video item : videos) {
            System.out.println(item);
        }
    }

    @Test
    public void testPerson() {

        Person person = new Person();
        String personId = UUID.randomUUID().toString();
        person.setPersonId(personId);
        person.setName("zuzhiang");
        person.setVideoId("111");
        person.setBowNum(11);
        System.out.println("addPerson");
        personMapper.addPerson(person);

        List<Person> persons = personMapper.selectPersonByName("zuzhiang");
        System.out.println("selectPersonByName");
        for (Person p : persons) {
            System.out.println(p);
        }

        persons = personMapper.selectPersonByVideoId("111");
        System.out.println("selectPersonByVideoId");
        for (Person p : persons) {
            System.out.println(p);
        }

        personMapper.deletePersonByVideoId("111");
        System.out.println("deletePersonByVideoId");

        persons = personMapper.selectPersonByVideoId("111");
        System.out.println("selectPersonByVideoId");
        for (Person p : persons) {
            System.out.println(p);
        }
    }

    @Test
    void testImage() {

        Image image = new Image();
        String image_id = UUID.randomUUID().toString();
        image.setImageId(image_id);
        image.setVideoId("111");
        image.setPersonId("111");
        image.setHandExist(true);
        image.setPhoneExist(false);
        System.out.println("addVideo");
        imageMapper.addImage(image);

        List<Image> images = imageMapper.selectImageByPersonId("111");
        System.out.println("selectImageByPersonId");
        for (Image item : images) {
            System.out.println(item);
        }

        images = imageMapper.selectImageByVideoId("111");
        System.out.println("selectImageByVideoId");
        for (Image item : images) {
            System.out.println(item);
        }

        imageMapper.deleteImageByVideoId("111");
        System.out.println("deleteImageByVideoId");

        images = imageMapper.selectImageByVideoId("111");
        System.out.println("selectImageByVideoId");
        for (Image item : images) {
            System.out.println(item);
        }
    }
}

