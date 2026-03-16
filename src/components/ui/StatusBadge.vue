<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, default: 'available' },
  operation: { type: String, default: '' },
})

const statusLabel = computed(() => {
  const map = { available: 'Disponible', reserved: 'Reservado', sold: 'Vendido' }
  return map[props.status] || props.status
})

const statusClass = computed(() => {
  const map = {
    available: 'bg-green-100 text-green-800',
    reserved: 'bg-amber-100 text-amber-800',
    sold: 'bg-gray-200 text-gray-700',
  }
  return map[props.status] || 'bg-gray-100 text-gray-700'
})

const operationLabel = computed(() => {
  return props.operation === 'venta' ? 'Venta' : props.operation === 'alquiler' ? 'Alquiler' : ''
})

const operationClass = computed(() => {
  return props.operation === 'venta'
    ? 'bg-emerald-100 text-emerald-900'
    : props.operation === 'alquiler'
      ? 'bg-emerald-200 text-emerald-900'
      : 'bg-gray-100 text-gray-700'
})
</script>

<template>
  <div class="flex flex-wrap gap-1.5">
    <span
      v-if="operation"
      class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
      :class="operationClass"
    >
      {{ operationLabel }}
    </span>
    <span
      class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
      :class="statusClass"
    >
      {{ statusLabel }}
    </span>
  </div>
</template>
