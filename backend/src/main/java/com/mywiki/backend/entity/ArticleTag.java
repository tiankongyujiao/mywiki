package com.mywiki.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 文章-标签关联表，联合主键(article_id, tag_id)，无自增 id
@Data
@TableName("article_tag")
public class ArticleTag {
    private Long articleId;
    private Long tagId;
}
