package com.se.backend.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.se.backend.dto.ScenarioListItem;
import com.se.backend.entity.ScenarioTemplate;
import com.se.backend.mapper.ScenarioTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScenarioService {

    @Autowired
    private ScenarioTemplateMapper templateMapper;

    public List<ScenarioListItem> getScenarios(String category) {
        // 1. 构建查询
        LambdaQueryWrapper<ScenarioTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScenarioTemplate::getIsActive, true); // 只查已启用
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(ScenarioTemplate::getSceneType, category);
        }
        
        wrapper.orderByDesc(ScenarioTemplate::getVersion);

        // 2. 查询数据库
        List<ScenarioTemplate> list = templateMapper.selectList(wrapper);

        // 3. 转换为 DTO
        return list.stream().map(t -> {
            ScenarioListItem item = new ScenarioListItem();
            item.setTemplateId(t.getTemplateId());
            item.setTitle(t.getTitle());
            item.setCategory(t.getSceneType());
            item.setDifficulty(t.getDifficulty());
            item.setRolePersona(t.getRolePersona());

            // 解析 JSON 提取描述
            String desc = t.getRolePersona(); // 默认值
            try {
                if (t.getConfigJson() != null) {
                    JSONObject json = JSONUtil.parseObj(t.getConfigJson());
                    if (json.containsKey("description")) {
                        desc = json.getStr("description");
                    }
                }
            } catch (Exception e) {
                // ignore error
            }
            item.setDescription(desc);
            
            return item;
        }).collect(Collectors.toList());
    }
}
