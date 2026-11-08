package com.mywiki.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 契约：文章编辑/删除仅作者本人（403 + 10005）；不存在 → 404 + 10001
@Sql({"file:sql/schema.sql", "classpath:test-data.sql"})
class ArticlePermissionApiTest extends BaseApiTest {

    @Test
    void 非作者删除别人的文章返回403() throws Exception {
        // 前提：文章 1 属于 alice(1)；操作者 bob(2) 非作者
        String token = login("bob");
        mockMvc.perform(delete("/articles/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(10005));
    }

    @Test
    void 非作者编辑别人的文章返回403() throws Exception {
        // 前提：文章 1 属于 alice(1)；操作者 bob(2) 非作者
        String token = login("bob");
        mockMvc.perform(put("/articles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"alice post 11\",\"content\":\"alice post 11\",\"categoryId\":null,\"status\":\"PUBLISHED\",\"tags\":[]}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(10005));
    }

    @Test
    void 作者编辑自己的文章() throws Exception {
        String token = login("alice");
        mockMvc.perform(put("/articles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"alice post 11\",\"content\":\"alice post 11\",\"categoryId\":null,\"status\":\"PUBLISHED\",\"tags\":[]}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void 作者删除自己的文章() throws Exception {
        String token = login("alice");
        mockMvc.perform(delete("/articles/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test 
    void 编辑不存在的文章404() throws Exception {
        String token = login("alice");
        mockMvc.perform(put("/articles/22")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"alice post 11\",\"content\":\"alice post 11\",\"categoryId\":null,\"status\":\"PUBLISHED\",\"tags\":[]}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(10001));
    }

    @Test 
    void 删除不存在的文章返回404() throws Exception {
        String token = login("alice");
        mockMvc.perform(delete("/articles/22")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(10001));
    }
}