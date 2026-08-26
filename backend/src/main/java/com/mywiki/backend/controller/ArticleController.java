package com.mywiki.backend.controller;

import com.mywiki.backend.dto.ArticleCreateDTO;
import com.mywiki.backend.entity.Article;
import com.mywiki.backend.service.ArticleService;
import org.springframework.web.bind.annotation.*;
import com.mywiki.backend.common.Result;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;



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
    public Result<Article> get(@PathVariable Long id){
        Article article = articleService.getById(id);
        return Result.success(article);
    }

    // 创建
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ArticleCreateDTO dto, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        articleService.create(dto, userId);
        return Result.success(null);
    }

    // 修改
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id , @Valid @RequestBody ArticleCreateDTO dto, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        articleService.update(id, dto, userId);
        return Result.success(null);
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        articleService.delete(id, userId);
        return Result.success(null);
    }
}
