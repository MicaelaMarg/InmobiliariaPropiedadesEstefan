// Servicio de propiedades. Usa mock data si no hay backend configurado.

import { getMockProperties } from '../data/mockProperties'
import { API_BASE_URL, USE_MOCK, assertApiConfigured } from '../config/api'
const MOCK_STORAGE_KEY = 'inmobiliaria_mock_properties'

// Estado en memoria para mock (persiste create/update/delete durante la sesion)
let mockList = null

function normalizeProperty(item = {}) {
  return {
    ...item,
    showPrice: item.showPrice ?? true,
    hasExpenses: item.hasExpenses ?? false,
    highlightedMessages: Array.isArray(item.highlightedMessages) ? item.highlightedMessages : [],
    paymentOptions: Array.isArray(item.paymentOptions) ? item.paymentOptions : [],
    services: Array.isArray(item.services) ? item.services : [],
  }
}

function getMockList() {
  if (!mockList) {
    const stored = readStoredMockList()
    mockList = stored || getMockProperties()
    mockList = mockList.map(normalizeProperty)
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

function isFormDataPayload(value) {
  return typeof FormData !== 'undefined' && value instanceof FormData
}

async function getResponseErrorMessage(response, fallbackMessage) {
  try {
    const data = await response.json()
    if (typeof data?.error === 'string' && data.error.trim()) {
      return data.error
    }
  } catch {
    // Ignorar cuerpo no JSON o vacio.
  }
  return fallbackMessage
}

async function parseMockAdminPayload(data) {
  if (!isFormDataPayload(data)) {
    return data
  }

  const rawProperty = data.get('property')
  const parsed = rawProperty ? JSON.parse(String(rawProperty)) : {}
  const filesByToken = new Map()

  for (const [key, value] of data.entries()) {
    if (!key.startsWith('imageFile_') || !(value instanceof File)) continue
    filesByToken.set(key.slice('imageFile_'.length), value)
  }

  const images = await Promise.all((parsed.images || []).map(async (image) => {
    if (!image?.uploadToken) return image

    const file = filesByToken.get(image.uploadToken)
    if (!file) return image

    const objectUrl = URL.createObjectURL(file)
    return {
      ...image,
      url: objectUrl,
      thumbnailUrl: objectUrl,
      mediumUrl: objectUrl,
      largeUrl: objectUrl,
      placeholderUrl: null,
      mimeType: file.type || image.mimeType || null,
      originalName: file.name || image.originalName || null,
    }
  }))

  return {
    ...parsed,
    images,
  }
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
    let list = getMockList().filter(p => p.isPublished)
    if (filters.operation) list = list.filter(p => p.operation === filters.operation)
    if (filters.type) list = list.filter(p => p.type === filters.type)
    if (filters.minPrice != null) list = list.filter(p => p.price >= filters.minPrice)
    if (filters.maxPrice != null) list = list.filter(p => p.price <= filters.maxPrice)
    if (filters.location) {
      const loc = filters.location.toLowerCase()
      list = list.filter(p => (p.location + ' ' + p.city).toLowerCase().includes(loc))
    }
    if (filters.search) {
      const q = filters.search.toLowerCase()
      list = list.filter(
        p =>
          p.title?.toLowerCase().includes(q) ||
          p.location?.toLowerCase().includes(q) ||
          p.city?.toLowerCase().includes(q) ||
          p.referenceCode?.toLowerCase().includes(q)
      )
    }
    const total = list.length
    const totalPages = Math.max(Math.ceil(total / limit), 1)
    const currentPage = Math.min(page, totalPages)
    const start = (currentPage - 1) * limit
    return {
      items: list.slice(start, start + limit).map(normalizeProperty),
      total,
      page: currentPage,
      limit,
      totalPages,
    }
  }

  assertApiConfigured()
  const res = await fetch(buildUrl('/properties', filters))
  if (!res.ok) throw new Error('Error al cargar propiedades')
  const data = await res.json()
  if (Array.isArray(data?.items)) {
    data.items = data.items.map(normalizeProperty)
  }
  return data
}

export async function fetchFeaturedProperties() {
  if (USE_MOCK) {
    return getMockList()
      .filter(p => p.isPublished && p.status === 'available' && p.isFeatured)
      .slice(0, 6)
  }

  assertApiConfigured()
  const res = await fetch(buildUrl('/properties', { featured: 1, limit: 6, page: 1 }))
  if (!res.ok) throw new Error('Error al cargar destacadas')
  const data = await res.json()
  return Array.isArray(data?.items) ? data.items.map(normalizeProperty) : []
}

export async function fetchPropertyBySlug(slug) {
  if (USE_MOCK) {
    const list = getMockList().filter(p => p.isPublished)
    return list.find(p => p.slug === slug) || null
  }

  assertApiConfigured()
  const res = await fetch(buildUrl(`/properties/by-slug/${slug}`))
  if (res.status === 404) return null
  if (!res.ok) throw new Error('Error al cargar propiedad')
  const data = await res.json()
  return data ? normalizeProperty(data) : null
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

  assertApiConfigured()
  const res = await fetch(buildUrl('/admin/properties', filters), {
    headers: getAuthHeader(),
  })
  if (!res.ok) throw new Error('Error al cargar propiedades')
  return res.json()
}

export async function fetchPropertyById(id) {
  if (USE_MOCK) return getMockList().find(p => p.id === id) || null

  assertApiConfigured()
  const res = await fetch(buildUrl(`/admin/properties/${id}`), {
    headers: getAuthHeader(),
  })
  if (res.status === 404) return null
  if (!res.ok) throw new Error('Error al cargar propiedad')
  const data = await res.json()
  return data ? normalizeProperty(data) : null
}

export async function createProperty(data) {
  if (USE_MOCK) {
    const payload = await parseMockAdminPayload(data)
    const list = getMockList()
    const id = String(Math.max(...list.map(p => parseInt(p.id, 10) || 0), 0) + 1)
    const slug = payload.slug || `${slugify(payload.title || 'propiedad')}-${id}`
    const now = new Date().toISOString()
    const newProp = {
      ...payload,
      id,
      slug,
      images: payload.images || [],
      createdAt: now,
      updatedAt: now,
    }
    list.push(newProp)
    persistMockList()
    return newProp
  }

  assertApiConfigured()
  const headers = isFormDataPayload(data)
    ? { ...getAuthHeader() }
    : { 'Content-Type': 'application/json', ...getAuthHeader() }
  const res = await fetch(buildUrl('/admin/properties'), {
    method: 'POST',
    headers,
    body: isFormDataPayload(data) ? data : JSON.stringify(data),
  })
  if (!res.ok) throw new Error(await getResponseErrorMessage(res, 'Error al crear propiedad'))
  return res.json()
}

export async function updateProperty(id, data) {
  if (USE_MOCK) {
    const payload = await parseMockAdminPayload(data)
    const list = getMockList()
    const idx = list.findIndex(p => p.id === id)
    if (idx === -1) return null
    list[idx] = { ...list[idx], ...payload, updatedAt: new Date().toISOString() }
    persistMockList()
    return list[idx]
  }

  assertApiConfigured()
  const headers = isFormDataPayload(data)
    ? { ...getAuthHeader() }
    : { 'Content-Type': 'application/json', ...getAuthHeader() }
  const res = await fetch(buildUrl(`/admin/properties/${id}`), {
    method: 'PUT',
    headers,
    body: isFormDataPayload(data) ? data : JSON.stringify(data),
  })
  if (!res.ok) throw new Error(await getResponseErrorMessage(res, 'Error al actualizar propiedad'))
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

  assertApiConfigured()
  const res = await fetch(buildUrl(`/admin/properties/${id}`), {
    method: 'DELETE',
    headers: getAuthHeader(),
  })
  if (!res.ok) throw new Error('Error al eliminar propiedad')
  return true
}

export { slugify }
