import request from '@/utils/request'

// ========== 团购活动 ==========

export function listGroupon(query) {
  return request({
    url: '/mall/groupon/list',
    method: 'get',
    params: query
  })
}

export function getGroupon(id) {
  return request({
    url: '/mall/groupon/' + id,
    method: 'get'
  })
}

export function addGroupon(data) {
  return request({
    url: '/mall/groupon',
    method: 'post',
    data
  })
}

export function updateGroupon(data) {
  return request({
    url: '/mall/groupon',
    method: 'put',
    data
  })
}

export function deleteGroupon(ids) {
  return request({
    url: '/mall/groupon/' + ids,
    method: 'delete'
  })
}

export function changeGrouponStatus(data) {
  return request({
    url: '/mall/groupon/status',
    method: 'put',
    data
  })
}

export function listGrouponOptions(merchantId) {
  return request({
    url: '/mall/groupon/options',
    method: 'get',
    params: { merchantId }
  })
}

export function uploadGrouponImage(data) {
  return request({
    url: '/mall/groupon/image/upload',
    method: 'post',
    data
  })
}

export function listGrouponProducts(grouponId) {
  return request({
    url: '/mall/groupon/product/list',
    method: 'get',
    params: { grouponId }
  })
}

export function bindGrouponProducts(data) {
  return request({
    url: '/mall/groupon/product/bind',
    method: 'post',
    data
  })
}

export function unbindGrouponProducts(data) {
  return request({
    url: '/mall/groupon/product/unbind',
    method: 'post',
    data
  })
}

export function listProduct(query) {
  return request({
    url: '/mall/product/list',
    method: 'get',
    params: query
  })
}

// ========== 团购商品明细 ==========

export function listGrouponItem(grouponId) {
  return request({
    url: '/mall/groupon/item/listByGroupon',
    method: 'get',
    params: { grouponId }
  })
}

export function listGrouponItemPage(query) {
  return request({
    url: '/mall/groupon/item/list',
    method: 'get',
    params: query
  })
}

export function getGrouponItem(id) {
  return request({
    url: '/mall/groupon/item/' + id,
    method: 'get'
  })
}

export function addGrouponItem(data) {
  return request({
    url: '/mall/groupon/item',
    method: 'post',
    data
  })
}

export function updateGrouponItem(data) {
  return request({
    url: '/mall/groupon/item',
    method: 'put',
    data
  })
}

export function deleteGrouponItem(ids) {
  return request({
    url: '/mall/groupon/item/' + ids,
    method: 'delete'
  })
}

export function changeGrouponItemStatus(data) {
  return request({
    url: '/mall/groupon/item/status',
    method: 'put',
    data
  })
}

export function uploadGrouponItemImage(data) {
  return request({
    url: '/mall/groupon/item/image/upload',
    method: 'post',
    data
  })
}

// ========== 其他 ==========

export function listMerchantSimple() {
  return request({
    url: '/mall/merchant/list',
    method: 'get'
  })
}
