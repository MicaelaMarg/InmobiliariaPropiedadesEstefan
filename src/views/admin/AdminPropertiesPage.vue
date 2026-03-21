<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import StatusBadge from '../../components/ui/StatusBadge.vue'
import LoadingSpinner from '../../components/ui/LoadingSpinner.vue'
import EmptyState from '../../components/ui/EmptyState.vue'
import { fetchAllProperties, deleteProperty, updateProperty, createProperty, slugify } from '../../services/properties'

const router = useRouter()
const route = useRoute()
const list = ref([])
const loading = ref(true)
const search = ref('')
const filterStatus = ref('')
const toastMessage = ref('')
let toastTimer = null

function showToast(message) {
  toastMessage.value = message
  if (toastTimer) {
    clearTimeout(toastTimer)
  }
  toastTimer = setTimeout(() => {
    toastMessage.value = ''
    toastTimer = null
  }, 3500)
}

onMounted(() => {
  const saved = route.query.saved
  if (saved === 'published') {
    showToast('Propiedad publicada correctamente.')
    router.replace({ path: route.path, query: {} })
  } else if (saved === 'created') {
    showToast('Propiedad guardada correctamente.')
    router.replace({ path: route.path, query: {} })
  } else if (saved === 'updated') {
    showToast('Propiedad actualizada correctamente.')
    router.replace({ path: route.path, query: {} })
  }
  load()
})

async function load() {
  loading.value = true
  try {
    list.value = await fetchAllProperties({ search: search.value || undefined, status: filterStatus.value || undefined })
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function goNew() {
  router.push({ name: 'AdminPropertyNew' })
}

function goEdit(id) {
  router.push({ name: 'AdminPropertyEdit', params: { id } })
}

async function togglePublish(prop) {
  try {
    await updateProperty(prop.id, { isPublished: !prop.isPublished })
    prop.isPublished = !prop.isPublished
  } catch (e) {
    alert('Error al actualizar: ' + (e.message || e))
  }
}

async function duplicate(prop) {
  try {
    const copy = { ...prop }
    delete copy.id
    delete copy.slug
    delete copy.createdAt
    delete copy.updatedAt
    copy.title = 'Copia de ' + (prop.title || '')
    copy.slug = slugify(copy.title)
    copy.isPublished = false
    const created = await createProperty(copy)
    router.push({ name: 'AdminPropertyEdit', params: { id: created.id } })
  } catch (e) {
    alert('Error al duplicar: ' + (e.message || e))
  }
}

async function remove(prop) {
  if (!confirm('¿Eliminar esta propiedad?')) return
  try {
    await deleteProperty(prop.id)
    list.value = list.value.filter(p => p.id !== prop.id)
  } catch (e) {
    alert('Error al eliminar: ' + (e.message || e))
  }
}

const typeLabel = (type) => {
  const map = {
    casa: 'Casa',
    departamento: 'Dpto',
    duplex: 'Dúplex',
    ph: 'PH',
    lote: 'Lote',
    campo: 'Campo',
    local: 'Local',
    fondo_comercio: 'F. comercio',
    kiosco: 'Kiosko',
    kiosko: 'Kiosko',
    galpon: 'Galpón',
    hotel_complejo: 'Hotel/Complejo',
    terreno: 'Terreno',
    oficina: 'Oficina',
    otro: 'Otro',
  }
  return map[type] || type
}
</script>

<template>
  <div>
    <div
      v-if="toastMessage"
      class="fixed top-6 right-6 z-50 max-w-sm rounded-2xl border border-green-200 bg-white px-4 py-3 text-sm font-medium text-green-800 shadow-[0_20px_45px_rgba(22,101,52,0.18)]"
    >
      <div class="flex items-start gap-3">
        <span class="mt-0.5 flex h-6 w-6 items-center justify-center rounded-full bg-green-100 text-green-700">
          ✓
        </span>
        <span>{{ toastMessage }}</span>
      </div>
    </div>

    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Propiedades</h1>
      <button type="button" class="btn-primary" @click="goNew">Nueva propiedad</button>
    </div>

    <div class="bg-white rounded-2xl shadow-card p-4 mb-6">
      <div class="flex flex-wrap gap-4">
        <input
          v-model="search"
          type="search"
          placeholder="Buscar por título, referencia, ubicación..."
          class="flex-1 min-w-[200px] rounded-xl border border-gray-300 px-3 py-2 text-sm"
          @keyup.enter="load"
        />
        <select
          v-model="filterStatus"
          class="rounded-xl border border-gray-300 px-3 py-2 text-sm"
          @change="load"
        >
          <option value="">Todos los estados</option>
          <option value="available">Disponible</option>
          <option value="reserved">Reservado</option>
          <option value="sold">Vendido</option>
        </select>
        <button type="button" class="btn-primary text-sm" @click="load">Buscar</button>
      </div>
    </div>

    <LoadingSpinner v-if="loading" />
    <EmptyState v-else-if="!list.length" title="No hay propiedades" description="Creá la primera publicación.">
      <button type="button" class="btn-primary mt-4" @click="goNew">Nueva propiedad</button>
    </EmptyState>
    <div v-else class="bg-white rounded-2xl shadow-card overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Título / Ref.</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Tipo</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Precio</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Estado</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Publicado</th>
              <th class="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            <tr v-for="p in list" :key="p.id" class="hover:bg-gray-50">
              <td class="px-4 py-3">
                <p class="font-medium text-gray-900 truncate max-w-[200px]">{{ p.title }}</p>
                <p class="text-xs text-gray-500">{{ p.referenceCode }}</p>
              </td>
              <td class="px-4 py-3 text-sm text-gray-600">{{ typeLabel(p.type) }}</td>
              <td class="px-4 py-3 text-sm">{{ p.currency }} {{ Number(p.price).toLocaleString('es-AR') }}</td>
              <td class="px-4 py-3">
                <StatusBadge :status="p.status" :operation="p.operation" />
              </td>
              <td class="px-4 py-3">
                <span class="text-sm" :class="p.isPublished ? 'text-green-600' : 'text-gray-400'">
                  {{ p.isPublished ? 'Sí' : 'No' }}
                </span>
              </td>
              <td class="px-4 py-3 text-right">
                <button type="button" class="text-primary-600 hover:underline text-sm mr-2" @click="goEdit(p.id)">Editar</button>
                <button type="button" class="text-gray-500 hover:underline text-sm mr-2" @click="togglePublish(p)">
                  {{ p.isPublished ? 'Despublicar' : 'Publicar' }}
                </button>
                <button type="button" class="text-gray-500 hover:underline text-sm mr-2" @click="duplicate(p)">Duplicar</button>
                <button type="button" class="text-red-600 hover:underline text-sm" @click="remove(p)">Eliminar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
