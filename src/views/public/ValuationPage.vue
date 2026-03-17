<script setup>
import { computed, ref } from 'vue'
import { useAppStore } from '../../stores/app'

const app = useAppStore()
const settings = app.settings
const valuationImageSrc = '/images/imagen-hero-optimized.jpg'

const form = ref({
  name: '',
  phone: '',
  email: '',
  propertyType: '',
  address: '',
  message: '',
})

const whatsappUrl = computed(() => {
  const lines = [
    'Hola, quiero solicitar una tasacion.',
    '',
    `Nombre: ${form.value.name || '-'}`,
    `Telefono: ${form.value.phone || '-'}`,
    `Email: ${form.value.email || '-'}`,
    `Tipo de propiedad: ${form.value.propertyType || '-'}`,
    `Direccion: ${form.value.address || '-'}`,
    `Mensaje adicional: ${form.value.message || '-'}`,
  ]

  const text = encodeURIComponent(lines.join('\n'))
  return `https://wa.me/${settings.whatsapp.replace(/\D/g, '')}?text=${text}`
})

const infoCards = [
  {
    title: 'Tasacion profesional',
    text: 'Analizamos ubicacion, estado, metros y potencial comercial de tu inmueble.',
    icon: 'M12 8c-3.314 0-6 2.239-6 5s2.686 5 6 5 6-2.239 6-5-2.686-5-6-5zm0 8a3 3 0 110-6 3 3 0 010 6zm8.94-2A8.96 8.96 0 0121 15a9 9 0 01-18 0c0-.34.02-.674.06-1',
  },
  {
    title: 'Mercado inmobiliario',
    text: 'Tomamos referencias comparables para construir un valor realista y competitivo.',
    icon: 'M3 13h4v8H3zm7-6h4v14h-4zm7 3h4v11h-4z',
  },
  {
    title: 'Asesoramiento cercano',
    text: 'Te acompanamos en cada decision para vender o alquilar con mayor seguridad.',
    icon: 'M17 20h5V4H2v16h5m10 0v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6m10 0H7m10 0v1a1 1 0 11-2 0v-1m-6 0v1a1 1 0 11-2 0v-1m3-12h.01M12 8h.01M15 8h.01M9 12h.01M12 12h.01M15 12h.01',
  },
]

const socialLinks = computed(() => [
  {
    name: 'Instagram',
    href: 'https://www.instagram.com/estefanpropiedades/',
    icon: 'M7 2h10a5 5 0 015 5v10a5 5 0 01-5 5H7a5 5 0 01-5-5V7a5 5 0 015-5zm9.5 2a1.5 1.5 0 100 3 1.5 1.5 0 000-3zM12 7a5 5 0 100 10 5 5 0 000-10z',
  },
  {
    name: 'Facebook',
    href: 'https://web.facebook.com/EstefanPropiedades/?_rdc=1&_rdr#',
    icon: 'M14 8h3V4h-3c-2.21 0-4 1.79-4 4v3H7v4h3v5h4v-5h3l1-4h-4V8a1 1 0 011-1z',
  },
  {
    name: 'WhatsApp',
    href: whatsappUrl.value,
    icon: 'M20.52 3.48A11.8 11.8 0 0012.15 0C5.59 0 .24 5.35.24 11.92c0 2.1.55 4.15 1.6 5.95L0 24l6.31-1.79a11.88 11.88 0 005.84 1.49h.01c6.56 0 11.91-5.35 11.91-11.92 0-3.18-1.24-6.17-3.55-8.3z',
  },
])
</script>

<template>
  <section class="bg-gradient-to-b from-emerald-50 via-[#f7faf8] to-white">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14 md:py-20">
      <div class="mb-10 overflow-hidden rounded-[30px] bg-white shadow-[0_24px_60px_rgba(15,23,42,0.08)] ring-1 ring-emerald-100 md:mb-14">
        <div class="grid lg:grid-cols-2">
          <div class="relative flex items-center bg-gradient-to-br from-emerald-950 via-emerald-800 to-emerald-400 px-6 py-12 text-white sm:px-10 lg:px-14 lg:py-16">
            <div class="max-w-2xl">
              <p class="mb-3 text-sm font-semibold uppercase tracking-[0.28em] text-emerald-100/90">Tasacion</p>
              <h1 class="text-3xl font-bold leading-tight md:text-5xl">Tasación de propiedades</h1>
              <p class="mt-5 text-base leading-8 text-emerald-50 md:text-lg">
                Un espacio profesional para propietarios que quieren vender o alquilar con una valuacion clara, estrategica y respaldada por mercado real.
              </p>
              <p class="mt-4 text-sm leading-7 text-emerald-100/90 md:text-base">
                Analizamos tu inmueble, el contexto del mercado y el potencial comercial para ayudarte a tomar mejores decisiones.
              </p>
            </div>
            <div class="pointer-events-none absolute inset-y-0 -right-16 hidden w-40 bg-gradient-to-r from-emerald-700/90 via-emerald-400/45 to-transparent blur-2xl lg:block" />
          </div>

          <div class="relative bg-slate-100">
            <img
              :src="valuationImageSrc"
              alt="Imagen de presentación para tasación de propiedades"
              class="h-full min-h-[280px] w-full object-cover object-center lg:min-h-[460px]"
              loading="eager"
            />
            <div class="pointer-events-none absolute inset-0 bg-[linear-gradient(115deg,rgba(2,6,23,0.12)_0%,rgba(2,6,23,0.04)_45%,rgba(2,6,23,0.14)_100%)]" />
            <div class="pointer-events-none absolute inset-y-0 left-0 hidden w-36 bg-gradient-to-r from-black/65 via-black/26 to-transparent lg:block" />
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-8 items-stretch">
        <article class="flex h-full flex-col rounded-[30px] bg-white p-7 md:p-9 lg:p-10 shadow-[0_24px_60px_rgba(15,23,42,0.08)] ring-1 ring-emerald-100">
          <div class="inline-flex items-center rounded-full bg-emerald-100 px-4 py-2 text-sm font-semibold uppercase tracking-[0.2em] text-emerald-800">
            Solicitud de tasacion
          </div>

          <div class="mt-7 space-y-6 text-slate-700">
            <h2 class="text-2xl md:text-4xl font-bold text-slate-950 leading-tight">
              ¿Quieres vender o alquilar tu propiedad?
            </h2>
            <p class="text-base md:text-lg leading-8">
              En <span class="font-semibold text-emerald-900">Erika M. Estefan Propiedades</span> realizamos un asesoramiento profesional para que puedas tomar mejores decisiones al momento de vender o alquilar tu inmueble.
            </p>
            <p class="text-base md:text-lg leading-8">
              Evaluamos las características de la propiedad, analizamos valores comparables de mercado y te acompañamos con una tasación clara, realista y estratégica.
            </p>
            <p class="text-base md:text-lg leading-8">
              Sabemos lo importante que es este proceso. Por eso te proponemos transitarlo con acompañamiento cercano, información precisa y una mirada comercial pensada para obtener el mejor resultado posible.
            </p>
          </div>

          <div class="mt-8 grid gap-4">
            <div
              v-for="item in infoCards"
              :key="item.title"
              class="flex items-start gap-4 rounded-3xl bg-emerald-50/80 border border-emerald-100 p-5 shadow-[0_10px_28px_rgba(16,24,40,0.05)]"
            >
              <span class="flex h-12 w-12 items-center justify-center rounded-2xl bg-white text-emerald-800 shadow-sm flex-shrink-0">
                <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.7" :d="item.icon" />
                </svg>
              </span>
              <div>
                <h3 class="text-base font-semibold uppercase tracking-[0.16em] text-emerald-800">{{ item.title }}</h3>
                <p class="mt-2 text-sm md:text-base leading-7 text-slate-600">{{ item.text }}</p>
              </div>
            </div>
          </div>

        </article>

        <article class="rounded-[30px] bg-white p-7 md:p-9 lg:p-10 shadow-[0_24px_60px_rgba(15,23,42,0.08)] ring-1 ring-emerald-100">
          <div class="flex items-center justify-between gap-4 flex-wrap">
            <div>
              <p class="text-sm font-semibold uppercase tracking-[0.24em] text-emerald-700 mb-2">Formulario</p>
              <h2 class="text-2xl md:text-4xl font-bold text-slate-950 leading-tight">Solicita una tasación profesional</h2>
            </div>
            <span class="inline-flex items-center rounded-full bg-emerald-100 px-4 py-2 text-sm font-medium text-emerald-800">
              Respuesta a la brevedad
            </span>
          </div>

          <form class="mt-8 space-y-5">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <label class="block">
                <span class="mb-2 block text-sm font-semibold text-slate-700">Nombre</span>
                <input
                  v-model="form.name"
                  type="text"
                  class="block w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-100"
                  placeholder="Tu nombre"
                />
              </label>

              <label class="block">
                <span class="mb-2 block text-sm font-semibold text-slate-700">Teléfono</span>
                <input
                  v-model="form.phone"
                  type="tel"
                  class="block w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-100"
                  placeholder="Tu telefono"
                />
              </label>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <label class="block">
                <span class="mb-2 block text-sm font-semibold text-slate-700">Email</span>
                <input
                  v-model="form.email"
                  type="email"
                  class="block w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-100"
                  placeholder="nombre@email.com"
                />
              </label>

              <label class="block">
                <span class="mb-2 block text-sm font-semibold text-slate-700">Tipo de propiedad</span>
                <select
                  v-model="form.propertyType"
                  class="block w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-100"
                >
                  <option value="">Seleccionar</option>
                  <option value="casa">Casa</option>
                  <option value="departamento">Departamento</option>
                  <option value="terreno">Terreno</option>
                  <option value="local">Local</option>
                </select>
              </label>
            </div>

            <label class="block">
              <span class="mb-2 block text-sm font-semibold text-slate-700">Dirección de la propiedad</span>
              <input
                v-model="form.address"
                type="text"
                class="block w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-100"
                placeholder="Calle, numero, localidad"
              />
            </label>

            <label class="block">
              <span class="mb-2 block text-sm font-semibold text-slate-700">Mensaje adicional</span>
              <textarea
                v-model="form.message"
                rows="5"
                class="block min-h-[170px] w-full resize-y rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-100"
                placeholder="Contanos detalles utiles sobre la propiedad o tu objetivo"
              />
            </label>

            <a
              :href="whatsappUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="inline-flex h-14 w-full items-center justify-center gap-3 rounded-2xl bg-[#0b7a4b] px-6 text-base font-semibold text-white shadow-[0_18px_32px_rgba(11,122,75,0.24)] transition-all hover:-translate-y-0.5 hover:bg-[#09663f]"
            >
              <svg class="h-5 w-5" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                <path d="M20.52 3.48A11.8 11.8 0 0012.15 0C5.59 0 .24 5.35.24 11.92c0 2.1.55 4.15 1.6 5.95L0 24l6.31-1.79a11.88 11.88 0 005.84 1.49h.01c6.56 0 11.91-5.35 11.91-11.92 0-3.18-1.24-6.17-3.55-8.3z" />
              </svg>
              Solicitar tasación por WhatsApp
            </a>
          </form>

          <div class="mt-8 flex items-center gap-3">
            <a
              v-for="social in socialLinks"
              :key="social.name"
              :href="social.href"
              target="_blank"
              rel="noopener noreferrer"
              class="inline-flex h-12 w-12 items-center justify-center rounded-full bg-white text-emerald-800 ring-1 ring-emerald-200 shadow-sm transition-all hover:-translate-y-0.5 hover:bg-emerald-700 hover:text-white hover:ring-emerald-700"
              :aria-label="social.name"
            >
              <svg class="h-5 w-5" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                <path :d="social.icon" />
              </svg>
            </a>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>
