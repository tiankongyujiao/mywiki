import request from './request'

export interface Category {
  id: string
  name: string
  createdAt?: unknown
  updatedAt?: unknown
}

export interface CategoryPayload {
  name: string
}

export const categoryApi = {
  list: () => request.get<unknown, Category[]>('/categories'),
  create: (data: CategoryPayload) =>
    request.post<unknown, void>('/categories', data),
  update: (id: number | string, data: CategoryPayload) =>
    request.put<unknown, void>(`/categories/${id}`, data),
  remove: (id: number | string) =>
    request.delete<unknown, void>(`/categories/${id}`),
}
