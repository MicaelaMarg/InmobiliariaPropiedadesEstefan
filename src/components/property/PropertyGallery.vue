<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  images: { type: Array, default: () => [] },
  alt: { type: String, default: 'Imagen de la propiedad' },
})

const currentIndex = ref(0)

const sortedImages = computed(() => {
  const list = [...(props.images || [])].sort((a, b) => (a.order ?? 0) - (b.order ?? 0))
  return list.length ? list : [{ url: 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=1200', isPrimary: true }]
})

const currentImage = computed(() => sortedImages.value[currentIndex.value] || sortedImages.value[0] || null)

watch(() => props.images, () => { currentIndex.value = 0 }, { deep: true })

function setIndex(i) {
  currentIndex.value = i
}
</script>

<template>
  <div class="overflow-hidden rounded-[28px] border border-gray-200 bg-white shadow-card">
    <div class="relative flex items-center justify-center bg-slate-950 px-3 py-3 md:px-6 md:py-5">
      <img
        :src="currentImage?.url"
        :alt="alt"
        class="h-[320px] w-full object-contain md:h-[560px]"
      />
    </div>

    <div v-if="sortedImages.length > 1" class="border-t border-gray-200 bg-white px-4 py-4">
      <div class="flex gap-3 overflow-x-auto pb-1">
        <button
          v-for="(img, i) in sortedImages"
          :key="`${img.url}-${i}`"
          type="button"
          class="relative h-20 w-24 flex-shrink-0 overflow-hidden rounded-2xl border-2 bg-gray-100 transition"
          :class="i === currentIndex ? 'border-primary-500 shadow-sm' : 'border-transparent hover:border-gray-300'"
          :aria-label="`Ver imagen ${i + 1}`"
          @click="setIndex(i)"
        >
          <img
            :src="img.url"
            :alt="`${alt} ${i + 1}`"
            class="h-full w-full object-cover"
          />
        </button>
      </div>
    </div>
  </div>
</template>
