package com.mywiki.backend.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywiki.backend.common.Result;
import com.mywiki.backend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws IOException {

        String method = request.getMethod();

        // 查看文章/分类不需要登录，但若带了 token 则解析出身份，供"是否作者"等判断使用
        if ("GET".equalsIgnoreCase(method)
                && (request.getRequestURI().startsWith("/articles")
                    || request.getRequestURI().startsWith("/categories"))) {

            String getAuth = request.getHeader("Authorization");
            if (getAuth != null && getAuth.startsWith("Bearer ")) {
                try {
                    Claims getClaims = jwtUtil.parseToken(getAuth.substring(7));
                    request.setAttribute(
                            "userId",
                            Long.valueOf(getClaims.getSubject())
                    );
                } catch (Exception ignored) {
                    // 无效/过期 token 按游客处理，照样放行
                }
            }
            return true;
        }

        String authorization =
                request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            writeUnauthorized(response, "未登录");

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

            writeUnauthorized(response, "登录已过期，请重新登录");

            return false;
        }
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {
        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        Result.error(10006, message)
                )
        );
    }
}
