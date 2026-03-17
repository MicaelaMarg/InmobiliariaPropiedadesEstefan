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
const page = ref(1)
const pageSize = 12
const total = ref(0)
const totalPages = ref(1)

const queryFromRoute = computed(() => route.query.q || '')
const queryReference = computed(() => route.query.ref || route.query.reference || '')

watch(
  () => route.query,
  () => load(),
  { deep: true }
)

onMounted(() => {
  const newFilters = { ...filters.value }
  if (route.query.q) newFilters.location = route.query.q
  if (queryReference.value) newFilters.search = queryReference.value
  filters.value = newFilters
  load()
})

async function load(nextPage = page.value) {
  loading.value = true
  try {
    const f = { ...filters.value }
    if (queryFromRoute.value) f.location = queryFromRoute.value
    if (queryReference.value) f.search = queryReference.value
    f.page = nextPage
    f.limit = pageSize
    const result = await fetchPropertiesPublic(f)
    properties.value = result.items || []
    total.value = result.total || 0
    page.value = result.page || nextPage
    totalPages.value = result.totalPages || 1
  } catch {
    properties.value = []
    total.value = 0
    totalPages.value = 1
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  load(1)
}

function previousPage() {
  if (page.value <= 1) return
  load(page.value - 1)
}

function nextPage() {
  if (page.value >= totalPages.value) return
  load(page.value + 1)
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
      <div v-if="properties.length" class="mt-8 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p class="text-sm text-gray-500">
          Mostrando {{ properties.length }} de {{ total }} propiedades
        </p>
        <div class="flex items-center gap-3">
          <button type="button" class="btn-secondary text-sm" :disabled="page <= 1" @click="previousPage">
            Anterior
          </button>
          <span class="text-sm text-gray-600">Página {{ page }} de {{ totalPages }}</span>
          <button
            type="button"
            class="inline-flex items-center justify-center rounded-xl bg-[#0b5b38] px-5 py-2.5 text-sm font-medium text-white transition-colors hover:bg-[#08472c] focus:outline-none focus:ring-2 focus:ring-emerald-200 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="page >= totalPages"
            @click="nextPage"
          >
            Siguiente
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
