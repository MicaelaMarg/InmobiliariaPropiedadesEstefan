// Servicio de propiedades. Usa mock data si no hay backend configurado.

import { getMockProperties } from '../data/mockProperties'
import { API_BASE_URL, USE_MOCK, assertApiConfigured } from '../config/api'
const MOCK_STORAGE_KEY = 'inmobiliaria_mock_properties'

// Estado en memoria para mock (persiste create/update/delete durante la sesion)
let mockList = null
const CLOUDINARY_CONCURRENCY = 3
const CLOUDINARY_WIDTHS = {
  thumbnail: 320,
  medium: 960,
  large: 1440,
  placeholder: 24,
}

function normalizeProperty(item = {}) {
  return {
    ...item,
    country: item.country || 'Argentina',
    streetNumber: item.streetNumber ?? '',
    mapSource: item.mapSource || 'approximate',
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

function hasPendingImageUploads(images = []) {
  return Array.isArray(images) && images.some(image => image?.file instanceof File)
}

function clampWidth(originalWidth, targetWidth) {
  return Number.isFinite(originalWidth) ? Math.min(originalWidth, targetWidth) : targetWidth
}

function addCloudinaryTransformation(urlString, width) {
  try {
    const url = new URL(urlString)
    const segments = url.pathname.split('/').filter(Boolean)
    const uploadIdx = segments.indexOf('upload')
    if (uploadIdx === -1) return urlString

    const nextSegment = segments[uploadIdx + 1]
    const hasVersionSegment = nextSegment && /^v\d+/.test(nextSegment)
    const transformation = `f_auto,q_auto,c_limit,w_${width}`
    const nextSegments = [...segments]

    if (nextSegment && !hasVersionSegment) {
      nextSegments[uploadIdx + 1] = transformation
    } else {
      nextSegments.splice(uploadIdx + 1, 0, transformation)
    }

    url.pathname = `/${nextSegments.join('/')}`
    return url.toString()
  } catch {
    return urlString
  }
}

function stripTransientImageFields(image = {}, index = 0) {
  return {
    id: image.id ?? null,
    publicId: image.publicId ?? null,
    uploadToken: null,
    url: image.url || image.largeUrl || image.mediumUrl || image.thumbnailUrl || '',
    thumbnailUrl: image.thumbnailUrl || image.mediumUrl || image.url || '',
    mediumUrl: image.mediumUrl || null,
    largeUrl: image.largeUrl || image.url || image.mediumUrl || image.thumbnailUrl || '',
    placeholderUrl: image.placeholderUrl || null,
    width: image.width ?? null,
    height: image.height ?? null,
    thumbnailWidth: image.thumbnailWidth ?? null,
    mediumWidth: image.mediumWidth ?? null,
    largeWidth: image.largeWidth ?? image.width ?? null,
    mimeType: image.mimeType || null,
    originalName: image.originalName || null,
    order: image.order ?? index,
    isPrimary: !!image.isPrimary,
  }
}

function normalizeCloudinaryUpload(uploadResult = {}, sourceImage = {}, index = 0) {
  const secureUrl = uploadResult.secure_url || uploadResult.url || ''
  const width = Number.isFinite(uploadResult.width) ? uploadResult.width : null
  return {
    id: sourceImage.id ?? null,
    publicId: uploadResult.public_id || null,
    uploadToken: null,
    url: secureUrl,
    thumbnailUrl: secureUrl ? addCloudinaryTransformation(secureUrl, CLOUDINARY_WIDTHS.thumbnail) : '',
    mediumUrl: secureUrl ? addCloudinaryTransformation(secureUrl, CLOUDINARY_WIDTHS.medium) : '',
    largeUrl: secureUrl ? addCloudinaryTransformation(secureUrl, CLOUDINARY_WIDTHS.large) : '',
    placeholderUrl: secureUrl ? addCloudinaryTransformation(secureUrl, CLOUDINARY_WIDTHS.placeholder) : null,
    width,
    height: Number.isFinite(uploadResult.height) ? uploadResult.height : null,
    thumbnailWidth: clampWidth(width, CLOUDINARY_WIDTHS.thumbnail),
    mediumWidth: clampWidth(width, CLOUDINARY_WIDTHS.medium),
    largeWidth: clampWidth(width, CLOUDINARY_WIDTHS.large),
    mimeType: sourceImage.file?.type || sourceImage.mimeType || null,
    originalName: sourceImage.originalName || sourceImage.file?.name || null,
    order: sourceImage.order ?? index,
    isPrimary: !!sourceImage.isPrimary,
  }
}

async function getResponseErrorMessage(response, fallbackMessage) {
  try {
    const data = await response.json()
    if (typeof data?.error === 'string' && data.error.trim()) {
      return data.error
    }
    if (typeof data?.error?.message === 'string' && data.error.message.trim()) {
      return data.error.message
    }
  } catch {
    // Ignorar cuerpo no JSON o vacio.
  }
  return fallbackMessage
}

async function parseMockAdminPayload(data) {
  if (!isFormDataPayload(data)) {
    return parseMockObjectPayload(data)
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

async function parseMockObjectPayload(data) {
  if (!data || !Array.isArray(data.images)) {
    return data
  }

  const images = await Promise.all(data.images.map(async (image, index) => {
    if (!(image?.file instanceof File)) {
      return stripTransientImageFields(image, index)
    }

    const objectUrl = URL.createObjectURL(image.file)
    return {
      ...stripTransientImageFields(image, index),
      publicId: null,
      url: objectUrl,
      thumbnailUrl: objectUrl,
      mediumUrl: objectUrl,
      largeUrl: objectUrl,
      placeholderUrl: null,
      mimeType: image.file.type || image.mimeType || null,
      originalName: image.file.name || image.originalName || null,
    }
  }))

  return {
    ...data,
    images,
  }
}

async function fetchCloudinarySignature() {
  const res = await fetch(buildUrl('/admin/cloudinary/signature'), {
    headers: getAuthHeader(),
  })
  if (!res.ok) {
    throw new Error(await getResponseErrorMessage(res, 'Error al preparar la subida de imágenes'))
  }
  return res.json()
}

async function uploadImageToCloudinary(image, signature) {
  const formData = new FormData()
  formData.append('file', image.file)
  formData.append('api_key', String(signature.apiKey))
  formData.append('timestamp', String(signature.timestamp))
  formData.append('signature', String(signature.signature))
  formData.append('folder', String(signature.folder))
  formData.append('use_filename', String(signature.useFilename))
  formData.append('unique_filename', String(signature.uniqueFilename))
  formData.append('overwrite', String(signature.overwrite))

  const res = await fetch(signature.uploadUrl, {
    method: 'POST',
    body: formData,
  })

  if (!res.ok) {
    throw new Error(await getResponseErrorMessage(res, 'Error al subir una imagen a Cloudinary'))
  }

  return res.json()
}

async function mapWithConcurrency(items, limit, worker) {
  const results = new Array(items.length)
  let nextIndex = 0

  async function runWorker() {
    while (nextIndex < items.length) {
      const currentIndex = nextIndex++
      results[currentIndex] = await worker(items[currentIndex], currentIndex)
    }
  }

  const workerCount = Math.max(1, Math.min(limit, items.length))
  await Promise.all(Array.from({ length: workerCount }, () => runWorker()))
  return results
}

async function prepareAdminPayload(data) {
  if (!data || !Array.isArray(data.images) || !hasPendingImageUploads(data.images)) {
    if (!data || !Array.isArray(data?.images)) {
      return data
    }

    return {
      ...data,
      images: data.images.map((image, index) => stripTransientImageFields(image, index)),
    }
  }

  const signature = await fetchCloudinarySignature()
  const images = await mapWithConcurrency(data.images, CLOUDINARY_CONCURRENCY, async (image, index) => {
    if (!(image?.file instanceof File)) {
      return stripTransientImageFields(image, index)
    }

    const uploadResult = await uploadImageToCloudinary(image, signature)
    return normalizeCloudinaryUpload(uploadResult, image, index)
  })

  return {
    ...data,
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

export async function fetchAdminStats() {
  if (USE_MOCK) {
    const list = getMockList()
    return {
      total: list.length,
      active: list.filter(p => p.status === 'available').length,
      sold: list.filter(p => p.status === 'sold').length,
      reserved: list.filter(p => p.status === 'reserved').length,
    }
  }

  assertApiConfigured()
  const res = await fetch(buildUrl('/admin/properties', { stats: 1 }), {
    headers: getAuthHeader(),
  })
  if (!res.ok) throw new Error('Error al cargar estadísticas')
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
  const payload = isFormDataPayload(data) ? data : await prepareAdminPayload(data)
  const headers = isFormDataPayload(payload)
    ? { ...getAuthHeader() }
    : { 'Content-Type': 'application/json', ...getAuthHeader() }
  const res = await fetch(buildUrl('/admin/properties'), {
    method: 'POST',
    headers,
    body: isFormDataPayload(payload) ? payload : JSON.stringify(payload),
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
  const payload = isFormDataPayload(data) ? data : await prepareAdminPayload(data)
  const headers = isFormDataPayload(payload)
    ? { ...getAuthHeader() }
    : { 'Content-Type': 'application/json', ...getAuthHeader() }
  const res = await fetch(buildUrl(`/admin/properties/${id}`), {
    method: 'PUT',
    headers,
    body: isFormDataPayload(payload) ? payload : JSON.stringify(payload),
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
