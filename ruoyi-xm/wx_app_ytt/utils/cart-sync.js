const productApi = require('../api/product')
const cartStore = require('./cart')

const extractPayload = (res = {}) => res.data || res || {}

const refreshCart = () => {
  const cart = cartStore.getCart()
  if (!cart.items.length) return Promise.resolve(cart)

  return Promise.all(cart.items.map((item) => {
    const entryId = item.entryId || item.productId
    if (!entryId) return Promise.resolve(null)
    return productApi.getGrouponDetail(entryId)
      .then(extractPayload)
      .catch(() => null)
  })).then((latestProducts) => {
    const items = cart.items.map((item, index) => {
      const latest = latestProducts[index]
      if (!latest || !latest.id) return item
      return {
        ...item,
        ...latest,
        entryId: latest.id || latest.goodsId || item.entryId,
        productId: latest.productId || item.productId,
        quantity: item.quantity,
        merchantId: latest.merchantId || item.merchantId,
        merchantName: latest.merchantName || item.merchantName
      }
    })
    return cartStore.saveCart({ ...cart, items })
  })
}

module.exports = {
  refreshCart
}
