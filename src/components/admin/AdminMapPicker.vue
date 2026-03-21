<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  latitude: { type: [Number, String, null], default: null },
  longitude: { type: [Number, String, null], default: null },
  heightClass: { type: String, default: 'h-[320px]' },
  searchQuery: { type: String, default: '' },
  address: { type: String, default: '' },
  city: { type: String, default: '' },
  area: { type: String, default: '' },
  lockSuggestedSearch: { type: Boolean, default: false },
})

const emit = defineEmits(['update:coordinates', 'suggested-coordinates'])

const mapRoot = ref(null)
const searchStatus = ref('')
let leaflet = null
let map = null
let marker = null
let tileLayer = null
let searchTimer = null
let activeSearchId = 0

const hasCoordinates = computed(() => {
  const lat = Number(props.latitude)
  const lng = Number(props.longitude)
  return Number.isFinite(lat) && Number.isFinite(lng)
})

function toCoordinates() {
  if (!hasCoordinates.value) return null
  return [Number(props.latitude), Number(props.longitude)]
}

function buildMarkerIcon() {
  if (!leaflet) return null

  return leaflet.divIcon({
    className: 'admin-map-pin',
    html: '<span class="admin-map-pin__inner"></span>',
    iconSize: [26, 26],
    iconAnchor: [13, 13],
  })
}

function emitCoordinates(lat, lng) {
  emit('update:coordinates', {
    lat: Number(lat.toFixed(6)),
    lng: Number(lng.toFixed(6)),
  })
}

function emitSuggestedCoordinates(lat, lng, label = '') {
  emit('suggested-coordinates', {
    lat: Number(lat.toFixed(6)),
    lng: Number(lng.toFixed(6)),
    label,
  })
}

function ensureMarker() {
  if (!leaflet || !map) return null
  if (!marker) {
    marker = leaflet.marker([0, 0], {
      draggable: true,
      icon: buildMarkerIcon(),
    })
    marker.on('dragend', () => {
      const next = marker.getLatLng()
      emitCoordinates(next.lat, next.lng)
    })
    marker.addTo(map)
  }
  return marker
}

function updateMarkerAndView(shouldPan = false) {
  if (!map || !leaflet) return

  const coordinates = toCoordinates()
  if (!coordinates) {
    if (marker) {
      map.removeLayer(marker)
      marker = null
    }
    return
  }

  const nextMarker = ensureMarker()
  nextMarker.setLatLng(coordinates)

  if (shouldPan) {
    map.setView(coordinates, Math.max(map.getZoom(), 17), { animate: false })
  }
}

async function searchLocation(query) {
  const normalizedQuery = String(query || '').trim()
  if (!normalizedQuery || (hasCoordinates.value && props.lockSuggestedSearch)) {
    searchStatus.value = ''
    return
  }

  const searchId = ++activeSearchId
  searchStatus.value = 'Buscando ubicación aproximada...'

  try {
    const structuredResults = await fetchStructuredResults()
    const results = Array.isArray(structuredResults) && structuredResults.length
      ? structuredResults
      : await fetchSearchResults(buildQueryUrl(normalizedQuery))

    if (searchId !== activeSearchId || (hasCoordinates.value && props.lockSuggestedSearch)) {
      return
    }

    const firstResult = Array.isArray(results) ? results[0] : null
    if (!firstResult?.lat || !firstResult?.lon) {
      searchStatus.value = 'No encontré esa dirección todavía. Podés marcarla manualmente en el mapa.'
      return
    }

    const lat = Number(firstResult.lat)
    const lng = Number(firstResult.lon)
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      searchStatus.value = 'No encontré esa dirección todavía. Podés marcarla manualmente en el mapa.'
      return
    }

    emitSuggestedCoordinates(lat, lng, firstResult.display_name || normalizedQuery)
    searchStatus.value = 'Ubicación aproximada encontrada. Si hace falta, ajustala haciendo click en el mapa.'
  } catch {
    if (searchId !== activeSearchId) return
    searchStatus.value = 'No pude buscar la dirección ahora. Podés marcar el punto manualmente en el mapa.'
  }
}

function buildQueryUrl(query) {
  const url = new URL('https://nominatim.openstreetmap.org/search')
  url.searchParams.set('format', 'jsonv2')
  url.searchParams.set('limit', '1')
  url.searchParams.set('countrycodes', 'ar')
  url.searchParams.set('accept-language', 'es')
  url.searchParams.set('q', query)
  return url
}

async function fetchStructuredResults() {
  const address = String(props.address || '').trim()
  const city = String(props.city || '').trim()
  const area = String(props.area || '').trim()

  if (!address && !city && !area) {
    return []
  }

  const url = new URL('https://nominatim.openstreetmap.org/search')
  url.searchParams.set('format', 'jsonv2')
  url.searchParams.set('limit', '1')
  url.searchParams.set('countrycodes', 'ar')
  url.searchParams.set('accept-language', 'es')
  url.searchParams.set('country', 'Argentina')
  if (address) url.searchParams.set('street', address)
  if (city) url.searchParams.set('city', city)
  if (area) url.searchParams.set('county', area)

  return fetchSearchResults(url)
}

async function fetchSearchResults(url) {
  const response = await fetch(url.toString(), {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error('No se pudo buscar la ubicación')
  }

  return response.json()
}

async function initMap() {
  if (!mapRoot.value || map) return

  const module = await import('leaflet')
  leaflet = module.default || module

  const initialCoordinates = toCoordinates() || [-37.9901, -57.5467]
  const initialZoom = toCoordinates() ? 17 : 12

  map = leaflet.map(mapRoot.value, {
    center: initialCoordinates,
    zoom: initialZoom,
    scrollWheelZoom: false,
  })

  tileLayer = leaflet.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors',
    maxZoom: 19,
  })
  tileLayer.addTo(map)

  map.on('click', (event) => {
    emitCoordinates(event.latlng.lat, event.latlng.lng)
  })

  updateMarkerAndView(false)

  window.setTimeout(() => {
    map?.invalidateSize()
  }, 0)
}

watch(
  () => [props.latitude, props.longitude],
  (next, previous) => {
    if (!map) return
    const changed = next[0] !== previous?.[0] || next[1] !== previous?.[1]
    updateMarkerAndView(changed)
  }
)

watch(
  () => props.searchQuery,
  (nextQuery) => {
    if (searchTimer) {
      window.clearTimeout(searchTimer)
      searchTimer = null
    }

    const normalizedQuery = String(nextQuery || '').trim()
    if (!normalizedQuery || (hasCoordinates.value && props.lockSuggestedSearch)) {
      if (!normalizedQuery) {
        searchStatus.value = ''
      }
      return
    }

    searchTimer = window.setTimeout(() => {
      searchLocation(normalizedQuery)
    }, 600)
  },
  { immediate: true }
)

onMounted(() => {
  initMap()
})

onBeforeUnmount(() => {
  if (searchTimer) {
    window.clearTimeout(searchTimer)
    searchTimer = null
  }
  if (map) {
    map.remove()
    map = null
    marker = null
    tileLayer = null
  }
})
</script>

<template>
  <div class="overflow-hidden rounded-2xl border border-emerald-200 bg-white shadow-sm">
    <div
      ref="mapRoot"
      :class="['w-full', heightClass]"
    />
    <div class="flex flex-wrap items-center justify-between gap-2 border-t border-emerald-100 bg-emerald-50/70 px-4 py-3 text-xs text-emerald-900">
      <span>Hacé click en el mapa para marcar el punto exacto.</span>
      <span v-if="hasCoordinates">
        {{ Number(latitude).toFixed(6) }}, {{ Number(longitude).toFixed(6) }}
      </span>
    </div>
    <div v-if="searchStatus" class="border-t border-emerald-100 bg-white px-4 py-3 text-xs text-gray-600">
      {{ searchStatus }}
    </div>
  </div>
</template>

<style scoped>
:deep(.admin-map-pin) {
  align-items: center;
  display: flex;
  height: 26px;
  justify-content: center;
  width: 26px;
}

:deep(.admin-map-pin__inner) {
  background: #047857;
  border: 3px solid #ecfdf5;
  border-radius: 999px;
  box-shadow: 0 10px 20px rgba(4, 120, 87, 0.28);
  display: block;
  height: 18px;
  width: 18px;
}
</style>
