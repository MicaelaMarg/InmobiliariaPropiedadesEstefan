<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  maxFiles: { type: Number, default: 20 },
})

const emit = defineEmits(['update:modelValue'])
const input = ref(null)
const uploading = ref(false)

const images = ref(props.modelValue.length ? props.modelValue.map(img => ({ url: img.url, order: img.order ?? 0, isPrimary: !!img.isPrimary })) : [])

watch(() => props.modelValue, (v) => {
  if (v?.length) images.value = v.map(img => ({ url: img.url, order: img.order ?? 0, isPrimary: !!img.isPrimary }))
  else images.value = []
}, { deep: true })

function emitValue() {
  emit('update:modelValue', images.value.map((img, idx) => ({ url: img.url, order: idx, isPrimary: !!img.isPrimary })))
}

function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(new Error('No se pudo leer la imagen'))
    reader.readAsDataURL(file)
  })
}

async function addFiles(files) {
  if (!files?.length) return
  uploading.value = true
  const toAdd = Array.from(files).slice(0, props.maxFiles - images.value.length)
  try {
    for (const file of toAdd) {
      if (!file.type.startsWith('image/')) continue
      const url = await fileToDataUrl(file)
      images.value.push({
        url,
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
    <p class="text-xs text-gray-500">Podés elegir la imagen principal y reordenar. Máximo {{ maxFiles }}.</p>
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
      <div
        v-for="(img, i) in images"
        :key="i"
        class="relative aspect-square rounded-xl overflow-hidden bg-gray-100 border-2"
        :class="img.isPrimary ? 'border-primary-500' : 'border-transparent'"
      >
        <img :src="img.url" alt="" class="w-full h-full object-cover" />
        <div class="absolute inset-0 bg-black/40 opacity-0 hover:opacity-100 transition-opacity flex items-center justify-center gap-1">
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
        <span v-if="img.isPrimary" class="absolute top-1 left-1 px-2 py-0.5 bg-primary-500 text-white text-xs rounded-full">Principal</span>
      </div>
    </div>
  </div>
</template>
