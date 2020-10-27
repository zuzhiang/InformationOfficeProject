package com.ecnu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Data包含了ToString/Getter/Setter等功能
@Data
// 有参构造函数
@AllArgsConstructor
// 无参构造函数
@NoArgsConstructor
// 图片的实体类
public class Image {

    private String imageId; // 图片id
    private String videoId; // 视频id
    private String personId; // 人物id
    private String objPath; // 物体视频图片路径
    private String posePath; // 姿态检测图片路径
    private boolean handExist; // 是否检测到手
    private boolean phoneExist; // 是否检测到手机
    private boolean laptopExist; // 是否检测到笔记本电脑
    private boolean playPhoneExist; // 是否玩手机
    private boolean playLaptopExist; // 是否玩笔记本电脑
    private boolean raiseHandExist; // 是否举手
    private boolean bowExist; // 是否低头
    private boolean leanExist; // 是否侧身
    private String time; // 图片的时间戳，用于排序

}
