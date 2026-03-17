<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useAppStore } from '../../stores/app'
import ContactForm from '../../components/contact/ContactForm.vue'

const app = useAppStore()
const settings = app.settings
const mapSrc = 'https://www.google.com/maps?q=Acapulco+84,+Santa+Clara+del+Mar,+Buenos+Aires,+Argentina&z=16&output=embed'
const contactHeroSlides = [
  {
    type: 'image',
    src: '/images/branding/fotoContacto.jpeg',
    alt: 'Atención personalizada de Erika M. Estefan Propiedades',
  },
  {
    type: 'video',
    src: '/images/branding/adentro1.webm',
    poster: '/images/branding/fotoContacto.jpeg',
    alt: 'Video interior de propiedad',
  },
  {
    type: 'image',
    src: '/images/branding/inmobiliaria-cliente.jpeg',
    alt: 'Atencion personalizada con clientes de Erika M. Estefan Propiedades',
  },
  {
    type: 'image',
    src: '/images/branding/inmobiliaria-noche.jpg',
    alt: 'Fachada nocturna de Erika M. Estefan Propiedades',
  },
]
const currentContactSlide = ref(0)
const contactVideoRefs = ref({})
let contactCarouselTimer = null
const CONTACT_IMAGE_DURATION = 5000
const CONTACT_VIDEO_DURATION = 10000

function getContactSlideDuration(index) {
  return contactHeroSlides[index]?.type === 'video'
    ? CONTACT_VIDEO_DURATION
    : CONTACT_IMAGE_DURATION
}

function startContactCarousel() {
  stopContactCarousel()
  contactCarouselTimer = setTimeout(() => {
    currentContactSlide.value = (currentContactSlide.value + 1) % contactHeroSlides.length
  }, getContactSlideDuration(currentContactSlide.value))
}

function stopContactCarousel() {
  if (contactCarouselTimer) {
    clearTimeout(contactCarouselTimer)
    contactCarouselTimer = null
  }
}

function goToContactSlide(index) {
  currentContactSlide.value = index
  startContactCarousel()
}

onMounted(() => {
  startContactCarousel()
})

onBeforeUnmount(() => {
  stopContactCarousel()
})

watch(currentContactSlide, async (index) => {
  startContactCarousel()

  if (contactHeroSlides[index]?.type !== 'video') {
    return
  }

  await nextTick()
  const video = contactVideoRefs.value[index]
  if (!video) {
    return
  }

  try {
    video.currentTime = 0
    video.playbackRate = 0.75
    await video.play()
  } catch {
    // Ignoramos bloqueos de autoplay del navegador.
  }
})

function setContactVideoRef(index, el) {
  if (el) {
    contactVideoRefs.value[index] = el
  } else {
    delete contactVideoRefs.value[index]
  }
}

const contactItems = [
  {
    title: 'Ubicacion',
    text: settings.address,
    subtext: 'Santa Clara del Mar, Buenos Aires',
    icon: 'M17.657 16.657L13.414 20.9a2 2 0 01-2.828 0l-4.243-4.243a8 8 0 1111.314 0z M15 11a3 3 0 11-6 0 3 3 0 016 0z',
    wide: true,
  },
  {
    title: 'Email',
    text: settings.email,
    subtext: 'Respuesta dentro del horario comercial',
    icon: 'M3 8l7.89 4.26a2 2 0 002.22 0L21 8m-18 8h18a2 2 0 002-2V8a2 2 0 00-2-2H3a2 2 0 00-2 2v6a2 2 0 002 2z',
    wide: true,
  },
  {
    title: 'Celular',
    text: settings.phone,
    subtext: 'Atencion personalizada',
    icon: 'M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.128a11.042 11.042 0 005.516 5.516l1.128-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z',
    wide: true,
  },
  {
    title: 'Horario de atencion',
    text: 'Lunes a viernes de 09:00 a 19:00 hs',
    subtext: 'Sábado de 09:00 a 13:00 hs. Horarios sujetos a cambios.',
    icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z',
    wide: true,
  },
]

const socialLinks = [
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
]
</script>

<template>
  <section class="bg-gradient-to-b from-emerald-50 via-white to-white">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14 md:py-20">
      <div class="mb-10 overflow-hidden rounded-[30px] bg-white shadow-[0_24px_60px_rgba(15,23,42,0.08)] ring-1 ring-emerald-100 md:mb-14">
        <div class="grid lg:grid-cols-[1.26fr_0.74fr]">
          <div class="relative bg-gradient-to-br from-emerald-950 via-emerald-800 to-emerald-500">
            <template v-for="(slide, index) in contactHeroSlides" :key="`${slide.type}-${slide.src}-${index}`">
              <img
                v-if="slide.type === 'image'"
                :src="slide.src"
                :alt="slide.alt"
                class="absolute inset-0 h-full min-h-[380px] w-full rounded-[26px] object-contain object-center p-4 transition-opacity duration-700 lg:min-h-[620px] lg:p-8"
                :class="currentContactSlide === index ? 'opacity-100' : 'opacity-0'"
                loading="eager"
              />
              <video
                v-else
                :ref="(el) => setContactVideoRef(index, el)"
                class="absolute inset-0 h-full min-h-[380px] w-full rounded-[26px] object-contain object-center p-4 transition-opacity duration-700 lg:min-h-[620px] lg:p-8"
                :class="currentContactSlide === index ? 'opacity-100' : 'opacity-0'"
                :poster="slide.poster"
                autoplay
                muted
                loop
                playsinline
                preload="auto"
                @loadedmetadata="$event.target.playbackRate = 0.75"
              >
                <source :src="slide.src" type="video/webm" />
              </video>
            </template>
            <div class="pointer-events-none absolute inset-0 bg-[linear-gradient(115deg,rgba(2,6,23,0.18)_0%,rgba(2,6,23,0.05)_48%,rgba(2,6,23,0.18)_100%)]" />
            <div class="absolute bottom-5 left-1/2 z-10 flex -translate-x-1/2 items-center gap-2 rounded-full bg-slate-950/35 px-3 py-2 backdrop-blur-sm">
              <button
                v-for="(_, index) in contactHeroSlides"
                :key="`dot-${index}`"
                type="button"
                class="h-2.5 rounded-full transition-all"
                :class="currentContactSlide === index ? 'w-6 bg-white' : 'w-2.5 bg-white/55 hover:bg-white/80'"
                :aria-label="`Ir a slide ${index + 1}`"
                @click="goToContactSlide(index)"
              />
            </div>
          </div>

          <div class="relative flex items-center bg-gradient-to-br from-emerald-950 via-emerald-800 to-emerald-400 px-6 py-12 text-white sm:px-10 lg:px-16 lg:py-20">
            <div class="max-w-2xl">
              <p class="mb-3 text-sm font-semibold uppercase tracking-[0.28em] text-emerald-100/90">Nosotros</p>
              <p class="mt-5 max-w-lg text-base leading-8 text-emerald-50 md:text-lg">
                Nos caracterizamos por ser una empresa seria, cumpliendo con los mas altos regimenes de calidad en servicio y trato con nuestros clientes.
              </p>
              <p class="mt-4 max-w-lg text-base leading-8 text-emerald-100/95 md:text-lg">
                Logrando asi un posicionamiento de excelencia y compromiso. Hoy en dia, ya con cientos operaciones concretadas, nos hemos propuesto llegar mas alla, brindando mediante esta Web, operaciones en tiempo real, apuntaladas a la base de nuestras raices iniciales: servicio y honestidad.
              </p>
              <div class="mt-7 inline-flex items-center rounded-full border border-white/15 bg-white/10 px-4 py-2 text-sm font-medium text-emerald-50 backdrop-blur-sm">
                Erika M. Estefan Propiedades · Reg. 3867
              </div>
            </div>
            <div class="pointer-events-none absolute inset-y-0 -left-16 hidden w-40 bg-gradient-to-l from-emerald-700/90 via-emerald-400/45 to-transparent blur-2xl lg:block" />
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 xl:grid-cols-[minmax(0,1.02fr)_minmax(0,1fr)] gap-8 lg:gap-10 items-stretch">
        <article class="flex h-full flex-col rounded-[28px] bg-white shadow-[0_20px_50px_rgba(15,23,42,0.08)] ring-1 ring-emerald-100 overflow-hidden">
          <div class="grid h-[250px] grid-rows-[auto_1fr_auto] content-start px-7 py-8 md:h-[264px] md:px-9 md:py-10 bg-gradient-to-br from-emerald-950 via-[#0b5b38] to-emerald-800 text-white">
            <p class="inline-flex items-center rounded-full bg-white/14 px-3 py-1 text-sm font-semibold uppercase tracking-[0.14em] text-white mb-4 ring-1 ring-white/15">
              Erika M. Estefan
            </p>
            <h2 class="text-2xl md:text-3xl font-bold leading-tight self-start">Asesoramiento cercano con mirada profesional</h2>
            <p class="mt-3 text-emerald-50/90 text-base self-end">Reg. 3867</p>
          </div>

          <div class="flex flex-1 flex-col p-7 md:p-9 lg:p-10">
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 md:gap-5">
              <div
                v-for="item in contactItems"
                :key="item.title"
                class="h-full rounded-2xl bg-emerald-50/70 border border-emerald-100 p-5 md:p-6 shadow-[0_8px_24px_rgba(16,24,40,0.05)]"
                :class="item.wide ? 'lg:col-span-2' : ''"
              >
                <div class="flex items-start gap-4 min-w-0">
                  <span class="flex h-11 w-11 items-center justify-center rounded-2xl bg-white text-emerald-800 shadow-sm flex-shrink-0">
                    <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" :d="item.icon" />
                    </svg>
                  </span>
                  <div class="min-w-0">
                    <p class="text-xs font-semibold uppercase tracking-[0.14em] text-emerald-700 leading-5">{{ item.title }}</p>
                    <p class="mt-1.5 text-base font-semibold text-slate-900 leading-6" :class="item.wide ? 'text-[15px] md:text-base break-all' : 'break-words'">{{ item.text }}</p>
                    <p class="mt-1.5 text-sm text-slate-500 leading-6 break-words">{{ item.subtext }}</p>
                  </div>
                </div>
              </div>
            </div>

            <div class="mt-5 rounded-2xl border border-emerald-100 bg-emerald-50/70 p-5 md:p-6 shadow-[0_8px_24px_rgba(16,24,40,0.05)]">
              <p class="text-xs font-semibold uppercase tracking-[0.14em] text-emerald-700">Redes</p>
              <p class="mt-2 text-sm leading-6 text-slate-500">Seguinos y encontranos tambien en nuestras cuentas oficiales.</p>
              <div class="mt-4 flex flex-wrap gap-3">
                <a
                  v-for="social in socialLinks"
                  :key="social.name"
                  :href="social.href"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="inline-flex h-12 items-center gap-3 rounded-2xl border border-emerald-200 bg-white px-4 text-sm font-semibold text-slate-900 shadow-sm transition-all hover:-translate-y-0.5 hover:border-emerald-300 hover:bg-emerald-50"
                >
                  <span class="flex h-9 w-9 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-800">
                    <svg class="h-4.5 w-4.5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" :d="social.icon" />
                    </svg>
                  </span>
                  {{ social.name }}
                </a>
              </div>
            </div>
          </div>
        </article>

        <ContactForm />
      </div>

      <div class="mt-10 rounded-[28px] bg-white shadow-[0_20px_50px_rgba(15,23,42,0.08)] ring-1 ring-emerald-100 overflow-hidden">
        <div class="px-7 py-6 md:px-9 md:py-7 border-b border-emerald-100 bg-emerald-50/60">
          <p class="text-sm font-semibold uppercase tracking-[0.18em] text-emerald-700 mb-2">Ubicacion</p>
          <h2 class="text-xl md:text-2xl font-bold text-slate-900 leading-tight">Acapulco 84, Santa Clara del Mar, Buenos Aires, Argentina</h2>
        </div>
        <div class="p-3 md:p-4 bg-white">
          <iframe
            :src="mapSrc"
            class="w-full h-[320px] md:h-[420px] rounded-3xl"
            style="border: 0"
            allowfullscreen=""
            loading="lazy"
            referrerpolicy="no-referrer-when-downgrade"
            title="Mapa de Erika M. Estefan Propiedades"
          />
        </div>
      </div>
    </div>
  </section>
</template>
