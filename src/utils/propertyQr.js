import QRCode from 'qrcode'

// Los QR se imprimen, así que siempre apuntan al sitio publicado aunque se generen desde otro lado (por ejemplo localhost).
const PUBLIC_SITE_URL = (import.meta.env.VITE_PUBLIC_SITE_URL || 'https://estefanpropiedades.com.ar').replace(/\/+$/, '')

const QR_DARK_COLOR = '#022c22'
const CANVAS_WIDTH = 1000
const PADDING = 60
const QR_SIZE = CANVAS_WIDTH - PADDING * 2
const FONT_FAMILY = 'Inter, Arial, sans-serif'

export function getPropertyPublicUrl(slug) {
  return `${PUBLIC_SITE_URL}/propiedad/${encodeURIComponent(slug)}`
}

function wrapText(ctx, text, maxWidth, maxLines) {
  const words = String(text || '').split(/\s+/).filter(Boolean)
  const lines = []
  let current = ''

  for (const word of words) {
    const candidate = current ? `${current} ${word}` : word
    if (ctx.measureText(candidate).width <= maxWidth || !current) {
      current = candidate
    } else {
      lines.push(current)
      current = word
    }
  }
  if (current) lines.push(current)

  if (lines.length > maxLines) {
    const kept = lines.slice(0, maxLines)
    let last = kept[maxLines - 1]
    while (last && ctx.measureText(`${last}…`).width > maxWidth) {
      last = last.slice(0, -1)
    }
    kept[maxLines - 1] = `${last.trimEnd()}…`
    return kept
  }
  return lines
}

// Imagen lista para imprimir: el QR y debajo el título, el código de referencia y la dirección.
export async function createPropertyQrCanvas({ slug, title, referenceCode }) {
  const url = getPropertyPublicUrl(slug)
  const qrCanvas = document.createElement('canvas')
  await QRCode.toCanvas(qrCanvas, url, {
    width: QR_SIZE,
    margin: 0,
    errorCorrectionLevel: 'M',
    color: { dark: QR_DARK_COLOR, light: '#ffffff' },
  })

  if (document.fonts?.ready) {
    await document.fonts.ready
  }

  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  const textWidth = CANVAS_WIDTH - PADDING * 2

  ctx.font = `700 44px ${FONT_FAMILY}`
  const titleLines = wrapText(ctx, title, textWidth, 2)
  const textHeight = titleLines.length * 56 + (referenceCode ? 56 : 0) + 44

  canvas.width = CANVAS_WIDTH
  canvas.height = PADDING + QR_SIZE + 48 + textHeight + PADDING

  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.drawImage(qrCanvas, PADDING, PADDING, QR_SIZE, QR_SIZE)

  ctx.textAlign = 'center'
  ctx.textBaseline = 'top'
  let y = PADDING + QR_SIZE + 48

  ctx.fillStyle = '#0f172a'
  ctx.font = `700 44px ${FONT_FAMILY}`
  for (const line of titleLines) {
    ctx.fillText(line, CANVAS_WIDTH / 2, y)
    y += 56
  }

  if (referenceCode) {
    ctx.fillStyle = '#065f46'
    ctx.font = `600 36px ${FONT_FAMILY}`
    ctx.fillText(`Código: ${referenceCode}`, CANVAS_WIDTH / 2, y)
    y += 56
  }

  ctx.fillStyle = '#64748b'
  ctx.font = `400 24px ${FONT_FAMILY}`
  ctx.fillText(url.replace(/^https?:\/\//, ''), CANVAS_WIDTH / 2, y)

  return canvas
}

export function getPropertyQrFileName({ slug, referenceCode }) {
  const base = String(referenceCode || slug || 'propiedad')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^a-zA-Z0-9_-]+/g, '-')
    .replace(/^-+|-+$/g, '')
  return `qr-${base || 'propiedad'}.png`
}

export async function downloadPropertyQr(property) {
  const canvas = await createPropertyQrCanvas(property)
  const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/png'))
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = getPropertyQrFileName(property)
  document.body.appendChild(link)
  link.click()
  link.remove()
  setTimeout(() => URL.revokeObjectURL(link.href), 1000)
}
