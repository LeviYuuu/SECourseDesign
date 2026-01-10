package com.se.backend.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.se.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {}