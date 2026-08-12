import request from './request'

export interface Article {
  id: string
  title: string
  content: string
  userId?: string
  createTime?: unknown
}

export interface ArticlePayload {
  title: string
  content: string
}

export const articleApi = {
  list: () => request.get<unknown, Article[]>('/articles'),
  getById: (id: number | string) =>
    request.get<unknown, Article>(`/articles/${id}`),
  create: (data: ArticlePayload) =>
    request.post<unknown, void>('/articles', data),
  update: (id: number | string, data: ArticlePayload) =>
    request.put<unknown, void>(`/articles/${id}`, data),
  remove: (id: number | string) =>
    request.delete<unknown, void>(`/articles/${id}`),
}
