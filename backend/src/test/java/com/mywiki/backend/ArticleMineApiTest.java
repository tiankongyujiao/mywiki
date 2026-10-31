package com.mywiki.backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql({"file:sql/schema.sql", "classpath:test-data.sql"})
class ArticleMineApiTest extends BaseApiTest {

    @Test
    void 未登录请求我的文章返回401() throws Exception {
        mockMvc.perform(get("/articles").param("mine", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    @Test
    void 登录后我的文章只返回自己的() throws Exception {
        String token = login("alice");

        mockMvc.perform(get("/articles")
                        .param("mine", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(3))
                .andExpect(jsonPath("$.data.records[*].title",
                    containsInAnyOrder("alice post 1", "alice post 2", "alice draft post")));
    }
}
