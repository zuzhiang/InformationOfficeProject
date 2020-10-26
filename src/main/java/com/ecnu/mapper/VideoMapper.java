package com.ecnu.mapper;

import com.ecnu.pojo.Video;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

// 这个注解表示该类是一个mybatis的mapper类
@Mapper
@Repository
public interface VideoMapper {

    // 查询所有视频
    @Select("select * from video order by upload_date desc")
    // property是要查询的信息，即实体类Video中的persons变量，column是查询所依据的数据，即视频id
//    @Results(id = "videoMap", value = {
//            @Result(property = "videoId", column = "video_id"),
//            // 一对多查询（一个视频对应多个人物）
//            @Result(property = "persons", column = "video_id", many = @Many(select = "com.ecnu.mapper.PersonMapper.selectPersonByVideoId",
//                    fetchType = FetchType.LAZY))})
    List<Video> selectAllVideo();

    // 根据视频id查询视频
    @Select("select * from video where video_id=#{id} order by upload_date desc")
    // @ResultMap("videoMap") // 即selectAllVideo方法上的Results注解中的id名，避免了重复定义
    Video selectVideoById(String id);

    // 根据教师查询视频,模糊查询时占位符必须是value
    @Select("select * from video where teacher like '%${value}%' order by upload_date desc")
    List<Video> selectVideoByTeacher(String teacher);

    // 根据上课地点查询视频，模糊查询时占位符必须是value
    @Select("select * from video where place like '%${value}%' order by upload_date desc")
    List<Video> selectVideoByPlace(String place);

    // 添加视频
    @Insert("insert into video(video_id, path, teacher, place, course_date, upload_date, state, owner) values(#{videoId}, #{path}, #{teacher}, #{place}, #{courseDate}, #{uploadDate}, #{state}, #{owner})")
    void addVideo(Video video);

    // 更新视频信息
    @Update("update video set video_id=#{videoId}, path=#{path}, teacher=#{teacher}, place=#{place}, course_date=#{courseDate}, upload_date=#{uploadDate}, state=#{state}, owner=#{owner} where video_id=#{videoId}")
    void updateVideo(Video video);

    // 删除视频
    @Delete("delete from video where video_id=#{id}")
    void deleteVideo(String id);

}
