<script setup>
import { ref, watch } from 'vue'
import { getImageUrl } from '../../utils/propertyImages'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  maxFiles: { type: Number, default: 20 },
})

const emit = defineEmits(['update:modelValue'])
const input = ref(null)
const uploading = ref(false)
const IMAGE_DIMENSIONS = {
  thumbnail: 320,
  large: 1440,
  placeholder: 24,
}
let preferredMimeTypePromise = null

const images = ref(props.modelValue.length ? props.modelValue.map(normalizeStoredImage) : [])

watch(() => props.modelValue, (v) => {
  if (v?.length) images.value = v.map(normalizeStoredImage)
  else images.value = []
}, { deep: true })

function emitValue() {
  emit('update:modelValue', images.value.map((img, idx) => serializeImage(img, idx)))
}

function normalizeStoredImage(image = {}) {
  return {
    ...image,
    url: image.url || image.largeUrl || image.mediumUrl || image.thumbnailUrl || '',
    thumbnailUrl: image.thumbnailUrl || image.mediumUrl || image.url || '',
    mediumUrl: image.mediumUrl || null,
    largeUrl: image.largeUrl || image.url || image.mediumUrl || image.thumbnailUrl || '',
    placeholderUrl: image.placeholderUrl || null,
    width: image.width ?? null,
    height: image.height ?? null,
    thumbnailWidth: image.thumbnailWidth ?? null,
    mediumWidth: image.mediumWidth ?? null,
    largeWidth: image.largeWidth ?? image.width ?? null,
    mimeType: image.mimeType || null,
    originalName: image.originalName || null,
    order: image.order ?? 0,
    isPrimary: !!image.isPrimary,
  }
}

function serializeImage(image = {}, index = 0) {
  return {
    url: image.largeUrl || image.url || image.mediumUrl || image.thumbnailUrl || '',
    thumbnailUrl: image.thumbnailUrl || image.mediumUrl || image.url || '',
    mediumUrl: image.mediumUrl || null,
    largeUrl: image.largeUrl || image.url || image.mediumUrl || image.thumbnailUrl || '',
    placeholderUrl: image.placeholderUrl || null,
    width: image.width ?? null,
    height: image.height ?? null,
    thumbnailWidth: image.thumbnailWidth ?? null,
    mediumWidth: image.mediumWidth ?? null,
    largeWidth: image.largeWidth ?? image.width ?? null,
    mimeType: image.mimeType || null,
    originalName: image.originalName || null,
    order: index,
    isPrimary: !!image.isPrimary,
  }
}

function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(new Error('No se pudo leer la imagen'))
    reader.readAsDataURL(file)
  })
}

function blobToDataUrl(blob) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(new Error('No se pudo generar la imagen optimizada'))
    reader.readAsDataURL(blob)
  })
}

function loadImage(file) {
  return new Promise((resolve, reject) => {
    const objectUrl = URL.createObjectURL(file)
    const image = new Image()
    image.onload = () => {
      URL.revokeObjectURL(objectUrl)
      resolve(image)
    }
    image.onerror = () => {
      URL.revokeObjectURL(objectUrl)
      reject(new Error('No se pudo procesar la imagen'))
    }
    image.src = objectUrl
  })
}

async function getPreferredMimeType() {
  if (!preferredMimeTypePromise) {
    preferredMimeTypePromise = Promise.resolve().then(() => {
      const canvas = document.createElement('canvas')
      canvas.width = 1
      canvas.height = 1
      return canvas.toDataURL('image/webp').startsWith('data:image/webp') ? 'image/webp' : 'image/jpeg'
    })
  }

  return preferredMimeTypePromise
}

async function canvasToDataUrl(canvas, mimeType, quality) {
  if (!canvas.toBlob) {
    return canvas.toDataURL(mimeType, quality)
  }

  return new Promise((resolve) => {
    canvas.toBlob(async (blob) => {
      if (!blob) {
        resolve(canvas.toDataURL(mimeType, quality))
        return
      }

      resolve(await blobToDataUrl(blob))
    }, mimeType, quality)
  })
}

async function createVariant(image, maxDimension, mimeType, quality) {
  const longestSide = Math.max(image.width, image.height)
  const scale = longestSide > maxDimension ? maxDimension / longestSide : 1
  const canvas = document.createElement('canvas')
  canvas.width = Math.max(1, Math.round(image.width * scale))
  canvas.height = Math.max(1, Math.round(image.height * scale))

  const context = canvas.getContext('2d')
  if (!context) {
    return {
      dataUrl: null,
      width: image.width,
      height: image.height,
    }
  }

  context.imageSmoothingEnabled = true
  context.imageSmoothingQuality = 'high'
  context.drawImage(image, 0, 0, canvas.width, canvas.height)

  return {
    dataUrl: await canvasToDataUrl(canvas, mimeType, quality),
    width: canvas.width,
    height: canvas.height,
  }
}

async function fileToOptimizedImage(file) {
  const image = await loadImage(file)
  const mimeType = await getPreferredMimeType()
  const quality = mimeType === 'image/webp'
    ? { thumbnail: 0.64, large: 0.76 }
    : { thumbnail: 0.68, large: 0.8 }

  const [thumbnail, large, placeholder] = await Promise.all([
    createVariant(image, IMAGE_DIMENSIONS.thumbnail, mimeType, quality.thumbnail),
    createVariant(image, IMAGE_DIMENSIONS.large, mimeType, quality.large),
    createVariant(image, IMAGE_DIMENSIONS.placeholder, 'image/jpeg', 0.45),
  ])

  if (!large.dataUrl) {
    const url = await fileToDataUrl(file)
    return normalizeStoredImage({
      url,
      thumbnailUrl: url,
      mediumUrl: null,
      largeUrl: url,
      placeholderUrl: url,
      width: image.width,
      height: image.height,
      thumbnailWidth: image.width,
      mediumWidth: null,
      largeWidth: image.width,
      mimeType: file.type || 'image/jpeg',
      originalName: file.name,
    })
  }

  return normalizeStoredImage({
    url: large.dataUrl,
    thumbnailUrl: thumbnail.dataUrl || large.dataUrl,
    mediumUrl: null,
    largeUrl: large.dataUrl,
    placeholderUrl: placeholder.dataUrl || null,
    width: image.width,
    height: image.height,
    thumbnailWidth: thumbnail.width,
    mediumWidth: null,
    largeWidth: large.width,
    mimeType,
    originalName: file.name,
  })
}

async function addFiles(files) {
  if (!files?.length) return
  uploading.value = true
  const toAdd = Array.from(files).slice(0, props.maxFiles - images.value.length)
  try {
    for (const file of toAdd) {
      if (!file.type.startsWith('image/')) continue
      const nextImage = await fileToOptimizedImage(file)
      images.value.push({
        ...nextImage,
        order: images.value.length,
        isPrimary: images.value.length === 0,
      })
    }
    emitValue()
  } finally {
    uploading.value = false
  }
}

function remove(i) {
  images.value.splice(i, 1)
  if (images.value.length && !images.value.some(img => img.isPrimary)) images.value[0].isPrimary = true
  emitValue()
}

function setPrimary(i) {
  images.value.forEach((img, idx) => { img.isPrimary = idx === i })
  emitValue()
}

function move(i, delta) {
  const j = i + delta
  if (j < 0 || j >= images.value.length) return
  const a = images.value[i]
  images.value[i] = images.value[j]
  images.value[j] = a
  images.value.forEach((img, idx) => { img.order = idx })
  emitValue()
}

function triggerInput() {
  input.value?.click()
}
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <label class="block text-sm font-medium text-gray-700">Imágenes</label>
      <input
        ref="input"
        type="file"
        accept="image/*"
        multiple
        class="hidden"
        @change="(e) => { addFiles(e.target.files); e.target.value = '' }"
      />
      <button type="button" class="btn-secondary text-sm" :disabled="uploading || images.length >= maxFiles" @click="triggerInput">
        {{ uploading ? 'Procesando...' : 'Subir imágenes' }}
      </button>
    </div>
    <p class="text-xs text-gray-500">
      Máximo {{ maxFiles }} archivos. Se generan automáticamente miniatura, versión media, versión grande y placeholder suave.
      Se prioriza WebP cuando el navegador lo soporta y se conserva la proporción original.
    </p>
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <div
        v-for="(img, i) in images"
        :key="i"
        class="group relative overflow-hidden rounded-xl border-2 bg-gray-100"
        :class="img.isPrimary ? 'border-primary-500' : 'border-transparent'"
      >
        <div class="aspect-[4/3]">
          <img :src="getImageUrl(img, 'thumbnail')" alt="" class="h-full w-full object-cover" loading="lazy" />
        </div>

        <div class="absolute left-2 top-2 rounded-full bg-slate-950/65 px-2 py-1 text-[10px] font-medium uppercase tracking-[0.2em] text-white">
          {{ img.mimeType === 'image/webp' ? 'WebP' : 'JPEG' }}
        </div>

        <div class="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/70 via-black/35 to-transparent p-2 text-[11px] text-white">
          <div class="flex items-center justify-between gap-2">
            <span>{{ img.width || '?' }}×{{ img.height || '?' }}</span>
            <span>{{ img.largeWidth || img.width || '?' }}px</span>
          </div>
        </div>

        <div class="absolute inset-0 flex items-center justify-center gap-1 bg-black/40 opacity-100 transition-opacity md:opacity-0 md:group-hover:opacity-100">
          <button type="button" class="p-1.5 rounded-lg bg-white text-gray-700 hover:bg-gray-100" title="Principal" @click="setPrimary(i)">
            ⭐
          </button>
          <button type="button" class="p-1.5 rounded-lg bg-white text-gray-700 hover:bg-gray-100" title="Izquierda" :disabled="i === 0" @click="move(i, -1)">
            ←
          </button>
          <button type="button" class="p-1.5 rounded-lg bg-white text-gray-700 hover:bg-gray-100" title="Derecha" :disabled="i === images.length - 1" @click="move(i, 1)">
            →
          </button>
          <button type="button" class="p-1.5 rounded-lg bg-red-100 text-red-700 hover:bg-red-200" title="Eliminar" @click="remove(i)">
            ✕
          </button>
        </div>
        <span v-if="img.isPrimary" class="absolute right-2 top-2 px-2 py-0.5 bg-primary-500 text-white text-xs rounded-full">Principal</span>
      </div>
    </div>
  </div>
</template>
