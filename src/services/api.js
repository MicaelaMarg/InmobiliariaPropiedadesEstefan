// Cliente API base. Usa config para normalizar la URL (siempre termina en /api).
import { API_BASE_URL } from '../config/api'

const BASE_URL = API_BASE_URL

function getAuthHeaders() {
  const auth = localStorage.getItem('inmobiliaria_auth')
  if (!auth) return {}
  try {
    const { token } = JSON.parse(auth)
    return token ? { Authorization: `Bearer ${token}` } : {}
  } catch {
    return {}
  }
}

export async function apiGet(endpoint) {
  const res = await fetch(BASE_URL + endpoint, { headers: getAuthHeaders() })
  if (!res.ok) throw new Error(res.statusText)
  return res.json()
}

export async function apiPost(endpoint, body) {
  const res = await fetch(BASE_URL + endpoint, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(res.statusText)
  return res.json()
}

export async function apiPut(endpoint, body) {
  const res = await fetch(BASE_URL + endpoint, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(res.statusText)
  return res.json()
}

export async function apiDelete(endpoint) {
  const res = await fetch(BASE_URL + endpoint, { method: 'DELETE', headers: getAuthHeaders() })
  if (!res.ok) throw new Error(res.statusText)
  return res.json?.() ?? {}
}
