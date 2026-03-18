import { API_BASE_URL, ASSETS_BASE_URL } from '../config/api'

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
  if (/^(data:|blob:|https?:)/i.test(url)) return addCloudinaryAutoParams(url)
  if (url.startsWith('//')) return addCloudinaryAutoParams(`https:${url}`)
  if (url.startsWith('/')) {
    if (ASSETS_BASE_URL) return `${ASSETS_BASE_URL}${url}`
    return addCloudinaryAutoParams(API_ORIGIN ? `${API_ORIGIN}${url}` : url)
  }
  // ruta relativa (ej. images/branding/foo.jpg)
  if (ASSETS_BASE_URL) return addCloudinaryAutoParams(`${ASSETS_BASE_URL}/${url}`)
  return addCloudinaryAutoParams(url)
}

function addCloudinaryAutoParams(urlString) {
  try {
    const url = new URL(urlString)
    if (!isCloudinaryHost(url.hostname)) return urlString

    const segments = url.pathname.split('/').filter(Boolean)
    const uploadIdx = segments.indexOf('upload')
    if (uploadIdx === -1) return urlString

    const afterUpload = segments[uploadIdx + 1]
    const isVersion = afterUpload && /^v\d+/.test(afterUpload)
    const hasTransformSegment = afterUpload && !isVersion

    const transforms = new Set(
      hasTransformSegment ? afterUpload.split(',').filter(Boolean) : []
    )

    const hasFormat = Array.from(transforms).some(t => t.startsWith('f_'))
    const hasQuality = Array.from(transforms).some(t => t.startsWith('q_'))

    if (!hasFormat) transforms.add('f_auto')
    if (!hasQuality) transforms.add('q_auto')

    const transformSegment = Array.from(transforms).join(',')

    const newSegments = [...segments]
    if (hasTransformSegment) {
      newSegments[uploadIdx + 1] = transformSegment
    } else {
      newSegments.splice(uploadIdx + 1, 0, transformSegment)
    }

    url.pathname = '/' + newSegments.join('/')
    return url.toString()
  } catch {
    return urlString
  }
}

function isCloudinaryHost(hostname) {
  return (
    hostname === 'res.cloudinary.com' ||
    hostname.endsWith('.res.cloudinary.com') ||
    hostname.endsWith('.cloudinary.com')
  )
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
