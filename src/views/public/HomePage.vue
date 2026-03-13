<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PropertyCard from '../../components/property/PropertyCard.vue'
import LoadingSpinner from '../../components/ui/LoadingSpinner.vue'
import { fetchFeaturedProperties } from '../../services/properties'

const router = useRouter()
const featured = ref([])
const loading = ref(true)
const googleReviewsUrl = 'https://maps.app.goo.gl/j8Vnf6GnBffEAH9g6'

const testimonials = [
  {
    name: 'Renzo Rios',
    role: 'Cliente de Santa Clara del Mar',
    initials: 'RR',
    quote: 'Excelente atención y asesoramiento. Muy profesional y siempre predispuesta a ayudar en todo el proceso.',
  },
  {
    name: 'Mariana Lopez',
    role: 'Propietaria',
    initials: 'ML',
    quote: 'Muy buena atención y profesionalismo. Recomiendo totalmente su trabajo.',
  },
  {
    name: 'Carlos Fernandez',
    role: 'Comprador',
    initials: 'CF',
    quote: 'Gran atención, responsabilidad y acompañamiento durante toda la operación.',
  },
  {
    name: 'Lucia Mendez',
    role: 'Inquilina',
    initials: 'LM',
    quote: 'Transparencia, cercanía y una gestión muy ordenada de principio a fin. Me sentí acompañada en cada etapa.',
  },
]

const whyChooseItems = [
  {
    title: 'Asesoramiento profesional',
    description: 'Acompañamiento personalizado en operaciones de compra, venta y alquiler, con claridad y estrategia en cada decision.',
    icon: 'M12 12a4 4 0 100-8 4 4 0 000 8zm-7 9a7 7 0 1114 0H5z',
  },
  {
    title: 'Tasaciones precisas',
    description: 'Evaluaciones respaldadas por comparables reales de mercado para definir el valor correcto de cada propiedad.',
    icon: 'M9 17v-6m3 6V7m3 10v-3m4 7H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v12a2 2 0 01-2 2z',
  },
  {
    title: 'Gestión integral',
    description: 'Nos ocupamos de todo el proceso, desde la presentación y difusión hasta el seguimiento y cierre de la operación.',
    icon: 'M4 7h16M4 12h10M4 17h16',
  },
  {
    title: 'Atención cercana',
    description: 'Trato humano, transparente y profesional, priorizando siempre las necesidades reales de cada cliente.',
    icon: 'M8 10h.01M12 10h.01M16 10h.01M9 16h6M7 4h10a3 3 0 013 3v10a3 3 0 01-3 3H7a3 3 0 01-3-3V7a3 3 0 013-3z',
  },
]

onMounted(async () => {
  try {
    featured.value = await fetchFeaturedProperties()
  } catch {
    featured.value = []
  } finally {
    loading.value = false
  }
})

function goCatalog() {
  router.push({ name: 'Catalog' })
}

function goContact() {
  router.push({ name: 'Contact' })
}
</script>

<template>
  <div>
    <!-- Hero -->
    <section class="relative bg-gradient-to-br from-primary-600 to-primary-800 text-white overflow-hidden">
      <div class="absolute inset-0 opacity-20">
       <img
  src="/images/banner-home.webp"
  alt="Erika M Estefan Propiedades"
  class="w-full h-full object-contain"
  loading="lazy"
/>
      </div>
      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 md:py-28">
        <div class="max-w-2xl">
          <h1 class="text-4xl md:text-5xl font-bold leading-tight mb-4">
            Encontrá tu próximo hogar o inversión
          </h1>
          <p class="text-lg text-primary-100 mb-8">
            Propiedades, lotes y fondos de comercio. Asesoramiento profesional para venta y alquiler.
          </p>
          <div class="flex flex-wrap gap-4">
            <button type="button" class="btn-primary bg-white text-primary-700 hover:bg-gray-100 text-lg px-6 py-3" @click="goCatalog">
              Ver catálogo
            </button>
            <button type="button" class="border-2 border-white rounded-xl px-6 py-3 font-medium hover:bg-white/10 transition-colors" @click="goContact">
              Contacto
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Destacadas -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14">
      <h2 class="text-2xl md:text-3xl font-bold text-gray-900 mb-6">Propiedades destacadas</h2>
      <LoadingSpinner v-if="loading" />
      <div v-else-if="featured.length" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <PropertyCard v-for="prop in featured.slice(0, 6)" :key="prop.id" :property="prop" />
      </div>
      <div v-else class="text-center py-12 text-gray-500">
        <p>No hay propiedades destacadas en este momento.</p>
        <button type="button" class="btn-primary mt-4" @click="goCatalog">Ver todo el catálogo</button>
      </div>
      <div class="mt-8 text-center">
        <button type="button" class="btn-primary" @click="goCatalog">Ver todas las propiedades</button>
      </div>
    </section>

    <!-- Why Choose Us -->
    <section class="relative overflow-hidden bg-[#f7faf8] py-16 md:py-20">
      <div class="absolute inset-0 opacity-70">
        <div class="absolute top-0 left-1/3 h-40 w-40 rounded-full bg-emerald-100 blur-3xl" />
        <div class="absolute bottom-0 right-0 h-56 w-56 rounded-full bg-emerald-50 blur-3xl" />
      </div>
      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="max-w-3xl mx-auto text-center mb-12 md:mb-14">
          <p class="text-sm font-semibold uppercase tracking-[0.28em] text-emerald-700 mb-3">Por qué elegirnos</p>
          <h2 class="text-3xl md:text-4xl font-bold text-slate-950 leading-tight">
            ¿Por qué elegir Erika M. Estefan Propiedades?
          </h2>
          <p class="mt-4 text-base md:text-lg text-slate-600">
            Acompañamiento profesional en cada etapa de tu operación inmobiliaria.
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
          <article
            v-for="item in whyChooseItems"
            :key="item.title"
            class="group rounded-[30px] bg-white p-7 md:p-8 shadow-[0_18px_40px_rgba(15,23,42,0.06)] ring-1 ring-emerald-100 transition-all duration-300 hover:-translate-y-1.5 hover:shadow-[0_26px_50px_rgba(15,23,42,0.10)]"
          >
            <span class="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-emerald-100 to-white text-emerald-800 shadow-sm ring-1 ring-emerald-100 mb-6">
              <svg class="h-7 w-7" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" :d="item.icon" />
              </svg>
            </span>
            <h3 class="text-xl font-semibold text-slate-950 leading-snug">{{ item.title }}</h3>
            <p class="mt-4 text-slate-600 leading-8 text-base">
              {{ item.description }}
            </p>
          </article>
        </div>
      </div>
    </section>

    <!-- Reviews -->
    <section class="relative overflow-hidden bg-gradient-to-b from-[#eef8f1] via-white to-white py-16 md:py-20">
      <div class="absolute inset-0 opacity-60">
        <div class="absolute -top-16 right-0 h-64 w-64 rounded-full bg-emerald-100 blur-3xl" />
        <div class="absolute bottom-0 left-0 h-56 w-56 rounded-full bg-emerald-50 blur-3xl" />
      </div>
      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="max-w-3xl mb-10 md:mb-14">
          <p class="text-sm font-semibold uppercase tracking-[0.28em] text-emerald-700 mb-3">Opiniones</p>
          <h2 class="text-3xl md:text-4xl font-bold text-slate-950 leading-tight">Opiniones de nuestros clientes</h2>
          <p class="mt-4 text-base md:text-lg text-slate-600">La confianza de nuestros clientes es nuestro mayor respaldo.</p>
        </div>

        <div class="rounded-[30px] bg-white/95 backdrop-blur shadow-[0_24px_60px_rgba(15,23,42,0.08)] ring-1 ring-emerald-100 p-7 md:p-9 lg:p-10 mb-8">
          <div class="grid grid-cols-1 lg:grid-cols-[minmax(0,1fr)_auto] gap-8 items-center">
            <div class="flex items-start gap-4">
              <span class="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#4285F4]/10 text-[#4285F4] shadow-sm flex-shrink-0">
                <svg class="h-8 w-8" viewBox="0 0 24 24" aria-hidden="true">
                  <path fill="currentColor" d="M21.35 11.1h-9.18v2.98h5.27c-.23 1.5-1.74 4.39-5.27 4.39-3.17 0-5.75-2.63-5.75-5.87s2.58-5.87 5.75-5.87c1.8 0 3.01.77 3.7 1.43l2.52-2.44C16.8 4.22 14.72 3.2 12.17 3.2 7.27 3.2 3.3 7.24 3.3 12.2S7.27 21.2 12.17 21.2c6.16 0 8.53-4.32 8.53-6.56 0-.44-.05-.76-.11-1.09z"/>
                </svg>
              </span>
              <div>
                <p class="text-sm font-semibold uppercase tracking-[0.22em] text-emerald-700 mb-2">Calificación en Google</p>
                <div class="flex items-center gap-1 text-amber-400 mb-3" aria-label="5 estrellas">
                  <span v-for="i in 5" :key="i">★</span>
                </div>
                <p class="text-2xl md:text-3xl font-bold text-slate-950">5.0 basado en reseñas de clientes</p>
                <p class="mt-3 text-slate-600 max-w-2xl">Opiniones reales que reflejan cercanía, profesionalismo y acompañamiento durante todo el proceso inmobiliario.</p>
              </div>
            </div>

            <a
              :href="googleReviewsUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="inline-flex h-14 items-center justify-center rounded-2xl bg-[#0b7a4b] px-7 text-base font-semibold text-white shadow-[0_18px_32px_rgba(11,122,75,0.24)] transition-all hover:-translate-y-0.5 hover:bg-[#09663f]"
            >
              Ver reseñas en Google
            </a>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
          <article
            v-for="item in testimonials"
            :key="item.quote"
            class="group rounded-[28px] bg-white p-6 md:p-7 shadow-[0_18px_40px_rgba(15,23,42,0.07)] ring-1 ring-emerald-100 transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_24px_50px_rgba(15,23,42,0.10)]"
          >
            <div class="flex items-center gap-4 mb-5">
              <span class="flex h-12 w-12 items-center justify-center rounded-full bg-emerald-100 text-emerald-800 text-sm font-bold shadow-sm">
                {{ item.initials }}
              </span>
              <div>
                <p class="font-semibold text-slate-950">{{ item.name }}</p>
                <p class="text-sm text-slate-500">{{ item.role }}</p>
              </div>
            </div>
            <div class="flex items-center gap-1 text-amber-400 mb-4" aria-label="5 estrellas">
              <span v-for="i in 5" :key="i">★</span>
            </div>
            <p class="text-slate-700 leading-8 text-base">"{{ item.quote }}"</p>
          </article>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="bg-gray-100 py-14">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h2 class="text-2xl font-bold text-gray-900 mb-2">¿Buscás algo en particular?</h2>
        <p class="text-gray-600 mb-6">Escribinos por WhatsApp o completá el formulario de contacto.</p>
        <a
          href="https://wa.me/5491112345678"
          target="_blank"
          rel="noopener noreferrer"
          class="inline-flex items-center gap-2 btn-primary bg-green-600 hover:bg-green-700"
        >
          WhatsApp
        </a>
        <button type="button" class="btn-secondary ml-3" @click="goContact">Formulario de contacto</button>
      </div>
    </section>
  </div>
</template>
