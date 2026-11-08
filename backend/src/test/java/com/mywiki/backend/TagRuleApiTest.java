package com.mywiki.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 契约：标签增改删仅 ADMIN(403)；重名 400(10013)；被引用（含草稿的关联）不可删 400(10016)；
// 名字 trim 后为空 → 400(10002)；改成自己的名字允许
@Sql({"file:sql/schema.sql", "classpath:test-data.sql"})
class TagRuleApiTest extends BaseApiTest {

    @Test
    void 普通用户不能创建标签() throws Exception {
        String token = login("alice");
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"new tag\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(10014));
    }

    @Test
    void 未登录创建标签返回401() throws Exception {
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"new tag\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 管理员可以创建标签() throws Exception {
        String token = login("admin_test");
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"admin tag\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void 标签重名被拒绝() throws Exception {
        String token = login("admin_test");
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"rule-test-tag\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10013));
    }

    @Test
    void 标签名空白被拒绝() throws Exception {
        String token = login("admin_test");
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10002));
    }

    @Test
    void 被引用的标签不可删除() throws Exception {
        String token = login("admin_test");
        // rule-test-tag 被文章 9（草稿）引用
        mockMvc.perform(delete("/tags/9")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10016));
    }
}