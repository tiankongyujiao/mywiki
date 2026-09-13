<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { articleApi, type Article } from '../../api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const article = ref<Article>()

const backToList = () => router.push('/articles')

const goEdit = () => router.push(`/articles/${article.value?.id}/edit`)

const handleDelete = async () => {
  if (!article.value) return
  await ElMessageBox.confirm(`确定删除文章「${article.value.title}」吗？`, '提示', {
    type: 'warning',
  })
  // 无权限时后端返回 403，request 拦截器会统一提示
  await articleApi.remove(article.value.id)
  ElMessage.success('删除成功')
  router.push('/articles')
}

const fetchDetail = async () => {
  loading.value = true
  try {
    article.value = await articleApi.getById(route.params.id as string)
  } finally {
    loading.value = false
  }
}

const formatTime = (t: unknown): string => {
  if (!t) return '-'
  if (Array.isArray(t)) {
    const [y, mo, d, h = 0, mi = 0, s = 0] = t as number[]
    const dt = new Date(y, (mo ?? 1) - 1, d, h, mi, s)
    return Number.isNaN(dt.getTime()) ? String(t) : dt.toLocaleString('zh-CN')
  }
  const dt = new Date(t as string)
  return Number.isNaN(dt.getTime()) ? String(t) : dt.toLocaleString('zh-CN')
}

onMounted(fetchDetail)
</script>

<template>
  <el-card v-loading="loading" shadow="never">
    <template #header>
      <div class="header">
        <span class="title">{{ article?.title }}</span>
        <div>
          <el-button v-if="article?.canEdit" type="primary" @click="goEdit">编辑</el-button>
          <el-button v-if="article?.canDelete" type="danger" @click="handleDelete">删除</el-button>
          <el-button @click="backToList">返回列表</el-button>
        </div>
      </div>
    </template>

    <el-descriptions :column="2" border>
      <el-descriptions-item label="作者">
        {{ article?.username || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ formatTime(article?.createdAt) }}
      </el-descriptions-item>
      <el-descriptions-item label="更新时间" :span="2">
        {{ formatTime(article?.updatedAt) }}
      </el-descriptions-item>
      <el-descriptions-item label="内容" :span="2">
        <div class="content">{{ article?.content }}</div>
      </el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header .title {
  font-size: 16px;
  font-weight: 600;
}
.content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}
</style>
