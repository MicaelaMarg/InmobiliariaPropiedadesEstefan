/**
 * URL base de la API. Si VITE_API_URL no termina en /api, se agrega.
 * Si no está definida, se devuelve vacío para evitar apuntar por error al dominio del frontend.
 */
const raw = (import.meta.env.VITE_API_URL || '').trim().replace(/\/+$/, '')

export const API_BASE_URL =
  raw && !raw.endsWith('/api')
    ? raw + '/api'
    : raw
