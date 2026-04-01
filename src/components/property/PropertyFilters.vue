<script setup>
import { ref, watch } from 'vue'
import {
  PROPERTY_TYPES,
  OPERATIONS,
  HIGHLIGHTED_MESSAGE_OPTIONS,
  PAYMENT_OPTION_OPTIONS,
} from '../../data/mockProperties'

const props = defineProps({
  modelValue: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['update:modelValue', 'search'])

function buildFiltersState(value = {}) {
  return {
    operation: value.operation || '',
    operationTag: value.operationTag || '',
    type: value.type || '',
    minPrice: value.minPrice ?? '',
    maxPrice: value.maxPrice ?? '',
    location: value.location || '',
  }
}

const filters = ref(buildFiltersState(props.modelValue))

watch(
  () => props.modelValue,
  (nextValue) => {
    const nextFilters = buildFiltersState(nextValue)
    if (JSON.stringify(nextFilters) !== JSON.stringify(filters.value)) {
      filters.value = nextFilters
    }
  },
  { deep: true }
)

watch(
  filters,
  (v) => {
    const out = {}
    if (v.operation) out.operation = v.operation
    if (v.operationTag) out.operationTag = v.operationTag
    if (v.type) out.type = v.type
    if (v.minPrice !== '') out.minPrice = Number(v.minPrice)
    if (v.maxPrice !== '') out.maxPrice = Number(v.maxPrice)
    if (v.location?.trim()) out.location = v.location.trim()
    emit('update:modelValue', out)
  },
  { deep: true }
)

function clearFilters() {
  filters.value = {
    operation: '',
    operationTag: '',
    type: '',
    minPrice: '',
    maxPrice: '',
    location: '',
  }
  emit('search')
}
</script>

<template>
  <div class="bg-white rounded-2xl shadow-card p-4 md:p-6">
    <div class="pb-2">
      <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">
        <div class="2xl:min-w-0">
          <label class="block text-sm font-medium text-gray-700 mb-1">Operación</label>
          <select
            v-model="filters.operation"
            class="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm focus:border-emerald-700 focus:ring-2 focus:ring-emerald-200"
          >
            <option value="">Todas</option>
            <option v-for="op in OPERATIONS" :key="op.value" :value="op.value">{{ op.label }}</option>
          </select>
        </div>
        <div class="2xl:min-w-0">
          <label class="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
          <select
            v-model="filters.type"
            class="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm focus:border-emerald-700 focus:ring-2 focus:ring-emerald-200"
          >
            <option value="">Todos</option>
            <option v-for="t in PROPERTY_TYPES" :key="t.value" :value="t.value">{{ t.label }}</option>
          </select>
        </div>
        <div class="2xl:min-w-0">
          <label class="block text-sm font-medium text-gray-700 mb-1">Precio mín.</label>
          <input
            v-model.number="filters.minPrice"
            type="number"
            min="0"
            placeholder="0"
            class="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm focus:border-emerald-700 focus:ring-2 focus:ring-emerald-200"
          />
        </div>
        <div class="2xl:min-w-0">
          <label class="block text-sm font-medium text-gray-700 mb-1">Precio máx.</label>
          <input
            v-model.number="filters.maxPrice"
            type="number"
            min="0"
            placeholder="Sin tope"
            class="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm focus:border-emerald-700 focus:ring-2 focus:ring-emerald-200"
          />
        </div>
        <div class="2xl:min-w-0">
          <label class="block text-sm font-medium text-gray-700 mb-1">Condición especial</label>
          <select
            v-model="filters.operationTag"
            class="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm focus:border-emerald-700 focus:ring-2 focus:ring-emerald-200"
          >
            <option value="">Todos</option>
            <optgroup label="Mensajes destacados">
              <option v-for="option in HIGHLIGHTED_MESSAGE_OPTIONS" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </optgroup>
            <optgroup label="Formas de pago">
              <option v-for="option in PAYMENT_OPTION_OPTIONS" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </optgroup>
          </select>
        </div>
        <div class="md:col-span-2 xl:col-span-2 2xl:col-span-1 2xl:min-w-0">
          <label class="block text-sm font-medium text-gray-700 mb-1">Ubicación</label>
          <input
            v-model="filters.location"
            type="text"
            placeholder="Ciudad o zona"
            class="w-full rounded-xl border border-gray-300 px-3 py-2 text-sm focus:border-emerald-700 focus:ring-2 focus:ring-emerald-200"
          />
        </div>
      </div>
    </div>
    <div class="mt-4 flex justify-end gap-2">
      <button type="button" class="btn-secondary text-sm" @click="clearFilters">Limpiar</button>
      <button
        type="button"
        class="inline-flex items-center justify-center rounded-xl bg-[#0b5b38] px-5 py-2.5 text-sm font-medium text-white transition-colors hover:bg-[#08472c] focus:outline-none focus:ring-2 focus:ring-emerald-200 focus:ring-offset-2"
        @click="$emit('search')"
      >
        Buscar
      </button>
    </div>
  </div>
</template>
