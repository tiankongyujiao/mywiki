package com.mywiki.backend.config;

import com.mywiki.backend.interceptor.JwtInterceptor;
import com.mywiki.backend.resolver.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;



@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final JwtInterceptor jwtInterceptor;
    private final CurrentUserArgumentResolver resolver;

    public WebMvcConfig(JwtInterceptor jwtInterceptor, CurrentUserArgumentResolver resolver) {
        this.jwtInterceptor = jwtInterceptor;
        this.resolver = resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns(
                        "/articles",
                        "/articles/**",
                        "/users/me"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }
}