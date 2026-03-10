<script setup>
import { ref, watch } from 'vue'
import { PROPERTY_TYPES, OPERATIONS } from '../../data/mockProperties'

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['update:modelValue', 'search'])

const filters = ref({
  operation: props.modelValue.operation || '',
  type: props.modelValue.type || '',
  minPrice: props.modelValue.minPrice ?? '',
  maxPrice: props.modelValue.maxPrice ?? '',
  location: props.modelValue.location || '',
})

watch(
  filters,
  (v) => {
    const out = {}
    if (v.operation) out.operation = v.operation
    if (v.type) out.type = v.type
    if (v.minPrice !== '') out.minPrice = Number(v.minPrice)
    if (v.maxPrice !== '') out.maxPrice = Number(v.maxPrice)
    if (v.location?.trim()) out.location = v.location.trim()
    emit('update:modelValue', out)
  },
  { deep: true }
)

function clearFilters() {
  filters.value = { operation: '', type: '', minPrice: '', maxPrice: '', location: '' }
  emit('search')
}
</script>

<template>
  <div class="bg-white rounded-2xl shadow-card p-4 md:p-6">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Operación</label>
        <select
          v-model="filters.operation"
          class="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
        >
          <option value="">Todas</option>
          <option v-for="op in OPERATIONS" :key="op.value" :value="op.value">{{ op.label }}</option>
        </select>
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
        <select
          v-model="filters.type"
          class="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
        >
          <option value="">Todos</option>
          <option v-for="t in PROPERTY_TYPES" :key="t.value" :value="t.value">{{ t.label }}</option>
        </select>
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Precio mín.</label>
        <input
          v-model.number="filters.minPrice"
          type="number"
          min="0"
          placeholder="0"
          class="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
        />
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Precio máx.</label>
        <input
          v-model.number="filters.maxPrice"
          type="number"
          min="0"
          placeholder="Sin tope"
          class="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
        />
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Ubicación</label>
        <input
          v-model="filters.location"
          type="text"
          placeholder="Ciudad o zona"
          class="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
        />
      </div>
    </div>
    <div class="mt-4 flex justify-end gap-2">
      <button type="button" class="btn-secondary text-sm" @click="clearFilters">Limpiar</button>
      <button type="button" class="btn-primary text-sm" @click="$emit('search')">Buscar</button>
    </div>
  </div>
</template>
