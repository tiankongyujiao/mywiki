<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, View, Edit, Delete } from '@element-plus/icons-vue'
import { articleApi, categoryApi, tagApi, type Article, type Category, type Tag } from '../../api'
import { articlePager } from './pager'

const router = useRouter()
const loading = ref(false)
const records = ref<Article[]>([])
const total = ref(0)
const keyword = ref('')
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const selectedCategoryId = ref<string | number | undefined>(undefined)
const selectedTagId = ref<string | number | undefined>(undefined)
const currentPage = ref(articlePager.page)
const pageSize = ref(articlePager.size)

const fetchCategories = async () => {
  categories.value = await categoryApi.list()
}

const fetchTags = async () => {
  tags.value = await tagApi.list()
}
const onlyMine = ref(false)
const fetchList = async () => {
  loading.value = true
  try {
    const data = await articleApi.list({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      categoryId: selectedCategoryId.value ?? undefined,
      tagId: selectedTagId.value ?? undefined,
      mine: onlyMine.value || undefined,
    })
    records.value = data.records
    total.value = Number(data.total) || 0
    // 当前页被删空时回退一页（watch 会触发重新请求）
    if (!records.value.length && currentPage.value > 1) {
      currentPage.value -= 1
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (currentPage.value === 1) {
    fetchList()
  } else {
    currentPage.value = 1
  }
}

watch([currentPage, pageSize], ([page, size]) => {
  articlePager.page = page
  articlePager.size = size
  fetchList()
})

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

onMounted(() => {
  fetchCategories()
  fetchTags()
  fetchList()
})
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="按标题搜索"
          clearable
          :prefix-icon="Search"
          style="width: 240px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select
          v-model="selectedCategoryId"
          placeholder="按分类筛选"
          clearable
          style="width: 200px"
          @change="handleSearch"
        >
          <el-option
            v-for="c in categories"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          />
        </el-select>
        <el-select
          v-model="selectedTagId"
          placeholder="按标签筛选"
          clearable
          filterable
          style="width: 200px"
          @change="handleSearch"
        >
          <el-option
            v-for="t in tags"
            :key="t.id"
            :label="`${t.name} (${t.articleCount ?? 0})`"
            :value="t.id"
          />
        </el-select>
        <el-checkbox v-model="onlyMine" @change="handleSearch">我的文章</el-checkbox>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增文章</el-button>
    </div>

    <el-table :data="records" v-loading="loading" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="分类" width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.categoryName || '-' }}</template>
      </el-table-column>
      <el-table-column label="标签" min-width="180">
        <template #default="{ row }">
          <el-tag
            v-for="t in row.tags || []"
            :key="t"
            size="small"
            style="margin-right: 6px"
          >
            {{ t }}
          </el-tag>
          <span v-if="!row.tags || !row.tags.length">-</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="View" @click="handleView(row)">查看</el-button>
          <el-button v-if="row.canEdit" link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.canDelete" link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
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
.filters {
  display: flex;
  gap: 12px;
  align-items: center;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
