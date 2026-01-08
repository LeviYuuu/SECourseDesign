package com.se.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.se.backend.entity.TrainingSession;
import com.se.backend.mapper.SessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ProfileService {
    @Autowired private SessionMapper sessionMapper;

    // 获取历史记录
    public Map<String, Object> getHistory(Integer userId) {
        QueryWrapper<TrainingSession> qw = new QueryWrapper<>();
        // ✅ 修正：字段名对齐 Entity 中的 userId (MyBatisPlus会自动映射为 user_id)
        qw.eq("user_id", userId).orderByDesc("started_at");
        
        List<TrainingSession> list = sessionMapper.selectList(qw);
        return Map.of("total", list.size(), "records", list);
    }
    
    // 获取趋势 (Mock数据，暂不涉及复杂SQL聚合)
    public Map<String, Object> getTrend(Integer userId) {
        // 这里暂时返回假数据，确保前端图表能画出来
        return Map.of(
            "labels", Arrays.asList("12-01", "12-05", "12-10", "12-15", "12-20"),
            "scores", Arrays.asList(60, 75, 72, 85, 88)
        );
    }
}
