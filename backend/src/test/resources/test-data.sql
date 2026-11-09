-- 后端接口测试固定数据：schema.sql 之后执行
-- 密码明文都是 Test123456 的 BCrypt 哈希
INSERT INTO sys_user (id, username, password, role) VALUES
    (1, 'alice', '$2y$10$mgeEUEpvyu3BD3XmSQzBH.ZkTMPHPbCEJ.d.QUH9Xsi7CmUTfV00G', 'USER'),
    (2, 'bob',   '$2y$10$mgeEUEpvyu3BD3XmSQzBH.ZkTMPHPbCEJ.d.QUH9Xsi7CmUTfV00G', 'USER'),
    (3, 'charlie', '$2y$10$mgeEUEpvyu3BD3XmSQzBH.ZkTMPHPbCEJ.d.QUH9Xsi7CmUTfV00G', 'USER');

-- alice 2 篇，bob 1 篇
INSERT INTO article (id, title, content, user_id, category_id) VALUES
    (1, 'alice post 1', 'content', 1, NULL),
    (2, 'alice post 2', 'content', 1, NULL),
    (3, 'bob post 1',   'content', 2, NULL);

  -- 评论 1：bob 发在 alice 的文章（article 1）下；评论 2：alice 发在 bob 的文章（article 3）下
INSERT INTO comment (id, article_id, user_id, content, created_at, updated_at) VALUES
  (1, 1, 2, 'bob comment on alice', '2026-01-01 12:00:00', '2026-01-01 12:00:00'),
  (2, 3, 1, 'alice comment on bob',   '2026-01-02 12:00:00', '2026-01-02 12:00:00');


-- 草稿：alice、bob 各一篇（mine 可见；主列表/他人不可见）
INSERT INTO article (id, title, content, user_id, category_id, status) VALUES
  (4, 'alice draft post', 'draft content', 1, NULL, 'DRAFT'),
  (5, 'bob draft post',   'draft content', 2, NULL, 'DRAFT');

-- 标签计数素材：draftcount 同挂一篇已发布(1)和一篇草稿(4)，只应计 1
INSERT INTO tag (id, name, created_at, updated_at) VALUES
  (1, 'draftcount', '2026-01-01 00:00:00', '2026-01-01 00:00:00');
INSERT INTO article_tag (article_id, tag_id) VALUES
  (1, 1),
  (4, 1);


-- 管理员：分类/标签管理权限测试用
INSERT INTO sys_user (id, username, password, role) VALUES
  (9, 'admin_test', '$2y$10$mgeEUEpvyu3BD3XmSQzBH.ZkTMPHPbCEJ.d.QUH9Xsi7CmUTfV00G', 'ADMIN');

-- 专用分类/标签（不干扰现有 1/2 号数据）：分类 9 下挂一篇草稿，验证"占用"判定
  INSERT INTO category (id, name, created_at, updated_at) VALUES
    (8, 'rule-test-cat-2', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
    (9, 'rule-test-cat', '2026-01-01 00:00:00', '2026-01-01 00:00:00');

  INSERT INTO article (id, title, content, user_id, category_id, status) VALUES
  (9, 'draft in rule-test-cat', 'c', 9, 9, 'DRAFT');

  INSERT INTO tag (id, name, created_at, updated_at) VALUES
  (9, 'rule-test-tag', '2026-01-01 00:00:00', '2026-01-01 00:00:00');

  INSERT INTO article_tag (article_id, tag_id) VALUES
  (9, 9);


  -- 评论分页素材：文章 3 共 12 条（已有 alice 1 条 + 这里 11 条），时间递增
  INSERT INTO comment (id, article_id, user_id, content, created_at, updated_at) VALUES
    (10, 3, 2, 'page test comment 02', '2026-01-10 10:00:00', '2026-01-10 10:00:00'),
    (11, 3, 2, 'page test comment 03', '2026-01-11 10:00:00', '2026-01-11 10:00:00'),
    (12, 3, 2, 'page test comment 04', '2026-01-12 10:00:00', '2026-01-12 10:00:00'),
    (13, 3, 2, 'page test comment 05', '2026-01-13 10:00:00', '2026-01-13 10:00:00'),
    (14, 3, 2, 'page test comment 06', '2026-01-14 10:00:00', '2026-01-14 10:00:00'),
    (15, 3, 2, 'page test comment 07', '2026-01-15 10:00:00', '2026-01-15 10:00:00'),
    (16, 3, 2, 'page test comment 08', '2026-01-16 10:00:00', '2026-01-16 10:00:00'),
    (17, 3, 2, 'page test comment 09', '2026-01-17 10:00:00', '2026-01-17 10:00:00'),
    (18, 3, 2, 'page test comment 10', '2026-01-18 10:00:00', '2026-01-18 10:00:00'),
    (19, 3, 2, 'page test comment 11', '2026-01-19 10:00:00', '2026-01-19 10:00:00'),
    (20, 3, 2, 'page test comment 12', '2026-01-20 10:00:00', '2026-01-20 10:00:00');