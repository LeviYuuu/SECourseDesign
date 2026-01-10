package com.se.backend.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // 生产环境请移至配置文件
    private static final byte[] SECRET_KEY = "SeBackendSecretKeyForTrainingSystemDoNotLeak".getBytes(StandardCharsets.UTF_8);
    private static final JWTSigner SIGNER = JWTSignerUtil.hs256(SECRET_KEY);

    public String createToken(Long userId, String accountNo) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("sub", accountNo); // subject
        payload.put("exp", System.currentTimeMillis() + (1000 * 60 * 60 * 24)); // 24小时过期
        
        return JWTUtil.createToken(payload, SIGNER);
    }

    public boolean validateToken(String token) {
        try {
            return JWTUtil.verify(token, SIGNER);
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            Object userId = jwt.getPayload("userId");
            return Long.valueOf(userId.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
