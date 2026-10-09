import request from './request'

export interface Tag {
  id: string
  name: string
  // 引用该标签的文章数量（标签云用）
  articleCount?: number
  createdAt?: unknown
  updatedAt?: unknown
}

export interface TagPayload {
  name: string
}

export interface TagPageQuery {
  page: number
  size: number
  keyword?: string
  sortField?: string
  sortOrder?: string
}

export interface TagPage {
  records: Tag[]
  total: string | number
  size: string | number
  current: string | number
}

export const tagApi = {
  // 标签云：所有标签及引用文章数，按文章数倒序（公开）
  list: () => request.get<unknown, Tag[]>('/tags'),
  // 标签管理分页（公开读取，增删改需 ADMIN）
  page: (params: TagPageQuery) =>
    request.get<unknown, TagPage>('/tags/page', { params }),
  create: (data: TagPayload) => request.post<unknown, void>('/tags', data),
  update: (id: number | string, data: TagPayload) =>
    request.put<unknown, void>(`/tags/${id}`, data),
  remove: (id: number | string) =>
    request.delete<unknown, void>(`/tags/${id}`),
}
