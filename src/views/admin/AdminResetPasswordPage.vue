<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const BASE_URL = (import.meta.env.VITE_API_URL || '').replace(/\/+$/, '')

const router = useRouter()
const route = useRoute()
const token = computed(() => route.query.token || '')
const newPassword = ref('')
const confirmPassword = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)

/** Reglas: mínimo 8 caracteres, al menos una mayúscula, un número y un símbolo. */
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
  if (!token.value) {
    error.value = 'Falta el enlace de recuperación. Solicitá uno nuevo desde "¿Olvidaste tu contraseña?"'
    return
  }
  const pwdError = validatePassword(newPassword.value)
  if (pwdError) {
    error.value = pwdError
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    error.value = 'Las contraseñas no coinciden.'
    return
  }
  if (!BASE_URL) {
    error.value = 'Backend no configurado.'
    return
  }
  loading.value = true
  try {
    const res = await fetch(`${BASE_URL}/auth/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token: token.value, newPassword: newPassword.value }),
    })
    const data = await res.json().catch(() => ({}))
    if (!res.ok) {
      error.value = data.error || 'No se pudo restablecer la contraseña.'
      return
    }
    success.value = data.message || 'Contraseña actualizada. Ya podés iniciar sesión.'
    setTimeout(() => router.push({ name: 'AdminLogin' }), 2000)
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
        <h1 class="text-2xl font-bold text-gray-900 mb-2">Nueva contraseña</h1>
        <p class="text-gray-500 text-sm mb-6">Mínimo 8 caracteres, una mayúscula, un número y un símbolo.</p>

        <form v-if="token" @submit.prevent="submit" class="space-y-4">
          <p v-if="error" class="text-sm text-red-600 bg-red-50 p-3 rounded-xl">{{ error }}</p>
          <p v-if="success" class="text-sm text-green-700 bg-green-50 p-3 rounded-xl">{{ success }}</p>
          <div>
            <label for="new-password" class="block text-sm font-medium text-gray-700 mb-1">Nueva contraseña</label>
            <input
              id="new-password"
              v-model="newPassword"
              type="password"
              autocomplete="new-password"
              required
              minlength="8"
              class="w-full rounded-xl border border-gray-300 px-3 py-2.5 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            />
          </div>
          <div>
            <label for="confirm-password" class="block text-sm font-medium text-gray-700 mb-1">Confirmar contraseña</label>
            <input
              id="confirm-password"
              v-model="confirmPassword"
              type="password"
              autocomplete="new-password"
              required
              minlength="8"
              class="w-full rounded-xl border border-gray-300 px-3 py-2.5 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            />
          </div>
          <button type="submit" class="btn-primary w-full py-3" :disabled="loading || !!success">
            {{ loading ? 'Guardando...' : 'Guardar contraseña' }}
          </button>
        </form>

        <template v-else>
          <p class="text-sm text-red-600 bg-red-50 p-3 rounded-xl">Falta el enlace de recuperación. Andá al login y usá "¿Olvidaste tu contraseña?".</p>
          <p class="mt-4 text-center">
            <router-link to="/admin/login" class="text-primary-600 hover:underline">Ir al login</router-link>
          </p>
        </template>

        <p class="mt-6 text-center text-sm text-gray-500">
          <router-link to="/admin/login" class="text-primary-600 hover:underline">Volver al login</router-link>
        </p>
      </div>
    </div>
  </div>
</template>
