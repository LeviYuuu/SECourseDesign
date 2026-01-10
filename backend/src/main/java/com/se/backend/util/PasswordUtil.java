package com.se.backend.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    // 生成随机盐值 (64位)
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32]; // 32 bytes = 256 bits
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // SHA-256 哈希
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // 验证密码
    public static boolean verify(String inputPassword, String salt, String storedHash) {
        String calculatedHash = hashPassword(inputPassword, salt);
        return calculatedHash.equals(storedHash);
    }
}
