package com.ecnu.controller;

import com.ecnu.mapper.PersonMapper;
import com.ecnu.mapper.StudentMapper;
import com.ecnu.mapper.VideoMapper;
import com.ecnu.pojo.Person;
import com.ecnu.pojo.Student;
import com.ecnu.pojo.Video;
import com.ecnu.pojo.Visual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.util.*;


@Controller
public class VisualController {

    @Autowired
    VideoMapper videoMapper;
    @Autowired
    PersonMapper personMapper;
    @Autowired
    StudentMapper studentMapper;

    @RequestMapping("/visual")
    public String toVisual(Model model) {
        List<Student> studentList = studentMapper.selectAllStudent();
        model.addAttribute("studentList", studentList);
        return "visual";
    }

    @RequestMapping("/computation")
    public String toCompuation() {
        return "computation";
    }


    public List<Visual> getVisualList(String unit, String number, Timestamp startTimeStamp, Timestamp endTimeStamp) {
        List<Visual> visualList = new ArrayList<Visual>();
        if (number.equals("all")) {
            List<Video> videoList = videoMapper.selectVideoByPlace(unit);
            for (Video video : videoList) {
                Timestamp courseDate = video.getCourseDate();
                if (courseDate.before(startTimeStamp) || courseDate.after(endTimeStamp)) {
                    continue;
                }
                List<Person> personList = personMapper.selectPersonByVideoId(video.getVideoId());
                double investment = 0, divorce = 0, listen = 0, cmnct = 0, phoneScore = 0, laptopScore = 0, bookScore = 0, handScore = 0, bowScore = 0, leanScore = 0;
                int personNum = 0, phoneNum = 0, laptopNum = 0, bookNum = 0, handNum = 0, bowNum = 0, leanNum = 0;
                for (Person person : personList) {
                    personNum += 1;
                    investment += person.getInvestment();
                    divorce += person.getDivorce();
                    listen += person.getListen();
                    cmnct += person.getCommunication();
                    phoneScore += person.getPlayPhoneScore();
                    laptopScore += person.getPlayLaptopScore();
                    bookScore += person.getReadBookScore();
                    handScore += person.getRaiseHandScore();
                    bowScore += person.getBowScore();
                    leanScore += person.getLeanScore();
                    phoneNum += person.getPlayPhoneNum();
                    laptopNum += person.getPlayLaptopNum();
                    bookNum += person.getReadBookNum();
                    handNum += person.getRaiseHandNum();
                    bowNum += person.getBowNum();
                    leanNum += person.getLeanNum();
                }
                Visual visual = new Visual();
                visual.setTime(courseDate);
                visual.setInvestment(investment / personNum);
                visual.setDivorce(divorce / personNum);
                visual.setListen(listen / personNum);
                visual.setCommunication(cmnct / personNum);
                visual.setPlayPhoneScore(phoneScore / personNum);
                visual.setPlayLaptopScore(laptopScore / personNum);
                visual.setReadBookScore(bookScore / personNum);
                visual.setRaiseHandScore(handScore / personNum);
                visual.setBowScore(bowScore / personNum);
                visual.setLeanScore(leanScore / personNum);
                visual.setPlayPhoneNum(phoneNum / personNum);
                visual.setPlayLaptopNum(laptopNum / personNum);
                visual.setReadBookNum(bookNum / personNum);
                visual.setRaiseHandNum(handNum / personNum);
                visual.setBowNum(bowNum / personNum);
                visual.setLeanNum(leanNum / personNum);
                visualList.add(visual);
            }

        } else {
            List<Person> personList = personMapper.selectPersonByNumber(number);
            for (Person person : personList) {
                Timestamp courseDate = videoMapper.selectVideoById(person.getVideoId()).getCourseDate();
                if (courseDate.before(startTimeStamp) || courseDate.after(endTimeStamp)) {
                    continue;
                }
                Visual visual = new Visual();
                visual.setTime(courseDate);
                visual.setInvestment(person.getInvestment());
                visual.setDivorce(person.getDivorce());
                visual.setListen(person.getListen());
                visual.setCommunication(person.getCommunication());
                visual.setPlayPhoneScore(person.getPlayPhoneScore());
                visual.setPlayLaptopScore(person.getPlayLaptopScore());
                visual.setReadBookScore(person.getReadBookScore());
                visual.setRaiseHandScore(person.getRaiseHandScore());
                visual.setBowScore(person.getBowScore());
                visual.setLeanScore(person.getLeanScore());
                visual.setPlayPhoneNum(person.getPlayPhoneNum());
                visual.setPlayLaptopNum(person.getPlayLaptopNum());
                visual.setReadBookNum(person.getReadBookNum());
                visual.setRaiseHandNum(person.getRaiseHandNum());
                visual.setBowNum(person.getBowNum());
                visual.setLeanNum(person.getLeanNum());
                visualList.add(visual);
            }
        }
        visualList.sort(Comparator.comparing(Visual::getTime));
        return visualList;
    }

    @RequestMapping("/getVisualData")
    public String getVisualData(@RequestParam("unit") String unit,
                                @RequestParam("number1") String number1,
                                @RequestParam("number2") String number2,
                                @RequestParam("startTime") String startTime,
                                @RequestParam("endTime") String endTime,
                                Model model) {
        if (startTime.length() < 17) startTime += ":00";
        Timestamp startTimeStamp = Timestamp.valueOf(startTime.replace("T", " "));
        if (endTime.length() < 17) endTime += ":00";
        Timestamp endTimeStamp = Timestamp.valueOf(endTime.replace("T", " "));
        model.addAttribute("unit", unit);
        model.addAttribute("number1", number1);
        model.addAttribute("number2", number2);
        model.addAttribute("startTime", startTime);
        model.addAttribute("endTime", endTime);
        model.addAttribute("visualList1", getVisualList(unit, number1, startTimeStamp, endTimeStamp));
        model.addAttribute("visualList2", getVisualList(unit, number2, startTimeStamp, endTimeStamp));
        List<Student> studentList = studentMapper.selectAllStudent();
        model.addAttribute("studentList", studentList);
        return "visual";
    }

}
