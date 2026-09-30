<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { articleApi, type Article, type Comment } from '../../api'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const article = ref<Article>()

// 评论
const comments = ref<Comment[]>([])
const commentContent = ref('')
const submitting = ref(false)

const fetchComments = async () => {
  comments.value = await articleApi.listComments(route.params.id as string)
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  submitting.value = true
  try {
    await articleApi.createComment(route.params.id as string, {
      content: commentContent.value,
    })
    commentContent.value = ''
    ElMessage.success('评论成功')
    await fetchComments()
  } finally {
    submitting.value = false
  }
}

const goLogin = () => {
  router.push({
    path: '/login',
    query: { redirect: `/articles/${route.params.id}` },
  })
}

// 是否有权限删除该评论：评论作者或文章作者
const canDeleteComment = (comment: Comment): boolean => {
  if (!authStore.isLogin || !authStore.user) return false
  const uid = String(authStore.user.id)
  return String(comment.userId) === uid || String(article.value?.userId) === uid
}

const handleDeleteComment = async (comment: Comment) => {
  await ElMessageBox.confirm(`确定删除该评论吗？`, '提示', {
    type: 'warning',
  })
  // 无权限时后端返回 403，request 拦截器会统一提示
  await articleApi.deleteComment(route.params.id as string, comment.id)
  ElMessage.success('删除成功')
  await fetchComments()
}

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

onMounted(() => {
  fetchDetail()
  fetchComments()
})
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
      <el-descriptions-item label="分类">
        {{ article?.categoryName || '-' }}
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

  <el-card shadow="never" class="comment-card">
    <template #header>评论</template>

    <!-- 未登录：引导登录后发表评论 -->
    <div v-if="!authStore.isLogin" class="comment-login">
      <el-button type="primary" plain @click="goLogin">登录后发表评论</el-button>
    </div>

    <!-- 已登录：发表评论 -->
    <div v-else class="comment-form">
      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="3"
        maxlength="1000"
        show-word-limit
        placeholder="写下你的评论..."
      />
      <div class="comment-submit">
        <el-button type="primary" :loading="submitting" @click="submitComment">
          发表评论
        </el-button>
      </div>
    </div>

    <el-empty v-if="!comments.length" description="暂无评论" />
    <div v-else class="comment-list">
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-meta">
          <span class="comment-user">{{ c.username || '匿名' }}</span>
          <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
          <el-button
            v-if="canDeleteComment(c)"
            type="danger"
            link
            size="small"
            @click="handleDeleteComment(c)"
          >
            删除
          </el-button>
        </div>
        <div class="comment-content">{{ c.content }}</div>
      </div>
    </div>
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
.comment-card {
  margin-top: 16px;
}
.comment-login {
  margin-bottom: 12px;
}
.comment-submit {
  margin-top: 8px;
  text-align: right;
}
.comment-list {
  margin-top: 8px;
}
.comment-item {
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
}
.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.comment-user {
  font-weight: 600;
}
.comment-time {
  font-size: 12px;
  color: #999;
}
.comment-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}
</style>
