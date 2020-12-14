package com.ecnu.mapper;

import com.ecnu.pojo.Person;
import com.ecnu.pojo.Video;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;

import java.util.List;

// 这个注解表示该类是一个mybatis的mapper类
@Mapper
@Repository
public interface PersonMapper {

    // 根据视频id查询人物
    @Select("select * from person where video_id=#{videoId} order by number asc")
//    @Results(id = "personMap", value = {
//            @Result(property = "personId",column = "person_id"),
//            // 一对多查询（一个人物对应多个图片）
//            @Result(property = "images", column = "person_id", many = @Many(select = "com.ecnu.mapper.ImageMapper.selectImageByPersonId",
//                    fetchType = FetchType.LAZY))})
    List<Person> selectPersonByVideoId(String videoId);

    // 根据人物id查询人物
    @Select("select * from person where person_id=#{personId}")
    // @ResultMap("personMap")
    Person selectPersonByPersonId(String personId);

    // 根据视频id和学号统计该视频下该学号出现的次数
    @Select("select * from person where video_id=#{videoId} and number=#{number}")
    List<Person> selectPersonByNumber(String videoId, String number);

    // 添加人物
    @Insert("insert into person(person_id, video_id, number, name, play_phone_num, play_laptop_num, read_book_num, raise_hand_num, bow_num, lean_num) values(#{personId}, #{videoId}, #{number}, #{name}, #{playPhoneNum}, #{playLaptopNum}, #{readBookNum}, #{raiseHandNum}, #{bowNum}, #{leanNum})")
    void addPerson(Person person);

    // 更新人物信息
    @Update("update person set person_id=#{personId}, video_id=#{videoId}, number=#{number}, name=#{name}, play_phone_num=#{playPhoneNum}, play_laptop_num=#{playLaptopNum}, read_book_num=#{readBookNum}, raise_hand_num=#{raiseHandNum}, bow_num=#{bowNum}, lean_num=#{leanNum} where person_id=#{personId}")
    void updatePerson(Person person);

    // 删除某视频id下的所有人物
    @Delete("delete from person where video_id=#{videoId}")
    void deletePersonByVideoId(String videoId);

    // 根据人物id进行删除
    @Delete("delete from person where person_id=#{personId}")
    void deletePersonByPersonId(String personId);

}
