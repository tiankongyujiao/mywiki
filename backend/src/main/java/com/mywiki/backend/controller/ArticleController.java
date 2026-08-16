package com.mywiki.backend.controller;

import com.mywiki.backend.dto.ArticleCreateDTO;
import com.mywiki.backend.entity.Article;
import com.mywiki.backend.service.ArticleService;
import org.springframework.web.bind.annotation.*;
import com.mywiki.backend.common.Result;

import jakarta.validation.Valid;



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
    public Result<Void> create(@Valid @RequestBody ArticleCreateDTO dto){
        articleService.create(dto);
        return Result.success(null);
    }

    // 修改
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id , @Valid @RequestBody ArticleCreateDTO dto){
        articleService.update(id,dto);
        return Result.success(null);
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        articleService.delete(id);
        return Result.success(null);
    }
}
