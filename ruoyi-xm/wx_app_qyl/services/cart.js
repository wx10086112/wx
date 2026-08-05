const CART_STORAGE_KEY = 'o2o_cart_list'

const clone = (data) => JSON.parse(JSON.stringify(data))

const getCartList = () => {
  try {
    return clone(wx.getStorageSync(CART_STORAGE_KEY) || [])
  } catch (e) {
    return []
  }
}

const setCartList = (list) => {
  wx.setStorageSync(CART_STORAGE_KEY, clone(list))
}

const getCartCount = () => {
  const list = getCartList()
  return list.reduce((sum, item) => sum + item.quantity, 0)
}

const addToCart = (product, quantity = 1) => {
  const list = getCartList()
  const idx = list.findIndex((item) => item.productId === product.id)
  if (idx > -1) {
    list[idx].quantity += quantity
    list[idx].updateTime = Date.now()
  } else {
    list.push({
      cartItemId: Date.now(),
      productId: product.id,
      goodsId: product.goodsId || product.id,
      title: product.title,
      subtitle: product.subtitle || '',
      image: product.image || '',
      price: product.price,
      originalPrice: product.originalPrice || product.price,
      merchantId: product.merchantId,
      merchantName: product.merchantName || '',
      stock: product.stock || 999,
      quantity,
      selected: true,
      createTime: Date.now(),
      updateTime: Date.now()
    })
  }
  setCartList(list)
  return list
}

const updateQuantity = (productId, quantity) => {
  const list = getCartList()
  const idx = list.findIndex((item) => item.productId === productId)
  if (idx === -1) return list
  if (quantity <= 0) {
    list.splice(idx, 1)
  } else {
    list[idx].quantity = Math.min(quantity, list[idx].stock || 999)
    list[idx].updateTime = Date.now()
  }
  setCartList(list)
  return list
}

const removeFromCart = (productId) => {
  const list = getCartList().filter((item) => item.productId !== productId)
  setCartList(list)
  return list
}

const toggleSelect = (productId) => {
  const list = getCartList()
  const item = list.find((item) => item.productId === productId)
  if (item) {
    item.selected = !item.selected
    item.updateTime = Date.now()
  }
  setCartList(list)
  return list
}

const selectAll = (selected) => {
  const list = getCartList().map((item) => ({ ...item, selected, updateTime: Date.now() }))
  setCartList(list)
  return list
}

const removeSelected = () => {
  const list = getCartList().filter((item) => !item.selected)
  setCartList(list)
  return list
}

const getSelectedItems = () => {
  return getCartList().filter((item) => item.selected)
}

const clearCart = () => {
  setCartList([])
}

module.exports = {
  getCartList,
  getCartCount,
  addToCart,
  updateQuantity,
  removeFromCart,
  toggleSelect,
  selectAll,
  removeSelected,
  getSelectedItems,
  clearCart
}
