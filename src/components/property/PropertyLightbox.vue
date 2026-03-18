<script setup>
import { computed, onBeforeUnmount, watch, ref } from 'vue'
import ResponsiveImage from '../ui/ResponsiveImage.vue'
import { sortPropertyImages } from '../../utils/propertyImages'

const props = defineProps({
  open: { type: Boolean, default: false },
  images: { type: Array, default: () => [] },
  initialIndex: { type: Number, default: 0 },
  alt: { type: String, default: 'Imagen de la propiedad' },
})

const emit = defineEmits(['close', 'update:index'])
const currentIndex = ref(props.initialIndex)
const zoom = ref(1)

const sortedImages = computed(() => sortPropertyImages(props.images))
const currentImage = computed(() => sortedImages.value[currentIndex.value] || sortedImages.value[0] || null)

function clampIndex(index) {
  if (!sortedImages.value.length) return 0
  if (index < 0) return sortedImages.value.length - 1
  if (index >= sortedImages.value.length) return 0
  return index
}

function close() {
  emit('close')
}

function setIndex(index) {
  currentIndex.value = clampIndex(index)
  zoom.value = 1
  emit('update:index', currentIndex.value)
}

function previous() {
  if (sortedImages.value.length <= 1) return
  setIndex(currentIndex.value - 1)
}

function next() {
  if (sortedImages.value.length <= 1) return
  setIndex(currentIndex.value + 1)
}

function zoomIn() {
  zoom.value = Math.min(zoom.value + 0.5, 4)
}

function zoomOut() {
  zoom.value = Math.max(zoom.value - 0.5, 1)
}

function toggleZoom() {
  zoom.value = zoom.value > 1 ? 1 : 2
}

function onKeydown(event) {
  if (!props.open) return

  if (event.key === 'Escape') close()
  if (event.key === 'ArrowLeft') previous()
  if (event.key === 'ArrowRight') next()
  if (event.key === '+' || event.key === '=') zoomIn()
  if (event.key === '-') zoomOut()
}

watch(
  () => props.initialIndex,
  (nextIndex) => {
    currentIndex.value = clampIndex(nextIndex)
    zoom.value = 1
  },
  { immediate: true }
)

watch(
  () => props.open,
  (isOpen) => {
    if (typeof window === 'undefined') return

    if (isOpen) {
      currentIndex.value = clampIndex(props.initialIndex)
      zoom.value = 1
      window.addEventListener('keydown', onKeydown)
      document.body.style.overflow = 'hidden'
      return
    }

    window.removeEventListener('keydown', onKeydown)
    document.body.style.overflow = ''
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('keydown', onKeydown)
    document.body.style.overflow = ''
  }
})
</script>

<template>
  <Teleport to="body">
    <transition name="property-lightbox">
      <div
        v-if="open"
        class="fixed inset-0 z-[80] bg-slate-950/95 backdrop-blur-sm"
        @click.self="close"
      >
        <div class="flex h-full flex-col">
          <div class="sticky top-0 z-10 flex items-center justify-between gap-3 px-4 py-4 text-white md:px-6 bg-slate-950/95 backdrop-blur-md border-b border-white/10">
            <div>
              <p class="text-sm font-medium uppercase tracking-[0.24em] text-white/70">Galería</p>
              <p class="text-sm text-white/80">{{ currentIndex + 1 }} / {{ sortedImages.length }}</p>
            </div>

          <div class="flex items-center gap-2">
            <button type="button" class="rounded-full border border-white/30 bg-black/40 px-3 py-2 text-sm transition hover:bg-black/60 shadow-lg shadow-black/30" @click="zoomOut">
              -
            </button>
            <button type="button" class="rounded-full border border-white/30 bg-black/40 px-3 py-2 text-sm transition hover:bg-black/60 shadow-lg shadow-black/30" @click="zoomIn">
              +
            </button>
            <button type="button" class="rounded-full border border-white/30 bg-black/50 px-4 py-2 text-sm transition hover:bg-black/70 shadow-lg shadow-black/30" @click="close">
              Cerrar
            </button>
          </div>
          </div>

          <div class="flex min-h-0 flex-1 items-center gap-3 px-3 pb-3 md:gap-5 md:px-6 md:pb-6">
            <button
              v-if="sortedImages.length > 1"
              type="button"
              class="hidden h-12 w-12 items-center justify-center rounded-full border border-white/15 bg-white/10 text-white transition hover:bg-white/20 md:flex"
              aria-label="Imagen anterior"
              @click="previous"
            >
              ‹
            </button>

            <div class="flex min-h-0 flex-1 flex-col gap-4">
              <div class="relative min-h-0 flex-1 overflow-hidden rounded-[28px] border border-white/10 bg-white/5">
                <div class="flex h-full w-full items-center justify-center overflow-auto p-3 md:p-6">
                  <div
                    class="h-full w-full max-w-full transition-transform duration-200 ease-out"
                    :style="{ transform: `scale(${zoom})`, transformOrigin: 'center center' }"
                    @dblclick="toggleZoom"
                  >
                    <ResponsiveImage
                      :image="currentImage"
                      :alt="`${alt} ${currentIndex + 1}`"
                      variant="large"
                      :eager="true"
                      :contain="true"
                      class="h-full w-full"
                    />
                  </div>
                </div>

                <button
                  v-if="sortedImages.length > 1"
                  type="button"
                  class="absolute left-3 top-1/2 flex h-11 w-11 -translate-y-1/2 items-center justify-center rounded-full border border-white/30 bg-black/50 text-white transition hover:bg-black/70 shadow-lg shadow-black/30 md:hidden"
                  aria-label="Imagen anterior"
                  @click="previous"
                >
                  ‹
                </button>
                <button
                  v-if="sortedImages.length > 1"
                  type="button"
                  class="absolute right-3 top-1/2 flex h-11 w-11 -translate-y-1/2 items-center justify-center rounded-full border border-white/30 bg-black/50 text-white transition hover:bg-black/70 shadow-lg shadow-black/30 md:hidden"
                  aria-label="Imagen siguiente"
                  @click="next"
                >
                  ›
                </button>
              </div>

              <div v-if="sortedImages.length > 1" class="flex gap-3 overflow-x-auto pb-1">
                <button
                  v-for="(image, index) in sortedImages"
                  :key="`${image.url || image.largeUrl}-${index}`"
                  type="button"
                  class="relative h-20 w-24 flex-shrink-0 overflow-hidden rounded-2xl border-2 transition"
                  :class="index === currentIndex ? 'border-white shadow-lg shadow-black/20' : 'border-white/10 opacity-70 hover:opacity-100'"
                  :aria-label="`Ver imagen ${index + 1}`"
                  @click="setIndex(index)"
                >
                  <ResponsiveImage
                    :image="image"
                    :alt="`${alt} miniatura ${index + 1}`"
                    variant="thumbnail"
                    :eager="index < 5"
                    class="h-full w-full"
                  />
                </button>
              </div>
            </div>

            <button
              v-if="sortedImages.length > 1"
              type="button"
              class="hidden h-12 w-12 items-center justify-center rounded-full border border-white/15 bg-white/10 text-white transition hover:bg-white/20 md:flex"
              aria-label="Imagen siguiente"
              @click="next"
            >
              ›
            </button>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<style scoped>
.property-lightbox-enter-active,
.property-lightbox-leave-active {
  transition: opacity 0.25s ease;
}

.property-lightbox-enter-from,
.property-lightbox-leave-to {
  opacity: 0;
}
</style>
