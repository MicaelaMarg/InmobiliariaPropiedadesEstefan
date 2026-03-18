/**
 * URL base de la API. Si VITE_API_URL no termina en /api, se agrega.
 * En dev puede quedar vacío para habilitar el modo mock; en prod debe estar siempre.
 */
const raw = (import.meta.env.VITE_API_URL || '').trim().replace(/\/+$/, '')

export const API_BASE_URL = raw && !raw.endsWith('/api') ? `${raw}/api` : raw

export const IS_DEV = import.meta.env.DEV
export const USE_MOCK = IS_DEV && !API_BASE_URL

export function assertApiConfigured() {
  if (!API_BASE_URL && !USE_MOCK) {
    throw new Error('Backend no configurado: definí VITE_API_URL para producción')
  }
}
