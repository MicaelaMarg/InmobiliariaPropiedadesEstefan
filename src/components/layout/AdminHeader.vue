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
        class="lg:hidden p-2 rounded-lg hover:bg-gray-100 text-gray-700"
        aria-label="Abrir menú"
        @click="toggleSidebar"
      >
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
      <span class="text-sm font-semibold text-gray-800 hidden lg:inline">Panel administrador</span>
    </div>
    <div class="flex items-center gap-2 text-gray-600">
      <span class="text-sm hidden sm:inline truncate max-w-[160px]">{{ auth.user?.name }}</span>
    </div>
  </header>
</template>
