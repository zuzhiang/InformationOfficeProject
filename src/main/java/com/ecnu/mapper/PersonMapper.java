package com.ecnu.mapper;

import com.ecnu.pojo.Person;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;

import java.util.List;

// 这个注解表示该类是一个mybatis的mapper类
@Mapper
@Repository
public interface PersonMapper {

    // 根据视频id查询人物
    @Select("select * from person where video_id=#{id}")
//    @Results(id = "personMap", value = {
//            @Result(property = "personId",column = "person_id"),
//            // 一对多查询（一个人物对应多个图片）
//            @Result(property = "images", column = "person_id", many = @Many(select = "com.ecnu.mapper.ImageMapper.selectImageByPersonId",
//                    fetchType = FetchType.LAZY))})
    List<Person> selectPersonByVideoId(String id);

    // 根据姓名查询人物
    @Select("select * from person where name=#{name}")
    // @ResultMap("personMap")
    List<Person> selectPersonByName(String name);

    // 添加人物
    @Insert("insert into person(person_id, video_id, number, name, play_phone_num, play_laptop_num, raise_hand_num, bow_num, lean_num) values(#{personId}, #{videoId}, #{number}, #{name}, #{playPhoneNum}, #{playLaptopNum}, #{raiseHandNum}, #{bowNum}, #{leanNum})")
    void addPerson(Person person);

    // 删除某视频id下的所有人物
    @Delete("delete from person where video_id=#{id}")
    void deletePersonByVideoId(String id);

}
