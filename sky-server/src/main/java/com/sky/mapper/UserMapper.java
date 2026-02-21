package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {


    @Select("select * from sky_take_out.user where openid = #{openid}")
    User getUserByOpenid(String openid);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into sky_take_out.user (openid, name, phone, sex, id_number, avatar, create_time) VALUES (#{openid}, #{name}, #{phone}, #{sex}, #{idNumber}, #{avatar}, #{createTime})")
    void insert(User newUser);

    @Select("select * from user where id = #{userId}")
    User getUserById(Long userId);

}
