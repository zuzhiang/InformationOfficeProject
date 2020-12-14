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
// 学生的实体类
public class Student {
    private String studentId; // 学生编号
    private String number; // 学号
    private String name; // 姓名
    private String unit; // 班级代号
    private String teacher; // 教师名
}
