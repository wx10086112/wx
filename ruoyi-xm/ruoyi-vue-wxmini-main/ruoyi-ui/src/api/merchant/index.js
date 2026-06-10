import request from '@/utils/request'

// 商家列表
export function getMerchantList(query) {
  return request({
    url: '/mall/merchant/list',
    method: 'get',
    params: query
  })
}

// 商家详情
export function getMerchantDetail(id) {
  return request({
    url: '/mall/merchant/' + id,
    method: 'get'
  })
}

export function getMerchantEntryQrCode(id) {
  return request({
    url: '/mall/merchant/entry-qrcode/' + id,
    method: 'get'
  })
}

// 新增商家
export function addMerchant(data) {
  return request({
    url: '/mall/merchant',
    method: 'post',
    data: data
  })
}

// 修改商家
export function updateMerchant(data) {
  return request({
    url: '/mall/merchant',
    method: 'put',
    data: data
  })
}

// 删除商家
export function deleteMerchant(ids) {
  return request({
    url: '/mall/merchant/' + ids,
    method: 'delete'
  })
}

// 停止合作
export function stopMerchant(id) {
  return request({
    url: '/mall/merchant/stop/' + id,
    method: 'put'
  })
}

// 恢复合作
export function resumeMerchant(id) {
  return request({
    url: '/mall/merchant/resume/' + id,
    method: 'put'
  })
}

// 商品列表
export function getProductList(query) {
  return request({
    url: '/mall/product/list',
    method: 'get',
    params: query
  })
}

// 商品分类列表
export function getProductCategoryList(query) {
  return request({
    url: '/mall/product/category/list',
    method: 'get',
    params: query
  })
}

// 新增商品分类
export function addProductCategory(data) {
  return request({
    url: '/mall/product/category',
    method: 'post',
    data: data
  })
}

// 新增商品
export function addProduct(data) {
  return request({
    url: '/mall/product',
    method: 'post',
    data: data
  })
}

// 修改商品
export function updateProduct(data) {
  return request({
    url: '/mall/product',
    method: 'put',
    data: data
  })
}

// 删除商品
export function deleteProduct(ids) {
  return request({
    url: '/mall/product/' + ids,
    method: 'delete'
  })
}

// 商家审核列表
export function getAuditList(query) {
  return request({
    url: '/mall/merchant/audit/list',
    method: 'get',
    params: query
  })
}

// 商家审核操作
export function auditMerchant(id, status, data) {
  return request({
    url: '/mall/merchant/audit/' + id + '/' + status,
    method: 'put',
    data: data
  })
}

// 商家流水
export function getMerchantFlowList(query) {
  return request({
    url: '/mall/merchant/flow/list',
    method: 'get',
    params: query
  })
}

// 商家订单列表
export function getMerchantOrders(merchantId, query) {
  return request({
    url: '/mall/order/list',
    method: 'get',
    params: { ...query, merchantId: merchantId }
  })
}

// 订单状态映射
export const orderStatusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'primary' },
  2: { text: '已使用', type: '' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已退款', type: 'danger' },
  5: { text: '已取消', type: 'info' }
}

// ==================== 商家用户管理 ====================

// 商家用户列表
export function getMerchantUserList(query) {
  return request({
    url: '/mall/merchant-user/list',
    method: 'get',
    params: query
  })
}

// 商家用户详情
export function getMerchantUser(id) {
  return request({
    url: '/mall/merchant-user/' + id,
    method: 'get'
  })
}

// 新增商家用户
export function addMerchantUser(data) {
  return request({
    url: '/mall/merchant-user',
    method: 'post',
    data: data
  })
}

// 修改商家用户
export function updateMerchantUser(data) {
  return request({
    url: '/mall/merchant-user',
    method: 'put',
    data: data
  })
}

// 删除商家用户
export function deleteMerchantUser(ids) {
  return request({
    url: '/mall/merchant-user/' + ids,
    method: 'delete'
  })
}

// 重置密码
export function resetMerchantUserPwd(id, password) {
  return request({
    url: '/mall/merchant-user/resetPwd/' + id,
    method: 'put',
    data: { password }
  })
}

// 切换状态
export function changeMerchantUserStatus(id, status) {
  return request({
    url: '/mall/merchant-user/changeStatus',
    method: 'put',
    data: { id, status }
  })
}
