package com.mywiki.backend.service;

import com.mywiki.backend.dto.ArticleCreateDTO;
import com.mywiki.backend.entity.Article;
import com.mywiki.backend.entity.User;
import com.mywiki.backend.mapper.ArticleMapper;
import com.mywiki.backend.mapper.UserMapper;
import com.mywiki.backend.vo.ArticleVO;
import org.springframework.stereotype.Service;
import com.mywiki.backend.common.ErrorCode;
import com.mywiki.backend.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleService {
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public ArticleService(ArticleMapper articleMapper, UserMapper userMapper){
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
    }

    // 查询详情（游客可访问，currentUserId 可能为 null）
    public ArticleVO getById(Long id, Long currentUserId){
        Article article = articleMapper.selectById(id);
        if(article == null){
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        User author = userMapper.selectById(article.getUserId());
        ArticleVO vo = toVO(article, currentUserId);
        vo.setUsername(author == null ? null : author.getUsername());
        return vo;
    }

    // 查询列表
    // public List<Article> list(){
    //     return articleMapper.selectList(null);
    // }

    public Page<ArticleVO> page(long current, long size, String keyword, Long currentUserId) {
        Page<Article> page = new Page<>(current, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(
                Article::getTitle,
                keyword.trim()
            )
            .or()
            .like(Article::getContent, keyword.trim());
        }
        wrapper.orderByDesc(
            Article::getCreateTime
        );
        articleMapper.selectPage(page, wrapper);

        Page<ArticleVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(
            page.getRecords().stream()
                .map(a -> toVO(a, currentUserId))
                .toList()
        );
        return voPage;
    }

    // 组装 VO：isOwner 即"当前登录用户是作者"，未登录时恒为 false
    private ArticleVO toVO(Article article, Long currentUserId) {
        boolean isOwner = currentUserId != null && currentUserId.equals(article.getUserId());
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setUserId(article.getUserId());
        vo.setCreatedAt(article.getCreateTime());
        vo.setUpdatedAt(article.getUpdateTime());
        vo.setCanEdit(isOwner);
        vo.setCanDelete(isOwner);
        return vo;
    }

    // 新增文章
    public void create(ArticleCreateDTO dto, Long userId){
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setUserId(userId);
        articleMapper.insert(article);
    }

    // 修改文章
    public void update(Long id, ArticleCreateDTO dto, Long userId){
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);
    }

    // 删除文章
    public void delete(Long id, Long userId){
        Article article = articleMapper.selectById(id);

        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        articleMapper.deleteById(id);
    }
}
