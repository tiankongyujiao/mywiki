import request from './request'

export interface Category {
  id: string
  name: string
  createdAt?: unknown
  updatedAt?: unknown
  articleCount?: number
}

export interface CategoryPayload {
  name: string
}

export interface CategoryPageQuery {
  page: number
  size: number
  keyword?: string
  sortField?: string
  sortOrder?: string
}

export interface CategoryPage {
  records: Category[]
  total: string | number
  size: string | number
  current: string | number
}

export const categoryApi = {
  list: () => request.get<unknown, Category[]>('/categories'),
  page: (params: CategoryPageQuery) =>
    request.get<unknown, CategoryPage>('/categories/page', { params }),
  create: (data: CategoryPayload) =>
    request.post<unknown, void>('/categories', data),
  update: (id: number | string, data: CategoryPayload) =>
    request.put<unknown, void>(`/categories/${id}`, data),
  remove: (id: number | string) =>
    request.delete<unknown, void>(`/categories/${id}`),
}
