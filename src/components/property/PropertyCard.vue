<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import StatusBadge from '../ui/StatusBadge.vue'
import ResponsiveImage from '../ui/ResponsiveImage.vue'
import { getPropertyPrimaryImage } from '../../utils/propertyImages'
import { HIGHLIGHTED_MESSAGE_OPTIONS } from '../../data/mockProperties'

const props = defineProps({
  property: { type: Object, required: true },
})

const router = useRouter()

const image = computed(() => getPropertyPrimaryImage(props.property))

const typeLabel = computed(() => {
  const types = {
    casa: 'Casa',
    departamento: 'Dpto',
    duplex: 'Dúplex',
    ph: 'PH',
    lote: 'Lote',
    campo: 'Campo',
    local: 'Local',
    fondo_comercio: 'Fondo de comercio',
    kiosco: 'Kiosko',
    kiosko: 'Kiosko',
    galpon: 'Galpón',
    terreno: 'Terreno',
    oficina: 'Oficina',
    otro: 'Otro',
  }
  return types[props.property.type] || props.property.type
})

const priceText = computed(() => {
  const p = props.property
  const sym = p.currency === 'USD' ? 'USD ' : '$'
  return `${sym}${Number(p.price).toLocaleString('es-AR')}`
})

const ribbonText = computed(() => {
  const status = (props.property.status || '').toLowerCase()
  if (status === 'retasado') return 'Retasado'
  if (status === 'reserved' || status === 'reservado') return 'Reservado'
  if (status === 'sold' || status === 'vendido') return 'Vendido'
  return ''
})

const hideStatusPill = computed(() => {
  const status = (props.property.status || '').toLowerCase()
  return ['retasado', 'reserved', 'reservado', 'sold', 'vendido'].includes(status)
})

const highlightedLabels = computed(() => {
  const map = new Map(HIGHLIGHTED_MESSAGE_OPTIONS.map(item => [item.value, item.label]))
  const statusLike = new Set(['retasado'])
  return (props.property.highlightedMessages || [])
    .filter(value => !statusLike.has(value))
    .map(value => map.get(value) || value)
})

function goToDetail() {
  router.push({ name: 'PropertyDetail', params: { slug: props.property.slug } })
}
</script>

<template>
  <article
    class="card cursor-pointer transition duration-300 hover:-translate-y-1 hover:shadow-lg group"
    @click="goToDetail"
  >
    <div class="relative aspect-[4/3] overflow-hidden bg-gray-100">
      <ResponsiveImage
        :image="image"
        :alt="property.title"
        variant="thumbnail"
        :srcset-variants="['thumbnail']"
        sizes="(min-width: 1024px) 30vw, (min-width: 640px) 50vw, 100vw"
        class="h-full w-full"
        img-class="group-hover:scale-105"
      />

      <div v-if="ribbonText" class="absolute left-3 top-3 z-20">
        <span class="inline-flex items-center bg-red-600 text-white text-xs font-semibold uppercase px-3 py-1 rounded-md shadow-md">
          {{ ribbonText }}
        </span>
      </div>

      <div
        v-if="property.images?.length > 1"
        class="absolute bottom-3 right-3 rounded-full bg-slate-950/65 px-3 py-1 text-xs font-medium text-white backdrop-blur-sm"
      >
        {{ property.images.length }} fotos
      </div>
    </div>
    <div class="p-4">
      <div class="flex items-start justify-between gap-2 mb-2">
        <span class="text-xs font-medium uppercase tracking-wide text-[#0b5b38]">{{ typeLabel }}</span>
        <div class="flex items-center justify-end gap-1.5 flex-wrap">
          <StatusBadge :operation="property.operation" :status="property.status" :hide-status="hideStatusPill" />
          <span
            v-for="label in highlightedLabels"
            :key="label"
            class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-50 text-emerald-800"
          >
            {{ label }}
          </span>
        </div>
      </div>
      <h2 class="font-semibold text-gray-900 line-clamp-2 mb-1">{{ property.title }}</h2>
      <p class="text-sm text-gray-500 mb-2">{{ property.location }}</p>
      <div class="flex items-baseline justify-between flex-wrap gap-2">
        <span class="text-lg font-bold text-[#0b5b38]" v-if="property.showPrice !== false">{{ priceText }}</span>
        <span class="text-sm font-semibold text-red-600" v-else>Consultar</span>
        <span v-if="property.totalArea" class="text-sm text-gray-500">{{ property.totalArea }} m²</span>
      </div>
    </div>
  </article>
</template>
