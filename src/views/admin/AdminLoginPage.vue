<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const TURNSTILE_SITE_KEY = import.meta.env.VITE_TURNSTILE_SITE_KEY || ''

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const turnstileContainer = ref(null)
let turnstileWidgetId = null

function getTurnstileToken() {
  if (!TURNSTILE_SITE_KEY || typeof window.turnstile === 'undefined') return ''
  const el = document.querySelector('[name="cf-turnstile-response"]')
  return el ? el.value : ''
}

async function submit() {
  error.value = ''
  const emailTrim = email.value.trim()
  if (!emailTrim) {
    error.value = 'El email es obligatorio.'
    return
  }
  if (!password.value) {
    error.value = 'La contraseña es obligatoria.'
    return
  }
  if (TURNSTILE_SITE_KEY && !getTurnstileToken()) {
    error.value = 'Completá la verificación de seguridad antes de continuar.'
    return
  }
  loading.value = true
  try {
    const result = await auth.login(emailTrim, password.value, getTurnstileToken())
    if (result.success) {
      const redirect = route.query.redirect || '/admin'
      router.push(redirect)
    } else {
      error.value = result.message || 'Credenciales incorrectas.'
      if (TURNSTILE_SITE_KEY && window.turnstile && turnstileWidgetId != null) {
        window.turnstile.reset(turnstileWidgetId)
      }
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (route.query.msg === 'sesion_expirada') {
    error.value = 'Tu sesión expiró o no es válida. Volvé a iniciar sesión.'
  }
  if (!TURNSTILE_SITE_KEY) return
  const script = document.createElement('script')
  script.src = 'https://challenges.cloudflare.com/turnstile/v0/api.js'
  script.async = true
  script.defer = true
  script.onload = () => {
    nextTick(() => {
      if (window.turnstile && turnstileContainer.value) {
        turnstileWidgetId = window.turnstile.render(turnstileContainer.value, {
          sitekey: TURNSTILE_SITE_KEY,
          theme: 'light',
        })
      }
    })
  }
  document.head.appendChild(script)
})

onUnmounted(() => {
  if (TURNSTILE_SITE_KEY && turnstileWidgetId != null && window.turnstile) {
    try { window.turnstile.remove(turnstileWidgetId) } catch (_) {}
  }
})
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-emerald-950 px-4 py-8">
    <div class="w-full max-w-md relative mt-6">
      <div class="absolute left-1/2 -top-14 -translate-x-1/2">
        <div class="h-28 w-28 rounded-full bg-white/95 border border-emerald-200 overflow-hidden shadow-2xl">
          <img src="/images/branding/logoinmobiliaria.webp" alt="Logo inmobiliaria" class="h-full w-full object-cover" />
        </div>
      </div>
      <div class="bg-emerald-900 rounded-2xl shadow-soft p-8 pt-16 mt-10">
        <div class="text-center mb-4">
          <h1 class="text-2xl font-bold text-white">Panel administrador</h1>
          <p class="text-emerald-200 text-sm">Iniciá sesión con tu cuenta.</p>
        </div>

        <form @submit.prevent="submit" class="space-y-4">
          <p v-if="error" class="text-sm text-red-600 bg-red-50 p-3 rounded-xl">{{ error }}</p>
          <div>
            <label for="admin-email" class="block text-sm font-medium text-gray-700 mb-1">Correo electrónico</label>
            <input
              id="admin-email"
              v-model="email"
              type="email"
              autocomplete="email"
              required
              class="w-full rounded-xl border border-gray-300 px-3 py-2.5 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              placeholder=""
            />
          </div>
          <div>
            <label for="admin-password" class="block text-sm font-medium text-gray-700 mb-1">Contraseña</label>
            <input
              id="admin-password"
              v-model="password"
              type="password"
              autocomplete="current-password"
              required
              class="w-full rounded-xl border border-gray-300 px-3 py-2.5 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              placeholder=""
            />
          </div>
          <div v-if="TURNSTILE_SITE_KEY" ref="turnstileContainer" class="flex justify-center min-h-[65px]"></div>
          <button type="submit" class="w-full py-3 rounded-xl text-white font-semibold bg-emerald-700 hover:bg-emerald-800 transition" :disabled="loading">
            {{ loading ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>

        <p class="mt-6 text-center text-sm text-gray-500">
          <router-link to="/" class="text-primary-600 hover:underline">Volver al sitio</router-link>
        </p>
      </div>
    </div>
  </div>
</template>
