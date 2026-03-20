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
    <div class="grid gap-3 p-2 sm:gap-4 sm:p-3 md:p-5 xl:grid-cols-[minmax(0,1fr)_112px]">
      <div class="order-1 min-w-0 xl:order-1">
        <div
          class="group relative overflow-hidden rounded-[20px] bg-slate-950 sm:rounded-[24px]"
          @touchstart.passive="onTouchStart"
          @touchend.passive="onTouchEnd"
        >
          <button
            type="button"
            class="absolute right-2 top-2 z-10 rounded-full border border-white/15 bg-slate-950/55 px-3 py-1.5 text-[11px] font-medium uppercase tracking-[0.16em] text-white transition hover:bg-slate-950/75 sm:right-3 sm:top-3 sm:px-4 sm:py-2 sm:text-xs sm:tracking-[0.24em]"
            @click="openLightbox"
          >
            Ver grande
          </button>

          <div class="absolute left-2 top-2 z-10 rounded-full border border-white/10 bg-black/35 px-2.5 py-1 text-[11px] font-medium text-white sm:left-3 sm:top-3 sm:px-3 sm:text-xs">
            {{ currentIndex + 1 }} / {{ sortedImages.length }}
          </div>

          <button
            v-if="sortedImages.length > 1"
            type="button"
            class="absolute left-2 top-1/2 z-10 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full border border-white/15 bg-slate-950/50 text-xl text-white transition hover:bg-slate-950/70 sm:left-3 sm:h-11 sm:w-11"
            aria-label="Imagen anterior"
            @click="previous"
          >
            ‹
          </button>

          <button
            v-if="sortedImages.length > 1"
            type="button"
            class="absolute right-2 top-1/2 z-10 flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full border border-white/15 bg-slate-950/50 text-xl text-white transition hover:bg-slate-950/70 sm:right-3 sm:h-11 sm:w-11"
            aria-label="Imagen siguiente"
            @click="next"
          >
            ›
          </button>

          <button
            type="button"
            class="flex w-full items-center justify-center px-2 py-2 sm:px-3 sm:py-3 md:px-6 md:py-5"
            :aria-label="`Abrir galería de ${alt}`"
            @click="openLightbox"
          >
            <div class="mx-auto flex h-[220px] w-full max-w-4xl items-center justify-center sm:h-[280px] md:h-[380px] lg:h-[430px]">
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

      <div v-if="sortedImages.length > 1" class="order-2 min-w-0 xl:order-2">
        <div class="flex gap-2 overflow-x-auto pb-1 xl:max-h-[35rem] xl:flex-col xl:gap-3 xl:overflow-y-auto xl:pb-0">
          <button
            v-for="(img, i) in sortedImages"
            :key="`${img.url || img.largeUrl}-${i}`"
            type="button"
            class="relative h-16 w-20 flex-shrink-0 overflow-hidden rounded-xl border-2 bg-gray-100 transition sm:h-20 sm:w-24 sm:rounded-2xl xl:h-24 xl:w-full"
            :class="i === currentIndex ? 'border-emerald-700 shadow-sm' : 'border-transparent hover:border-emerald-200'"
            :aria-label="`Ver imagen ${i + 1}`"
            @click="setIndex(i)"
          >
            <ResponsiveImage
              :image="img"
              :alt="`${alt} miniatura ${i + 1}`"
              variant="thumbnail"
              :eager="i < 5"
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

