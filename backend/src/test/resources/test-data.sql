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