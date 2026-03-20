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
const originalImagesSnapshot = ref('[]')

function normalizeYouTubeUrl(value) {
  const raw = (value || '').trim()
  if (!raw) return null

  try {
    const url = new URL(raw)
    const host = url.hostname.replace(/^www\./, '')

    if (host === 'youtu.be') {
      const id = url.pathname.split('/').filter(Boolean)[0]
      return id ? `https://www.youtube.com/watch?v=${id}` : null
    }

    if (host === 'youtube.com' || host === 'm.youtube.com') {
      if (url.pathname === '/watch') {
        const id = url.searchParams.get('v')
        return id ? `https://www.youtube.com/watch?v=${id}` : null
      }

      const segments = url.pathname.split('/').filter(Boolean)
      if (segments[0] === 'embed' || segments[0] === 'shorts') {
        return segments[1] ? `https://www.youtube.com/watch?v=${segments[1]}` : null
      }
    }
  } catch {
    return null
  }

  return null
}

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
    mapLatitude: toNullableNumber(value.mapLatitude),
    mapLongitude: toNullableNumber(value.mapLongitude),
    totalArea: toNullableNumber(value.totalArea),
    coveredArea: toNullableNumber(value.coveredArea),
    frontLength: toNullableNumber(value.frontLength),
    depthLength: toNullableNumber(value.depthLength),
    bedrooms: toNullableNumber(value.bedrooms),
    bathrooms: toNullableNumber(value.bathrooms),
    rooms: toNullableNumber(value.rooms),
    youtubeUrl: normalizeYouTubeUrl(value.youtubeUrl),
  }
}

function normalizeImageForComparison(image = {}, index = 0) {
  return {
    id: image.id ?? null,
    uploadToken: image.uploadToken ?? null,
    hasLocalFile: image.file instanceof File,
    url: image.file instanceof File ? '' : image.url || image.largeUrl || image.mediumUrl || image.thumbnailUrl || '',
    thumbnailUrl: image.file instanceof File ? '' : image.thumbnailUrl || image.mediumUrl || image.url || '',
    mediumUrl: image.mediumUrl || null,
    largeUrl: image.file instanceof File ? '' : image.largeUrl || image.url || image.mediumUrl || image.thumbnailUrl || '',
    placeholderUrl: image.file instanceof File ? null : image.placeholderUrl || null,
    width: image.width ?? null,
    height: image.height ?? null,
    thumbnailWidth: image.thumbnailWidth ?? null,
    mediumWidth: image.mediumWidth ?? null,
    largeWidth: image.largeWidth ?? image.width ?? null,
    mimeType: image.mimeType || null,
    originalName: image.originalName || null,
    order: image.order ?? index,
    isPrimary: !!image.isPrimary,
  }
}

function createImagesSnapshot(list = []) {
  return JSON.stringify((list || []).map((image, index) => normalizeImageForComparison(image, index)))
}

function buildImagePayloadItem(image = {}, index = 0) {
  const hasLocalFile = image.file instanceof File
  return {
    id: image.id ?? null,
    uploadToken: hasLocalFile ? image.uploadToken || `image_${index}` : null,
    url: hasLocalFile ? null : image.largeUrl || image.url || image.mediumUrl || image.thumbnailUrl || '',
    thumbnailUrl: hasLocalFile ? null : image.thumbnailUrl || image.mediumUrl || image.url || '',
    mediumUrl: hasLocalFile ? null : image.mediumUrl || null,
    largeUrl: hasLocalFile ? null : image.largeUrl || image.url || image.mediumUrl || image.thumbnailUrl || '',
    placeholderUrl: hasLocalFile ? null : image.placeholderUrl || null,
    width: image.width ?? null,
    height: image.height ?? null,
    thumbnailWidth: image.thumbnailWidth ?? null,
    mediumWidth: image.mediumWidth ?? null,
    largeWidth: image.largeWidth ?? image.width ?? null,
    mimeType: image.mimeType || image.file?.type || null,
    originalName: image.originalName || image.file?.name || null,
    order: index,
    isPrimary: index === 0 || image.isPrimary === true,
  }
}

function buildMultipartPayload(propertyPayload, imagePayload, sourceImages) {
  const formDataPayload = new FormData()
  formDataPayload.append('property', JSON.stringify({
    ...propertyPayload,
    images: imagePayload,
  }))

  sourceImages.forEach((image, index) => {
    if (!(image.file instanceof File)) return
    const uploadToken = imagePayload[index]?.uploadToken
    if (!uploadToken) return
    formDataPayload.append(`imageFile_${uploadToken}`, image.file, image.file.name)
  })

  return formDataPayload
}

onMounted(async () => {
  if (isEdit.value) {
    try {
      const p = await fetchPropertyById(route.params.id)
      if (p) {
        formData.value = { ...p }
        images.value = p.images ? p.images.map(img => ({ ...img })) : []
        originalImagesSnapshot.value = createImagesSnapshot(images.value)
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
    originalImagesSnapshot.value = '[]'
    loading.value = false
  }
})

async function save() {
  if (!formData.value.title?.trim()) {
    error.value = 'El título es obligatorio.'
    return
  }
  const hasLatitude = formData.value.mapLatitude !== '' && formData.value.mapLatitude !== null && formData.value.mapLatitude !== undefined
  const hasLongitude = formData.value.mapLongitude !== '' && formData.value.mapLongitude !== null && formData.value.mapLongitude !== undefined
  if (hasLatitude !== hasLongitude) {
    error.value = 'Para usar ubicación exacta tenés que completar latitud y longitud.'
    return
  }
  if (formData.value.youtubeUrl?.trim() && !normalizeYouTubeUrl(formData.value.youtubeUrl)) {
    error.value = 'El enlace de YouTube no es válido.'
    return
  }
  error.value = ''
  saving.value = true
  try {
    const normalizedImages = images.value.map((img, i) => buildImagePayloadItem(img, i))
    const imagesChanged = !isEdit.value || createImagesSnapshot(images.value) !== originalImagesSnapshot.value
    const hasNewFiles = images.value.some(img => img.file instanceof File)

    const payload = {
      ...sanitizePayload(formData.value),
      slug: formData.value.slug || slugify(formData.value.title),
    }

    let requestBody = payload
    if (imagesChanged) {
      payload.images = normalizedImages
      requestBody = hasNewFiles ? buildMultipartPayload(payload, normalizedImages, images.value) : payload
    }

    if (isEdit.value) {
      await updateProperty(route.params.id, requestBody)
      router.push({
        name: 'AdminDashboard',
        query: { saved: payload.isPublished ? 'published' : 'updated' },
      })
    } else {
      await createProperty(requestBody)
      router.push({
        name: 'AdminDashboard',
        query: { saved: payload.isPublished ? 'published' : 'created' },
      })
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

        <div class="mt-6">
          <label class="block text-sm font-medium text-gray-700 mb-1">Enlace de YouTube</label>
          <input
            v-model="formData.youtubeUrl"
            type="url"
            class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500"
            placeholder="https://www.youtube.com/watch?v=..."
          />
          <p class="mt-2 text-xs text-gray-500">Pegá el enlace del video y se mostrará en la ficha de la propiedad.</p>
        </div>
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
