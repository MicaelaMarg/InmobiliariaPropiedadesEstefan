<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import PropertyForm from '../../components/property/PropertyForm.vue'
import ImageUploader from '../../components/admin/ImageUploader.vue'
import LoadingSpinner from '../../components/ui/LoadingSpinner.vue'
import { fetchPropertyById, createProperty, updateProperty } from '../../services/properties'
import { slugify } from '../../services/properties'

const router = useRouter()
const route = useRoute()
const isEdit = computed(() => !!route.params.id && route.params.id !== 'nueva')
const loading = ref(!!isEdit.value)
const saving = ref(false)
const error = ref('')

const formData = ref({})
const images = ref([])

function toNullableNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return null
  }

  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : null
}

function sanitizePayload(value = {}) {
  return {
    ...value,
    price: toNullableNumber(value.price),
    totalArea: toNullableNumber(value.totalArea),
    coveredArea: toNullableNumber(value.coveredArea),
    bedrooms: toNullableNumber(value.bedrooms),
    bathrooms: toNullableNumber(value.bathrooms),
    rooms: toNullableNumber(value.rooms),
  }
}

onMounted(async () => {
  if (isEdit.value) {
    try {
      const p = await fetchPropertyById(route.params.id)
      if (p) {
        formData.value = { ...p }
        images.value = p.images ? p.images.map(img => ({ ...img })) : []
      } else {
        error.value = 'Propiedad no encontrada'
      }
    } catch {
      error.value = 'Error al cargar la propiedad'
    } finally {
      loading.value = false
    }
  } else {
    formData.value = {}
    images.value = []
    loading.value = false
  }
})

async function save() {
  if (!formData.value.title?.trim()) {
    error.value = 'El título es obligatorio.'
    return
  }
  error.value = ''
  saving.value = true
  try {
    const payload = {
      ...sanitizePayload(formData.value),
      slug: formData.value.slug || slugify(formData.value.title),
      images: images.value.map((img, i) => ({
        url: img.url || '',
        order: i,
        isPrimary: i === 0 || (img.isPrimary === true),
      })).filter(img => img.url),
    }
    if (isEdit.value) {
      await updateProperty(route.params.id, payload)
      router.push({ name: 'AdminProperties', query: { saved: '1' } })
    } else {
      const created = await createProperty(payload)
      router.push({ name: 'AdminProperties', query: { saved: '1' } })
    }
  } catch (e) {
    error.value = e.message || 'Error al guardar'
  } finally {
    saving.value = false
  }
}

function cancel() {
  router.push({ name: 'AdminProperties' })
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6">{{ isEdit ? 'Editar propiedad' : 'Nueva propiedad' }}</h1>

    <LoadingSpinner v-if="loading" />
    <template v-else>
      <p v-if="error" class="mb-4 text-red-600 bg-red-50 p-3 rounded-xl text-sm">{{ error }}</p>

      <PropertyForm v-model="formData" class="mb-8" />

      <div class="bg-white rounded-2xl shadow-card p-6 mb-8">
        <h3 class="font-semibold text-gray-900 mb-4">Imágenes</h3>
        <ImageUploader v-model="images" />
      </div>

      <div class="flex gap-3">
        <button type="button" class="btn-primary" :disabled="saving" @click="save">
          {{ saving ? 'Guardando...' : 'Guardar' }}
        </button>
        <button type="button" class="btn-secondary" :disabled="saving" @click="cancel">Cancelar</button>
      </div>
    </template>
  </div>
</template>
