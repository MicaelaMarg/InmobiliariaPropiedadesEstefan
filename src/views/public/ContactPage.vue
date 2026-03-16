<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useAppStore } from '../../stores/app'
import ContactForm from '../../components/contact/ContactForm.vue'

const app = useAppStore()
const settings = app.settings
const whatsappUrl = `https://wa.me/${settings.whatsapp.replace(/\D/g, '')}`
const mapSrc = 'https://www.google.com/maps?q=Acapulco+79,+Santa+Clara+del+Mar,+Buenos+Aires,+Argentina&z=16&output=embed'
const contactHeroSlides = [
  {
    type: 'image',
    src: '/images/branding/fotoContacto.jpeg',
    alt: 'Atención personalizada de Erika M. Estefan Propiedades',
  },
  {
    type: 'image',
    src: '/images/imagen-hero.png',
    alt: 'Propiedad destacada para contacto inmobiliario',
  },
  {
    type: 'image',
    src: '/images/branding/IMG_3241_1_11zon.jpg',
    alt: 'Interior de propiedad para consultas inmobiliarias',
  },
  {
    type: 'video',
    src: '/images/branding/adentro1.webm',
    poster: '/images/branding/fotoContacto.jpeg',
    alt: 'Video interior de propiedad',
  },
  {
    type: 'image',
    src: '/images/branding/fotoContacto.jpeg',
    alt: 'Contacto directo con Erika M. Estefan Propiedades',
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
  },
  {
    title: 'Horario de atencion',
    text: '09:00 a 19:00 hs',
    subtext: 'Lunes a viernes',
    icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z',
  },
]
</script>

<template>
  <section class="bg-gradient-to-b from-emerald-50 via-white to-white">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14 md:py-20">
      <div class="mb-10 overflow-hidden rounded-[30px] bg-white shadow-[0_24px_60px_rgba(15,23,42,0.08)] ring-1 ring-emerald-100 md:mb-14">
        <div class="grid lg:grid-cols-[1.15fr_0.85fr]">
          <div class="relative bg-gradient-to-br from-emerald-950 via-emerald-800 to-emerald-500">
            <template v-for="(slide, index) in contactHeroSlides" :key="`${slide.type}-${slide.src}-${index}`">
              <img
                v-if="slide.type === 'image'"
                :src="slide.src"
                :alt="slide.alt"
                class="absolute inset-0 h-full min-h-[280px] w-full object-cover object-center transition-opacity duration-700 lg:min-h-[420px]"
                :class="currentContactSlide === index ? 'opacity-100' : 'opacity-0'"
                loading="eager"
              />
              <video
                v-else
                :ref="(el) => setContactVideoRef(index, el)"
                class="absolute inset-0 h-full min-h-[280px] w-full object-contain object-center transition-opacity duration-700 lg:min-h-[420px]"
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
            <div class="pointer-events-none absolute inset-0 bg-[linear-gradient(115deg,rgba(2,6,23,0.10)_0%,rgba(2,6,23,0.03)_48%,rgba(2,6,23,0.10)_100%)]" />
            <div class="absolute bottom-4 left-1/2 z-10 flex -translate-x-1/2 items-center gap-2 rounded-full bg-slate-950/35 px-3 py-2 backdrop-blur-sm">
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

          <div class="relative flex items-center bg-gradient-to-br from-emerald-950 via-emerald-800 to-emerald-400 px-6 py-12 text-white sm:px-10 lg:px-14 lg:py-16">
            <div class="max-w-2xl">
              <p class="mb-3 text-sm font-semibold uppercase tracking-[0.28em] text-emerald-100/90">Nosotros</p>
              <h1 class="text-3xl font-bold leading-tight md:text-5xl">
                
              </h1>
              <p class="mt-5 text-base leading-8 text-emerald-50 md:text-lg">
                Nos caracterizamos por ser una empresa seria, cumpliendo con los más altos regímenes de calidad en servicio y trato con nuestros clientes.
              </p>
              <p class="mt-4 text-base leading-8 text-emerald-100/95 md:text-lg">
                Logrando así un posicionamiento de excelencia y compromiso. Hoy en día, ya con cientos operaciones concretadas, nos hemos propuesto llegar más alla, brindando mediante esta Web, operaciones en tiempo real, apuntaladas a la base de nuestras raíces iniciales: servicio y honestidad.
              </p>
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

            <a
              :href="whatsappUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="mt-7 inline-flex h-14 w-full items-center justify-center gap-3 rounded-2xl bg-[#0b7a4b] px-6 text-base font-semibold text-white shadow-[0_16px_30px_rgba(11,122,75,0.28)] transition-all hover:-translate-y-0.5 hover:bg-[#09663f]"
            >
              <svg class="h-5 w-5" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                <path d="M20.52 3.48A11.8 11.8 0 0012.15 0C5.59 0 .24 5.35.24 11.92c0 2.1.55 4.15 1.6 5.95L0 24l6.31-1.79a11.88 11.88 0 005.84 1.49h.01c6.56 0 11.91-5.35 11.91-11.92 0-3.18-1.24-6.17-3.55-8.3zM12.16 21.7h-.01a9.87 9.87 0 01-5.03-1.38l-.36-.21-3.75 1.06 1-3.65-.24-.38a9.88 9.88 0 01-1.52-5.23c0-5.45 4.43-9.88 9.89-9.88 2.64 0 5.13 1.03 6.99 2.89a9.79 9.79 0 012.9 6.99c0 5.45-4.44 9.89-9.87 9.89zm5.42-7.42c-.3-.15-1.77-.87-2.04-.96-.27-.1-.47-.15-.66.15-.2.3-.76.96-.94 1.16-.17.2-.35.22-.65.08-.3-.15-1.25-.46-2.38-1.46-.88-.78-1.48-1.73-1.66-2.03-.17-.3-.02-.46.13-.6.14-.14.3-.35.45-.52.15-.18.2-.3.3-.5.1-.2.05-.38-.02-.53-.08-.15-.66-1.6-.91-2.2-.24-.57-.48-.49-.66-.5h-.56c-.2 0-.52.07-.79.38-.27.3-1.04 1.02-1.04 2.48 0 1.46 1.06 2.87 1.21 3.07.15.2 2.08 3.18 5.04 4.46.7.3 1.26.49 1.69.63.71.22 1.36.19 1.87.11.57-.08 1.77-.72 2.02-1.42.25-.7.25-1.31.17-1.43-.07-.11-.27-.18-.57-.33z"/>
              </svg>
              Escribinos por WhatsApp
            </a>
          </div>
        </article>

        <ContactForm />
      </div>

      <div class="mt-10 rounded-[28px] bg-white shadow-[0_20px_50px_rgba(15,23,42,0.08)] ring-1 ring-emerald-100 overflow-hidden">
        <div class="px-7 py-6 md:px-9 md:py-7 border-b border-emerald-100 bg-emerald-50/60">
          <p class="text-sm font-semibold uppercase tracking-[0.18em] text-emerald-700 mb-2">Ubicacion</p>
          <h2 class="text-xl md:text-2xl font-bold text-slate-900 leading-tight">Acapulco 79, Santa Clara del Mar, Buenos Aires, Argentina</h2>
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
