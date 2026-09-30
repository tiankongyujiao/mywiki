package com.mywiki.backend.controller;

import  com.mywiki.backend.dto.CategoryCreateDTO;
import com.mywiki.backend.entity.Category;
import com.mywiki.backend.service.CategoryService;
import com.mywiki.backend.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.List;
import jakarta.validation.Valid;
import com.mywiki.backend.annotation.CurrentUser;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<Category>> getAllCategories() {
        return Result.success(
                categoryService.getAllCategories()
        );
    }

    @PostMapping
    public Result<?> createCategory(
            @Valid @RequestBody CategoryCreateDTO dto,
            @CurrentUser Long userId
    ) {
        categoryService.createCategory(dto, userId);
        return Result.success(null);
    }

    // 修改分类
    @PutMapping("/{id}")
    public Result<Void> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryCreateDTO dto,
            @CurrentUser Long userId
    ) {
        categoryService.updateCategory(id, dto, userId);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id, @CurrentUser Long userId){
        categoryService.deleteCategory(id, userId);
        return Result.success(null);
    }
}
