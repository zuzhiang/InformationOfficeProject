package com.ecnu.controller;

import com.ecnu.mapper.*;
import com.ecnu.pojo.*;
import com.ecnu.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    @Autowired
    Utils utils;


    public Model getPersons(String videoId, Model model) {
        List<Person> persons = personMapper.selectPersonByVideoId(videoId);
        String unit = videoMapper.selectVideoById(videoId).getPlace();
        List<Student> students = studentMapper.selectStudentByUnit(unit);
        int absent = 0, total = -1;
        for (Student student : students) {
            total++;
            int flag = 0;
            // 查看当前学生是否被识别
            for (Person person : persons) {
                if (student.getNumber().equals(person.getNumber()) || student.getNumber().equals("-")) {
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
        if (total < 0) total = 0;
        model.addAttribute("persons", persons);
        model.addAttribute("absent", absent);
        model.addAttribute("total", total);
        return model;
    }

    // 根据视频id查询所有人物，并在人物页面显示
    @RequestMapping("/person/{videoId}")
    public String showPerson(@PathVariable("videoId") String videoId, Model model) {
        model = getPersons(videoId, model);
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
        List<Student> students = studentMapper.selectStudentByUnit(unit);
        model.addAttribute("students", students);
        return "person/updatePerson";
    }

    // 更新人物信息
    @RequestMapping("/updatePerson")
    // 开启事务支持
    @Transactional
    public String updatePerson(@RequestParam("personId") String personId,
                               @RequestParam("number") String number,
                               // @RequestParam("name") String name,
                               Model model) {
        String unit = videoMapper.selectVideoById(personMapper.selectPersonByPersonId(personId).getVideoId()).getPlace();
        String name = studentMapper.selectStudentByNumber(number, unit).getName();
        System.out.println("personId: " + personId + "  number: " + number + " name: " + name);
        Person person = personMapper.selectPersonByPersonId(personId);

        // 更新人物信息
        person.setNumber(number);
        person.setName(name);
        personMapper.updatePerson(person);

        model = getPersons(person.getVideoId(), model);
        model.addAttribute("updatePersonState", "人物信息更新成功！");
        return "person/person";
    }


    // 合并人物信息
    @RequestMapping("/mergePerson")
    // 开启事务支持
    @Transactional
    public String mergePerson(@RequestParam("personIdList") String[] personIdList,
                              Model model) {
        ArrayList<Person> personList = new ArrayList<Person>();
        for (String personId : personIdList) {
            personList.add(personMapper.selectPersonByPersonId(personId));
        }
        Person person = personList.get(0);
        String personId = person.getPersonId();
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
        person.setPlayPhoneNum(tmpPlayPhoneNum + person.getPlayPhoneNum());
        person.setPlayLaptopNum(tmpPlayLaptopNum + person.getPlayLaptopNum());
        person.setReadBookNum(tmpReadBookNum + person.getReadBookNum());
        person.setRaiseHandNum(tmpRaiseHandNum + person.getRaiseHandNum());
        person.setBowNum(tmpBowNum + person.getBowNum());
        person.setLeanNum(tmpLeanNum + person.getLeanNum());
        personMapper.updatePerson(person);
        model = getPersons(person.getVideoId(), model);
        model.addAttribute("mergePersonState", "人物信息合并成功！");
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

        model = getPersons(video.getVideoId(), model);
        return "person/person";
    }


    // 获取人物表的统计信息（动作得分、投入度等）
    @RequestMapping("/getStaticalInfo")
    public String getStaticalInfo(@RequestParam("investPhone") double investPhone, @RequestParam("investLaptop") double investLaptop,
                                @RequestParam("investBook") double investBook, @RequestParam("investHand") double investHand,
                                @RequestParam("investBow") double investBow, @RequestParam("investLean") double investLean,
                                @RequestParam("divorcePhone") double divorcePhone, @RequestParam("divorceLaptop") double divorceLaptop,
                                @RequestParam("divorceBook") double divorceBook, @RequestParam("divorceHand") double divorceHand,
                                @RequestParam("divorceBow") double divorceBow, @RequestParam("divorceLean") double divorceLean,
                                @RequestParam("listenPhone") double listenPhone, @RequestParam("listenLaptop") double listenLaptop,
                                @RequestParam("listenBook") double listenBook, @RequestParam("listenHand") double listenHand,
                                @RequestParam("listenBow") double listenBow, @RequestParam("listenLean") double listenLean,
                                @RequestParam("cmnctPhone") double cmnctPhone, @RequestParam("cmnctLaptop") double cmnctLaptop,
                                @RequestParam("cmnctBook") double cmnctBook, @RequestParam("cmnctHand") double cmnctHand,
                                @RequestParam("cmnctBow") double cmnctBow, @RequestParam("cmnctLean") double cmnctLean,
                                Model model) {
        try {
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
                double phoneScore = utils.getExpScore(person.getPlayPhoneNum(), expList), laptopScore = utils.getExpScore(person.getPlayLaptopNum(), expList);
                double bookScore = utils.getExpScore(person.getReadBookNum(), expList), handScore = utils.getExpScore(person.getRaiseHandNum(), expList);
                double bowScore = utils.getExpScore(person.getBowNum(), expList), leanScore = utils.getExpScore(person.getLeanNum(), expList);
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
                person.setPlayPhoneScore(utils.getMeanScore(person.getPlayPhoneScore(), meanList));
                person.setPlayLaptopScore(utils.getMeanScore(person.getPlayLaptopScore(), meanList));
                person.setReadBookScore(utils.getMeanScore(person.getReadBookScore(), meanList));
                person.setRaiseHandScore(utils.getMeanScore(person.getRaiseHandScore(), meanList));
                person.setBowScore(utils.getMeanScore(person.getBowScore(), meanList));
                person.setLeanScore(utils.getMeanScore(person.getLeanScore(), meanList));

                person.setInvestment(investPhone * person.getPlayPhoneScore() + investLaptop * person.getPlayLaptopScore() + investBook * person.getReadBookScore() + investHand * person.getRaiseHandScore() + investBow * person.getBowScore() + investLean * person.getLeanScore());
                person.setDivorce(divorcePhone * person.getPlayPhoneScore() + divorceLaptop * person.getPlayLaptopScore() + divorceBook * person.getReadBookScore() + divorceHand * person.getRaiseHandScore() + divorceBow * person.getBowScore() + divorceLean * person.getLeanScore());
                person.setListen(listenPhone * person.getPlayPhoneScore() + listenLaptop * person.getPlayLaptopScore() + listenBook * person.getReadBookScore() + listenHand * person.getRaiseHandScore() + listenBow * person.getBowScore() + listenLean * person.getLeanScore());
                person.setCommunication(cmnctPhone * person.getPlayPhoneScore() + cmnctLaptop * person.getPlayLaptopScore() + cmnctBook * person.getReadBookScore() + cmnctHand * person.getRaiseHandScore() + cmnctBow * person.getBowScore() + cmnctLean * person.getLeanScore());
                personMapper.updatePerson(person);
            }
            model.addAttribute("getStaticalInfoState","计算统计信息完毕！");
        } catch (Exception e) {
            model.addAttribute("getStaticalInfoState","计算统计信息失败！");
            System.out.println(e);
        }
        return "computation";
    }

    @RequestMapping("/getVisualInfo")
    public void getVisualInfo(@RequestParam("number") String number, Model model) {
        List<Person> personList = personMapper.selectPersonByNumber(number);

    }

}
