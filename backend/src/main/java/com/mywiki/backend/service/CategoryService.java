package com.mywiki.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mywiki.backend.common.ErrorCode;
import com.mywiki.backend.entity.Category;
import com.mywiki.backend.entity.User;
import com.mywiki.backend.mapper.CategoryMapper;
import com.mywiki.backend.mapper.UserMapper;
import com.mywiki.backend.entity.Article;
import com.mywiki.backend.mapper.ArticleMapper;
import com.mywiki.backend.exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.mywiki.backend.dto.CategoryCreateDTO;

import java.util.List;

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
    public List<Category> getAllCategories() {
        return categoryMapper.selectList(null);
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
