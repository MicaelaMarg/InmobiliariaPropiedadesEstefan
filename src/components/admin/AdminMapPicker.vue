<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  latitude: { type: [Number, String, null], default: null },
  longitude: { type: [Number, String, null], default: null },
  heightClass: { type: String, default: 'h-[320px]' },
})

const emit = defineEmits(['update:coordinates'])

const mapRoot = ref(null)
let leaflet = null
let map = null
let marker = null
let tileLayer = null

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

onMounted(() => {
  initMap()
})

onBeforeUnmount(() => {
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
