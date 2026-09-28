package com.mywiki.backend.controller;

import  com.mywiki.backend.dto.CategoryCreateDTO;
import com.mywiki.backend.entity.Category;
import com.mywiki.backend.service.CategoryService;
import com.mywiki.backend.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
}
