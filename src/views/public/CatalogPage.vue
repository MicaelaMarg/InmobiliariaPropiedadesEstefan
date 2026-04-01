<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PropertyCard from '../../components/property/PropertyCard.vue'
import PropertyFilters from '../../components/property/PropertyFilters.vue'
import LoadingSpinner from '../../components/ui/LoadingSpinner.vue'
import EmptyState from '../../components/ui/EmptyState.vue'
import { fetchPropertiesPublic } from '../../services/properties'
import {
  PROPERTY_TYPES,
  HIGHLIGHTED_MESSAGE_OPTIONS,
  PAYMENT_OPTION_OPTIONS,
} from '../../data/mockProperties'

const route = useRoute()
const router = useRouter()
const properties = ref([])
const loading = ref(true)
const filters = ref({})
const page = ref(1)
const pageSize = 12
const total = ref(0)
const totalPages = ref(1)
const filterOptionLabels = new Map(
  [...HIGHLIGHTED_MESSAGE_OPTIONS, ...PAYMENT_OPTION_OPTIONS].map(option => [option.value, option.label])
)
const propertyTypeLabels = new Map(PROPERTY_TYPES.map(type => [type.value, type.label]))
const syncingFromRoute = ref(false)

const selectedConditionLabel = computed(() => filterOptionLabels.get(filters.value.operationTag) || '')
const selectedTypeLabel = computed(() => propertyTypeLabels.get(filters.value.type) || '')

function getQueryValue(value) {
  if (Array.isArray(value)) return value[0] || ''
  return typeof value === 'string' ? value : ''
}

function normalizeText(value) {
  return String(value || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, ' ')
    .trim()
}

function resolveSpecialOperationTag(value) {
  const normalized = normalizeText(value)
  const compact = normalized.replace(/\s+/g, '')

  if (normalized === 'apto credito' || compact === 'aptocredito') return 'apto_credito'
  if (normalized === 'financiado' || normalized === 'refinanciado') return 'financiado'

  return ''
}

function parseNumberValue(value) {
  const raw = getQueryValue(value)
  if (!raw) return ''

  const parsed = Number(raw)
  return Number.isFinite(parsed) ? parsed : ''
}

function parsePageValue(value) {
  const parsed = Number(getQueryValue(value))
  return Number.isFinite(parsed) && parsed > 0 ? parsed : 1
}

function buildFiltersFromQuery(query) {
  const nextFilters = {}
  const operation = getQueryValue(query.operation)
  const type = getQueryValue(query.type)
  const locationQuery = getQueryValue(query.q || query.location)
  const reference = getQueryValue(query.ref || query.reference)
  const routeOperationTag = getQueryValue(query.operationTag || query.tag)
  const normalizedRouteOperationTag = resolveSpecialOperationTag(routeOperationTag) || routeOperationTag
  const specialLocationOperationTag = resolveSpecialOperationTag(locationQuery)
  const minPrice = parseNumberValue(query.minPrice)
  const maxPrice = parseNumberValue(query.maxPrice)

  if (operation) nextFilters.operation = operation
  if (type) nextFilters.type = type
  if (minPrice !== '') nextFilters.minPrice = minPrice
  if (maxPrice !== '') nextFilters.maxPrice = maxPrice
  if (normalizedRouteOperationTag) {
    nextFilters.operationTag = normalizedRouteOperationTag
  } else if (specialLocationOperationTag) {
    nextFilters.operationTag = specialLocationOperationTag
  }
  if (locationQuery && !specialLocationOperationTag) nextFilters.location = locationQuery
  if (reference) nextFilters.search = reference

  return nextFilters
}

function buildQueryFromFilters(activeFilters = {}, targetPage = 1) {
  const query = {}
  const normalizedLocation = activeFilters.location?.trim() || ''
  const specialLocationOperationTag = resolveSpecialOperationTag(normalizedLocation)

  if (activeFilters.operation) query.operation = activeFilters.operation
  if (activeFilters.type) query.type = activeFilters.type
  if (activeFilters.minPrice !== '' && activeFilters.minPrice != null) {
    query.minPrice = String(activeFilters.minPrice)
  }
  if (activeFilters.maxPrice !== '' && activeFilters.maxPrice != null) {
    query.maxPrice = String(activeFilters.maxPrice)
  }
  if (activeFilters.operationTag) {
    query.operationTag = activeFilters.operationTag
  } else if (specialLocationOperationTag) {
    query.operationTag = specialLocationOperationTag
  }
  if (normalizedLocation && !specialLocationOperationTag) {
    query.q = normalizedLocation
  }
  if (activeFilters.search?.trim()) query.ref = activeFilters.search.trim()
  if (targetPage > 1) query.page = String(targetPage)

  return query
}

function areQueriesEqual(left, right) {
  const leftEntries = Object.entries(left)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .sort(([leftKey], [rightKey]) => leftKey.localeCompare(rightKey))
  const rightEntries = Object.entries(right)
    .map(([key, value]) => [key, getQueryValue(value)])
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .sort(([leftKey], [rightKey]) => leftKey.localeCompare(rightKey))

  return JSON.stringify(leftEntries) === JSON.stringify(rightEntries)
}

const emptyStateTitle = computed(() => {
  if (selectedConditionLabel.value) return 'No encontramos coincidencias'
  return 'No hay propiedades'
})

const emptyStateDescription = computed(() => {
  const typeFragment = selectedTypeLabel.value ? ` de tipo ${selectedTypeLabel.value.toLowerCase()}` : ''
  const conditionFragment = selectedConditionLabel.value ? ` con ${selectedConditionLabel.value.toLowerCase()}` : ''

  if (typeFragment || conditionFragment) {
    return `En este momento no disponemos de propiedades${typeFragment}${conditionFragment}. Probá con otra búsqueda o quitá algún filtro.`
  }

  return 'No encontramos propiedades con los filtros aplicados. Probá cambiar los criterios.'
})

watch(
  () => route.query,
  async (query) => {
    const nextFilters = buildFiltersFromQuery(query)
    const nextPage = parsePageValue(query.page)
    const canonicalQuery = buildQueryFromFilters(nextFilters, nextPage)

    if (!areQueriesEqual(canonicalQuery, query)) {
      await router.replace({ name: 'Catalog', query: canonicalQuery })
      return
    }

    syncingFromRoute.value = true
    filters.value = nextFilters
    syncingFromRoute.value = false

    await load(nextPage, nextFilters)
  },
  { deep: true, immediate: true }
)

watch(
  () => filters.value.operationTag,
  async (nextValue, previousValue) => {
    if (syncingFromRoute.value || nextValue === previousValue) return
    await syncRouteWithFilters(1)
  }
)

async function load(nextPage = page.value, nextFilters = filters.value) {
  loading.value = true
  try {
    const f = { ...nextFilters }
    const requestedPage = nextPage
    f.page = nextPage
    f.limit = pageSize
    const result = await fetchPropertiesPublic(f)
    properties.value = result.items || []
    total.value = result.total || 0
    page.value = result.page || nextPage
    totalPages.value = result.totalPages || 1

    if (result.page !== requestedPage) {
      const canonicalQuery = buildQueryFromFilters(nextFilters, result.page || 1)
      if (!areQueriesEqual(canonicalQuery, route.query)) {
        await router.replace({ name: 'Catalog', query: canonicalQuery })
      }
    }
  } catch {
    properties.value = []
    total.value = 0
    totalPages.value = 1
  } finally {
    loading.value = false
  }
}

async function syncRouteWithFilters(targetPage = 1) {
  const nextQuery = buildQueryFromFilters(filters.value, targetPage)
  if (areQueriesEqual(nextQuery, route.query)) {
    await load(targetPage, filters.value)
    return
  }

  await router.push({ name: 'Catalog', query: nextQuery })
}

function onSearch() {
  syncRouteWithFilters(1)
}

function previousPage() {
  if (page.value <= 1) return
  syncRouteWithFilters(page.value - 1)
}

function nextPage() {
  if (page.value >= totalPages.value) return
  syncRouteWithFilters(page.value + 1)
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
        :title="emptyStateTitle"
        :description="emptyStateDescription"
      />
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <PropertyCard v-for="prop in properties" :key="prop.id" :property="prop" open-in-new-tab />
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
