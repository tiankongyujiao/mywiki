package com.mywiki.backend.vo;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleVO {
    private Long id;
    
    private String title;

    private String content;

    private Long userId;

    private String username;

    private Long categoryId;

    private String categoryName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 权限由后端统一计算后下发，前端只负责展示
    private Boolean canEdit;

    private Boolean canDelete;
}
