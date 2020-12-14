package com.ecnu.mapper;

import com.ecnu.pojo.Person;
import com.ecnu.pojo.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

// 这个注解表示该类是一个mybatis的mapper类
@Mapper
@Repository
public interface StudentMapper {
    // 根据班级代号查询所有学生
    @Select("select * from student where unit=#{unit} order by number asc")
    List<Student> selectStudentByUnit(String unit);

    // 根据学号查询学生
    @Select("select * from student where number=#{number}")
    Student selectStudentByNumber(String number);
}
