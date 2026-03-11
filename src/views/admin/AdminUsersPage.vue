<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const authStore = useAuthStore()
const BASE_URL = API_BASE_URL
// Si el backend está en /inmobiliaria-api, usar esta URL cuando la principal dé 404
const BASE_URL_ALT = BASE_URL.includes('/inmobiliaria-api/')
  ? BASE_URL
  : BASE_URL.replace(/\/api\/?$/, '') + '/inmobiliaria-api/api'

function getAuthHeaders() {
  const token = authStore.token
  if (token) return { Authorization: `Bearer ${token}` }
  try {
    const raw = localStorage.getItem('inmobiliaria_auth')
    if (!raw) return {}
    const parsed = JSON.parse(raw)
    return parsed?.token ? { Authorization: `Bearer ${parsed.token}` } : {}
  } catch {
    return {}
  }
}

const users = ref([])
const loading = ref(true)
const listError = ref('')
/** URL base que respondió bien (puede ser BASE_URL o BASE_URL_ALT tras un 404) */
const effectiveBase = ref('')

const newEmail = ref('')
const newPassword = ref('')
const newPasswordConfirm = ref('')
const formError = ref('')
const formSuccess = ref('')
const submitting = ref(false)

function validatePassword(p) {
  if (!p || p.length < 8) return 'La contraseña debe tener al menos 8 caracteres.'
  if (!/[A-Z]/.test(p)) return 'Debe incluir al menos una mayúscula.'
  if (!/[0-9]/.test(p)) return 'Debe incluir al menos un número.'
  if (!/[^A-Za-z0-9]/.test(p)) return 'Debe incluir al menos un símbolo (ej. !@#$%&*).'
  return null
}

async function fetchUsers(base) {
  const res = await fetch(`${base}/admin/users`, { headers: getAuthHeaders() })
  const data = await res.json().catch(() => ({}))
  return { res, data }
}

async function loadUsers() {
  if (!BASE_URL) {
    listError.value = 'Backend no configurado.'
    loading.value = false
    return
  }
  listError.value = ''
  loading.value = true
  try {
    let { res, data } = await fetchUsers(BASE_URL)
    let baseUsed = BASE_URL
    // Si 404, probar con contexto /inmobiliaria-api (despliegue típico en Tomcat/NetBeans)
    if (res.status === 404 && BASE_URL_ALT !== BASE_URL) {
      const alt = await fetchUsers(BASE_URL_ALT)
      if (alt.res.ok) {
        res = alt.res
        data = alt.data
        baseUsed = BASE_URL_ALT
      }
    }
    if (!res.ok) {
      if (res.status === 401) {
        authStore.logout()
        router.push({ name: 'AdminLogin', query: { msg: 'sesion_expirada' } })
        return
      }
      listError.value = data.error || (res.status === 404
        ? 'Ruta no encontrada (404). Probá en .env: VITE_API_URL=http://localhost:8080/inmobiliaria-api/api y reiniciá npm run dev.'
        : 'No se pudieron cargar los administradores.')
      users.value = []
      return
    }
    effectiveBase.value = baseUsed
    users.value = data.users || []
  } catch (e) {
    listError.value = e.message || 'Error de conexión.'
    users.value = []
  } finally {
    loading.value = false
  }
}

async function addAdmin() {
  formError.value = ''
  formSuccess.value = ''
  const emailTrim = newEmail.value.trim()
  if (!emailTrim) {
    formError.value = 'El email es obligatorio.'
    return
  }
  const pwdError = validatePassword(newPassword.value)
  if (pwdError) {
    formError.value = pwdError
    return
  }
  if (newPassword.value !== newPasswordConfirm.value) {
    formError.value = 'Las contraseñas no coinciden.'
    return
  }
  if (!BASE_URL) {
    formError.value = 'Backend no configurado.'
    return
  }
  submitting.value = true
  const base = effectiveBase.value || BASE_URL
  try {
    const res = await fetch(`${base}/admin/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify({ email: emailTrim, password: newPassword.value }),
    })
    const data = await res.json().catch(() => ({}))
    if (!res.ok) {
      if (res.status === 401) {
        authStore.logout()
        router.push({ name: 'AdminLogin', query: { msg: 'sesion_expirada' } })
        return
      }
      formError.value = data.error || `No se pudo crear (${res.status}). Revisá que el backend esté en marcha y la URL en .env sea correcta.`
      return
    }
    formSuccess.value = data.message || 'Administrador creado.'
    newEmail.value = ''
    newPassword.value = ''
    newPasswordConfirm.value = ''
    await loadUsers()
  } catch (e) {
    formError.value = e.message || 'Error de conexión.'
  } finally {
    submitting.value = false
  }
}

onMounted(loadUsers)
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6">Administradores</h1>

    <div class="bg-white rounded-2xl shadow-card p-6 mb-6 max-w-2xl">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">Agregar administrador</h2>
      <form @submit.prevent="addAdmin" class="space-y-4">
        <p v-if="formError" class="text-sm text-red-600 bg-red-50 p-3 rounded-xl">{{ formError }}</p>
        <p v-if="formSuccess" class="text-sm text-green-700 bg-green-50 p-3 rounded-xl">{{ formSuccess }}</p>
        <div>
          <label for="new-admin-email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input
            id="new-admin-email"
            v-model="newEmail"
            type="email"
            autocomplete="email"
            required
            class="w-full rounded-xl border border-gray-300 px-3 py-2.5 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            placeholder="nuevo@email.com"
          />
        </div>
        <div>
          <label for="new-admin-password" class="block text-sm font-medium text-gray-700 mb-1">Contraseña</label>
          <input
            id="new-admin-password"
            v-model="newPassword"
            type="password"
            autocomplete="new-password"
            required
            minlength="8"
            class="w-full rounded-xl border border-gray-300 px-3 py-2.5 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
          />
          <p class="text-xs text-gray-500 mt-1">Mínimo 8 caracteres, una mayúscula, un número y un símbolo.</p>
        </div>
        <div>
          <label for="new-admin-confirm" class="block text-sm font-medium text-gray-700 mb-1">Confirmar contraseña</label>
          <input
            id="new-admin-confirm"
            v-model="newPasswordConfirm"
            type="password"
            autocomplete="new-password"
            required
            minlength="8"
            class="w-full rounded-xl border border-gray-300 px-3 py-2.5 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
          />
        </div>
        <button type="submit" class="btn-primary" :disabled="submitting">
          {{ submitting ? 'Guardando...' : 'Agregar administrador' }}
        </button>
      </form>
    </div>

    <div class="bg-white rounded-2xl shadow-card overflow-hidden max-w-2xl">
      <h2 class="text-lg font-semibold text-gray-900 px-6 py-4 border-b border-gray-200">Administradores actuales</h2>
      <p v-if="loading" class="px-6 py-4 text-gray-500">Cargando...</p>
      <p v-else-if="listError" class="px-6 py-4 text-red-600">{{ listError }}</p>
      <div v-else-if="users.length === 0" class="px-6 py-8 text-gray-500 text-center">No hay administradores cargados.</div>
      <table v-else class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
            <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Rol</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="u in users" :key="u.id">
            <td class="px-6 py-3 text-sm text-gray-900">{{ u.email }}</td>
            <td class="px-6 py-3 text-sm text-gray-600">admin</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
