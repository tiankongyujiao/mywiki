<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { articleApi, categoryApi, tagApi, type ArticlePayload, type Category, type Tag } from '../../api'
import { articlePager } from './pager'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => route.name === 'ArticleEdit')
const originalStatus = ref<'DRAFT' | 'PUBLISHED' | null>(null)

const formRef = ref<FormInstance>()
const loading = ref(false)
const categories = ref<Category[]>([])
const tags = ref<Tag[]>([])
const form = reactive<ArticlePayload>({ title: '', content: '', categoryId: null, tags: [] })
const rules: FormRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 50, message: '标题不能超过50个字符', trigger: 'blur' },
  ],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const fetchCategories = async () => {
  categories.value = await categoryApi.list()
}

const fetchTags = async () => {
  tags.value = await tagApi.list()
}

const fetchDetail = async () => {
  const data = await articleApi.getById(route.params.id as string)
  form.title = data.title
  form.content = data.content
  form.categoryId = data.categoryId ?? null
  form.tags = data.tags ?? []
  originalStatus.value = data.status ?? null
}

const backToList = () => router.push('/articles')

const onSubmit = async (status: 'DRAFT' | 'PUBLISHED') => {
  if (!formRef.value) return
  await formRef.value.validate()
  loading.value = true
  try {
    const payload: ArticlePayload = {
      title: form.title,
      content: form.content,
      categoryId: form.categoryId ?? null,
      tags: form.tags ?? [],
      status,
    }
    const message =
      status === 'PUBLISHED'
        ? (isEdit.value && originalStatus.value === 'PUBLISHED' ? '修改成功' : '发布成功')
        : '已存草稿'
    if (isEdit.value) {
      await articleApi.update(route.params.id as string, payload)
      ElMessage.success(message)
      backToList()
    } else {
      await articleApi.create(payload)
      ElMessage.success(message)
      articlePager.page = 1
      backToList()
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCategories()
  fetchTags()
  if (isEdit.value) fetchDetail()
})
</script>

<template>
  <el-card shadow="never">
    <template #header>{{ isEdit ? '编辑文章' : '新增文章' }}</template>
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      style="max-width: 800px"
    >
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="form.title"
          :maxlength="50"
          show-word-limit
          placeholder="请输入标题"
        />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select
          v-model="form.categoryId"
          clearable
          placeholder="请选择分类（可选）"
          style="width: 240px"
        >
          <el-option
            v-for="c in categories"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="标签" prop="tags">
        <el-select
          v-model="form.tags"
          multiple
          filterable
          allow-create
          default-first-option
          clearable
          placeholder="请选择或输入标签（可选，回车可创建新标签）"
          style="width: 100%"
        >
          <el-option
            v-for="t in tags"
            :key="t.id"
            :label="t.name"
            :value="t.name"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="12"
          placeholder="请输入内容"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit('PUBLISHED')">
          {{ originalStatus === 'PUBLISHED' ? '保存' : '发布' }}
        </el-button>
        <el-button
          v-if="originalStatus !== 'PUBLISHED'"
          :loading="loading"
          @click="onSubmit('DRAFT')"
        >
          保存草稿
        </el-button>
        <el-button @click="backToList">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>
