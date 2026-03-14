<script setup>
import { ref, watch } from 'vue'
import { PROPERTY_TYPES, OPERATIONS, STATUSES, CURRENCIES } from '../../data/mockProperties'

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['update:modelValue'])

function createDefaultForm() {
  return {
  title: '',
  type: 'casa',
  category: 'inmueble',
  operation: 'venta',
  price: '',
  currency: 'USD',
  location: '',
  address: '',
  city: '',
  area: '',
  totalArea: '',
  coveredArea: '',
  bedrooms: '',
  bathrooms: '',
  rooms: '',
  state: '',
  description: '',
  features: [],
  referenceCode: '',
  status: 'available',
  isPublished: true,
  isFeatured: false,
  contactPhone: '',
  contactEmail: '',
  observations: '',
  youtubeUrl: '',
  }
}

const form = ref(createDefaultForm())
const featuresText = ref('')

function arraysEqual(a = [], b = []) {
  return JSON.stringify(a) === JSON.stringify(b)
}

function normalizeForm(value = {}) {
  const nextFeatures = Array.isArray(value.features) ? value.features : []
  return {
    ...createDefaultForm(),
    ...value,
    features: nextFeatures,
  }
}

function serializeForm(value = {}) {
  return JSON.stringify(normalizeForm(value))
}

function syncFromModelValue(value = {}) {
  const normalized = normalizeForm(value)
  if (serializeForm(form.value) === JSON.stringify(normalized)) {
    const nextFeaturesText = normalized.features.join(', ')
    if (featuresText.value !== nextFeaturesText) {
      featuresText.value = nextFeaturesText
    }
    return
  }

  form.value = normalized
  featuresText.value = normalized.features.join(', ')
}

watch(
  () => props.modelValue,
  (v) => {
    syncFromModelValue(v)
  },
  { immediate: true }
)

watch(
  form,
  (v) => {
    const normalized = normalizeForm(v)
    if (serializeForm(props.modelValue) === JSON.stringify(normalized)) return
    emit('update:modelValue', normalized)
  },
  { deep: true }
)

watch(featuresText, (t) => {
  const nextFeatures = t.split(',').map(s => s.trim()).filter(Boolean)
  if (arraysEqual(form.value.features, nextFeatures)) return
  form.value = { ...form.value, features: nextFeatures }
}, { immediate: true })
</script>

<template>
  <div class="space-y-6">
    <section class="bg-white rounded-2xl shadow-card p-6">
      <h3 class="font-semibold text-gray-900 mb-4">Datos básicos</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-gray-700 mb-1">Título *</label>
          <input v-model="form.title" type="text" required class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Tipo *</label>
          <select v-model="form.type" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500">
            <option v-for="t in PROPERTY_TYPES" :key="t.value" :value="t.value">{{ t.label }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Operación *</label>
          <select v-model="form.operation" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500">
            <option v-for="o in OPERATIONS" :key="o.value" :value="o.value">{{ o.label }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Precio *</label>
          <input v-model.number="form.price" type="number" min="0" step="0.01" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Moneda</label>
          <select v-model="form.currency" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500">
            <option v-for="c in CURRENCIES" :key="c.value" :value="c.value">{{ c.label }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Estado publicación</label>
          <select v-model="form.status" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500">
            <option v-for="s in STATUSES" :key="s.value" :value="s.value">{{ s.label }}</option>
          </select>
        </div>
        <div class="flex items-center gap-2">
          <input id="pub" v-model="form.isPublished" type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
          <label for="pub">Publicado (visible en sitio)</label>
        </div>
        <div class="flex items-center gap-2">
          <input id="feat" v-model="form.isFeatured" type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
          <label for="feat">Destacada</label>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Código referencia</label>
          <input v-model="form.referenceCode" type="text" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
      </div>
    </section>

    <section class="bg-white rounded-2xl shadow-card p-6">
      <h3 class="font-semibold text-gray-900 mb-4">Ubicación y medidas</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="md:col-span-2">
          <label class="block text-sm font-medium text-gray-700 mb-1">Ubicación / Zona</label>
          <input v-model="form.location" type="text" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" placeholder="Barrio, ciudad, zona" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Dirección</label>
          <input v-model="form.address" type="text" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Ciudad</label>
          <input v-model="form.city" type="text" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Superficie total (m²)</label>
          <input v-model.number="form.totalArea" type="number" min="0" step="0.01" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Superficie cubierta (m²)</label>
          <input v-model.number="form.coveredArea" type="number" min="0" step="0.01" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Ambientes</label>
          <input v-model.number="form.rooms" type="number" min="0" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Dormitorios</label>
          <input v-model.number="form.bedrooms" type="number" min="0" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Baños</label>
          <input v-model.number="form.bathrooms" type="number" min="0" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Estado (ej. nuevo, usado)</label>
          <input v-model="form.state" type="text" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
      </div>
    </section>

    <section class="bg-white rounded-2xl shadow-card p-6">
      <h3 class="font-semibold text-gray-900 mb-4">Descripción y características</h3>
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
          <textarea v-model="form.description" rows="4" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Características (separadas por coma)</label>
          <input v-model="featuresText" type="text" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" placeholder="Jardín, Parrilla, Cochera" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Observaciones internas</label>
          <textarea v-model="form.observations" rows="2" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
      </div>
    </section>

    <section class="bg-white rounded-2xl shadow-card p-6">
      <h3 class="font-semibold text-gray-900 mb-4">Contacto (opcional)</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
          <input v-model="form.contactPhone" type="text" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input v-model="form.contactEmail" type="email" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
      </div>
    </section>
  </div>
</template>
