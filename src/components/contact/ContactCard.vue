<script setup>
import { computed } from 'vue'
import { useAppStore } from '../../stores/app'

const props = defineProps({
  property: { type: Object, default: null },
})

const app = useAppStore()
const settings = app.settings

const phone = computed(() => props.property?.contactPhone || settings.phone)
const email = computed(() => props.property?.contactEmail || settings.email)
</script>

<template>
  <div class="flex flex-wrap gap-4 text-gray-600">
    <span v-if="phone">{{ phone }}</span>
    <span v-if="email">{{ email }}</span>
    <span v-if="!phone && !email">—</span>
  </div>
</template>
