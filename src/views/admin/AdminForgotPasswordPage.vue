<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const BASE_URL = (import.meta.env.VITE_API_URL || '').replace(/\/+$/, '')

const router = useRouter()
const email = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  success.value = ''
  if (!email.value.trim()) {
    error.value = 'Ingresá tu email.'
    return
  }
  if (!BASE_URL) {
    error.value = 'Backend no configurado.'
    return
  }
  loading.value = true
  try {
    const res = await fetch(`${BASE_URL}/auth/forgot-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value.trim() }),
    })
    const data = await res.json().catch(() => ({}))
    if (!res.ok) {
      error.value = data.error || 'Error al enviar el correo.'
      return
    }
    success.value = (data.message || 'Si ese email está registrado, recibirás un enlace.') + ' Revisá la bandeja de entrada y la carpeta de spam. Si no llega, puede que ese email no esté registrado como admin o que el correo no esté configurado en el servidor (SMTP_USER, SMTP_APP_PASSWORD, FRONTEND_URL).'
  } catch (e) {
    error.value = (e.message || 'Error de conexión.') + ' ¿Está corriendo el backend (Tomcat)?'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100 px-4">
    <div class="w-full max-w-md">
      <div class="bg-white rounded-2xl shadow-soft p-8">
        <h1 class="text-2xl font-bold text-gray-900 mb-2">¿Olvidaste tu contraseña?</h1>
        <p class="text-gray-500 text-sm mb-6">Ingresá tu email y te enviamos un enlace para restablecerla.</p>

        <form @submit.prevent="submit" class="space-y-4">
          <p v-if="error" class="text-sm text-red-600 bg-red-50 p-3 rounded-xl">{{ error }}</p>
          <p v-if="success" class="text-sm text-green-700 bg-green-50 p-3 rounded-xl">{{ success }}</p>
          <div>
            <label for="forgot-email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              id="forgot-email"
              v-model="email"
              type="email"
              autocomplete="email"
              required
              class="w-full rounded-xl border border-gray-300 px-3 py-2.5 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              placeholder="tu@email.com"
            />
          </div>
          <button type="submit" class="btn-primary w-full py-3" :disabled="loading">
            {{ loading ? 'Enviando...' : 'Enviar enlace' }}
          </button>
        </form>

        <p class="mt-6 text-center text-sm text-gray-500">
          <router-link to="/admin/login" class="text-primary-600 hover:underline">Volver al login</router-link>
        </p>
      </div>
    </div>
  </div>
</template>
