package com.se.backend.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.se.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.Map;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            
            // 1. 从 URL 参数获取 Token
            String query = servletRequest.getQueryString();
            // 简单的参数解析，生产环境建议用更健壮的工具
            String token = null;
            if (StrUtil.isNotEmpty(query)) {
                Map<String, String> paramMap = HttpUtil.decodeParamMap(query, Charset.defaultCharset());
                token = paramMap.get("token");
            }

            // 2. 校验 Token
            if (StrUtil.isNotEmpty(token) && jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                // 3. 将 userId 放入 WebSocket Session 的属性中
                attributes.put("userId", userId);
                return true;
            }
        }
        return false; // 拒绝连接
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后处理，留空即可
    }
}
