<script setup>
import { ref, computed, watch } from 'vue'
import { createPropertyQrCanvas, downloadPropertyQr, getPropertyPublicUrl } from '../../utils/propertyQr'

const props = defineProps({
  slug: { type: String, required: true },
  title: { type: String, default: '' },
  referenceCode: { type: String, default: '' },
  isPublished: { type: Boolean, default: false },
})

const previewUrl = ref('')
const downloading = ref(false)
const copied = ref(false)
const error = ref('')
const publicUrl = computed(() => getPropertyPublicUrl(props.slug))

watch(
  () => [props.slug, props.title, props.referenceCode],
  async () => {
    try {
      const canvas = await createPropertyQrCanvas(props)
      previewUrl.value = canvas.toDataURL('image/png')
      error.value = ''
    } catch {
      error.value = 'No se pudo generar el código QR.'
    }
  },
  { immediate: true }
)

async function download() {
  downloading.value = true
  try {
    await downloadPropertyQr(props)
  } catch {
    error.value = 'No se pudo descargar el código QR.'
  } finally {
    downloading.value = false
  }
}

async function copyLink() {
  try {
    await navigator.clipboard.writeText(publicUrl.value)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    error.value = 'No se pudo copiar el enlace.'
  }
}
</script>

<template>
  <div class="bg-white rounded-2xl shadow-card p-6">
    <h3 class="font-semibold text-gray-900 mb-1">Código QR</h3>
    <p class="text-sm text-gray-500 mb-4">Al escanearlo, abre la ficha de esta propiedad. Descargalo para imprimirlo en carteles o folletos.</p>

    <p v-if="!isPublished" class="mb-4 rounded-xl bg-amber-50 p-3 text-sm text-amber-800">
      Esta propiedad no está publicada: quien escanee el QR verá "No se encontró la propiedad" hasta que la publiques.
    </p>
    <p v-if="error" class="mb-4 rounded-xl bg-red-50 p-3 text-sm text-red-600">{{ error }}</p>

    <div class="flex flex-col gap-6 sm:flex-row sm:items-start">
      <img
        v-if="previewUrl"
        :src="previewUrl"
        :alt="`Código QR de ${title}`"
        class="w-48 shrink-0 rounded-xl border border-gray-200"
      />
      <div class="min-w-0 flex-1">
        <p class="text-xs font-medium uppercase text-gray-500 mb-1">Enlace</p>
        <a :href="publicUrl" target="_blank" rel="noopener noreferrer" class="block break-all text-sm text-primary-600 hover:underline">
          {{ publicUrl }}
        </a>
        <div class="mt-4 flex flex-wrap gap-3">
          <button type="button" class="btn-primary text-sm disabled:opacity-70" :disabled="downloading || !previewUrl" @click="download">
            {{ downloading ? 'Generando...' : 'Descargar QR' }}
          </button>
          <button type="button" class="btn-secondary text-sm" @click="copyLink">
            {{ copied ? '¡Enlace copiado!' : 'Copiar enlace' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
