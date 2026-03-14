<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import StatusBadge from '../ui/StatusBadge.vue'

const props = defineProps({
  property: { type: Object, required: true },
})

const router = useRouter()

const imageUrl = computed(() => {
  const imgs = props.property.images || []
  const primary = imgs.find(i => i.isPrimary) || imgs[0]
  return primary?.url || 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800'
})

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

function goToDetail() {
  router.push({ name: 'PropertyDetail', params: { slug: props.property.slug } })
}
</script>

<template>
  <article
    class="card cursor-pointer hover:shadow-lg transition-shadow duration-200 group"
    @click="goToDetail"
  >
    <div class="aspect-[4/3] overflow-hidden bg-gray-100">
      <img
        :src="imageUrl"
        :alt="property.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        loading="lazy"
      />
    </div>
    <div class="p-4">
      <div class="flex items-start justify-between gap-2 mb-2">
        <span class="text-xs font-medium text-primary-600 uppercase tracking-wide">{{ typeLabel }}</span>
        <StatusBadge :operation="property.operation" :status="property.status" />
      </div>
      <h2 class="font-semibold text-gray-900 line-clamp-2 mb-1">{{ property.title }}</h2>
      <p class="text-sm text-gray-500 mb-2">{{ property.location }}</p>
      <div class="flex items-baseline justify-between flex-wrap gap-2">
        <span class="text-lg font-bold text-primary-600">{{ priceText }}</span>
        <span v-if="property.totalArea" class="text-sm text-gray-500">{{ property.totalArea }} m²</span>
      </div>
    </div>
  </article>
</template>
