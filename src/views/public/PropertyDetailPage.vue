<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '../../stores/app'
import PropertyGallery from '../../components/property/PropertyGallery.vue'
import PropertyMap from '../../components/property/PropertyMap.vue'
import ContactCard from '../../components/contact/ContactCard.vue'
import PropertyCard from '../../components/property/PropertyCard.vue'
import LoadingSpinner from '../../components/ui/LoadingSpinner.vue'
import { fetchPropertyBySlug, fetchPropertiesPublic } from '../../services/properties'
import { HIGHLIGHTED_MESSAGE_OPTIONS, PAYMENT_OPTION_OPTIONS, SERVICE_OPTIONS } from '../../data/mockProperties'
import StatusBadge from '../../components/ui/StatusBadge.vue'

const route = useRoute()
const app = useAppStore()
const property = ref(null)
const similar = ref([])
const loading = ref(true)
const loadingSimilar = ref(false)

function getYouTubeEmbedUrl(value) {
  const raw = (value || '').trim()
  if (!raw) return ''

  try {
    const url = new URL(raw)
    const host = url.hostname.replace(/^www\./, '')

    if (host === 'youtu.be') {
      const id = url.pathname.split('/').filter(Boolean)[0]
      return id ? `https://www.youtube.com/embed/${id}` : ''
    }

    if (host === 'youtube.com' || host === 'm.youtube.com') {
      if (url.pathname === '/watch') {
        const id = url.searchParams.get('v')
        return id ? `https://www.youtube.com/embed/${id}` : ''
      }

      const segments = url.pathname.split('/').filter(Boolean)
      if (segments[0] === 'embed' || segments[0] === 'shorts') {
        return segments[1] ? `https://www.youtube.com/embed/${segments[1]}` : ''
      }
    }
  } catch {
    return ''
  }

  return ''
}

const typeLabel = computed(() => {
  if (!property.value) return ''
  const types = {
    casa: 'Casa', departamento: 'Departamento', duplex: 'Dúplex', ph: 'PH',
    lote: 'Lote', campo: 'Campo', local: 'Local',
    fondo_comercio: 'Fondo de comercio', kiosco: 'Kiosko', kiosko: 'Kiosko', galpon: 'Galpón',
    terreno: 'Terreno', oficina: 'Oficina', otro: 'Otro',
  }
  return types[property.value.type] || property.value.type
})

const priceText = computed(() => {
  if (!property.value) return null
  const p = property.value
  if (p.price === null || p.price === undefined || p.price === '' || p.showPrice === false) return null
  const sym = p.currency === 'USD' ? 'USD ' : '$'
  return `${sym}${Number(p.price).toLocaleString('es-AR')}`
})

const statusHighlightText = computed(() => {
  const status = (property.value?.status || '').toLowerCase()
  if (status === 'retasado') return 'Retasado'
  if (status === 'reserved' || status === 'reservado') return 'Reservado'
  if (status === 'sold' || status === 'vendido') return 'Vendido'
  return ''
})

const hideStatusPill = computed(() => {
  const status = (property.value?.status || '').toLowerCase()
  return ['retasado', 'reserved', 'reservado', 'sold', 'vendido'].includes(status)
})

const whatsappLink = computed(() => {
  const phone = property.value?.contactPhone || app.settings.whatsapp
  const num = (phone || '').replace(/\D/g, '')
  const text = encodeURIComponent(
    `Hola, me interesa la propiedad: ${property.value?.title || ''} (${property.value?.referenceCode || ''})`
  )
  return `https://wa.me/${num || '5491112345678'}?text=${text}`
})

const propertyStats = computed(() => {
  if (!property.value) return []

  const p = property.value
  return [
    {
      key: 'totalArea',
      show: !!p.totalArea,
      label: 'Superficie total',
      value: `${p.totalArea} m²`,
      icon: 'M5 19L19 5M7 5h12v12',
    },
    {
      key: 'coveredArea',
      show: !!p.coveredArea,
      label: 'Superficie cubierta',
      value: `${p.coveredArea} m²`,
      icon: 'M4 7h16M7 4v16M17 4v16M4 17h16',
    },
    {
      key: 'frontLength',
      show: !!p.frontLength,
      label: 'Long. frente',
      value: `${p.frontLength} m`,
      icon: 'M4 12h16M7 8l-3 4 3 4M17 8l3 4-3 4',
    },
    {
      key: 'depthLength',
      show: !!p.depthLength,
      label: 'Long. fondo',
      value: `${p.depthLength} m`,
      icon: 'M12 4v16M8 7l4-3 4 3M8 17l4 3 4-3',
    },
    {
      key: 'rooms',
      show: !!p.rooms,
      label: 'Ambientes',
      value: `${p.rooms}`,
      icon: 'M3 11l9-7 9 7M5 10.5V20h14v-9.5M9 20v-5h6v5',
    },
    {
      key: 'bedrooms',
      show: !!p.bedrooms,
      label: 'Dormitorios',
      value: `${p.bedrooms}`,
      icon: 'M3 11.5V9.75A1.75 1.75 0 014.75 8h14.5A1.75 1.75 0 0121 9.75v1.75M3 11.5V16m0-4.5h18M21 11.5V16M7 8V6.75A1.75 1.75 0 018.75 5h6.5A1.75 1.75 0 0117 6.75V8',
    },
    {
      key: 'bathrooms',
      show: !!p.bathrooms,
      label: 'Baños',
      value: `${p.bathrooms}`,
      icon: 'M7 7V5.75A1.75 1.75 0 018.75 4h.5A1.75 1.75 0 0111 5.75V7m2 0V5.75A1.75 1.75 0 0114.75 4h.5A1.75 1.75 0 0117 5.75V7m-12 3h14v3a4 4 0 01-4 4H9a4 4 0 01-4-4v-3zm2 7v2m10-2v2',
    },
    {
      key: 'hasExpenses',
      show: p.hasExpenses !== undefined,
      label: 'Expensas',
      value: p.hasExpenses ? 'Con expensas' : 'Sin expensas',
      icon: 'M5 13l4 4L19 7',
    },
    {
      key: 'referenceCode',
      show: !!p.referenceCode,
      label: 'Referencia',
      value: p.referenceCode,
      icon: 'M9 12h6M9 16h6M9 8h6M5 4h14a2 2 0 012 2v12a2 2 0 01-2 2H5a2 2 0 01-2-2V6a2 2 0 012-2z',
    },
  ].filter(item => item.show)
})

const youtubeEmbedUrl = computed(() => getYouTubeEmbedUrl(property.value?.youtubeUrl))

const highlightedMessageLabels = computed(() => {
  const map = new Map(HIGHLIGHTED_MESSAGE_OPTIONS.map(item => [item.value, item.label]))
  const statusLike = new Set(['retasado'])
  return (property.value?.highlightedMessages || [])
    .filter(value => !statusLike.has(value))
    .map(value => map.get(value) || value)
})

const paymentOptionLabels = computed(() => {
  const map = new Map(PAYMENT_OPTION_OPTIONS.map(item => [item.value, item.label]))
  return (property.value?.paymentOptions || []).map(value => map.get(value) || value)
})

const serviceLabels = computed(() => {
  const map = new Map(SERVICE_OPTIONS.map(item => [item.value, item.label]))
  return (property.value?.services || []).map(value => map.get(value) || value)
})

onMounted(async () => {
  try {
    property.value = await fetchPropertyBySlug(route.params.slug)
  } catch {
    property.value = null
  } finally {
    loading.value = false
  }

  if (property.value) {
    loadingSimilar.value = true
    try {
      const result = await fetchPropertiesPublic({
        operation: property.value.operation,
        type: property.value.type,
        page: 1,
        limit: 6,
      })
      const items = result.items || []
      similar.value = items.filter(p => p.id !== property.value.id).slice(0, 3)
    } catch {
      similar.value = []
    } finally {
      loadingSimilar.value = false
    }
  }
})
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <LoadingSpinner v-if="loading" />
    <template v-else-if="property">
      <div class="flex flex-col md:flex-row md:items-start md:justify-between gap-4 mb-6">
        <div class="space-y-2">
          <div class="flex items-center gap-2 flex-wrap">
            <span class="text-sm font-medium uppercase tracking-wide text-[#0b5b38]">{{ typeLabel }}</span>
            <span
              v-if="statusHighlightText"
              class="inline-flex items-center bg-red-600 text-white text-xs font-semibold uppercase px-3 py-1 rounded-md shadow"
            >
              {{ statusHighlightText }}
            </span>
            <StatusBadge
              :operation="property.operation"
              :status="property.status"
              :hide-status="hideStatusPill"
            />
            <span
              v-for="label in highlightedMessageLabels"
              :key="label"
              class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-50 text-emerald-800"
            >
              {{ label }}
            </span>
          </div>
          <h1 class="text-2xl md:text-3xl font-bold text-gray-900">{{ property.title }}</h1>
          <p class="text-gray-500">{{ property.location }}</p>
        </div>
        <div class="text-2xl font-bold text-[#0b5b38]" v-if="priceText">{{ priceText }}</div>
        <div class="text-sm font-semibold text-red-600" v-else>Consultá para más información sobre valores</div>
      </div>

      <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8 text-sm">
        <div
          v-for="item in propertyStats"
          :key="item.key"
          class="bg-gray-50 rounded-xl p-4"
        >
          <div class="flex items-start gap-3">
            <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-white text-emerald-800 shadow-sm flex-shrink-0">
              <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.7" :d="item.icon" />
              </svg>
            </span>
            <div class="min-w-0">
              <span class="text-gray-500 block">{{ item.label }}</span>
              <span class="font-medium text-gray-900">{{ item.value }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="property.description" class="prose prose-gray max-w-none mb-8">
        <h2 class="text-lg font-semibold text-gray-900 mb-2">Descripción</h2>
        <p class="text-gray-600 whitespace-pre-line">{{ property.description }}</p>
      </div>

      <div v-if="property.features?.length" class="mb-8">
        <h2 class="text-lg font-semibold text-gray-900 mb-2">Características</h2>
        <ul class="flex flex-wrap gap-2">
          <li
            v-for="f in property.features"
            :key="f"
            class="rounded-xl bg-emerald-100 px-3 py-1.5 text-sm text-emerald-900"
          >
            {{ f }}
          </li>
        </ul>
      </div>

      <div v-if="highlightedMessageLabels.length || paymentOptionLabels.length" class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        <div v-if="highlightedMessageLabels.length" class="rounded-2xl bg-gray-50 p-5">
          <h2 class="text-lg font-semibold text-gray-900 mb-3">Mensajes destacados</h2>
          <ul class="flex flex-wrap gap-2">
            <li
              v-for="item in highlightedMessageLabels"
              :key="item"
              class="rounded-xl bg-emerald-100 px-3 py-1.5 text-sm font-medium text-emerald-900"
            >
              {{ item }}
            </li>
          </ul>
        </div>

        <div v-if="paymentOptionLabels.length" class="rounded-2xl bg-gray-50 p-5">
          <h2 class="text-lg font-semibold text-gray-900 mb-3">Formas de pago</h2>
          <ul class="flex flex-wrap gap-2">
            <li
              v-for="item in paymentOptionLabels"
              :key="item"
              class="px-3 py-1.5 rounded-xl bg-emerald-100 text-emerald-900 text-sm font-medium"
            >
              {{ item }}
            </li>
          </ul>
        </div>
      </div>

      <div v-if="serviceLabels.length" class="mb-8 rounded-2xl bg-gray-50 p-5">
        <h2 class="text-lg font-semibold text-gray-900 mb-3">Servicios</h2>
        <ul class="flex flex-wrap gap-2">
          <li
            v-for="item in serviceLabels"
            :key="item"
            class="px-3 py-1.5 rounded-xl bg-amber-100 text-amber-900 text-sm font-medium"
          >
            {{ item }}
          </li>
        </ul>
      </div>

      <PropertyGallery :images="property.images" :alt="property.title" class="mb-8" />

      <div v-if="youtubeEmbedUrl" class="mb-8">
        <h2 class="text-lg font-semibold text-gray-900 mb-3">Video</h2>
        <div class="overflow-hidden rounded-2xl bg-black shadow-card aspect-video">
          <iframe
            :src="youtubeEmbedUrl"
            :title="`Video de ${property.title}`"
            class="h-full w-full"
            loading="lazy"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
            allowfullscreen
            referrerpolicy="strict-origin-when-cross-origin"
          />
        </div>
      </div>

      <PropertyMap :property="property" />

      <div class="border-t border-gray-200 pt-8 mb-8">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Contacto</h2>
        <ContactCard :property="property" />
        <a
          :href="whatsappLink"
          target="_blank"
          rel="noopener noreferrer"
          class="inline-flex items-center gap-2 mt-4 btn-primary bg-green-600 hover:bg-green-700"
        >
          Consultar por WhatsApp
        </a>
      </div>

      <div v-if="loadingSimilar" class="mt-8">
        <LoadingSpinner />
      </div>
      <div v-else-if="similar.length">
        <h2 class="text-xl font-bold text-gray-900 mb-4">Propiedades similares</h2>
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <PropertyCard v-for="p in similar" :key="p.id" :property="p" />
        </div>
      </div>
    </template>
    <div v-else class="text-center py-16">
      <p class="text-gray-500">No se encontró la propiedad.</p>
      <router-link
        to="/catalogo"
        class="mt-4 inline-flex items-center justify-center rounded-xl bg-[#0b5b38] px-5 py-2.5 text-sm font-medium text-white transition-colors hover:bg-[#08472c] focus:outline-none focus:ring-2 focus:ring-emerald-200 focus:ring-offset-2"
      >
        Volver al catálogo
      </router-link>
    </div>
  </div>
</template>
