package com.ecnu.mapper;

import com.ecnu.pojo.Image;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

// 这个注解表示该类是一个mybatis的mapper类
@Mapper
@Repository
public interface ImageMapper {

    // 查找所有图片信息
    @Select("select * from image")
    List<Image> selectAllImage();

    // 根据图片id查询图片
    @Select("select * from image where image_id=#{imageId}")
    Image selectImageByImageId(String imageId);

    // 根据视频id查询图片
    @Select("select * from image where video_id=#{videoId} order by time asc")
    List<Image> selectImageByVideoId(String videoId);

    // 根据人物id查询图片
    @Select("select * from image where person_id=#{personId} order by time asc")
    List<Image> selectImageByPersonId(String personId);

    // 添加图片
    @Insert("insert into image(image_id, video_id, person_id, obj_path, pose_path, hand_exist, phone_exist, laptop_exist, book_exist, play_phone_exist, play_laptop_exist, read_book_exist, raise_hand_exist, bow_exist, lean_exist, time) values(#{imageId}, #{videoId}, #{personId}, #{objPath}, #{posePath}, #{handExist}, #{phoneExist}, #{laptopExist}, #{bookExist}, #{playPhoneExist}, #{playLaptopExist}, #{readBookExist}, #{raiseHandExist}, #{bowExist}, #{leanExist}, #{time})")
    void addImage(Image image);

    @Update("update image set image_id=#{imageId}, video_id=#{videoId}, person_id=#{personId}, obj_path=#{objPath}, pose_path=#{posePath}, hand_exist=#{handExist}, phone_exist=#{phoneExist}, laptop_exist=#{laptopExist}, book_exist=#{bookExist}, play_phone_exist=#{playPhoneExist}, play_laptop_exist=#{playLaptopExist}, read_book_exist=#{readBookExist}, raise_hand_exist=#{raiseHandExist}, bow_exist=#{bowExist}, lean_exist=#{leanExist}, time=#{time} where image_id=#{imageId}")
    void updateImage(Image image);

    // 根据视频id删除图片
    @Delete("delete from image where video_id=#{videoId}")
    void deleteImageByVideoId(String videoId);

    // 根据人物id删除图片
    @Delete("delete from image where person_id=#{personId}")
    void deleteImageByPersonId(String personId);

    // 根据图片id删除图片
    @Delete("delete from image where image_id=#{imageId}")
    void deleteImageByImageId(String imageId);

}
