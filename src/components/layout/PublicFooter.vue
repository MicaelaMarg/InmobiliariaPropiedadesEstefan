<script setup>
import { useAppStore } from '../../stores/app'

const app = useAppStore()
const settings = app.settings
const socialLinks = [
  {
    name: 'Instagram',
    href: 'https://www.instagram.com/estefanpropiedades/',
    icon: 'M7 2h10a5 5 0 015 5v10a5 5 0 01-5 5H7a5 5 0 01-5-5V7a5 5 0 015-5zm9.5 2a1.5 1.5 0 100 3 1.5 1.5 0 000-3zM12 7a5 5 0 100 10 5 5 0 000-10z',
  },
  {
    name: 'Facebook',
    href: 'https://web.facebook.com/EstefanPropiedades/?_rdc=1&_rdr#',
    icon: 'M14 8h3V4h-3c-2.21 0-4 1.79-4 4v3H7v4h3v5h4v-5h3l1-4h-4V8a1 1 0 011-1z',
  },
]
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
          <div class="mt-4 flex items-center gap-3">
            <a
              v-for="social in socialLinks"
              :key="social.name"
              :href="social.href"
              target="_blank"
              rel="noopener noreferrer"
              class="inline-flex h-11 w-11 items-center justify-center rounded-full border border-gray-700 bg-gray-800 text-emerald-300 transition-colors hover:border-emerald-500 hover:text-white"
              :aria-label="social.name"
              :title="social.name"
            >
              <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" :d="social.icon" />
              </svg>
            </a>
          </div>
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
