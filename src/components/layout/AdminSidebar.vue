<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAppStore } from '../../stores/app'

const router = useRouter()
const route = useRoute()
const app = useAppStore()
function closeSidebar() {
  app.adminSidebarOpen = false
}

const links = [
  { name: 'AdminDashboard', path: '/admin', label: 'Dashboard', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
  { name: 'AdminProperties', path: '/admin/propiedades', label: 'Propiedades', icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4' },
  { name: 'AdminPropertyNew', path: '/admin/propiedades/nueva', label: 'Nueva propiedad', icon: 'M12 4v16m8-8H4' },
  { name: 'AdminUsers', path: '/admin/usuarios', label: 'Administradores', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
  { name: 'AdminSettings', path: '/admin/configuracion', label: 'Configuración', icon: 'M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z M15 12a3 3 0 11-6 0 3 3 0 016 0z' },
]

function isActive(path) {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}

</script>

<template>
  <div class="fixed lg:static inset-y-0 left-0 z-40 w-64 bg-[#003820] border-r border-white/20 transform lg:transform-none transition-transform text-white" :class="{ '-translate-x-full': !app.adminSidebarOpen }" data-admin-sidebar>
    <div class="flex items-center justify-between h-14 px-4 border-b border-white/20 lg:hidden">
      <span class="font-semibold text-white">Admin</span>
      <button type="button" class="p-2 rounded-lg hover:bg-white/10" aria-label="Cerrar menú" @click="closeSidebar">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg>
      </button>
    </div>
    <nav class="p-4 space-y-1">
      <router-link
        v-for="link in links"
        :key="link.path"
        :to="link.path"
        class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors"
        :class="isActive(link.path) ? 'bg-white/15 text-white' : 'text-emerald-100 hover:bg-white/10 hover:text-white'"
        @click="closeSidebar"
      >
        <svg class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="link.icon" /></svg>
        {{ link.label }}
      </router-link>
    </nav>
  </div>
  <div v-if="app.adminSidebarOpen" class="fixed inset-0 bg-black/20 z-30 lg:hidden" aria-hidden="true" @click="closeSidebar" />
</template>
