const { toListThumbnailUrl } = require('./image-url')

const CART_STORAGE_KEY = 'o2o_cart_state'
const DEFAULT_PRODUCT_IMAGE = '/assets/images/merchant-logo-xiangyuan.png'

const clone = (data) => JSON.parse(JSON.stringify(data))

const getCart = () => {
  try {
    const cart = wx.getStorageSync(CART_STORAGE_KEY)
    if (cart && Array.isArray(cart.items)) {
      return normalizeCart(cart)
    }
  } catch (e) {
    wx.removeStorageSync(CART_STORAGE_KEY)
  }
  return emptyCart()
}

const emptyCart = () => ({
  merchantId: null,
  merchantName: '',
  items: []
})

const saveCart = (cart) => {
  const normalized = normalizeCart(cart)
  wx.setStorageSync(CART_STORAGE_KEY, normalized)
  return normalized
}

const clearCart = () => {
  wx.removeStorageSync(CART_STORAGE_KEY)
}

const normalizeCart = (cart = {}) => {
  const items = Array.isArray(cart.items)
    ? cart.items
        .map(normalizeItem)
        .filter((item) => item.productId && item.quantity > 0)
    : []
  return {
    merchantId: cart.merchantId || (items[0] && items[0].merchantId) || null,
    merchantName: cart.merchantName || (items[0] && items[0].merchantName) || '',
    items
  }
}

const normalizeItem = (item = {}) => {
  const price = Number(item.price || 0)
  const quantity = Math.max(1, Number(item.quantity || 1))
  const entryId = item.entryId || item.id || item.goodsId || item.productId
  return {
    entryId,
    productId: item.productId || entryId,
    merchantId: item.merchantId || null,
    merchantName: item.merchantName || '',
    title: item.title || item.name || item.productName || '商品',
    image: toListThumbnailUrl(item.image || item.coverImage || item.mainImage || DEFAULT_PRODUCT_IMAGE),
    price,
    stock: Number(item.stock || 0),
    quantity,
    priceText: (price / 100).toFixed(2),
    subtotalText: ((price * quantity) / 100).toFixed(2)
  }
}

const addItem = (product = {}, quantity = 1, options = {}) => {
  const item = normalizeItem({
    ...product,
    merchantName: product.merchantName || options.merchantName || ''
  })
  item.quantity = Math.max(1, Number(quantity || 1))
  item.subtotalText = ((item.price * item.quantity) / 100).toFixed(2)
  if (!item.productId || !item.merchantId) {
    return { ok: false, message: '商品信息不完整' }
  }
  if (item.stock <= 0) {
    return { ok: false, message: '当前商品已售罄' }
  }

  const cart = getCart()
  if (cart.merchantId && cart.merchantId !== item.merchantId) {
    return {
      ok: false,
      conflict: true,
      message: '不同门店商品不能合并下单',
      currentMerchantName: cart.merchantName,
      nextItem: item
    }
  }

  const nextCart = clone(cart)
  nextCart.merchantId = item.merchantId
  nextCart.merchantName = item.merchantName || cart.merchantName
  const existing = nextCart.items.find((cartItem) => cartItem.productId === item.productId)
  if (existing) {
    const quantity = existing.quantity + item.quantity
    Object.assign(existing, item, {
      quantity: item.stock > 0 ? Math.min(quantity, item.stock) : quantity
    })
    existing.subtotalText = ((existing.price * existing.quantity) / 100).toFixed(2)
  } else {
    nextCart.items.push(item)
  }
  return { ok: true, cart: saveCart(nextCart) }
}

const replaceWithItem = (item) => {
  return saveCart({
    merchantId: item.merchantId,
    merchantName: item.merchantName || '',
    items: [normalizeItem(item)]
  })
}

const updateQuantity = (productId, quantity) => {
  const cart = getCart()
  const nextQuantity = Math.max(1, Number(quantity || 1))
  cart.items = cart.items.map((item) => {
    if (item.productId !== productId) return item
    const quantityLimit = item.stock > 0 ? Math.min(nextQuantity, item.stock) : nextQuantity
    return {
      ...item,
      quantity: quantityLimit,
      subtotalText: ((item.price * quantityLimit) / 100).toFixed(2)
    }
  })
  return saveCart(cart)
}

const removeItem = (productId) => {
  const cart = getCart()
  cart.items = cart.items.filter((item) => item.productId !== productId)
  if (!cart.items.length) {
    clearCart()
    return emptyCart()
  }
  return saveCart(cart)
}

const buildSummary = (cart = getCart()) => {
  const totalQuantity = cart.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
  const totalAmount = cart.items.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0)
  return {
    totalQuantity,
    totalAmount,
    totalAmountText: (totalAmount / 100).toFixed(2)
  }
}

module.exports = {
  getCart,
  saveCart,
  clearCart,
  addItem,
  replaceWithItem,
  updateQuantity,
  removeItem,
  buildSummary
}
