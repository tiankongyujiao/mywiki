-- 后端接口测试固定数据：schema.sql 之后执行
-- 密码明文都是 Test123456 的 BCrypt 哈希
INSERT INTO sys_user (id, username, password, role) VALUES
                                                        (1, 'alice', '$2y$10$mgeEUEpvyu3BD3XmSQzBH.ZkTMPHPbCEJ.d.QUH9Xsi7CmUTfV00G', 'USER'),
                                                        (2, 'bob',   '$2y$10$mgeEUEpvyu3BD3XmSQzBH.ZkTMPHPbCEJ.d.QUH9Xsi7CmUTfV00G', 'USER');

-- alice 2 篇，bob 1 篇
INSERT INTO article (id, title, content, user_id, category_id) VALUES
                                                                   (1, 'alice post 1', 'content', 1, NULL),
                                                                   (2, 'alice post 2', 'content', 1, NULL),
                                                                   (3, 'bob post 1',   'content', 2, NULL);
