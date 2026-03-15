<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import StatusBadge from '../ui/StatusBadge.vue'
import ResponsiveImage from '../ui/ResponsiveImage.vue'
import { getPropertyPrimaryImage } from '../../utils/propertyImages'

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
        variant="medium"
        :srcset-variants="['thumbnail', 'medium']"
        sizes="(min-width: 1024px) 30vw, (min-width: 640px) 50vw, 100vw"
        class="h-full w-full"
        img-class="group-hover:scale-105"
      />

      <div
        v-if="property.images?.length > 1"
        class="absolute bottom-3 right-3 rounded-full bg-slate-950/65 px-3 py-1 text-xs font-medium text-white backdrop-blur-sm"
      >
        {{ property.images.length }} fotos
      </div>
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
