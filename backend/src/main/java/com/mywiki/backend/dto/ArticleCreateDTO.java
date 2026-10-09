package com.mywiki.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

import java.util.List;

@Data
public class ArticleCreateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(
        max = 50,
        message = "标题长度不能超过50个字符"
    )
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    // 所属分类，可为空
    private Long categoryId;

    // 标签名列表，可为空；不存在的标签会自动创建
    private List<String> tags;
}
