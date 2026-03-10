<script setup>
import PublicHeader from '../../components/layout/PublicHeader.vue'
import PublicFooter from '../../components/layout/PublicFooter.vue'
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <PublicHeader />
    <main class="flex-1">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <PublicFooter />
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
