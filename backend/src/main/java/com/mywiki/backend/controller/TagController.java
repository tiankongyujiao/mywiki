package com.mywiki.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mywiki.backend.annotation.CurrentUser;
import com.mywiki.backend.common.Result;
import com.mywiki.backend.dto.TagCreateDTO;
import com.mywiki.backend.service.TagService;
import com.mywiki.backend.vo.TagVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // 标签云：所有标签及文章数
    @GetMapping
    public Result<List<TagVO>> list() {
        return Result.success(tagService.listTagsWithCount());
    }

    // 分页列表：支持按名称搜索、按字段排序（名称/文章数量/创建时间/更新时间）
    @GetMapping("/page")
    public Result<Page<TagVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder
    ) {
        return Result.success(tagService.pageTags(page, size, keyword, sortField, sortOrder));
    }

    // 新增标签（仅管理员）
    @PostMapping
    public Result<Void> create(@Valid @RequestBody TagCreateDTO dto, @CurrentUser Long userId) {
        tagService.createTag(dto.getName(), userId);
        return Result.success(null);
    }

    // 重命名标签（仅管理员）
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody TagCreateDTO dto,
            @CurrentUser Long userId
    ) {
        tagService.updateTag(id, dto.getName(), userId);
        return Result.success(null);
    }

    // 删除标签（仅管理员）
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @CurrentUser Long userId) {
        tagService.deleteTag(id, userId);
        return Result.success(null);
    }
}
