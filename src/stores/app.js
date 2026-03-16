import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const adminSidebarOpen = ref(false)

  const settings = ref({
    businessName: 'Erika M. Estefan Propiedades',
    phone: '2236036761',
    whatsapp: '5492236036761',
    email: 'erikaestefanpropiedades@gmail.com',
    address: 'Acapulco 84',
  })

  function updateSettings(newSettings) {
    settings.value = { ...settings.value, ...newSettings }
  }

  return {
    adminSidebarOpen,
    settings,
    updateSettings,
  }
})
