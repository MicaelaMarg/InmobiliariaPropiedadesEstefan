<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useAppStore } from '../../stores/app'

const auth = useAuthStore()
const app = useAppStore()
const router = useRouter()

const displayName = computed(() => auth.user?.name || 'Administrador')
const userInitial = computed(() => displayName.value?.trim()?.charAt(0)?.toUpperCase() || 'A')

function toggleSidebar() {
  app.adminSidebarOpen = !app.adminSidebarOpen
}

function logout() {
  auth.logout()
  router.push({ name: 'AdminLogin' })
}
</script>

<template>
  <header class="sticky top-0 z-30 h-14 bg-white/90 backdrop-blur border-b border-gray-200">
    <div class="h-full px-4 flex items-center justify-between gap-4">
      <div class="flex items-center gap-3 min-w-0">
        <button
          type="button"
          class="lg:hidden p-2 rounded-lg hover:bg-gray-100 text-gray-700"
          aria-label="Abrir menú"
          @click="toggleSidebar"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>

        <router-link to="/admin" class="flex items-center gap-2 text-gray-900 group">
          <div class="h-9 w-9 rounded-full bg-white overflow-hidden border border-emerald-100 shadow-sm">
            <img src="/images/branding/logoinmobiliaria.webp" alt="Logo inmobiliaria" class="h-full w-full object-cover" />
          </div>
          <div class="leading-tight">
            <div class="text-[10px] uppercase tracking-[0.12em] text-gray-500">Inmobiliaria</div>
            <div class="text-sm font-semibold text-gray-900">Propiedades Estefan</div>
          </div>
        </router-link>
      </div>

      <div class="flex items-center gap-2">
        <div class="hidden sm:flex items-center gap-2 px-2.5 py-1.5 rounded-full border border-gray-200 bg-gray-50 shadow-sm">
          <div class="h-8 w-8 rounded-full bg-emerald-100 text-emerald-800 flex items-center justify-center text-sm font-semibold">
            {{ userInitial }}
          </div>
          <div class="text-sm font-medium text-gray-800 max-w-[140px] truncate">
            {{ displayName }}
          </div>
        </div>

        <button
          type="button"
          class="p-2 rounded-lg hover:bg-gray-100 text-gray-500 hover:text-gray-700"
          aria-label="Cerrar sesión"
          @click="logout"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a2 2 0 01-2 2H7a2 2 0 01-2-2V7a2 2 0 012-2h4a2 2 0 012 2v1" />
          </svg>
        </button>
      </div>
    </div>
  </header>
</template>
