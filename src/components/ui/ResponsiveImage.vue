<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { buildImageSrcSet, getImageDimensions, getImagePlaceholder, getImageUrl } from '../../utils/propertyImages'

const props = defineProps({
  image: { type: Object, default: null },
  alt: { type: String, default: '' },
  variant: { type: String, default: 'medium' },
  srcsetVariants: { type: Array, default: () => ['thumbnail', 'medium', 'large'] },
  sizes: { type: String, default: '' },
  eager: { type: Boolean, default: false },
  contain: { type: Boolean, default: false },
  useIntrinsicRatio: { type: Boolean, default: false },
  rootMargin: { type: String, default: '280px' },
  imgClass: { type: String, default: '' },
})

const container = ref(null)
const isVisible = ref(props.eager)
const isLoaded = ref(false)
let observer = null

const placeholder = computed(() => getImagePlaceholder(props.image))
const src = computed(() => getImageUrl(props.image, props.variant))
const srcSet = computed(() => buildImageSrcSet(props.image, props.srcsetVariants))
const dimensions = computed(() => getImageDimensions(props.image))
const wrapperStyle = computed(() => {
  if (!props.useIntrinsicRatio) return undefined
  if (!dimensions.value.width || !dimensions.value.height) return undefined

  return {
    aspectRatio: `${dimensions.value.width} / ${dimensions.value.height}`,
  }
})

watch(
  () => props.image,
  () => {
    isLoaded.value = false
  },
  { deep: true }
)

watch(
  () => props.eager,
  (nextEager) => {
    if (nextEager) {
      isVisible.value = true
      disconnectObserver()
    }
  }
)

function disconnectObserver() {
  if (observer) {
    observer.disconnect()
    observer = null
  }
}

function observeVisibility() {
  if (props.eager || isVisible.value || !container.value || typeof IntersectionObserver === 'undefined') {
    isVisible.value = true
    return
  }

  observer = new IntersectionObserver((entries) => {
    if (entries.some(entry => entry.isIntersecting)) {
      isVisible.value = true
      disconnectObserver()
    }
  }, { rootMargin: props.rootMargin })

  observer.observe(container.value)
}

function onLoad() {
  isLoaded.value = true
}

onMounted(() => {
  observeVisibility()
})

onBeforeUnmount(() => {
  disconnectObserver()
})
</script>

<template>
  <div ref="container" class="relative overflow-hidden" :style="wrapperStyle">
    <div class="absolute inset-0 bg-slate-200/70" :class="isLoaded ? 'opacity-0' : 'opacity-100'" />

    <img
      v-if="placeholder && !isLoaded"
      :src="placeholder"
      alt=""
      aria-hidden="true"
      class="absolute inset-0 h-full w-full scale-110 object-cover blur-2xl transition-opacity duration-500"
    />

    <div
      v-if="!isLoaded"
      class="absolute inset-0 animate-pulse bg-gradient-to-br from-white/25 via-white/10 to-transparent"
    />

    <img
      v-if="isVisible"
      :src="src"
      :srcset="srcSet || undefined"
      :sizes="sizes || undefined"
      :alt="alt"
      :loading="eager ? 'eager' : 'lazy'"
      decoding="async"
      class="relative h-full w-full transition-all duration-500 ease-out"
      :class="[
        contain ? 'object-contain' : 'object-cover',
        isLoaded ? 'opacity-100 blur-0 scale-100' : 'opacity-0 blur-xl scale-[1.03]',
        imgClass,
      ]"
      @load="onLoad"
    />
  </div>
</template>
