<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useAppStore } from '../../stores/app'

const auth = useAuthStore()
const app = useAppStore()
const router = useRouter()

function toggleSidebar() {
  app.adminSidebarOpen = !app.adminSidebarOpen
}

function logout() {
  auth.logout()
  router.push({ name: 'AdminLogin' })
}
</script>

<template>
  <header class="sticky top-0 z-20 bg-white/95 backdrop-blur border-b border-gray-200 h-14 flex items-center justify-between px-4 text-gray-800">
    <div class="flex items-center gap-3">
      <button
        type="button"
        class="lg:hidden p-2 rounded-lg hover:bg-gray-100"
        aria-label="Abrir menú"
        @click="toggleSidebar"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
      <router-link to="/admin" class="flex items-center gap-3 text-gray-900 group">
        <div class="h-12 w-12 rounded-full bg-white overflow-hidden border border-emerald-200 shadow-lg shadow-emerald-200/50">
          <img src="/images/branding/logoinmobiliaria.webp" alt="Logo inmobiliaria" class="h-full w-full object-cover" />
        </div>
        <div class="leading-tight">
          <div class="text-[11px] uppercase tracking-[0.08em] text-emerald-700">Inmobiliaria</div>
          <div class="text-sm font-semibold text-gray-900">Propiedades Estefan</div>
        </div>
      </router-link>
    </div>
    <div class="flex items-center gap-2">
      <span class="text-sm text-gray-700 hidden sm:inline">{{ auth.user?.name }}</span>
      <button
        type="button"
        class="text-sm text-emerald-700 hover:text-emerald-900"
        @click="logout"
      >
        Cerrar sesión
      </button>
    </div>
  </header>
</template>
