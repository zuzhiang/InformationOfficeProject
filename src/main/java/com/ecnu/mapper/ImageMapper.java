package com.ecnu.mapper;

import com.ecnu.pojo.Image;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

// 这个注解表示该类是一个mybatis的mapper类
@Mapper
@Repository
public interface ImageMapper {

    // 根据视频id查询图片
    @Select("select * from image where video_id=#{id} order by time asc")
    List<Image> selectImageByVideoId(String id);

    // 根据人物id查询图片
    @Select("select * from image where person_id=#{id} order by time asc")
    List<Image> selectImageByPersonId(String id);

    // 添加图片
    @Insert("insert into image(image_id, video_id, person_id, obj_path, pose_path, hand_exist, phone_exist, laptop_exist, play_phone_exist, play_laptop_exist, raise_hand_exist, bow_exist, lean_exist, time) values(#{imageId}, #{videoId}, #{personId}, #{objPath}, #{posePath}, #{handExist}, #{phoneExist}, #{laptopExist}, #{playPhoneExist}, #{playLaptopExist}, #{raiseHandExist}, #{bowExist}, #{leanExist}, #{time})")
    void addImage(Image image);

    // 根据视频id删除图片
    @Delete("delete from image where video_id=#{id}")
    void deleteImageByVideoId(String id);

}
