<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, View, Edit, Delete } from '@element-plus/icons-vue'
import { articleApi, type Article } from '../../api'

const router = useRouter()
const loading = ref(false)
const list = ref<Article[]>([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return list.value
  return list.value.filter((a) => a.title?.toLowerCase().includes(kw))
})
const total = computed(() => filtered.value.length)
const paged = computed(() =>
  filtered.value.slice(
    (currentPage.value - 1) * pageSize.value,
    currentPage.value * pageSize.value,
  ),
)

const fetchList = async () => {
  loading.value = true
  try {
    list.value = await articleApi.list()
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row: Article) => {
  await ElMessageBox.confirm(`确定删除文章「${row.title}」吗？`, '提示', {
    type: 'warning',
  })
  await articleApi.remove(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

const handleAdd = () => router.push('/articles/create')
const handleEdit = (row: Article) => router.push(`/articles/${row.id}/edit`)
const handleView = (row: Article) => router.push(`/articles/${row.id}`)

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

onMounted(fetchList)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="按标题搜索"
        clearable
        :prefix-icon="Search"
        style="width: 240px"
      />
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增文章</el-button>
    </div>

    <el-table :data="paged" v-loading="loading" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="View" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
