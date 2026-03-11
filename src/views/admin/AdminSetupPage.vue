<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import { API_BASE_URL } from '../../config/api'
const BASE_URL = API_BASE_URL

const router = useRouter()
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)

function validatePassword(p) {
  if (!p || p.length < 8) return 'La contraseña debe tener al menos 8 caracteres.'
  if (!/[A-Z]/.test(p)) return 'Debe incluir al menos una mayúscula.'
  if (!/[0-9]/.test(p)) return 'Debe incluir al menos un número.'
  if (!/[^A-Za-z0-9]/.test(p)) return 'Debe incluir al menos un símbolo (ej. !@#$%&*).'
  return null
}

async function submit() {
  error.value = ''
  success.value = ''
  const emailTrim = email.value.trim()
  if (!emailTrim) {
    error.value = 'El email es obligatorio.'
    return
  }
  const pwdError = validatePassword(password.value)
  if (pwdError) {
    error.value = pwdError
    return
  }
  if (password.value !== confirmPassword.value) {
    error.value = 'Las contraseñas no coinciden.'
    return
  }
  if (!BASE_URL) {
    error.value = 'Backend no configurado.'
    return
  }
  loading.value = true
  try {
    const res = await fetch(`${BASE_URL}/auth/setup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: emailTrim, password: password.value }),
    })
    const data = await res.json().catch(() => ({}))
    if (!res.ok) {
      error.value = data.error || 'No se pudo crear la cuenta.'
      return
    }
    success.value = data.message || 'Cuenta creada. Redirigiendo al login...'
    setTimeout(() => router.push({ name: 'AdminLogin' }), 1500)
  } catch (e) {
    error.value = e.message || 'Error de conexión.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100 px-4">
    <div class="w-full max-w-md">
      <div class="bg-white rounded-2xl shadow-soft p-8">
        <h1 class="text-2xl font-bold text-gray-900 mb-2">Crear cuenta de administrador</h1>
        <p class="text-gray-500 text-sm mb-6">Solo podés crear una cuenta si aún no hay ningún administrador (por ejemplo, la primera vez con MySQL).</p>

        <form @submit.prevent="submit" class="space-y-4">
          <p v-if="error" class="text-sm text-red-600 bg-red-50 p-3 rounded-xl">{{ error }}</p>
          <p v-if="success" class="text-sm text-green-700 bg-green-50 p-3 rounded-xl">{{ success }}</p>
          <div>
            <label for="setup-email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              id="setup-email"
              v-model="email"
              type="email"
              autocomplete="email"
              required
              class="w-full rounded-xl border border-gray-300 px-3 py-2.5 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              placeholder="tu@email.com"
            />
          </div>
          <div>
            <label for="setup-password" class="block text-sm font-medium text-gray-700 mb-1">Contraseña</label>
            <input
              id="setup-password"
              v-model="password"
              type="password"
              autocomplete="new-password"
              required
              minlength="8"
              class="w-full rounded-xl border border-gray-300 px-3 py-2.5 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            />
            <p class="text-xs text-gray-500 mt-1">Mínimo 8 caracteres, una mayúscula, un número y un símbolo.</p>
          </div>
          <div>
            <label for="setup-confirm" class="block text-sm font-medium text-gray-700 mb-1">Confirmar contraseña</label>
            <input
              id="setup-confirm"
              v-model="confirmPassword"
              type="password"
              autocomplete="new-password"
              required
              minlength="8"
              class="w-full rounded-xl border border-gray-300 px-3 py-2.5 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            />
          </div>
          <button type="submit" class="btn-primary w-full py-3" :disabled="loading || !!success">
            {{ loading ? 'Creando...' : 'Crear cuenta' }}
          </button>
        </form>

        <p class="mt-6 text-center text-sm text-gray-500">
          <router-link to="/admin/login" class="text-primary-600 hover:underline">Volver al login</router-link>
        </p>
      </div>
    </div>
  </div>
</template>
