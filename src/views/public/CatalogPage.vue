<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import PropertyCard from '../../components/property/PropertyCard.vue'
import PropertyFilters from '../../components/property/PropertyFilters.vue'
import LoadingSpinner from '../../components/ui/LoadingSpinner.vue'
import EmptyState from '../../components/ui/EmptyState.vue'
import { fetchPropertiesPublic } from '../../services/properties'

const route = useRoute()
const properties = ref([])
const loading = ref(true)
const filters = ref({})

const queryFromRoute = computed(() => route.query.q || '')

watch(
  () => route.query,
  () => load(),
  { deep: true }
)

onMounted(() => {
  if (route.query.q) filters.value = { ...filters.value, location: route.query.q }
  load()
})

async function load() {
  loading.value = true
  try {
    const f = { ...filters.value }
    if (queryFromRoute.value) f.location = queryFromRoute.value
    properties.value = await fetchPropertiesPublic(f)
  } catch {
    properties.value = []
  } finally {
    loading.value = false
  }
}

function onSearch() {
  load()
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl md:text-3xl font-bold text-gray-900 mb-6">Catálogo de propiedades</h1>

    <PropertyFilters v-model="filters" class="mb-8" @search="onSearch" />

    <LoadingSpinner v-if="loading" />
    <template v-else>
      <EmptyState
        v-if="!properties.length"
        title="No hay propiedades"
        description="No encontramos propiedades con los filtros aplicados. Probá cambiar los criterios."
      />
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <PropertyCard v-for="prop in properties" :key="prop.id" :property="prop" />
      </div>
    </template>
  </div>
</template>
