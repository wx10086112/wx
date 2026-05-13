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

// 商品列表
export function getProductList(query) {
  return request({
    url: '/mall/product/list',
    method: 'get',
    params: query
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

// 商家流水
export function getMerchantFlowList(query) {
  return request({
    url: '/mall/merchant/flow/list',
    method: 'get',
    params: query
  })
}
