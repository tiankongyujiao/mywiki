<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Document } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() =>
  route.path.startsWith('/articles') ? '/articles' : route.path,
)
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">MyWiki 管理后台</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#b7c0cd"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/articles">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ route.meta.title || '首页' }}</span>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background: #001529;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  border-bottom: 1px solid #1f2d3d;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.header {
  background: #fff;
  border-bottom: 1px solid #e5e4e7;
  display: flex;
  align-items: center;
  height: 60px;
}
.header .title {
  font-size: 18px;
  font-weight: 600;
  color: #08060d;
}
.main {
  background: #f5f6f8;
  padding: 20px;
}
</style>
