#!/bin/bash
  # e2e 测试库重置：删库重建 + 建表 + 插种子数据
  # 连接方式自动二选一：
  #   - 本机有 mysql 客户端（CI）：TCP 连 127.0.0.1:3306，密码读 MYSQL_PWD 环境变量
  #   - 没有（本地开发）：docker exec 进 mysql8 容器
  # --default-character-set 必须指定，否则导入 UTF-8 文件中文会双重编码
  set -e
  export MYSQL_PWD="${MYSQL_PWD:-123456}"

  if command -v mysql >/dev/null 2>&1; then
    MYSQL=(mysql -h127.0.0.1 -P3306 -uroot --default-character-set=utf8mb4)
  else
    MYSQL=(docker exec -i -e MYSQL_PWD="$MYSQL_PWD" mysql8 mysql -uroot --default-character-set=utf8mb4)
  fi

  SQL_DIR="$(cd "$(dirname "$0")" && pwd)"

  "${MYSQL[@]}" -e "DROP DATABASE IF EXISTS mywiki_test; CREATE DATABASE mywiki_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
  "${MYSQL[@]}" mywiki_test < "$SQL_DIR/schema.sql"
  "${MYSQL[@]}" mywiki_test < "$SQL_DIR/seed.sql"

  echo "mywiki_test 已重置"
