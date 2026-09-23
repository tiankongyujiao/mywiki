package com.mywiki.backend.controller;

import com.mywiki.backend.annotation.CurrentUser;
import com.mywiki.backend.common.Result;
import com.mywiki.backend.dto.CommentCreateDTO;
import com.mywiki.backend.service.CommentService;
import com.mywiki.backend.vo.CommentVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequestMapping("/articles")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 查询文章评论（公开，无需登录）
    @GetMapping("/{articleId}/comments")
    public Result<List<CommentVO>> list(@PathVariable Long articleId) {
        return Result.success(commentService.listByArticleId(articleId));
    }

    // 发表评论（需登录，userId 由 @CurrentUser 从 JWT 解析）
    @PostMapping("/{articleId}/comments")
    public Result<Void> create(
            @PathVariable Long articleId,
            @RequestBody @Valid CommentCreateDTO dto,
            @CurrentUser Long userId
    ) {
        commentService.createComment(articleId, userId, dto);
        return Result.success(null);
    }

    // 删除评论（需登录，评论作者或文章作者可删）
    @DeleteMapping("/{articleId}/comments/{commentId}")
    public Result<Void> delete(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @CurrentUser Long userId
    ) {
        commentService.deleteComment(articleId, commentId, userId);
        return Result.success(null);
    }
}
