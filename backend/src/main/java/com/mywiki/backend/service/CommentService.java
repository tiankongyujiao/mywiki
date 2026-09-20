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
        commentMapper.insert(comment);
    }

    // 查询文章评论：先查评论，再循环查用户名（N+1 Query，后续课程优化）
    public List<CommentVO> listByArticleId(Long articleId) {
        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .orderByDesc(Comment::getCreatedAt)
        );

        List<CommentVO> result = new ArrayList<>();
        for (Comment comment : comments) {
            User user = userMapper.selectById(comment.getUserId());

            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setArticleId(comment.getArticleId());
            vo.setUserId(comment.getUserId());
            vo.setUsername(user == null ? null : user.getUsername());
            vo.setContent(comment.getContent());
            vo.setCreatedAt(comment.getCreatedAt());
            result.add(vo);
        }
        return result;
    }
}
