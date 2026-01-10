package com.se.backend.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.se.backend.entity.SessionMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SessionMessageMapper extends BaseMapper<SessionMessage> {}