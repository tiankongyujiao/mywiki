package com.mywiki.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mywiki.backend.common.ErrorCode;
import com.mywiki.backend.entity.Category;
import com.mywiki.backend.mapper.CategoryMapper;
import com.mywiki.backend.exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.mywiki.backend.dto.CategoryCreateDTO;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryMapper categoryMapper;
    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }
    public List<Category> getAllCategories() {
        return categoryMapper.selectList(null);
    }

    public void createCategory(CategoryCreateDTO dto) {
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
}
