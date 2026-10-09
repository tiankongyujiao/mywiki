package com.mywiki.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mywiki.backend.common.ErrorCode;
import com.mywiki.backend.entity.Category;
import com.mywiki.backend.entity.User;
import com.mywiki.backend.mapper.CategoryMapper;
import com.mywiki.backend.mapper.UserMapper;
import com.mywiki.backend.entity.Article;
import com.mywiki.backend.mapper.ArticleMapper;
import com.mywiki.backend.exception.BusinessException;
import com.mywiki.backend.vo.CategoryVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.mywiki.backend.dto.CategoryCreateDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    public CategoryService(CategoryMapper categoryMapper, UserMapper userMapper, ArticleMapper articleMapper) {
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
    }
    public List<CategoryVO> getAllCategories() {
        return toCategoryVOs(categoryMapper.selectList(null));
    }

    // 分页 + 按名称搜索 + 排序（排序字段白名单，默认按创建时间倒序）
    public Page<CategoryVO> pageCategories(long current, long size, String keyword, String sortField, String sortOrder) {
        List<CategoryVO> all = toCategoryVOs(categoryMapper.selectList(null));

        String kw = keyword == null ? null : keyword.trim().toLowerCase();
        List<CategoryVO> filtered = (kw == null || kw.isEmpty())
                ? all
                : all.stream()
                    .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(kw))
                    .toList();

        List<CategoryVO> sorted = new ArrayList<>(filtered);
        sorted.sort(resolveComparator(sortField, sortOrder));

        // 内存分页：分类数据量小，可支持按文章数等任意字段排序
        long from = Math.max(0, (current - 1) * size);
        int start = (int) Math.min(from, sorted.size());
        int end = (int) Math.min(from + size, sorted.size());
        List<CategoryVO> records = sorted.subList(start, end);

        Page<CategoryVO> page = new Page<>(current, size, sorted.size());
        page.setRecords(records);
        return page;
    }

    private Comparator<CategoryVO> resolveComparator(String sortField, String sortOrder) {
        Comparator<CategoryVO> comparator;
        switch (sortField == null ? "" : sortField) {
            case "name":
                comparator = Comparator.comparing(CategoryVO::getName, Comparator.nullsLast(String::compareTo));
                break;
            case "articleCount":
                comparator = Comparator.comparing(CategoryVO::getArticleCount, Comparator.nullsLast(Long::compareTo));
                break;
            case "updatedAt":
                comparator = Comparator.comparing(CategoryVO::getUpdatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
                break;
            case "createdAt":
            default:
                comparator = Comparator.comparing(CategoryVO::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
        }
        // 默认（含未传排序方向）按倒序
        return "asc".equalsIgnoreCase(sortOrder) ? comparator : comparator.reversed();
    }

    private List<CategoryVO> toCategoryVOs(List<Category> categories) {
        if (categories.isEmpty()) {
            return List.of();
        }
        // 一次性按分类分组统计文章数，避免逐个分类 selectCount 的 N+1
        QueryWrapper<Article> countWrapper = new QueryWrapper<>();
        countWrapper.select("category_id", "count(*) AS cnt")
                .isNotNull("category_id")
                .groupBy("category_id");
        Map<Long, Long> countByCategoryId = articleMapper.selectMaps(countWrapper).stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("category_id")).longValue(),
                        m -> ((Number) m.get("cnt")).longValue()
                ));

        return categories.stream().map(category -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(category.getId());
            vo.setName(category.getName());
            vo.setCreatedAt(category.getCreatedAt());
            vo.setUpdatedAt(category.getUpdatedAt());
            vo.setArticleCount(countByCategoryId.getOrDefault(category.getId(), 0L));
            return vo;
        }).toList();
    }

    public void createCategory(CategoryCreateDTO dto, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.CATEGORY_ADMIN_ONLY);
        }

        LambdaQueryWrapper<Category> wrapper =
                new LambdaQueryWrapper<>();

        wrapper.eq(
                Category::getName,
                dto.getName()
        );

        Category existCategory =
                categoryMapper.selectOne(wrapper);

        if (existCategory != null) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NAME_EXIST
            );
        }

        Category category = new Category();
        category.setName(dto.getName());

        try {
            categoryMapper.insert(category);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(
                    ErrorCode.CATEGORY_NAME_EXIST
            );
        }
    }


    // 修改分类
    public void updateCategory(Long id, CategoryCreateDTO dto, Long userId) {

        // 1. 查询当前用户
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 判断是否管理员
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.CATEGORY_ADMIN_ONLY);
        }

        // 3. 查询要修改的分类
        Category category = categoryMapper.selectById(id);

        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 4. 检查新的分类名称是否已经存在
        LambdaQueryWrapper<Category> wrapper =
                new LambdaQueryWrapper<>();

        wrapper.eq(
                Category::getName,
                dto.getName()
        );

        Category existCategory =
                categoryMapper.selectOne(wrapper);

        if (existCategory != null
                && !existCategory.getId().equals(id)) {

            throw new BusinessException(
                    ErrorCode.CATEGORY_NAME_EXIST
            );
        }

        // 5. 修改分类名称
        category.setName(dto.getName());

        categoryMapper.updateById(category);
    }

    // 删除分类
    public void deleteCategory(Long id, Long userId) {
        // 1. 查询当前用户
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 判断是否管理员
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.CATEGORY_ADMIN_ONLY);
        }

        // 3. 查询要删除的分类
        Category category = categoryMapper.selectById(id);

        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 4. 检查该分类下是否还有文章
        LambdaQueryWrapper<Article> articleWrapper =
                new LambdaQueryWrapper<>();

        articleWrapper.eq(
                Article::getCategoryId,
                id
        );

        Long articleCount = articleMapper.selectCount(articleWrapper);

        if (articleCount != null && articleCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }

        // 5. 删除分类
        categoryMapper.deleteById(id);
    }
}
