package com.mywiki.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 契约：GET /articles/{id}/comments?page&size，默认 page=1 size=10，
// createdAt 倒序，响应为分页对象（records + total）
@Sql({"file:sql/schema.sql", "classpath:test-data.sql"})
class CommentPageApiTest extends BaseApiTest {

    // 前提：文章 3 共 12 条评论，'page test comment 12'（2026-01-20）最新
    @Test
    void 默认参数返回第一页10条() throws Exception {
        mockMvc.perform(get("/articles/3/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(10))
                .andExpect(jsonPath("$.data.total").value("12"));
    }
    
    // page=2 → records 剩余 2 条，内容 containsInAnyOrder("alice comment on bob", "page test comment 02")
    @Test
    void 第二页返回2条() throws Exception {
        mockMvc.perform(get("/articles/3/comments?page=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.total").value("12"))
                .andExpect(jsonPath("$.data.records[*].content",
                    containsInAnyOrder("alice comment on bob", "page test comment 02")));
    }

    // 倒序 → records[0].content 是 "page test comment 12"（最新在前）
    @Test
    void 最新在前() throws Exception {
        mockMvc.perform(get("/articles/3/comments?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(10))
                .andExpect(jsonPath("$.data.total").value("12"))
                .andExpect(jsonPath("$.data.records[0].content",
                    is("page test comment 12")));
    }

    // size=5 → records 5 条 + total 12（size 参数生效）
    @Test
    void 第一页返回5条() throws Exception {
        mockMvc.perform(get("/articles/3/comments?size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(5))
                .andExpect(jsonPath("$.data.total").value("12"));
    }
}