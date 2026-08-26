package com.mywiki.backend.interceptor;

import com.mywiki.backend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {

        String method = request.getMethod();

        // 查看文章不需要登录
        if ("GET".equalsIgnoreCase(method)) {
            return true;
        }

        String authorization =
                request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return false;
        }

        String token =
                authorization.substring(7);

        try {

            Claims claims =
                    jwtUtil.parseToken(token);

            request.setAttribute(
                    "userId",
                    Long.valueOf(claims.getSubject())
            );

            return true;

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return false;
        }
    }
}
