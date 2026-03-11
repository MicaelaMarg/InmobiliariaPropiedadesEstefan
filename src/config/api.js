/**
 * URL base de la API. Si VITE_API_URL no termina en /api, se agrega
 * (así funciona aunque en Railway pongan solo el dominio).
 */
const raw = (import.meta.env.VITE_API_URL || '').replace(/\/+$/, '')
export const API_BASE_URL = raw && !raw.endsWith('/api') ? raw + '/api' : raw
