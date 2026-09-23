package com.mywiki.backend.controller;

import com.mywiki.backend.dto.ArticleCreateDTO;
import com.mywiki.backend.service.ArticleService;
import com.mywiki.backend.vo.ArticleVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        @RequestParam(required = false) String keyword,
        HttpServletRequest request
    ) {
        return Result.success(
                articleService.page(page, size, keyword, currentUserId(request))
        );
    }

    // 查询详情（游客也可访问，currentUserId 为 null 时 VO 里权限字段为 false）
    @GetMapping("/{id}")
    public Result<ArticleVO> get(@PathVariable Long id, HttpServletRequest request){
        return Result.success(articleService.getById(id, currentUserId(request)));
    }

    // JwtInterceptor 对 GET /articles 放行时可能未解析 token，未登录返回 null
    private Long currentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return userId instanceof Long id ? id : null;
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
