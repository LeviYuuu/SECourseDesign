package com.se.backend.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.se.backend.entity.UserCredentials;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCredentialsMapper extends BaseMapper<UserCredentials> {}