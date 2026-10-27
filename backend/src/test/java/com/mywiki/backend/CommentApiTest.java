package com.mywiki.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql({"file:sql/schema.sql", "classpath:test-data.sql"})
class CommentApiTest extends BaseApiTest {

    @Test
    void 评论列表公开可读() throws Exception {
        mockMvc.perform(get("/articles/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].content").value("bob comment on alice"))
                .andExpect(jsonPath("$.data[0].username").value("bob"));
    }

    @Test
    void 未登录不能发表评论() throws Exception {
        mockMvc.perform(post("/articles/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hi\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(10006));
    }

    @Test
    void 登录后发表评论_列表可见且最新在前() throws Exception {
        String token = login("alice");

        mockMvc.perform(post("/articles/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"alice new comment\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/articles/1/comments"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].content").value("alice new comment"))
                .andExpect(jsonPath("$.data[0].username").value("alice"))
                .andExpect(jsonPath("$.data[1].content").value("bob comment on alice"));
    }

    @Test
    void 文章不存在时不能发表评论() throws Exception {
        String token = login("alice");
        mockMvc.perform(post("/articles/999/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hi\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(10001));
    }

    @Test
    void 空内容发表被拒绝() throws Exception {
        String token = login("alice");
        // 契约：入参非法属于客户端错误，应 400
        mockMvc.perform(post(" ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10002));
    }

    @Test
    void 评论作者可以删自己的评论() throws Exception {
        String token = login("bob");
        mockMvc.perform(delete("/articles/1/comments/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/articles/1/comments"))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void 文章作者可以删别人在自己文章下的评论() throws Exception {
        String token = login("alice");
        mockMvc.perform(delete("/articles/1/comments/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void 无关第三人不能删评论() throws Exception {
        String token = login("charlie");
        mockMvc.perform(delete("/articles/1/comments/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(10008));
    }

    @Test
    void 评论与URL文章不匹配时返回404() throws Exception {
        String token = login("alice");
        // 评论 1 属于文章 1，却从文章 3 的路径删 → 应拒绝
        mockMvc.perform(delete("/articles/3/comments/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(10007));
    }
}
