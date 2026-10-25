package com.mywiki.backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[*].title",
                        containsInAnyOrder("alice post 1", "alice post 2")));
    }

    // 走真实登录接口拿 token，而不是自己伪造 JWT——顺带把登录链路也测了
    private String login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"Test123456\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }
}
