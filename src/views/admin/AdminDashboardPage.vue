<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { fetchAllProperties } from '../../services/properties'

const router = useRouter()
const route = useRoute()
const stats = ref({ total: 0, active: 0, sold: 0, reserved: 0 })
const loading = ref(true)
const toastMessage = ref('')
let toastTimer = null

function showToast(message) {
  toastMessage.value = message
  if (toastTimer) {
    clearTimeout(toastTimer)
  }
  toastTimer = setTimeout(() => {
    toastMessage.value = ''
    toastTimer = null
  }, 3500)
}

onMounted(async () => {
  const saved = route.query.saved
  if (saved === 'published') {
    showToast('Propiedad cargada correctamente.')
    router.replace({ path: route.path, query: {} })
  } else if (saved === 'created') {
    showToast('Propiedad cargada correctamente.')
    router.replace({ path: route.path, query: {} })
  } else if (saved === 'updated') {
    showToast('Propiedad actualizada correctamente.')
    router.replace({ path: route.path, query: {} })
  }

  try {
    const list = await fetchAllProperties()
    stats.value = {
      total: list.length,
      active: list.filter(p => p.status === 'available').length,
      sold: list.filter(p => p.status === 'sold').length,
      reserved: list.filter(p => p.status === 'reserved').length,
    }
  } finally {
    loading.value = false
  }
})

function goNew() {
  router.push({ name: 'AdminPropertyNew' })
}
</script>

<template>
  <div>
    <div v-if="toastMessage" class="fixed top-6 right-6 z-50 max-w-sm rounded-2xl border border-green-200 bg-white px-4 py-3 text-sm font-medium text-green-800 shadow-[0_20px_45px_rgba(22,101,52,0.18)]">
      <div class="flex items-start gap-3">
        <span class="mt-0.5 flex h-6 w-6 items-center justify-center rounded-full bg-green-100 text-green-700">✓</span>
        <span>{{ toastMessage }}</span>
      </div>
    </div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6">Dashboard</h1>

    <div v-if="loading" class="animate-pulse flex gap-4">
      <div v-for="i in 4" :key="i" class="h-24 flex-1 bg-gray-200 rounded-2xl" />
    </div>
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <div class="bg-white rounded-2xl shadow-card p-6">
        <p class="text-sm text-gray-500">Total propiedades</p>
        <p class="text-2xl font-bold text-gray-900">{{ stats.total }}</p>
      </div>
      <div class="bg-white rounded-2xl shadow-card p-6">
        <p class="text-sm text-gray-500">Disponibles</p>
        <p class="text-2xl font-bold text-green-600">{{ stats.active }}</p>
      </div>
      <div class="bg-white rounded-2xl shadow-card p-6">
        <p class="text-sm text-gray-500">Vendidas</p>
        <p class="text-2xl font-bold text-gray-600">{{ stats.sold }}</p>
      </div>
      <div class="bg-white rounded-2xl shadow-card p-6">
        <p class="text-sm text-gray-500">Reservadas</p>
        <p class="text-2xl font-bold text-amber-600">{{ stats.reserved }}</p>
      </div>
    </div>

    <div class="bg-white rounded-2xl shadow-card p-6">
      <h2 class="font-semibold text-gray-900 mb-4">Accesos rápidos</h2>
      <button type="button" class="btn-primary" @click="goNew">
        Nueva publicación / propiedad
      </button>
      <router-link to="/admin/propiedades" class="btn-secondary ml-3">Ver listado</router-link>
    </div>
  </div>
</template>
