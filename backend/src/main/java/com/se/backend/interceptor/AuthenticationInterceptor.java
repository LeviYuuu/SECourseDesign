package com.se.backend.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.se.backend.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 【核心修复】放行所有 OPTIONS 请求
        // 浏览器的 CORS 预检请求(Preflight)方法为 OPTIONS，不带 Token，必须直接通过
        if (RequestMethod.OPTIONS.name().equals(request.getMethod())) {
            return true;
        }

        // 获取 Token
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }

        // 校验 Token (简单判空，实际项目中应解密 JWT)
        if (StrUtil.isBlank(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.error("未登录或 Token 失效")));
            return false;
        }

        // 简单的 Token 前缀处理 (Bearer )
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 这里可以添加 JWT 校验逻辑，解析 userId 放入 request attribute
        // request.setAttribute("userId", userId);

        return true;
    }
}
