// Servicio de propiedades. Usa mock data si no hay backend configurado.

import { getMockProperties } from '../data/mockProperties'
import { API_BASE_URL } from '../config/api'
const USE_MOCK = !API_BASE_URL
const MOCK_STORAGE_KEY = 'inmobiliaria_mock_properties'

// Estado en memoria para mock (persiste create/update/delete durante la sesion)
let mockList = null

function getMockList() {
  if (!mockList) {
    const stored = readStoredMockList()
    mockList = stored || getMockProperties()
  }
  return mockList
}

function readStoredMockList() {
  try {
    const raw = localStorage.getItem(MOCK_STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : null
  } catch {
    return null
  }
}

function persistMockList() {
  if (!USE_MOCK || !mockList) return
  try {
    localStorage.setItem(MOCK_STORAGE_KEY, JSON.stringify(mockList))
  } catch {
    // Ignorar errores de cuota/serializacion en modo mock.
  }
}

function getAuthHeader() {
  try {
    const raw = localStorage.getItem('inmobiliaria_auth')
    if (!raw) return {}
    const parsed = JSON.parse(raw)
    return parsed?.token ? { Authorization: `Bearer ${parsed.token}` } : {}
  } catch {
    return {}
  }
}

function buildUrl(path, filters = {}) {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params.append(key, value)
    }
  })
  const query = params.toString()
  return `${API_BASE_URL}${path}${query ? `?${query}` : ''}`
}

function slugify(text) {
  return text
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
}

export async function fetchPropertiesPublic(filters = {}) {
  const page = Number(filters.page) > 0 ? Number(filters.page) : 1
  const limit = Number(filters.limit) > 0 ? Number(filters.limit) : 12

  if (USE_MOCK) {
    let list = getMockList().filter(p => p.isPublished && p.status === 'available')
    if (filters.operation) list = list.filter(p => p.operation === filters.operation)
    if (filters.type) list = list.filter(p => p.type === filters.type)
    if (filters.minPrice != null) list = list.filter(p => p.price >= filters.minPrice)
    if (filters.maxPrice != null) list = list.filter(p => p.price <= filters.maxPrice)
    if (filters.location) {
      const loc = filters.location.toLowerCase()
      list = list.filter(p => (p.location + ' ' + p.city).toLowerCase().includes(loc))
    }
    const total = list.length
    const totalPages = Math.max(Math.ceil(total / limit), 1)
    const currentPage = Math.min(page, totalPages)
    const start = (currentPage - 1) * limit
    return {
      items: list.slice(start, start + limit),
      total,
      page: currentPage,
      limit,
      totalPages,
    }
  }

  const res = await fetch(buildUrl('/properties', filters))
  if (!res.ok) throw new Error('Error al cargar propiedades')
  return res.json()
}

export async function fetchFeaturedProperties() {
  if (USE_MOCK) {
    return getMockList()
      .filter(p => p.isPublished && p.status === 'available' && p.isFeatured)
      .slice(0, 6)
  }

  const res = await fetch(buildUrl('/properties', { featured: 1, limit: 6, page: 1 }))
  if (!res.ok) throw new Error('Error al cargar destacadas')
  const data = await res.json()
  return Array.isArray(data?.items) ? data.items : []
}

export async function fetchPropertyBySlug(slug) {
  if (USE_MOCK) {
    const list = getMockList().filter(p => p.isPublished && p.status === 'available')
    return list.find(p => p.slug === slug) || null
  }

  const res = await fetch(buildUrl(`/properties/by-slug/${slug}`))
  if (res.status === 404) return null
  if (!res.ok) throw new Error('Error al cargar propiedad')
  return res.json()
}

// Admin: todas las propiedades
export async function fetchAllProperties(filters = {}) {
  if (USE_MOCK) {
    let list = [...getMockList()]
    if (filters.search) {
      const q = filters.search.toLowerCase()
      list = list.filter(
        p =>
          p.title?.toLowerCase().includes(q) ||
          p.referenceCode?.toLowerCase().includes(q) ||
          p.location?.toLowerCase().includes(q)
      )
    }
    if (filters.status) list = list.filter(p => p.status === filters.status)
    if (filters.isPublished != null) list = list.filter(p => p.isPublished === filters.isPublished)
    return list
  }

  const res = await fetch(buildUrl('/admin/properties', filters), {
    headers: getAuthHeader(),
  })
  if (!res.ok) throw new Error('Error al cargar propiedades')
  return res.json()
}

export async function fetchPropertyById(id) {
  if (USE_MOCK) return getMockList().find(p => p.id === id) || null

  const res = await fetch(buildUrl(`/admin/properties/${id}`), {
    headers: getAuthHeader(),
  })
  if (res.status === 404) return null
  if (!res.ok) throw new Error('Error al cargar propiedad')
  return res.json()
}

export async function createProperty(data) {
  if (USE_MOCK) {
    const list = getMockList()
    const id = String(Math.max(...list.map(p => parseInt(p.id, 10) || 0), 0) + 1)
    const slug = data.slug || `${slugify(data.title || 'propiedad')}-${id}`
    const now = new Date().toISOString()
    const newProp = {
      ...data,
      id,
      slug,
      images: data.images || [],
      createdAt: now,
      updatedAt: now,
    }
    list.push(newProp)
    persistMockList()
    return newProp
  }

  const res = await fetch(buildUrl('/admin/properties'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error('Error al crear propiedad')
  return res.json()
}

export async function updateProperty(id, data) {
  if (USE_MOCK) {
    const list = getMockList()
    const idx = list.findIndex(p => p.id === id)
    if (idx === -1) return null
    list[idx] = { ...list[idx], ...data, updatedAt: new Date().toISOString() }
    persistMockList()
    return list[idx]
  }

  const res = await fetch(buildUrl(`/admin/properties/${id}`), {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
    body: JSON.stringify(data),
  })
  if (!res.ok) throw new Error('Error al actualizar propiedad')
  return res.json()
}

export async function deleteProperty(id) {
  if (USE_MOCK) {
    const list = getMockList()
    const idx = list.findIndex(p => p.id === id)
    if (idx === -1) return false
    list.splice(idx, 1)
    persistMockList()
    return true
  }

  const res = await fetch(buildUrl(`/admin/properties/${id}`), {
    method: 'DELETE',
    headers: getAuthHeader(),
  })
  if (!res.ok) throw new Error('Error al eliminar propiedad')
  return true
}

export { slugify }
