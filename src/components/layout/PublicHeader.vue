<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '../../stores/app'

const router = useRouter()
const mobileMenuOpen = ref(false)
const logoError = ref(false)
const logoSrc = ref('/images/branding/logo-erika-estefan-real.png')
const app = useAppStore()

function onLogoError() {
  if (logoSrc.value.endsWith('.png')) {
    logoSrc.value = '/images/branding/logo-erika-estefan.svg'
    return
  }
  logoError.value = true
}

function navTo(path) {
  mobileMenuOpen.value = false
  router.push(path)
}
</script>

<template>
  <header class="bg-[#003820] shadow-card sticky top-0 z-50">
    <div class="max-w-7xl mx-auto pl-0 pr-2 sm:pl-1 sm:pr-4 lg:pl-2 lg:pr-6">
      <div class="flex items-center justify-between h-16 md:h-[88px] gap-4">
        <router-link to="/" class="flex items-center min-w-0 flex-1 gap-3 text-white transition-colors">
          <span class="flex items-center justify-center flex-shrink-0 self-center">
            <img
              v-if="!logoError"
              :src="logoSrc"
              alt="Erika M. Estefan Propiedades"
              class="h-10 sm:h-11 md:h-12 lg:h-14 w-auto object-contain"
              @error="onLogoError"
            />
            <span v-else class="text-xs font-semibold text-white">Logo</span>
          </span>
          <span class="min-w-0 truncate text-sm sm:text-base md:text-lg lg:text-xl font-semibold tracking-tight leading-none">
            {{ app.settings.businessName }}
          </span>
        </router-link>

        <nav class="hidden md:flex items-center gap-5 lg:gap-6 flex-shrink-0">
          <router-link to="/" class="text-emerald-50 hover:text-white font-medium transition-colors">Inicio</router-link>
          <router-link to="/catalogo" class="text-emerald-50 hover:text-white font-medium transition-colors">Catálogo</router-link>
          <router-link to="/tasacion" class="text-emerald-50 hover:text-white font-medium transition-colors">Tasación</router-link>
          <router-link to="/contacto" class="text-emerald-50 hover:text-white font-medium transition-colors">Contacto</router-link>
        </nav>

        <div class="flex items-center gap-2 md:hidden">
          <button
            type="button"
            class="p-2 rounded-lg text-white hover:bg-white/10"
            aria-label="Abrir menú"
            @click="mobileMenuOpen = !mobileMenuOpen"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path v-if="!mobileMenuOpen" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <!-- Mobile menu -->
      <div v-show="mobileMenuOpen" class="md:hidden py-4 border-t border-white/20">
        <div class="flex flex-col gap-3">
          <router-link to="/" class="py-2 text-emerald-50 font-medium hover:text-white" @click="navTo('/')">Inicio</router-link>
          <router-link to="/catalogo" class="py-2 text-emerald-50 font-medium hover:text-white" @click="navTo('/catalogo')">Catálogo</router-link>
          <router-link to="/tasacion" class="py-2 text-emerald-50 font-medium hover:text-white" @click="navTo('/tasacion')">Tasación</router-link>
          <router-link to="/contacto" class="py-2 text-emerald-50 font-medium hover:text-white" @click="navTo('/contacto')">Contacto</router-link>
      </div>
      </div>
    </div>
  </header>
</template>
