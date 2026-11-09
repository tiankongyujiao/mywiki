package com.mywiki.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mywiki.backend.common.ErrorCode;
import com.mywiki.backend.dto.CommentCreateDTO;
import com.mywiki.backend.entity.Article;
import com.mywiki.backend.entity.Comment;
import com.mywiki.backend.entity.User;
import com.mywiki.backend.exception.BusinessException;
import com.mywiki.backend.mapper.ArticleMapper;
import com.mywiki.backend.mapper.CommentMapper;
import com.mywiki.backend.mapper.UserMapper;
import com.mywiki.backend.vo.CommentVO;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public CommentService(
            CommentMapper commentMapper,
            ArticleMapper articleMapper,
            UserMapper userMapper
    ) {
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
    }

    // 发表评论：articleId 来自 URL，userId 来自 JWT，content 来自请求体
    public void createComment(Long articleId, Long userId, CommentCreateDTO dto) {
        // 第一步：文章必须存在
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 第二步：创建 Comment
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent());

        // 草稿对外等于不存在，评论同样拒绝
        if (!"PUBLISHED".equals(article.getStatus())) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        commentMapper.insert(comment);
    }

    // 分页查询文章评论（createdAt 倒序，最新在前）
    public Page<CommentVO> pageComments(long current, long size, Long articleId) {
        Page<Comment> page = commentMapper.selectPage(
                new Page<>(current, size),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .orderByDesc(Comment::getCreatedAt)
        );

        Page<CommentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        // 1. 批量查询用户信息
        Map<Long, String> userMap = loadUserNames(page.getRecords());

        List<CommentVO> result = new ArrayList<>();
        for (Comment comment : page.getRecords()) {
            String username = userMap.get(comment.getUserId());

            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setArticleId(comment.getArticleId());
            vo.setUserId(comment.getUserId());
            vo.setUsername(username);
            vo.setContent(comment.getContent());
            vo.setCreatedAt(comment.getCreatedAt());
            result.add(vo);
        }
        voPage.setRecords(result);
        return voPage;
    }

    private Map<Long, String> loadUserNames(List<Comment> comments) {
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    public void deleteComment(Long articleId, Long commentId, Long userId) {
        // 1. 查询评论
        Comment comment = commentMapper.selectById(commentId);

        if (comment == null) {
            throw new BusinessException(
                ErrorCode.COMMENT_NOT_FOUND
            );
        }

        // 2. 校验评论确实属于 URL 中指定的那篇文章
        if (!comment.getArticleId().equals(articleId)) {
            throw new BusinessException(
                ErrorCode.COMMENT_NOT_FOUND
            );
        }

        // 3. 查询文章
        Article article = articleMapper.selectById(comment.getArticleId());

        if (article == null) {
            throw new BusinessException(
                ErrorCode.ARTICLE_NOT_FOUND
            );
        }

        // 4. 判断权限
        boolean isCommentOwner = comment.getUserId().equals(userId);

        boolean isArticleOwner = article.getUserId().equals(userId);

        if (!isCommentOwner && !isArticleOwner) {
            throw new BusinessException(
                ErrorCode.FORBIDDEN
            );
        }

        // 5. 删除
        commentMapper.deleteById(commentId);
    }
}
