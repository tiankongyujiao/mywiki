<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { categoryApi, type Category, type CategoryPayload } from '../../api'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
// 只有管理员才能新增/修改/删除分类，按钮按角色显示
const isAdmin = computed(() => authStore.user?.role === 'ADMIN')

const loading = ref(false)
const records = ref<Category[]>([])
const total = ref(0)
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const sortField = ref<string | undefined>(undefined)
const sortOrder = ref<string | undefined>(undefined)

const fetchList = async () => {
  loading.value = true
  try {
    const data = await categoryApi.page({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      sortField: sortField.value,
      sortOrder: sortOrder.value,
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

// el-table 自定义排序：prop 对应后端排序字段，order 为 ascending/descending/null
const handleSortChange = ({ prop, order }: { prop: string; order: string | null }) => {
  if (!order) {
    sortField.value = undefined
    sortOrder.value = undefined
  } else {
    sortField.value = prop
    sortOrder.value = order === 'ascending' ? 'asc' : 'desc'
  }
  handleSearch()
}

watch([currentPage, pageSize], () => fetchList())

// 新增 / 编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<CategoryPayload>({ name: '' })
const rules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 100, message: '分类名称不能超过100个字符', trigger: 'blur' },
  ],
}

const openCreate = () => {
  editingId.value = null
  dialogTitle.value = '新增分类'
  form.name = ''
  dialogVisible.value = true
}

const openEdit = (row: Category) => {
  editingId.value = row.id
  dialogTitle.value = '编辑分类'
  form.name = row.name
  dialogVisible.value = true
}

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload: CategoryPayload = { name: form.name }
    if (editingId.value) {
      await categoryApi.update(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await categoryApi.create(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: Category) => {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', {
    type: 'warning',
  })
  await categoryApi.remove(row.id)
  ElMessage.success('删除成功')
  fetchList()
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

onMounted(fetchList)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="按分类名称搜索"
        clearable
        :prefix-icon="Search"
        style="width: 240px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      />
      <el-button v-if="isAdmin" type="primary" :icon="Plus" @click="openCreate">新增分类</el-button>
    </div>

    <el-table
      :data="records"
      v-loading="loading"
      border
      stripe
      style="width: 100%"
      @sort-change="handleSortChange"
    >
      <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
      <el-table-column prop="name" label="分类名称" min-width="200" show-overflow-tooltip sortable="custom" />
      <el-table-column prop="articleCount" label="文章数量" width="120" sortable="custom" />
      <el-table-column prop="createdAt" label="创建时间" width="180" sortable="custom">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="180" sortable="custom">
        <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column v-if="isAdmin" label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="form.name"
            :maxlength="100"
            show-word-limit
            placeholder="请输入分类名称"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
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
