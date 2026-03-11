import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { API_BASE_URL } from '../config/api'

const STORAGE_KEY = 'inmobiliaria_auth'
const BASE_URL = API_BASE_URL

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(null)

  const isAuthenticated = computed(() => !!token.value && !!user.value)

  function initFromStorage() {
    try {
      const stored = localStorage.getItem(STORAGE_KEY)
      if (stored) {
        const { user: u, token: t } = JSON.parse(stored)
        if (u && t) {
          user.value = u
          token.value = t
        }
      }
    } catch {
      logout()
    }
  }

  async function login(email, password, turnstileToken = '') {
    if (!BASE_URL) {
      return { success: false, message: 'Backend no configurado (VITE_API_URL)' }
    }
    try {
      const body = { email: (email || '').trim(), password: password || '' }
      if (turnstileToken) body.turnstileToken = turnstileToken
      const res = await fetch(`${BASE_URL}/admin/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      const data = await res.json().catch(() => ({}))
      if (!res.ok) {
        return { success: false, message: data.error || 'Email o contraseña incorrectos' }
      }
      const t = data.token
      const u = data.user ? { email: data.user.email } : { email: (email || '').trim() }
      if (!t) {
        return { success: false, message: 'El servidor no devolvió un token' }
      }
      user.value = u
      token.value = t
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ user: user.value, token: token.value }))
      return { success: true }
    } catch (e) {
      return { success: false, message: e.message || 'Error de conexión' }
    }
  }

  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem(STORAGE_KEY)
  }

  return {
    user,
    token,
    isAuthenticated,
    initFromStorage,
    login,
    logout,
  }
})
