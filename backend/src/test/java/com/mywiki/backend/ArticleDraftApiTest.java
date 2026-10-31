package com.mywiki.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql({"file:sql/schema.sql", "classpath:test-data.sql"})
class ArticleDraftApiTest extends BaseApiTest {

    @Test
    void 主列表不显示草稿() throws Exception {
        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                // 已发布：alice 2 + bob 1 = 3；两篇草稿都不该出现
                .andExpect(jsonPath("$.data.records.length()").value(3));
    }

    @Test
    void 我的文章包含自己的草稿() throws Exception {
        String token = login("alice");
        mockMvc.perform(get("/articles")
                        .param("mine", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(3))
                .andExpect(jsonPath("$.data.records[*].title",
                        containsInAnyOrder("alice post 1", "alice post 2", "alice draft post")));
    }

    @Test
    void 游客访问草稿详情返回404() throws Exception {
        mockMvc.perform(get("/articles/4"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(10001));
    }

    @Test
    void 非作者访问草稿详情返回404() throws Exception {
        String token = login("bob");
        mockMvc.perform(get("/articles/4")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(10001));
    }

    @Test
    void 作者访问自己的草稿详情() throws Exception {
        String token = login("alice");
        mockMvc.perform(get("/articles/4")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("alice draft post"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void 向草稿发评论返回404() throws Exception {
        String token = login("bob");
        mockMvc.perform(post("/articles/4/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hi\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(10001));
    }

    @Test
    void 存草稿后仅作者可见() throws Exception {
        String token = login("bob");

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"bob new draft\",\"content\":\"c\",\"categoryId\":null,\"tags\":[],\"status\":\"DRAFT\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 主列表仍是 3 篇
        mockMvc.perform(get("/articles"))
                .andExpect(jsonPath("$.data.records.length()").value(3));

        // bob 自己的 mine 里能看到它（1 发布 + 2 草稿）
        mockMvc.perform(get("/articles")
                        .param("mine", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.records.length()").value(3))
                .andExpect(jsonPath("$.data.records[*].title",
                        containsInAnyOrder("bob post 1", "bob draft post", "bob new draft")));
    }

    @Test
    void 编辑已发布文章带草稿状态被忽略() throws Exception {
        String token = login("alice");
        mockMvc.perform(put("/articles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"edited title\",\"content\":\"c\",\"categoryId\":null,\"tags\":[],\"status\":\"DRAFT\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 状态保持 PUBLISHED → 游客仍可见
        mockMvc.perform(get("/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    void 编辑草稿时发布_主列表出现() throws Exception {
        String token = login("alice");
        mockMvc.perform(put("/articles/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"alice draft post\",\"content\":\"draft content\",\"categoryId\":null,\"tags\":[],\"status\":\"PUBLISHED\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/articles"))
                .andExpect(jsonPath("$.data.records.length()").value(4))
                .andExpect(jsonPath("$.data.records[*].title", hasItem("alice draft post")));
    }

    @Test
    void 标签计数不包含草稿() throws Exception {
        // draftcount 同挂已发布(1)和草稿(4)，只应计 1
        // 注：articleCount 是 Long，Jackson 按 String 序列化，断言用字符串
        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].articleCount").value("1"));
    }
}