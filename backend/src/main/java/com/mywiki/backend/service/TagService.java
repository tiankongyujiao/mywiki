package com.mywiki.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mywiki.backend.common.ErrorCode;
import com.mywiki.backend.entity.ArticleTag;
import com.mywiki.backend.entity.Tag;
import com.mywiki.backend.entity.User;
import com.mywiki.backend.exception.BusinessException;
import com.mywiki.backend.mapper.ArticleTagMapper;
import com.mywiki.backend.mapper.TagMapper;
import com.mywiki.backend.mapper.UserMapper;
import com.mywiki.backend.vo.TagVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TagService {

    private static final int MAX_TAG_NAME_LENGTH = 30;

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final UserMapper userMapper;

    public TagService(TagMapper tagMapper, ArticleTagMapper articleTagMapper, UserMapper userMapper) {
        this.tagMapper = tagMapper;
        this.articleTagMapper = articleTagMapper;
        this.userMapper = userMapper;
    }

    // 标签云：所有标签及其文章数，按文章数倒序
    public List<TagVO> listTagsWithCount() {
        List<Tag> tags = tagMapper.selectList(null);
        if (tags.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> countByTagId = countArticlesByTag();
        return tags.stream()
                .map(tag -> {
                    TagVO vo = new TagVO();
                    vo.setId(tag.getId());
                    vo.setName(tag.getName());
                    vo.setArticleCount(countByTagId.getOrDefault(tag.getId(), 0L));
                    vo.setCreatedAt(tag.getCreatedAt());
                    vo.setUpdatedAt(tag.getUpdatedAt());
                    return vo;
                })
                .sorted(Comparator.comparing(TagVO::getArticleCount).reversed()
                        .thenComparing(TagVO::getName))
                .toList();
    }

    // 标签管理分页：按名称搜索 + 排序（字段白名单，默认文章数倒序）
    public Page<TagVO> pageTags(long current, long size, String keyword, String sortField, String sortOrder) {
        List<TagVO> all = listTagsWithCount();

        String kw = keyword == null ? null : keyword.trim().toLowerCase();
        List<TagVO> filtered = (kw == null || kw.isEmpty())
                ? all
                : all.stream()
                    .filter(t -> t.getName() != null && t.getName().toLowerCase().contains(kw))
                    .toList();

        // 内存分页：标签数据量小，可支持按文章数等任意字段排序
        List<TagVO> sorted = new ArrayList<>(filtered);
        sorted.sort(resolveComparator(sortField, sortOrder));

        long from = Math.max(0, (current - 1) * size);
        int start = (int) Math.min(from, sorted.size());
        int end = (int) Math.min(from + size, sorted.size());

        Page<TagVO> page = new Page<>(current, size, sorted.size());
        page.setRecords(sorted.subList(start, end));
        return page;
    }

    private Comparator<TagVO> resolveComparator(String sortField, String sortOrder) {
        Comparator<TagVO> comparator;
        switch (sortField == null ? "" : sortField) {
            case "name":
                comparator = Comparator.comparing(TagVO::getName, Comparator.nullsLast(String::compareTo));
                break;
            case "createdAt":
                comparator = Comparator.comparing(TagVO::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
                break;
            case "updatedAt":
                comparator = Comparator.comparing(TagVO::getUpdatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
                break;
            case "articleCount":
            default:
                comparator = Comparator.comparing(TagVO::getArticleCount, Comparator.nullsLast(Long::compareTo));
        }
        // 默认（含未传排序方向）按倒序
        return "asc".equalsIgnoreCase(sortOrder) ? comparator : comparator.reversed();
    }

    // 新增标签（仅管理员）
    public void createTag(String name, Long userId) {
        requireAdmin(userId);
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID);
        }
        if (tagMapper.selectCount(new LambdaQueryWrapper<Tag>().eq(Tag::getName, normalized)) > 0) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXIST);
        }
        Tag tag = new Tag();
        tag.setName(normalized);
        try {
            tagMapper.insert(tag);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXIST);
        }
    }

    // 重命名标签（仅管理员）
    public void updateTag(Long id, String name, Long userId) {
        requireAdmin(userId);
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID);
        }
        Long exists = tagMapper.selectCount(
                new LambdaQueryWrapper<Tag>().eq(Tag::getName, normalized).ne(Tag::getId, id));
        if (exists != null && exists > 0) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXIST);
        }
        Tag update = new Tag();
        update.setId(id);
        update.setName(normalized);
        try {
            tagMapper.updateById(update);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXIST);
        }
    }

    // 删除标签（仅管理员）：仍有文章引用时不允许删除
    public void deleteTag(Long id, Long userId) {
        requireAdmin(userId);
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }
        Long count = articleTagMapper.selectCount(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, id));
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.TAG_IN_USE);
        }
        tagMapper.deleteById(id);
    }

    private void requireAdmin(Long userId) {
        User user = userId == null ? null : userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.TAG_ADMIN_ONLY);
        }
    }

    // 批量统计每个标签下的文章数（在 article_tag 上分组，避免 join）
    private Map<Long, Long> countArticlesByTag() {
        QueryWrapper<ArticleTag> wrapper = new QueryWrapper<>();
        wrapper.select("tag_id", "count(*) AS cnt").groupBy("tag_id");
        return articleTagMapper.selectMaps(wrapper).stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("tag_id")).longValue(),
                        m -> ((Number) m.get("cnt")).longValue()
                ));
    }

    // 批量查询多篇文章的标签名，避免列表页 N+1
    public Map<Long, List<String>> getTagNamesByArticleIds(Collection<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ArticleTag> links = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getArticleId, articleIds));
        if (links.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> tagIds = links.stream().map(ArticleTag::getTagId).distinct().toList();
        Map<Long, String> tagNames = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));

        Map<Long, List<String>> result = new HashMap<>();
        for (ArticleTag link : links) {
            String name = tagNames.get(link.getTagId());
            if (name != null) {
                result.computeIfAbsent(link.getArticleId(), k -> new ArrayList<>()).add(name);
            }
        }
        return result;
    }

    // 覆盖式同步文章标签：先清空旧关联，再按标签名解析或新建标签并建立关联
    public void syncArticleTags(Long articleId, List<String> tagNames) {
        articleTagMapper.delete(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId));

        List<String> names = normalize(tagNames);
        if (names.isEmpty()) {
            return;
        }

        Map<String, Long> nameToId = tagMapper
                .selectList(new LambdaQueryWrapper<Tag>().in(Tag::getName, names)).stream()
                .collect(Collectors.toMap(Tag::getName, Tag::getId, (a, b) -> a));

        for (String name : names) {
            Long tagId = nameToId.get(name);
            if (tagId == null) {
                tagId = createTagInternal(name);
            }
            if (tagId == null) {
                continue;
            }
            ArticleTag link = new ArticleTag();
            link.setArticleId(articleId);
            link.setTagId(tagId);
            articleTagMapper.insert(link);
        }
    }

    // 删除文章的全部标签关联
    public void removeArticleTags(Long articleId) {
        articleTagMapper.delete(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId));
    }

    // 去空、去重、限长，保持用户输入顺序
    private List<String> normalize(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }
        return tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> name.length() > MAX_TAG_NAME_LENGTH
                        ? name.substring(0, MAX_TAG_NAME_LENGTH) : name)
                .distinct()
                .toList();
    }

    // 新建标签（写作时自动创建），并发下若已被创建则回查
    private Long createTagInternal(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        try {
            tagMapper.insert(tag);
            return tag.getId();
        } catch (DuplicateKeyException e) {
            Tag created = tagMapper.selectOne(
                    new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
            return created == null ? null : created.getId();
        }
    }
}
