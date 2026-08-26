package com.mywiki.backend;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtTest {

    // 密钥（和配置文件保持一致）
    private static final String SECRET = "mywiki-local-secret-key-change-this-in-production";

    @Test
    public void testGenerateAndParseToken() {
        // 1. 生成密钥（0.12.x 新写法）
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        // 2. 生成 Token（0.12.x 新写法）
        String token = Jwts.builder()
                .subject("admin")
                .claim("username", "admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L))
                .signWith(key)
                .compact();

        System.out.println("生成的 Token:");
        System.out.println(token);
        System.out.println("\n====================================\n");

        // 3. 解析 Token（0.12.x 新写法）
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        System.out.println("解析结果:");
        System.out.println("Subject: " + claims.getSubject());
        System.out.println("Username: " + claims.get("username"));
    }

    @Test
    public void testExpiredToken() throws InterruptedException {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        // 生成 1 秒后过期的 Token
        String token = Jwts.builder()
                .subject("admin")
                .claim("username", "admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000)) // 1秒过期
                .signWith(key)
                .compact();

        System.out.println("等待 1.5 秒让 Token 过期...");
        Thread.sleep(1500);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            System.out.println("✅ Token 有效: " + claims.getSubject());
        } catch (Exception e) {
            System.out.println("❌ Token 已过期或无效: " + e.getMessage());
        }
    }
}
