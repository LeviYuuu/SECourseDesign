package com.se.backend.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * LLM 返回内容解析工具：
 * - 去除 ```json 代码块包裹
 * - 尝试从 JSON 中提取 "content" 字段作为真正展示文本
 */
public class AiResponseUtil {

    private AiResponseUtil() {}

    /**
     * 提取可展示的回复文本：
     * 1) 若 raw 是 JSON（或代码块包裹的 JSON），且包含 content 字段，则返回 content；
     * 2) 否则返回清洗后的 raw。
     */
    public static String extractDisplayText(String raw) {
        if (raw == null) return "";

        String cleaned = stripCodeFence(raw).trim();
        if (StrUtil.isBlank(cleaned)) return "";

        JSONObject obj = tryParseJsonObject(cleaned);
        if (obj != null) {
            // 优先取 "content" 作为真正回复
            if (obj.containsKey("content")) {
                String content = obj.getStr("content");
                if (StrUtil.isNotBlank(content)) return content.trim();
            }
            // 兼容其他可能字段
            if (obj.containsKey("reply")) {
                String reply = obj.getStr("reply");
                if (StrUtil.isNotBlank(reply)) return reply.trim();
            }
            if (obj.containsKey("answer")) {
                String answer = obj.getStr("answer");
                if (StrUtil.isNotBlank(answer)) return answer.trim();
            }
        }

        return cleaned;
    }

    /**
     * 去除 Markdown 代码块包裹（``` 或 ```json）。
     */
    public static String stripCodeFence(String s) {
        if (s == null) return "";
        String text = s.trim();

        // 以 ``` 开头并且包含结尾 ```
        if (text.startsWith("```")) {
            // 去掉首行 ``` 或 ```json
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0) {
                text = text.substring(firstNewline + 1);
            } else {
                text = text.replaceFirst("^```[a-zA-Z]*", "");
            }
            // 去掉末尾 ```
            text = text.replaceFirst("\\n```\\s*$", "");
            text = text.replaceFirst("```\\s*$", "");
        }

        return text.trim();
    }

    /**
     * 尝试解析 JSON 对象：
     * - 直接 parseObj
     * - 若包含前后噪声，尝试截取第一个 { 到最后一个 } 的子串再解析
     */
    public static JSONObject tryParseJsonObject(String s) {
        if (StrUtil.isBlank(s)) return null;

        try {
            return JSONUtil.parseObj(s);
        } catch (Exception ignore) {}

        int first = s.indexOf('{');
        int last = s.lastIndexOf('}');
        if (first >= 0 && last > first) {
            String sub = s.substring(first, last + 1);
            try {
                return JSONUtil.parseObj(sub);
            } catch (Exception ignore) {}
        }

        return null;
    }
}
