import request from './request'

export interface Article {
  id: string
  title: string
  content: string
  userId?: string
  username?: string
  categoryId?: string | null
  categoryName?: string | null
  tags?: string[]
  createTime?: unknown
  createdAt?: unknown
  updatedAt?: unknown
  // 后端按"当前登录用户是否作者"计算后下发，前端直接用
  canEdit?: boolean
  canDelete?: boolean
  status?: 'DRAFT' | 'PUBLISHED'
}

export interface ArticlePayload {
  title: string
  content: string
  categoryId?: number | string | null
  tags?: string[]
  status?: 'DRAFT' | 'PUBLISHED'
}

export interface ArticlePageQuery {
  page: number
  size: number
  keyword?: string
  categoryId?: number | string
  tagId?: number | string
  mine?: boolean
}

export interface ArticlePage {
  records: Article[]
  total: string | number
  size: string | number
  current: string | number
  page: string | number
}

export interface Comment {
  id: string
  articleId: string
  userId: string
  username?: string
  content: string
  createdAt?: unknown
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
  // 查询文章评论（公开）
  listComments: (articleId: number | string) =>
    request.get<unknown, Comment[]>(`/articles/${articleId}/comments`),
  // 发表评论（需登录，token 由 request 拦截器自动带上）
  createComment: (articleId: number | string, data: { content: string }) =>
    request.post<unknown, void>(`/articles/${articleId}/comments`, data),
  // 删除评论（需登录，评论作者或文章作者可删，token 由 request 拦截器自动带上）
  deleteComment: (articleId: number | string, commentId: number | string) =>
    request.delete<unknown, void>(`/articles/${articleId}/comments/${commentId}`),
}
