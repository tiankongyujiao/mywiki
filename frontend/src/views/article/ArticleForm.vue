<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { articleApi, type ArticlePayload } from '../../api'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => route.name === 'ArticleEdit')

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive<ArticlePayload>({ title: '', content: '' })
const rules: FormRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 50, message: '标题不能超过50个字符', trigger: 'blur' },
  ],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const fetchDetail = async () => {
  const data = await articleApi.getById(route.params.id as string)
  form.title = data.title
  form.content = data.content
}

const backToList = (backFirstPage = false) =>
  router.push({
    path: '/articles',
    query: {
      ...(backFirstPage ? {} : route.query.page ? { page: route.query.page } : {}),
      ...(route.query.size ? { size: route.query.size } : {}),
    },
  })

const onSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  loading.value = true
  try {
    const payload: ArticlePayload = { title: form.title, content: form.content }
    if (isEdit.value) {
      await articleApi.update(route.params.id as string, payload)
      ElMessage.success('修改成功')
      backToList()
    } else {
      await articleApi.create(payload)
      ElMessage.success('新增成功')
      backToList(true)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
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
      <el-form-item label="内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="12"
          placeholder="请输入内容"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit">保存</el-button>
        <el-button @click="backToList">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>
