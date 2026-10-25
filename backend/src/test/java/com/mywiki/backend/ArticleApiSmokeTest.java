package com.mywiki.backend;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArticleApiSmokeTest extends BaseApiTest {

    @Test
    @Sql("file:sql/schema.sql")
    void 文章列表接口返回200() throws Exception {
        mockMvc.perform(get("/articles").param("page", "1").param("size", "10"))
                .andExpect(status().isOk());
    }
}
