<script setup>
import AdminSidebar from '../../components/layout/AdminSidebar.vue'
import AdminHeader from '../../components/layout/AdminHeader.vue'
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex">
    <AdminSidebar />
    <div class="flex-1 flex flex-col lg:ml-64">
      <AdminHeader />
      <main class="flex-1 p-4 md:p-6">
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
