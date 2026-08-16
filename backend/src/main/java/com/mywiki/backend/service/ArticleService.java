package com.mywiki.backend.service;

import com.mywiki.backend.dto.ArticleCreateDTO;
import com.mywiki.backend.entity.Article;
import com.mywiki.backend.mapper.ArticleMapper;
import org.springframework.stereotype.Service;
import com.mywiki.backend.common.ErrorCode;
import com.mywiki.backend.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

@Service
public class ArticleService {
    private final ArticleMapper articleMapper;

    public ArticleService(ArticleMapper articleMapper){
        this.articleMapper = articleMapper;
    }

    // 查询详情
    public Article getById(Long id){
        Article article = articleMapper.selectById(id);
        if(article == null){
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        return article;
    }

    // 查询列表
    // public List<Article> list(){
    //     return articleMapper.selectList(null);
    // }

    public Page<Article> page(long current, long size) {
        Page<Article> page = new Page<>(current, size);
        return articleMapper.selectPage(page, null);
    }

    // 新增文章
    public void create(ArticleCreateDTO dto){
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        articleMapper.insert(article);
    }

    // 修改文章
    public void update(Long id, ArticleCreateDTO dto){
        Article article = new Article();
        article.setId(id);
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        articleMapper.updateById(article);
    }

    // 删除文章
    public void delete(Long id){
        articleMapper.deleteById(id);
    }
}
