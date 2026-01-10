package com.se.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.se.backend.dto.UserProfileResponse;
import com.se.backend.entity.TrainingSession;
import com.se.backend.entity.User;
import com.se.backend.mapper.TrainingSessionMapper;
import com.se.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired private UserMapper userMapper;
    @Autowired private TrainingSessionMapper sessionMapper;

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("User not found");

        // 统计训练次数
        Long sessionCount = sessionMapper.selectCount(new LambdaQueryWrapper<TrainingSession>()
                .eq(TrainingSession::getUserId, userId));

        UserProfileResponse resp = new UserProfileResponse();
        resp.setUserId(user.getUserId());
        resp.setNickname(user.getNickname());
        resp.setEmail(user.getAccountNo()); // 假设 accountNo 是邮箱
        resp.setTotalSessions(sessionCount.intValue());
        resp.setTotalDurationMinutes(sessionCount.intValue() * 15); // Mock: 每次15分钟

        return resp;
    }
}
