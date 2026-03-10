<script setup>
import { ref } from 'vue'

const form = ref({
  name: '',
  email: '',
  phone: '',
  message: '',
})
const sending = ref(false)
const sent = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  if (!form.value.name?.trim() || !form.value.email?.trim()) {
    error.value = 'Nombre y correo electronico son obligatorios.'
    return
  }

  sending.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 800))
    sent.value = true
    form.value = { name: '', email: '', phone: '', message: '' }
  } catch {
    error.value = 'No se pudo enviar. Intenta nuevamente.'
  } finally {
    sending.value = false
  }
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
      <p v-if="sent" class="mb-5 rounded-2xl bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800 border border-emerald-200">
        Mensaje enviado correctamente. Gracias por contactarte.
      </p>
      <p v-if="error" class="mb-5 rounded-2xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700 border border-red-200">
        {{ error }}
      </p>

      <form v-if="!sent" class="flex flex-1 flex-col space-y-4" @submit.prevent="submit">
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

        <button
          type="submit"
          class="mt-auto inline-flex h-14 w-full items-center justify-center rounded-2xl bg-[#0b7a4b] px-6 text-base font-semibold text-white shadow-[0_16px_30px_rgba(11,122,75,0.24)] transition-all hover:-translate-y-0.5 hover:bg-[#09663f] disabled:translate-y-0 disabled:opacity-70"
          :disabled="sending"
        >
          {{ sending ? 'Enviando...' : 'Enviar' }}
        </button>
      </form>
    </div>
  </article>
</template>
