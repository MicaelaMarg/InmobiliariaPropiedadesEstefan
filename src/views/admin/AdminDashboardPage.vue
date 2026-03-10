<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchAllProperties } from '../../services/properties'

const router = useRouter()
const stats = ref({ total: 0, active: 0, sold: 0, reserved: 0 })
const loading = ref(true)

onMounted(async () => {
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
