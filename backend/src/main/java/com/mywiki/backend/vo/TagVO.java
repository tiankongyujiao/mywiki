package com.mywiki.backend.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TagVO {
    private Long id;
    private String name;

    // 引用该标签的文章数量（标签云用）
    private Long articleCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
