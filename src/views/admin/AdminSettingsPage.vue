<script setup>
import { ref, onMounted } from 'vue'
import { useAppStore } from '../../stores/app'

const app = useAppStore()
const form = ref({
  businessName: '',
  phone: '',
  whatsapp: '',
  email: '',
  address: '',
})

onMounted(() => {
  form.value = { ...app.settings }
})

function save() {
  app.updateSettings(form.value)
  alert('Configuración guardada.')
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6">Configuración</h1>
    <div class="bg-white rounded-2xl shadow-card p-6 max-w-xl">
      <p class="text-sm text-gray-500 mb-4">Estos datos se muestran en el sitio público (footer, contacto).</p>
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Nombre de la inmobiliaria</label>
          <input v-model="form.businessName" type="text" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
          <input v-model="form.phone" type="text" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">WhatsApp (número con código país, sin espacios)</label>
          <input v-model="form.whatsapp" type="text" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" placeholder="5491112345678" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input v-model="form.email" type="email" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Dirección</label>
          <input v-model="form.address" type="text" class="w-full rounded-xl border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-primary-500" />
        </div>
        <button type="button" class="btn-primary" @click="save">Guardar configuración</button>
      </div>
    </div>
  </div>
</template>
