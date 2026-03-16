<script setup>
import { ref } from 'vue'
import { useAppStore } from '../../stores/app'

const form = ref({
  name: '',
  email: '',
  phone: '',
  message: '',
})
const app = useAppStore()
const whatsappUrl = `https://wa.me/${app.settings.whatsapp.replace(/\D/g, '')}`
const error = ref('')

function submit() {
  error.value = ''
  if (!form.value.name?.trim() || !form.value.email?.trim()) {
    error.value = 'Nombre y correo electronico son obligatorios.'
    return
  }

  const whatsappMessage = [
    'Hola, quiero hacer una consulta.',
    `Nombre: ${form.value.name.trim()}`,
    `Correo: ${form.value.email.trim()}`,
    `Telefono: ${form.value.phone?.trim() || '-'}`,
    `Mensaje: ${form.value.message?.trim() || '-'}`,
  ].join('\n')

  window.open(`${whatsappUrl}?text=${encodeURIComponent(whatsappMessage)}`, '_blank', 'noopener,noreferrer')
}
</script>

<template>
  <article class="flex h-full flex-col rounded-[28px] bg-white shadow-[0_20px_50px_rgba(15,23,42,0.08)] ring-1 ring-emerald-100 overflow-hidden">
    <div class="grid h-[250px] grid-rows-[auto_1fr_auto] content-start px-7 py-8 md:h-[264px] md:px-9 md:py-10 bg-gradient-to-br from-emerald-950 via-[#0b5b38] to-emerald-800 text-white">
      <p class="inline-flex items-center rounded-full bg-white/14 px-3 py-1 text-sm font-semibold uppercase tracking-[0.14em] text-white mb-4 ring-1 ring-white/15">
        Formulario
      </p>
      <h2 class="text-2xl md:text-3xl font-bold leading-tight self-start">Contanos que propiedad estas buscando</h2>
      <p class="mt-3 text-emerald-50/95 leading-7 self-end">
        Dejanos tus datos y tu mensaje. Te respondemos a la brevedad con atencion personalizada.
      </p>
    </div>

    <div class="flex flex-1 flex-col p-7 md:p-9 lg:p-10">
      <p v-if="error" class="mb-5 rounded-2xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700 border border-red-200">
        {{ error }}
      </p>

      <form class="flex flex-1 flex-col space-y-4" @submit.prevent="submit">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 items-start">
          <label class="block min-w-0">
            <span class="mb-1.5 block text-sm font-semibold text-slate-700">Nombre</span>
            <input
              v-model="form.name"
              type="text"
              required
              class="block w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-100"
              placeholder="Tu nombre"
            />
          </label>

          <label class="block min-w-0">
            <span class="mb-1.5 block text-sm font-semibold text-slate-700">Correo electronico</span>
            <input
              v-model="form.email"
              type="email"
              required
              class="block w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-100"
              placeholder="nombre@email.com"
            />
          </label>
        </div>

        <label class="block min-w-0">
          <span class="mb-1.5 block text-sm font-semibold text-slate-700">Telefono</span>
          <input
            v-model="form.phone"
            type="tel"
            class="block w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-100"
            placeholder="Tu celular"
          />
        </label>

        <label class="block min-w-0">
          <span class="mb-1.5 block text-sm font-semibold text-slate-700">Mensaje</span>
          <textarea
            v-model="form.message"
            rows="6"
            class="block w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-emerald-500 focus:bg-white focus:ring-4 focus:ring-emerald-100 resize-y min-h-[240px] md:min-h-[278px]"
            placeholder="Contanos sobre tu consulta, propiedad o zona de interes"
          />
        </label>

        <div class="mt-auto grid gap-3 pt-2">
          <button
            type="submit"
            class="inline-flex h-14 w-full items-center justify-center rounded-2xl bg-[#0b7a4b] px-6 text-base font-semibold text-white shadow-[0_16px_30px_rgba(11,122,75,0.24)] transition-all hover:-translate-y-0.5 hover:bg-[#09663f] disabled:translate-y-0 disabled:opacity-70"
          >
            <svg class="mr-3 h-5 w-5" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path d="M20.52 3.48A11.8 11.8 0 0012.15 0C5.59 0 .24 5.35.24 11.92c0 2.1.55 4.15 1.6 5.95L0 24l6.31-1.79a11.88 11.88 0 005.84 1.49h.01c6.56 0 11.91-5.35 11.91-11.92 0-3.18-1.24-6.17-3.55-8.3zM12.16 21.7h-.01a9.87 9.87 0 01-5.03-1.38l-.36-.21-3.75 1.06 1-3.65-.24-.38a9.88 9.88 0 01-1.52-5.23c0-5.45 4.43-9.88 9.89-9.88 2.64 0 5.13 1.03 6.99 2.89a9.79 9.79 0 012.9 6.99c0 5.45-4.44 9.89-9.87 9.89zm5.42-7.42c-.3-.15-1.77-.87-2.04-.96-.27-.1-.47-.15-.66.15-.2.3-.76.96-.94 1.16-.17.2-.35.22-.65.08-.3-.15-1.25-.46-2.38-1.46-.88-.78-1.48-1.73-1.66-2.03-.17-.3-.02-.46.13-.6.14-.14.3-.35.45-.52.15-.18.2-.3.3-.5.1-.2.05-.38-.02-.53-.08-.15-.66-1.6-.91-2.2-.24-.57-.48-.49-.66-.5h-.56c-.2 0-.52.07-.79.38-.27.3-1.04 1.02-1.04 2.48 0 1.46 1.06 2.87 1.21 3.07.15.2 2.08 3.18 5.04 4.46.7.3 1.26.49 1.69.63.71.22 1.36.19 1.87.11.57-.08 1.77-.72 2.02-1.42.25-.7.25-1.31.17-1.43-.07-.11-.27-.18-.57-.33z"/>
            </svg>
            Escribinos por WhatsApp
          </button>
        </div>
      </form>
    </div>
  </article>
</template>
