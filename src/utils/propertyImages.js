import { API_BASE_URL } from '../config/api'

const FALLBACK_URL = 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=1200'
const API_ORIGIN = API_BASE_URL ? API_BASE_URL.replace(/\/api$/, '') : ''

const VARIANT_URL_FIELDS = {
  thumbnail: 'thumbnailUrl',
  medium: 'mediumUrl',
  large: 'largeUrl',
}

const VARIANT_WIDTH_FIELDS = {
  thumbnail: 'thumbnailWidth',
  medium: 'mediumWidth',
  large: 'largeWidth',
}

function toNumber(value) {
  const numeric = Number(value)
  return Number.isFinite(numeric) && numeric > 0 ? numeric : null
}

function resolveImageReference(url) {
  if (!url) return null
  if (/^(data:|blob:|https?:)/i.test(url)) return url
  if (url.startsWith('//')) return url
  if (url.startsWith('/')) return API_ORIGIN ? `${API_ORIGIN}${url}` : url
  return url
}

export function createFallbackImage() {
  return {
    url: FALLBACK_URL,
    thumbnailUrl: FALLBACK_URL,
    mediumUrl: FALLBACK_URL,
    largeUrl: FALLBACK_URL,
    width: 1200,
    height: 800,
    thumbnailWidth: 480,
    mediumWidth: 960,
    largeWidth: 1200,
    isPrimary: true,
    order: 0,
  }
}

export function sortPropertyImages(images = []) {
  const list = Array.isArray(images)
    ? [...images].filter(Boolean).sort((a, b) => (a.order ?? 0) - (b.order ?? 0))
    : []

  return list.length ? list : [createFallbackImage()]
}

export function getPropertyPrimaryImage(property) {
  const images = sortPropertyImages(property?.images || [])
  return images.find(image => image?.isPrimary) || images[0] || createFallbackImage()
}

export function getImageUrl(image, variant = 'large') {
  if (!image) return FALLBACK_URL

  if (variant === 'thumbnail') {
    return resolveImageReference(image.thumbnailUrl || image.mediumUrl || image.largeUrl || image.url) || FALLBACK_URL
  }

  if (variant === 'medium') {
    return resolveImageReference(image.mediumUrl || image.largeUrl || image.url || image.thumbnailUrl) || FALLBACK_URL
  }

  return resolveImageReference(image.largeUrl || image.url || image.mediumUrl || image.thumbnailUrl) || FALLBACK_URL
}

export function getImagePlaceholder(image) {
  return resolveImageReference(image?.placeholderUrl) || null
}

export function getImageDimensions(image) {
  return {
    width: toNumber(image?.width),
    height: toNumber(image?.height),
  }
}

export function buildImageSrcSet(image, variants = ['thumbnail', 'medium', 'large']) {
  if (!image) return ''

  const sources = []
  const usedUrls = new Set()

  variants.forEach((variant) => {
    const urlField = VARIANT_URL_FIELDS[variant]
    const widthField = VARIANT_WIDTH_FIELDS[variant]
    const url = urlField ? resolveImageReference(image[urlField]) : null
    const width = widthField ? toNumber(image[widthField]) : null

    if (url && width && !usedUrls.has(url)) {
      usedUrls.add(url)
      sources.push(`${url} ${width}w`)
    }
  })

  const fallbackUrl = resolveImageReference(image.largeUrl || image.url)
  const fallbackWidth = toNumber(image.largeWidth) || toNumber(image.width)
  if (fallbackUrl && fallbackWidth && !usedUrls.has(fallbackUrl)) {
    sources.push(`${fallbackUrl} ${fallbackWidth}w`)
  }

  return sources.join(', ')
}
