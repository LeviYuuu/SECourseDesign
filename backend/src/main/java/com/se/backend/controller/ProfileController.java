package com.se.backend.controller;

import com.se.backend.common.Result;
import com.se.backend.dto.UserProfileResponse;
import com.se.backend.service.ProfileService;
import com.se.backend.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/user")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/profile")
    public Result<UserProfileResponse> getProfile() {
        Long userId = UserContext.getUserId();
        UserProfileResponse resp = profileService.getUserProfile(userId);
        return Result.success(resp);
    }

    /**
     * 成长趋势数据
     */
    @GetMapping("/growthTrend")
    public Result<Map<String, Object>> getGrowthTrend(@RequestParam Long userId, @RequestParam Integer days) {
        try {
            // 模拟数据：返回近N次的能力分数
            Map<String, Object> data = new HashMap<>();

            List<String> labels = new ArrayList<>();
            List<Integer> scores = new ArrayList<>();

            // 模拟7天数据
            for (int i = days; i > 0; i--) {
                labels.add(String.format("Day-%d", i));
                scores.add(60 + (int)(Math.random() * 35)); // 随机分数 60-95
            }

            data.put("labels", labels);
            data.put("scores", scores);

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取成长趋势失败: " + e.getMessage());
        }
    }

    /**
     * 训练历史列表
     */
    @GetMapping("/history")
    public Result<Map<String, Object>> getHistoryList(@RequestParam Long userId,
                                                      @RequestParam Integer page,
                                                      @RequestParam Integer size) {
        try {
            // 模拟数据
            List<Map<String, Object>> records = new ArrayList<>();

            String[] scenarios = {"面试模拟-技术岗位", "演讲模拟-产品发布会",
                    "社交模拟-商务洽谈", "面试模拟-管理岗位"};

            for (int i = 1; i <= 5; i++) {
                Map<String, Object> record = new HashMap<>();
                record.put("sessionId", 1000 + i);
                record.put("scenario", scenarios[i % scenarios.length]);
                record.put("score", 70 + (int)(Math.random() * 25));
                record.put("completedAt", String.format("2024-01-%02d 14:30:00", i));
                records.add(record);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("records", records);
            data.put("total", 15);
            data.put("page", page);
            data.put("size", size);

            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取历史记录失败: " + e.getMessage());
        }
    }
}