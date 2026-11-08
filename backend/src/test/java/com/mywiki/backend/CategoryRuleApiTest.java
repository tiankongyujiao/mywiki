package com.mywiki.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.List;
import java.util.Map;


// 契约：分类增改删仅 ADMIN(403)；重名 400(10009)；分类下有任何文章（含草稿）不可删 400(10012)；
// 未登录应 401（现状：@CurrentUser 未登录抛 USER_NOT_FOUND=404，属偏差，测试钉正确契约）
@Sql({"file:sql/schema.sql", "classpath:test-data.sql"})
class CategoryRuleApiTest extends BaseApiTest {

    @Test
    void 普通用户不能创建分类() throws Exception {
        String token = login("alice");
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"new cat\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(10010));
    }

    @Test
    void 未登录创建分类返回401() throws Exception {
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"new cat\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 管理员可以创建分类() throws Exception {
        String token = login("admin_test");
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"admin created\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void 分类重名被拒绝() throws Exception {
        String token = login("admin_test");
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"rule-test-cat\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10009));
    }

    @Test
    void 分类改名为其他分类的名字被拒绝() throws Exception {
        String token = login("admin_test");
        mockMvc.perform(put("/categories/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"rule-test-cat-2\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10009));
    }

    @Test
    void 分类改名为自己的名字允许() throws Exception {
        String token = login("admin_test");
        mockMvc.perform(put("/categories/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"rule-test-cat\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void 分类下有草稿文章时不可删除() throws Exception {
        String token = login("admin_test");
        mockMvc.perform(delete("/categories/9")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10012));
    }

    @Test
    void 空分类可以删除() throws Exception {
        String token = login("admin_test");
        // 先建一个没有任何文章的分类再删
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"empty cat\"}")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        Long id = findCategoryIdByName(token, "empty cat");
        mockMvc.perform(delete("/categories/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }


    // 创建接口不返回 id，从列表按名字反查
    private Long findCategoryIdByName(String token, String name) throws Exception {
        var result = mockMvc.perform(get("/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        List<java.util.Map<String, Object>> list = com.jayway.jsonpath.JsonPath.read(
                  result.getResponse().getContentAsString(), "$.data[*]");
        for (Map<String, Object> c : list) {
            if (name.equals(c.get("name"))) {
                return Long.valueOf(c.get("id").toString());
            }
        }
        throw new AssertionError("找不到分类: " + name);
    }
}
