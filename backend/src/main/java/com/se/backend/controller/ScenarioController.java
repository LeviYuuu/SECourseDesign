package com.se.backend.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.se.backend.common.Result;
import com.se.backend.dto.ScenarioListItem;
import com.se.backend.entity.ScenarioTemplate;
import com.se.backend.mapper.ScenarioTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/scenario")
public class ScenarioController {

    @Autowired
    private ScenarioTemplateMapper templateMapper;

    @GetMapping("/list")
    public Result<List<ScenarioListItem>> list(@RequestParam(required = false) String category) {
        // 1. 构建查询条件：根据 category 筛选，且必须是启用的(is_active = 1)
        LambdaQueryWrapper<ScenarioTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScenarioTemplate::getIsActive, true);
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(ScenarioTemplate::getSceneType, category);
        }
        
        // 按创建时间倒序或版本排序
        wrapper.orderByDesc(ScenarioTemplate::getVersion);

        List<ScenarioTemplate> templates = templateMapper.selectList(wrapper);

        // 2. 转换为 DTO，处理 description 字段
        List<ScenarioListItem> items = templates.stream().map(t -> {
            ScenarioListItem item = new ScenarioListItem();
            item.setTemplateId(t.getTemplateId());
            item.setTitle(t.getTitle());
            item.setCategory(t.getSceneType());
            item.setDifficulty(t.getDifficulty());
            item.setRolePersona(t.getRolePersona());

            // 【核心对齐逻辑】尝试从 config_json 提取 description
            // 如果 JSON 中没有 description，则降级使用 role_persona
            String description = t.getRolePersona(); // 默认值
            try {
                if (t.getConfigJson() != null && JSONUtil.isTypeJSON(t.getConfigJson())) {
                    JSONObject json = JSONUtil.parseObj(t.getConfigJson());
                    if (json.containsKey("description")) {
                        description = json.getStr("description");
                    }
                }
            } catch (Exception e) {
                // log error
            }
            item.setDescription(description);

            return item;
        }).collect(Collectors.toList());

        return Result.success(items);
    }
}
