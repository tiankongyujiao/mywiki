package com.mywiki.backend.service;

import com.mywiki.backend.dto.ArticleCreateDTO;
import com.mywiki.backend.entity.Article;
import com.mywiki.backend.entity.Category;
import com.mywiki.backend.entity.User;
import com.mywiki.backend.mapper.ArticleMapper;
import com.mywiki.backend.mapper.CategoryMapper;
import com.mywiki.backend.mapper.UserMapper;
import com.mywiki.backend.vo.ArticleVO;
import org.springframework.stereotype.Service;
import com.mywiki.backend.common.ErrorCode;
import com.mywiki.backend.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ArticleService {
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final TagService tagService;

    public ArticleService(ArticleMapper articleMapper, UserMapper userMapper, CategoryMapper categoryMapper, TagService tagService){
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.tagService = tagService;
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
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            vo.setCategoryName(category == null ? null : category.getName());
        }
        vo.setTags(tagService.getTagNamesByArticleIds(List.of(id))
                .getOrDefault(id, List.of()));
        return vo;
    }

    // 查询列表
    // public List<Article> list(){
    //     return articleMapper.selectList(null);
    // }

    public Page<ArticleVO> page(long current, long size, String keyword, Long categoryId, Long tagId, Long currentUserId) {
        Page<Article> page = new Page<>(current, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            // 用 and(...) 包一层，保证 (标题 like 或 内容 like) 与后续分类条件之间是 AND
            wrapper.and(w -> w.like(Article::getTitle, kw)
                    .or()
                    .like(Article::getContent, kw));
        }
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (tagId != null) {
            // 按标签筛选：文章 id 属于该标签的关联集合
            wrapper.apply("id IN (SELECT article_id FROM article_tag WHERE tag_id = {0})", tagId);
        }
        wrapper.orderByDesc(
            Article::getCreateTime
        );
        articleMapper.selectPage(page, wrapper);

        Page<ArticleVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        Map<Long, String> categoryNames = loadCategoryNames(page.getRecords());
        Map<Long, List<String>> tagNames = tagService.getTagNamesByArticleIds(
                page.getRecords().stream().map(Article::getId).toList());
        voPage.setRecords(
            page.getRecords().stream()
                .map(a -> {
                    ArticleVO vo = toVO(a, currentUserId);
                    if (a.getCategoryId() != null) {
                        vo.setCategoryName(categoryNames.get(a.getCategoryId()));
                    }
                    vo.setTags(tagNames.getOrDefault(a.getId(), List.of()));
                    return vo;
                })
                .toList()
        );
        return voPage;
    }

    // 批量加载文章分类名，避免列表页 N+1 查询
    private Map<Long, String> loadCategoryNames(List<Article> articles) {
        List<Long> ids = articles.stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return categoryMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    // 校验并返回合法的分类 id，未传分类时返回 null
    private Long resolveCategoryId(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return categoryId;
    }

    // 组装 VO：isOwner 即"当前登录用户是作者"，未登录时恒为 false
    private ArticleVO toVO(Article article, Long currentUserId) {
        boolean isOwner = currentUserId != null && currentUserId.equals(article.getUserId());
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setUserId(article.getUserId());
        vo.setCategoryId(article.getCategoryId());
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
        article.setCategoryId(resolveCategoryId(dto.getCategoryId()));
        articleMapper.insert(article);
        tagService.syncArticleTags(article.getId(), dto.getTags());
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
        // 用 UpdateWrapper 显式 set，保证清空分类（categoryId 为 null）也能生效
        LambdaUpdateWrapper<Article> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Article::getId, id)
                .set(Article::getTitle, dto.getTitle())
                .set(Article::getContent, dto.getContent())
                .set(Article::getCategoryId, resolveCategoryId(dto.getCategoryId()))
                .set(Article::getUpdateTime, LocalDateTime.now());
        articleMapper.update(null, wrapper);
        tagService.syncArticleTags(id, dto.getTags());
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
        tagService.removeArticleTags(id);
    }
}
