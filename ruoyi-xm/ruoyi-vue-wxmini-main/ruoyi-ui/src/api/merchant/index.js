import { mockSuccess, mockPage } from '@/mock'
import { merchantList, merchantLevels, merchantFlowList, withdrawList, productList, auditList, getNextProductId, orderList, orderStatusMap } from '@/mock/data'

export function getMerchantList(query) {
  return mockPage(merchantList, query.pageNum, query.pageSize)
}

export function getMerchantDetail(id) {
  const merchant = merchantList.find(m => m.id === id)
  return mockSuccess(merchant || merchantList[0])
}

export function getMerchantLevels() {
  return mockSuccess(merchantLevels)
}

export function getMerchantFlowList(query) {
  let list = merchantFlowList
  if (query.merchantId) {
    list = list.filter(f => f.merchantId === query.merchantId)
  }
  return mockPage(list, query.pageNum, query.pageSize)
}

export function getWithdrawList(query) {
  return mockPage(withdrawList, query.pageNum, query.pageSize)
}

export function auditMerchant(id, status) {
  return mockSuccess(null, status === 1 ? '审核通过' : '审核拒绝')
}

let nextMerchantId = merchantList.length + 1

export function addMerchant(data) {
  data.id = nextMerchantId++
  data.status = 2 // 待审核
  data.products = 0
  data.monthlySales = 0
  data.createTime = new Date().toISOString().slice(0, 10)
  merchantList.push(data)
  return mockSuccess(null, '添加成功')
}

// ========== 商品管理 ==========

export function getProductList(query) {
  let list = productList
  if (query.merchantId) {
    list = list.filter(p => p.merchantId === query.merchantId)
  }
  if (query.name) {
    list = list.filter(p => p.name.includes(query.name))
  }
  if (query.status !== undefined && query.status !== '' && query.status !== null) {
    list = list.filter(p => p.status === Number(query.status))
  }
  return mockPage(list, query.pageNum, query.pageSize)
}

export function addProduct(data) {
  data.id = getNextProductId()
  data.createTime = new Date().toISOString().slice(0, 10)
  data.sales = 0
  productList.push(data)
  return mockSuccess(null, '新增成功')
}

export function updateProduct(data) {
  const idx = productList.findIndex(p => p.id === data.id)
  if (idx > -1) {
    Object.assign(productList[idx], data)
  }
  return mockSuccess(null, '修改成功')
}

export function deleteProduct(id) {
  const idx = productList.findIndex(p => p.id === id)
  if (idx > -1) {
    productList.splice(idx, 1)
  }
  return mockSuccess(null, '删除成功')
}

// ========== 入驻审核 ==========

export function getAuditList(query) {
  let list = auditList
  if (query.name) {
    list = list.filter(a => a.name.includes(query.name))
  }
  if (query.status !== undefined && query.status !== '' && query.status !== null) {
    list = list.filter(a => a.status === Number(query.status))
  }
  return mockPage(list, query.pageNum, query.pageSize)
}

// ========== 商家订单 ==========

export function getMerchantOrders(merchantId, query) {
  let list = orderList.filter(o => o.merchantId === merchantId)
  return mockPage(list, query.pageNum, query.pageSize)
}

export { orderStatusMap }
