import request from './request'

export interface UserRegisterPayload {
  username: string
  password: string
}

export interface UserLoginPayload {
  username: string
  password: string
}

export interface UserLoginResponse {
  id: number
  username: string,
  token: string,
}

export const userApi = {
  register: (data: UserRegisterPayload) =>
    request.post<unknown, void>('/users/register', data),
  login: (data: UserLoginPayload) =>
    request.post<unknown, UserLoginResponse>('/users/login', data),
}
