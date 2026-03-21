<script setup>
import { onBeforeUnmount, ref, watch } from 'vue'
import { getImageUrl } from '../../utils/propertyImages'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  maxFiles: { type: Number, default: 20 },
})

const MAX_UPLOAD_DIMENSION = 1600
const JPEG_QUALITY = 0.82
const WEBP_QUALITY = 0.8

const emit = defineEmits(['update:modelValue'])
const input = ref(null)
const uploading = ref(false)

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
    uploadToken: image.uploadToken || null,
    file: image.file instanceof File ? image.file : null,
    objectUrl: image.objectUrl || null,
    url: image.url || image.largeUrl || image.mediumUrl || image.thumbnailUrl || image.objectUrl || '',
    thumbnailUrl: image.thumbnailUrl || image.mediumUrl || image.url || image.objectUrl || '',
    mediumUrl: image.mediumUrl || null,
    largeUrl: image.largeUrl || image.url || image.mediumUrl || image.thumbnailUrl || image.objectUrl || '',
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
    id: image.id ?? null,
    uploadToken: image.uploadToken || null,
    file: image.file instanceof File ? image.file : null,
    objectUrl: image.objectUrl || null,
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

function revokeObjectUrl(image) {
  if (image?.objectUrl && image.objectUrl.startsWith('blob:')) {
    URL.revokeObjectURL(image.objectUrl)
  }
}

function cleanupObjectUrls(list = []) {
  list.forEach(revokeObjectUrl)
}

function loadImage(url) {
  return new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => {
      resolve(image)
    }
    image.onerror = () => {
      reject(new Error('No se pudo procesar la imagen'))
    }
    image.src = url
  })
}

function createCanvas(width, height) {
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  return canvas
}

function detectPreferredMimeType() {
  const canvas = createCanvas(1, 1)
  try {
    return canvas.toDataURL('image/webp').startsWith('data:image/webp')
      ? 'image/webp'
      : 'image/jpeg'
  } catch {
    return 'image/jpeg'
  }
}

function canvasToBlob(canvas, mimeType, quality) {
  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (!blob) {
        reject(new Error('No se pudo optimizar la imagen'))
        return
      }
      resolve(blob)
    }, mimeType, quality)
  })
}

function buildOptimizedFilename(originalName = '', mimeType = 'image/jpeg') {
  const baseName = originalName.replace(/\.[^.]+$/, '') || 'imagen'
  const extension = mimeType === 'image/webp' ? 'webp' : 'jpg'
  return `${baseName}.${extension}`
}

async function optimizeImageFile(file) {
  const sourceUrl = URL.createObjectURL(file)

  try {
    const image = await loadImage(sourceUrl)
    const longestSide = Math.max(image.width, image.height)
    const scale = longestSide > MAX_UPLOAD_DIMENSION ? MAX_UPLOAD_DIMENSION / longestSide : 1
    const width = Math.max(1, Math.round(image.width * scale))
    const height = Math.max(1, Math.round(image.height * scale))
    const canvas = createCanvas(width, height)
    const context = canvas.getContext('2d')

    if (!context) {
      return {
        file,
        width: image.width,
        height: image.height,
      }
    }

    context.imageSmoothingEnabled = true
    context.imageSmoothingQuality = 'high'
    context.drawImage(image, 0, 0, width, height)

    const mimeType = detectPreferredMimeType()
    const quality = mimeType === 'image/webp' ? WEBP_QUALITY : JPEG_QUALITY
    const blob = await canvasToBlob(canvas, mimeType, quality)

    const optimizedFile = new File(
      [blob],
      buildOptimizedFilename(file.name, mimeType),
      { type: mimeType, lastModified: Date.now() }
    )

    return {
      file: optimizedFile,
      width,
      height,
    }
  } finally {
    URL.revokeObjectURL(sourceUrl)
  }
}

function createUploadToken() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `img_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`
}

async function fileToUploadImage(file) {
  const optimized = await optimizeImageFile(file)
  const objectUrl = URL.createObjectURL(optimized.file)
  const image = await loadImage(objectUrl)
  return {
    uploadToken: createUploadToken(),
    file: optimized.file,
    objectUrl,
    url: objectUrl,
    thumbnailUrl: objectUrl,
    mediumUrl: objectUrl,
    largeUrl: objectUrl,
    placeholderUrl: null,
    width: image.width,
    height: image.height,
    thumbnailWidth: image.width,
    mediumWidth: image.width,
    largeWidth: image.width,
    mimeType: optimized.file.type || 'image/jpeg',
    originalName: file.name,
  }
}

async function addFiles(files) {
  if (!files?.length) return
  uploading.value = true
  const toAdd = Array.from(files).slice(0, props.maxFiles - images.value.length)
  try {
    for (const file of toAdd) {
      if (!file.type.startsWith('image/')) continue
      const nextImage = await fileToUploadImage(file)
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
  revokeObjectUrl(images.value[i])
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

onBeforeUnmount(() => {
  cleanupObjectUrls(images.value)
})
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
      Máximo {{ maxFiles }} archivos. Antes de subir, las imágenes se optimizan para que el guardado sea más rápido y liviano.
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
