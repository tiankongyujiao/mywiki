package com.mywiki.backend.controller;

import com.mywiki.backend.dto.ArticleCreateDTO;
import com.mywiki.backend.service.ArticleService;
import com.mywiki.backend.vo.ArticleVO;
import org.springframework.web.bind.annotation.*;
import com.mywiki.backend.common.Result;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import com.mywiki.backend.annotation.CurrentUser;



import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService){
        this.articleService = articleService;
    }

    // 查询列表
    // @GetMapping
    // public Result<List<Article>> list(){
    //     return Result.success(articleService.list());
    // }
    @GetMapping 
    public Result<?> list(
        @RequestParam(defaultValue = "1") long page, 
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false) String keyword
    ) {
        return Result.success(
                articleService.page(page, size, keyword)
        );
    }

    // 查询详情
    @GetMapping("/{id}")
    public Result<ArticleVO> get(@PathVariable Long id){
        return Result.success(articleService.getById(id));
    }

    // 创建
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ArticleCreateDTO dto, HttpServletRequest request, @CurrentUser Long userId){
        articleService.create(dto, userId);
        return Result.success(null);
    }

    // 修改
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id , @Valid @RequestBody ArticleCreateDTO dto, HttpServletRequest request, @CurrentUser Long userId){
        articleService.update(id, dto, userId);
        return Result.success(null);
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request, @CurrentUser Long userId){
        articleService.delete(id, userId);
        return Result.success(null);
    }
}
