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
  <header class="sticky top-0 z-20 bg-[#003820] border-b border-white/20 h-14 flex items-center px-4 gap-4 text-white">
    <button
      type="button"
      class="lg:hidden p-2 rounded-lg hover:bg-white/10"
      aria-label="Abrir menú"
      @click="toggleSidebar"
    >
      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
      </svg>
    </button>
    <router-link to="/admin" class="flex items-center gap-3 text-white group">
      <div class="h-10 w-10 rounded-full bg-white overflow-hidden border border-emerald-100/70 shadow-lg shadow-emerald-900/40">
        <img src="/images/branding/logoinmobiliaria.webp" alt="Logo inmobiliaria" class="h-full w-full object-cover" />
      </div>
      <div class="leading-tight hidden sm:block">
        <div class="text-[11px] uppercase tracking-[0.08em] text-emerald-100">Inmobiliaria</div>
        <div class="text-sm font-semibold text-white">Propiedades Estefan</div>
      </div>
    </router-link>
    <div class="flex-1" />
    <div class="flex items-center gap-2">
      <span class="text-sm text-emerald-100 hidden sm:inline">{{ auth.user?.name }}</span>
      <button
        type="button"
        class="text-sm text-emerald-100 hover:text-white"
        @click="logout"
      >
        Cerrar sesión
      </button>
    </div>
  </header>
</template>
