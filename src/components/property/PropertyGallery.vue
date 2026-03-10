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

const currentImage = computed(() => sortedImages.value[currentIndex.value]?.url || sortedImages.value[0]?.url)

watch(() => props.images, () => { currentIndex.value = 0 }, { deep: true })

function setIndex(i) {
  currentIndex.value = i
}
</script>

<template>
  <div class="rounded-2xl overflow-hidden bg-gray-100">
    <div class="aspect-[4/3] md:aspect-[21/9] relative">
      <img
        :src="currentImage"
        :alt="alt"
        class="w-full h-full object-cover"
      />
      <div
        v-if="sortedImages.length > 1"
        class="absolute bottom-3 left-0 right-0 flex justify-center gap-2"
      >
        <button
          v-for="(img, i) in sortedImages"
          type="button"
          class="w-2.5 h-2.5 rounded-full transition-colors"
          :class="i === currentIndex ? 'bg-white ring-2 ring-primary-500' : 'bg-white/60 hover:bg-white/80'"
          :aria-label="`Ver imagen ${i + 1}`"
          @click="setIndex(i)"
        />
      </div>
    </div>
  </div>
</template>
