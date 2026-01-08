package com.se.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.se.backend.entity.TrainingSession;

@Mapper
public interface SessionMapper extends BaseMapper<TrainingSession> {}
