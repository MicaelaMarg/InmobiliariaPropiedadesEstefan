<script setup>
import { useAppStore } from '../../stores/app'

const app = useAppStore()
const settings = app.settings
</script>

<template>
  <footer class="bg-gray-900 text-gray-300 mt-auto">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div>
          <h3 class="text-white font-semibold text-lg mb-3">{{ settings.businessName }}</h3>
          <p class="text-sm">{{ settings.address }}</p>
        </div>
        <div>
          <h3 class="text-white font-semibold text-lg mb-3">Contacto</h3>
          <p class="text-sm">{{ settings.phone }}</p>
          <p class="text-sm">{{ settings.email }}</p>
          <a
            :href="`https://wa.me/${settings.whatsapp.replace(/\D/g, '')}`"
            target="_blank"
            rel="noopener noreferrer"
            class="inline-flex items-center gap-2 mt-2 text-green-400 hover:text-green-300"
          >
            WhatsApp
          </a>
        </div>
        <div>
          <h3 class="text-white font-semibold text-lg mb-3">Navegación</h3>
          <router-link to="/" class="block text-sm hover:text-white mb-1">Inicio</router-link>
          <router-link to="/catalogo" class="block text-sm hover:text-white mb-1">Catálogo</router-link>
          <router-link to="/tasacion" class="block text-sm hover:text-white mb-1">Tasación</router-link>
          <router-link to="/contacto" class="block text-sm hover:text-white mb-1">Contacto</router-link>
          <router-link to="/admin/login" class="block text-sm text-emerald-400 hover:text-emerald-300 mt-3">Acceso administración</router-link>
        </div>
      </div>
      <div class="border-t border-gray-700 mt-8 pt-6 text-center text-sm">
        © {{ new Date().getFullYear() }} {{ settings.businessName }}. Todos los derechos reservados.
      </div>
    </div>
  </footer>
</template>
