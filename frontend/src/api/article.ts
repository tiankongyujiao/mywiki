import request from './request'

export interface Article {
  id: string
  title: string
  content: string
  userId?: string
  username?: string
  createTime?: unknown
  createdAt?: unknown
  updatedAt?: unknown
  // 后端按"当前登录用户是否作者"计算后下发，前端直接用
  canEdit?: boolean
  canDelete?: boolean
}

export interface ArticlePayload {
  title: string
  content: string
}

export interface ArticlePageQuery {
  page: number
  size: number
  keyword?: string
}

export interface ArticlePage {
  records: Article[]
  total: string | number
  size: string | number
  current: string | number
  page: string | number
}

export const articleApi = {
  list: (params: ArticlePageQuery) =>
    request.get<unknown, ArticlePage>('/articles', { params }),
  getById: (id: number | string) =>
    request.get<unknown, Article>(`/articles/${id}`),
  create: (data: ArticlePayload) =>
    request.post<unknown, void>('/articles', data),
  update: (id: number | string, data: ArticlePayload) =>
    request.put<unknown, void>(`/articles/${id}`, data),
  remove: (id: number | string) =>
    request.delete<unknown, void>(`/articles/${id}`),
}
