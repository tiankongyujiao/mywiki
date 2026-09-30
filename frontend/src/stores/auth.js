import { defineStore } from 'pinia'
import { userApi } from '../api/user'

/**
 * @typedef {Object} UserInfo
 * @property {string} id
 * @property {string} username
 * @property {string} [role]
 */

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token'),
    /** @type {UserInfo | null} */
    user: null
  }),

  getters: {
    isLogin: (state) => !!state.token
  },

  actions: {

    setLogin(data) {
      this.token = data.token
      this.user = data.user || null

      localStorage.setItem(
        'token',
        data.token
      )
    },

    logout() {
      this.token = null
      this.user = null

      localStorage.removeItem('token')
    },

    async fetchUser() {
      if (!this.token) {
        return
      }
      try {
        const result = await userApi.getCurrentUser()
        this.user = result
      } catch (error) {
        this.logout()
        throw error
      }
    }
  }
})