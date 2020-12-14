package com.ecnu.controller;

import com.ecnu.mapper.*;
import com.ecnu.pojo.*;
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
public class PersonController {

    @Autowired
    UserMapper userMapper;
    @Autowired
    VideoMapper videoMapper;
    @Autowired
    PersonMapper personMapper;
    @Autowired
    ImageMapper imageMapper;
    @Autowired
    StudentMapper studentMapper;

    // 根据视频id查询所有人物，并在人物页面显示
    @RequestMapping("/person/{videoId}")
    public String showPerson(@PathVariable("videoId") String videoId, Model model) {
        Collection<Person> persons = personMapper.selectPersonByVideoId(videoId);
        String unit = videoMapper.selectVideoById(videoId).getPlace();
        Collection<Student> students = studentMapper.selectStudentByUnit(unit);
        int absent = 0, total = 0;
        for (Student student : students) {
            total++;
            int flag = 0;
            // 查看当前学生是否被识别
            for (Person person : persons) {
                if (student.getNumber().equals(person.getNumber())) {
                    flag = 1;
                    break;
                }
            }
            // 如果没有被识别出来，则认为缺席，并添加到persons中
            if (flag == 0) {
                absent++;
                Person newPerson = new Person();
                newPerson.setNumber(student.getNumber());
                newPerson.setName(student.getName());
                newPerson.setPlayPhoneNum(-1);
                newPerson.setPlayLaptopNum(-1);
                newPerson.setReadBookNum(-1);
                newPerson.setRaiseHandNum(-1);
                newPerson.setBowNum(-1);
                newPerson.setLeanNum(-1);
                persons.add(newPerson);
            }
        }
        model.addAttribute("persons", persons);
        model.addAttribute("absent", absent);
        model.addAttribute("total", total);
        return "person/person";
    }

    // 跳转到更新页面，利用携带的人物id信息查出人物，并在人物更新页面显示原数据
    @RequestMapping("/toUpdatePersonPage")
    public String toUpdatePersonPage(@RequestParam("personId") String personId,
                                     Model model) {
        // 查出原来的数据
        Person person = personMapper.selectPersonByPersonId(personId);
        model.addAttribute("person", person);
        // 根据上课地点（班级代号）查询所有学生名单
        String unit = videoMapper.selectVideoById(person.getVideoId()).getPlace();
        Collection<Student> students = studentMapper.selectStudentByUnit(unit);
        model.addAttribute("students", students);
        return "person/updatePerson";
    }

    // 更新人物信息
    @RequestMapping("/updatePerson")
    // 开启事务支持
    @Transactional
    public String updateImage(@RequestParam("personId") String personId,
                              @RequestParam("number") String number,
                              // @RequestParam("name") String name,
                              Model model) {
        String name = studentMapper.selectStudentByNumber(number).getName();
        System.out.println("personId: " + personId + "  number: " + number + " name: " + name);
        Person person = personMapper.selectPersonByPersonId(personId);
        List<Person> personList = personMapper.selectPersonByNumber(person.getVideoId(), number);

        // 如果学号已存在，则需要合并相关数据库信息
        int tmpPlayPhoneNum = 0, tmpPlayLaptopNum = 0, tmpReadBookNum = 0, tmpRaiseHandNum = 0, tmpBowNum = 0, tmpLeanNum = 0;
        for (Person curPerson : personList) {
            if (curPerson.getPersonId().equals(personId)) {
                continue;
            }
            // 将相同学号的人物信息合并
            tmpPlayPhoneNum += curPerson.getPlayPhoneNum();
            tmpPlayLaptopNum += curPerson.getPlayLaptopNum();
            tmpReadBookNum += curPerson.getReadBookNum();
            tmpRaiseHandNum += curPerson.getRaiseHandNum();
            tmpBowNum += curPerson.getBowNum();
            tmpLeanNum += curPerson.getLeanNum();
            // 将所有学号相同的人物下的所有图片对应的person_id改成当前的personId
            List<Image> imageList = imageMapper.selectImageByPersonId(curPerson.getPersonId());
            for (Image image : imageList) {
                image.setPersonId(personId);
                imageMapper.updateImage(image);
            }
            // 删除当前人物的数据库信息
            personMapper.deletePersonByPersonId(curPerson.getPersonId());
        }

        // 更新人物信息
        person.setNumber(number);
        person.setName(name);
        person.setPlayPhoneNum(tmpPlayPhoneNum + person.getPlayPhoneNum());
        person.setPlayLaptopNum(tmpPlayLaptopNum + person.getPlayLaptopNum());
        person.setReadBookNum(tmpReadBookNum + person.getReadBookNum());
        person.setRaiseHandNum(tmpRaiseHandNum + person.getRaiseHandNum());
        person.setBowNum(tmpBowNum + person.getBowNum());
        person.setLeanNum(tmpLeanNum + person.getLeanNum());
        personMapper.updatePerson(person);
        Collection<Person> persons = personMapper.selectPersonByVideoId(person.getVideoId());
        String unit = videoMapper.selectVideoById(person.getVideoId()).getPlace();
        Collection<Student> students = studentMapper.selectStudentByUnit(unit);
        int absent = 0, total = 0;
        for (Student student : students) {
            total++;
            int flag = 0;
            // 查看当前学生是否被识别
            for (Person ps : persons) {
                if (student.getNumber().equals(ps.getNumber())) {
                    flag = 1;
                    break;
                }
            }
            // 如果没有被识别出来，则认为缺席，并添加到persons中
            if (flag == 0) {
                absent++;
                Person newPerson = new Person();
                newPerson.setNumber(student.getNumber());
                newPerson.setName(student.getName());
                newPerson.setPlayPhoneNum(-1);
                newPerson.setPlayLaptopNum(-1);
                newPerson.setReadBookNum(-1);
                newPerson.setRaiseHandNum(-1);
                newPerson.setBowNum(-1);
                newPerson.setLeanNum(-1);
                persons.add(newPerson);
            }
        }
        model.addAttribute("persons", persons);
        model.addAttribute("absent", absent);
        model.addAttribute("total", total);
        model.addAttribute("updatePersonState", "人物信息更新成功！");
        return "person/person";
    }

    // 删除人物信息
    @RequestMapping("/deletePerson")
    // 开启事务支持
    @Transactional
    public String deletePerson(@RequestParam("personId") String personId, Model model) {
        String curUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.selectUserByUsername(curUser);
        Person person = personMapper.selectPersonByPersonId(personId);
        Video video = videoMapper.selectVideoById(person.getVideoId());
        // 如果当前用户不是管理员用户，并且不是当前视频的所有者，则不能删除
        if (user.getRole().contains("admin") || curUser.equals(video.getOwner())) {
            // 删除对应的图片文件

            // 删除数据库信息
            personMapper.deletePersonByPersonId(personId);
            imageMapper.deleteImageByPersonId(personId);
            model.addAttribute("deletePersonState", "删除人物信息成功！");
        } else {
            model.addAttribute("deletePersonState", "普通用户只能在自己上传的课程下进行删除！");
        }
        Collection<Person> persons = personMapper.selectPersonByVideoId(video.getVideoId());
        String unit = videoMapper.selectVideoById(person.getVideoId()).getPlace();
        Collection<Student> students = studentMapper.selectStudentByUnit(unit);
        int absent = 0, total = 0;
        for (Student student : students) {
            total++;
            int flag = 0;
            // 查看当前学生是否被识别
            for (Person ps : persons) {
                if (student.getNumber().equals(ps.getNumber())) {
                    flag = 1;
                    break;
                }
            }
            // 如果没有被识别出来，则认为缺席，并添加到persons中
            if (flag == 0) {
                absent++;
                Person newPerson = new Person();
                newPerson.setNumber(student.getNumber());
                newPerson.setName(student.getName());
                newPerson.setPlayPhoneNum(-1);
                newPerson.setPlayLaptopNum(-1);
                newPerson.setReadBookNum(-1);
                newPerson.setRaiseHandNum(-1);
                newPerson.setBowNum(-1);
                newPerson.setLeanNum(-1);
                persons.add(newPerson);
            }
        }
        model.addAttribute("persons", persons);
        model.addAttribute("absent", absent);
        model.addAttribute("total", total);
        return "person/person";
    }
}
