const PROD_BASE_URL = 'https://ld-console.lingdian.site/prod-api'
const BASE_URL_STORAGE_KEY = 'baseUrl'

const legacyImageMap = {
  'https://img.zcool.cn/community/01e07155431210000019ae9dd17df.jpg': '/assets/images/merchant-spa.png',
  'https://img.zcool.cn/community/01786555431210000019ae9d4c90b.jpg': '/assets/images/merchant-neck.png',
  'https://img.zcool.cn/community/01d8a155431210000019ae9d9c2d9.jpg': '/assets/images/merchant-fitness.png',
  'https://img.zcool.cn/community/01686555431210000019ae9d8c7f0.jpg': '/assets/images/merchant-meal.png',
  'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9V62zibQ/132': '/assets/images/avatar.svg'
}

const getAppInstance = () => {
  try {
    return getApp()
  } catch (e) {
    return null
  }
}

const getDefaultBaseUrl = () => {
  return PROD_BASE_URL
}

const normalizeBaseUrl = (baseUrl = getDefaultBaseUrl()) => {
  const normalized = String(baseUrl || getDefaultBaseUrl()).trim().replace(/\/+$/, '')
  const unsafe = !/^https:\/\//i.test(normalized) ||
    /^https?:\/\/(localhost|127\.|0\.0\.0\.0|10\.|192\.168\.|172\.(1[6-9]|2\d|3[0-1])\.)/i.test(normalized) ||
    /(example|invalid|placeholder|xxx)/i.test(normalized)
  return unsafe ? getDefaultBaseUrl() : normalized
}

const getRuntimeBaseUrl = () => {
  const app = getAppInstance()
  const runtimeBaseUrl = (app && app.baseUrl) || wx.getStorageSync(BASE_URL_STORAGE_KEY) || getDefaultBaseUrl()
  return normalizeBaseUrl(runtimeBaseUrl)
}

const isAbsoluteUrl = (url = '') => /^(https?:)?\/\//i.test(url)
const isRuntimeFileUrl = (url = '') => /^(wxfile|cloud|file):\/\//i.test(url)
const isInlineUrl = (url = '') => /^(data|blob):/i.test(url)
const isLocalAssetUrl = (url = '') => url.startsWith('/assets/') || url.startsWith('assets/')
const isServerImageUrl = (url = '') => url.startsWith('/profile/') || url.startsWith('profile/')
const isMerchantImageUrl = (url = '') => /(^|\/)profile\/(merchant_images|merchant-goods)\//i.test(url)

const normalizeImageUrl = (url = '') => {
  if (typeof url !== 'string') {
    return url
  }

  const trimmed = url.trim()
  if (!trimmed) {
    return ''
  }

  if (legacyImageMap[trimmed]) {
    return legacyImageMap[trimmed]
  }
  if (isAbsoluteUrl(trimmed) || isRuntimeFileUrl(trimmed) || isInlineUrl(trimmed)) {
    return trimmed
  }
  if (isLocalAssetUrl(trimmed)) {
    return trimmed.startsWith('/') ? trimmed : `/${trimmed}`
  }
  if (isServerImageUrl(trimmed)) {
    const relativePath = trimmed.replace(/^\/+/, '')
    return `${getRuntimeBaseUrl()}/${relativePath}`
  }
  return trimmed
}

const toStorageImageUrl = (url = '') => {
  if (typeof url !== 'string') {
    return url
  }
  const trimmed = url.trim()
  const baseUrl = getRuntimeBaseUrl()
  if (trimmed.startsWith(`${baseUrl}/profile/`)) {
    return trimmed.substring(baseUrl.length)
  }
  return trimmed
}

const toListThumbnailUrl = (url = '') => {
  const normalized = normalizeImageUrl(url)
  if (typeof normalized !== 'string' || !normalized || !isMerchantImageUrl(normalized) || /[?&]thumb=/.test(normalized)) {
    return normalized
  }
  return `${normalized}${normalized.includes('?') ? '&' : '?'}thumb=list`
}

const toDetailThumbnailUrl = (url = '') => {
  const normalized = normalizeImageUrl(url)
  if (typeof normalized !== 'string' || !normalized || !isMerchantImageUrl(normalized) || /[?&]thumb=/.test(normalized)) {
    return normalized
  }
  return `${normalized}${normalized.includes('?') ? '&' : '?'}thumb=detail`
}

const normalizeJsonImageArray = (value = '') => {
  const trimmed = value.trim()
  if (!trimmed.startsWith('[')) {
    return value
  }
  try {
    const parsed = JSON.parse(trimmed)
    if (!Array.isArray(parsed)) {
      return value
    }
    return JSON.stringify(parsed.map((item) => (typeof item === 'string' ? normalizeImageUrl(item) : normalizeImageFields(item))))
  } catch (e) {
    return value
  }
}

const normalizeImageFields = (data) => {
  if (Array.isArray(data)) {
    return data.map((item) => normalizeImageFields(item))
  }
  if (typeof data === 'string') {
    return normalizeImageUrl(data)
  }
  if (!data || typeof data !== 'object') {
    return data
  }

  const normalized = { ...data }
  Object.keys(normalized).forEach((key) => {
    const value = normalized[key]
    if (typeof value === 'string' && ['detailImages', 'images', 'imageList'].includes(key)) {
      normalized[key] = normalizeJsonImageArray(value)
      return
    }
    normalized[key] = normalizeImageFields(value)
  })
  return normalized
}

module.exports = {
  normalizeImageUrl,
  normalizeImageFields,
  toStorageImageUrl,
  toListThumbnailUrl,
  toDetailThumbnailUrl
}
