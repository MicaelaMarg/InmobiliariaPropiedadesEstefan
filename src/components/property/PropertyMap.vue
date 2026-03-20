<script setup>
import { computed } from 'vue'

const props = defineProps({
  property: {
    type: Object,
    required: true,
  },
})

const hasExactCoordinates = computed(() => (
  Number.isFinite(Number(props.property?.mapLatitude)) &&
  Number.isFinite(Number(props.property?.mapLongitude))
))

const mapQuery = computed(() => {
  const parts = [
    props.property?.address,
    props.property?.location,
    props.property?.city,
    'Argentina',
  ]

  return parts
    .filter(Boolean)
    .map(value => String(value).trim())
    .filter(Boolean)
    .join(', ')
})

const embedUrl = computed(() => {
  if (hasExactCoordinates.value) {
    return `https://www.google.com/maps?q=${encodeURIComponent(`${props.property.mapLatitude},${props.property.mapLongitude}`)}&z=17&output=embed`
  }
  if (!mapQuery.value) return ''
  return `https://www.google.com/maps?q=${encodeURIComponent(mapQuery.value)}&z=15&output=embed`
})

const directionsUrl = computed(() => {
  if (hasExactCoordinates.value) {
    return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${props.property.mapLatitude},${props.property.mapLongitude}`)}`
  }
  if (!mapQuery.value) return ''
  return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(mapQuery.value)}`
})
</script>

<template>
  <section v-if="embedUrl" class="mb-8">
    <div class="flex items-start justify-between gap-4 mb-4">
      <div>
        <h2 class="text-lg font-semibold text-gray-900">Ubicación</h2>
        <p class="text-sm text-gray-500 mt-1">
          {{ property.address || property.location }}
        </p>
      </div>
      <a
        :href="directionsUrl"
        target="_blank"
        rel="noopener noreferrer"
        class="inline-flex items-center gap-2 rounded-xl border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 transition hover:border-emerald-300 hover:text-emerald-900"
      >
        Abrir en Maps
      </a>
    </div>

    <div class="overflow-hidden rounded-2xl border border-gray-200 shadow-sm">
      <iframe
        :src="embedUrl"
        class="h-[320px] w-full border-0"
        loading="lazy"
        referrerpolicy="no-referrer-when-downgrade"
        title="Mapa de la propiedad"
        allowfullscreen
      />
    </div>
  </section>
</template>
