-- e2e 测试种子数据，每次跑测试前由重置脚本重建测试库并执行本文件
-- 测试账号：e2e_test / Test123456（password 列存的是 BCrypt 哈希）

-- 用户：唯一测试账号，所有文章/评论的 user_id 都指向它
INSERT INTO sys_user (id, username, password, role, create_time, update_time) VALUES
(1, 'e2e_test', '$2y$10$mgeEUEpvyu3BD3XmSQzBH.ZkTMPHPbCEJ.d.QUH9Xsi7CmUTfV00G', 'USER', '2026-01-01 00:00:00', '2026-01-01 00:00:00');

-- 分类：分类A 下 2 篇文章、分类B 下 1 篇，供按分类筛选的断言用
INSERT INTO category (id, name, created_at, updated_at) VALUES
(1, '测试分类A', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(2, '测试分类B', '2026-01-01 00:00:00', '2026-01-01 00:00:00');

-- 文章：create_time 固定且递增，保证"按时间排序/最新在前"的断言稳定
INSERT INTO article (id, title, content, user_id, category_id, create_time, update_time) VALUES
(1, '种子文章一', '种子内容1', 1, 1, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),
(2, '种子文章二', '种子内容2', 1, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(3, '种子文章三', '种子内容3', 1, 2, '2026-01-03 10:00:00', '2026-01-03 10:00:00');

-- 标签：vue 挂在文章 1、3 上；springboot 只挂在文章 1 上，供标签筛选断言用
INSERT INTO tag (id, name, created_at, updated_at) VALUES
(1, 'vue', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(2, 'springboot', '2026-01-01 00:00:00', '2026-01-01 00:00:00');

INSERT INTO article_tag (article_id, tag_id) VALUES
(1, 1),
(1, 2),
(3, 1);

-- 评论：文章 1 有 1 条评论
INSERT INTO comment (id, article_id, user_id, content, created_at, updated_at) VALUES
(1, 1, 1, '种子评论', '2026-01-01 12:00:00', '2026-01-01 12:00:00');
