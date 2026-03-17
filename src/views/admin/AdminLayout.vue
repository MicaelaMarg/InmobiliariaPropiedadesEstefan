<script setup>
import AdminSidebar from '../../components/layout/AdminSidebar.vue'
import AdminHeader from '../../components/layout/AdminHeader.vue'
</script>

<template>
  <div class="h-screen flex bg-gray-100 overflow-hidden">
    <!-- Sidebar fijo; el propio componente maneja responsivo/off-canvas -->
    <AdminSidebar />

    <!-- Columna principal -->
    <div class="flex-1 flex flex-col min-w-0 lg:ml-64 overflow-hidden">
      <AdminHeader class="shrink-0" />
      <main class="flex-1 overflow-y-auto p-4 md:p-6">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
