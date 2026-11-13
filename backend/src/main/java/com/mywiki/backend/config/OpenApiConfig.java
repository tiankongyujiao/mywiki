package com.mywiki.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    // 声明 Bearer 认证方式：Swagger UI 会出现 Authorize 按钮，
    // 填入 token 后所有"Try it out"请求都会自动带 Authorization 头
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("MyWiki API").version("1.0"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
