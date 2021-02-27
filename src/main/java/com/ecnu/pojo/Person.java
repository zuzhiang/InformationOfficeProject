package com.ecnu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Data包含了ToString/Getter/Setter等功能
@Data
// 有参构造函数
@AllArgsConstructor
// 无参构造函数
@NoArgsConstructor
// 人物的实体类
public class Person {

    private String personId; // 人物id
    private String videoId; // 视频id
    private String number; // 学号
    private String name; // 姓名
    private double investment; // 课堂投入度
    private double divorce; // 课堂脱离度
    private double listen; // 听课
    private double communication; //交流
    private double playPhoneScore; // 玩手机得分
    private double playLaptopScore; // 玩笔记本电脑得分
    private double readBookScore; // 看书的得分
    private double raiseHandScore; // 举手得分
    private double bowScore; // 低头得分
    private double leanScore; // 侧身得分
    private Integer playPhoneNum; // 玩手机次数
    private Integer playLaptopNum; // 玩笔记本电脑次数
    private Integer readBookNum; // 看书的次数
    private Integer raiseHandNum; // 举手次数
    private Integer bowNum; // 低头次数
    private Integer leanNum; // 侧身次数

    // private List<Image> images; // 人物对应的图片

}
