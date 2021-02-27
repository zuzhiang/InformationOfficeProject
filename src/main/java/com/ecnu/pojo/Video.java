package com.ecnu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

// Data包含了ToString/Getter/Setter等功能
@Data
// 有参构造函数
@AllArgsConstructor
// 无参构造函数
@NoArgsConstructor
// 视频的实体类
public class Video {

    private String videoId; // 视频id
    //private String path; // 存储路
    private String teacher; // 任课教师
    private String place; // 上课地点
    private Timestamp courseDate; // 上课日期
    private Timestamp uploadDate; // 上传日期
    private Boolean state; // 视频处理状态，0表示未处理，1表示已处理
    private String owner; // 视频的所有者（上传用户）
    //private Integer time; // 时间，用于排序，其值和uploadDate相同

    // private List<Person> persons; // 视频中包含的人

}
