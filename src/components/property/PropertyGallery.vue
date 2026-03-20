<script setup>
import { computed, ref, watch } from 'vue'
import PropertyLightbox from './PropertyLightbox.vue'
import ResponsiveImage from '../ui/ResponsiveImage.vue'
import { sortPropertyImages } from '../../utils/propertyImages'

const props = defineProps({
  images: { type: Array, default: () => [] },
  alt: { type: String, default: 'Imagen de la propiedad' },
})

const currentIndex = ref(0)
const isLightboxOpen = ref(false)
const touchStartX = ref(null)

const sortedImages = computed(() => sortPropertyImages(props.images))

const currentImage = computed(() => sortedImages.value[currentIndex.value] || sortedImages.value[0] || null)

watch(() => props.images, () => { currentIndex.value = 0 }, { deep: true })

function setIndex(i) {
  currentIndex.value = i
}

function previous() {
  if (sortedImages.value.length <= 1) return
  currentIndex.value = currentIndex.value === 0 ? sortedImages.value.length - 1 : currentIndex.value - 1
}

function next() {
  if (sortedImages.value.length <= 1) return
  currentIndex.value = currentIndex.value === sortedImages.value.length - 1 ? 0 : currentIndex.value + 1
}

function openLightbox() {
  isLightboxOpen.value = true
}

function onTouchStart(event) {
  touchStartX.value = event.changedTouches?.[0]?.clientX ?? null
}

function onTouchEnd(event) {
  if (touchStartX.value == null) return

  const endX = event.changedTouches?.[0]?.clientX ?? touchStartX.value
  const delta = endX - touchStartX.value
  touchStartX.value = null

  if (Math.abs(delta) < 40) return
  if (delta > 0) previous()
  else next()
}
</script>

<template>
  <div class="overflow-hidden rounded-[28px] border border-gray-200 bg-white shadow-card">
    <div class="grid gap-4 p-3 md:p-5 xl:grid-cols-[minmax(0,1fr)_112px]">
      <div class="order-2 xl:order-1">
        <div
          class="group relative overflow-hidden rounded-[24px] bg-slate-950"
          @touchstart.passive="onTouchStart"
          @touchend.passive="onTouchEnd"
        >
          <button
            type="button"
            class="absolute right-3 top-3 z-10 rounded-full border border-white/15 bg-slate-950/55 px-4 py-2 text-xs font-medium uppercase tracking-[0.24em] text-white transition hover:bg-slate-950/75"
            @click="openLightbox"
          >
            Ver grande
          </button>

          <div class="absolute left-3 top-3 z-10 rounded-full border border-white/10 bg-black/35 px-3 py-1 text-xs font-medium text-white">
            {{ currentIndex + 1 }} / {{ sortedImages.length }}
          </div>

          <button
            v-if="sortedImages.length > 1"
            type="button"
            class="absolute left-3 top-1/2 z-10 flex h-11 w-11 -translate-y-1/2 items-center justify-center rounded-full border border-white/15 bg-slate-950/50 text-xl text-white transition hover:bg-slate-950/70"
            aria-label="Imagen anterior"
            @click="previous"
          >
            ‹
          </button>

          <button
            v-if="sortedImages.length > 1"
            type="button"
            class="absolute right-3 top-1/2 z-10 flex h-11 w-11 -translate-y-1/2 items-center justify-center rounded-full border border-white/15 bg-slate-950/50 text-xl text-white transition hover:bg-slate-950/70"
            aria-label="Imagen siguiente"
            @click="next"
          >
            ›
          </button>

          <button
            type="button"
            class="flex w-full items-center justify-center px-3 py-3 md:px-6 md:py-5"
            :aria-label="`Abrir galería de ${alt}`"
            @click="openLightbox"
          >
            <div class="mx-auto flex h-[260px] w-full max-w-4xl items-center justify-center sm:h-[320px] md:h-[380px] lg:h-[430px]">
              <ResponsiveImage
                :image="currentImage"
                :alt="`${alt} ${currentIndex + 1}`"
                variant="large"
                sizes="(min-width: 1280px) 880px, 100vw"
                :eager="true"
                :contain="true"
                class="h-full w-full"
              />
            </div>
          </button>
        </div>
      </div>

      <div v-if="sortedImages.length > 1" class="order-1 xl:order-2">
        <div class="flex gap-3 overflow-x-auto pb-1 xl:max-h-[35rem] xl:flex-col xl:overflow-y-auto xl:pb-0">
          <button
            v-for="(img, i) in sortedImages"
            :key="`${img.url || img.largeUrl}-${i}`"
            type="button"
            class="relative h-20 w-24 flex-shrink-0 overflow-hidden rounded-2xl border-2 bg-gray-100 transition xl:h-24 xl:w-full"
            :class="i === currentIndex ? 'border-emerald-700 shadow-sm' : 'border-transparent hover:border-emerald-200'"
            :aria-label="`Ver imagen ${i + 1}`"
            @click="setIndex(i)"
          >
            <ResponsiveImage
              :image="img"
              :alt="`${alt} miniatura ${i + 1}`"
              variant="thumbnail"
              :eager="i < 5"
              :contain-portrait="true"
              class="h-full w-full"
            />
          </button>
        </div>
      </div>
    </div>

    <PropertyLightbox
      :open="isLightboxOpen"
      :images="sortedImages"
      :initial-index="currentIndex"
      :alt="alt"
      @close="isLightboxOpen = false"
      @update:index="setIndex"
    />
  </div>
</template>
