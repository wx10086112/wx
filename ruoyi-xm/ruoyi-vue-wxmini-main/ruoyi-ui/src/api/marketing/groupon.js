import request from '@/utils/request'

// ========== 团购活动 ==========

// 团购活动列表
export function listGroupon(query) {
  return request({
    url: '/mall/groupon/list',
    method: 'get',
    params: query
  })
}

// 团购活动详情
export function getGroupon(id) {
  return request({
    url: '/mall/groupon/' + id,
    method: 'get'
  })
}

// 新增团购活动
export function addGroupon(data) {
  return request({
    url: '/mall/groupon',
    method: 'post',
    data: data
  })
}

// 修改团购活动
export function updateGroupon(data) {
  return request({
    url: '/mall/groupon',
    method: 'put',
    data: data
  })
}

// 删除团购活动
export function deleteGroupon(ids) {
  return request({
    url: '/mall/groupon/' + ids,
    method: 'delete'
  })
}

// 修改活动状态
export function changeGrouponStatus(data) {
  return request({
    url: '/mall/groupon/status',
    method: 'put',
    data: data
  })
}

// 团购活动下拉选项
export function listGrouponOptions(merchantId) {
  return request({
    url: '/mall/groupon/options',
    method: 'get',
    params: { merchantId }
  })
}

// 团购活动图片上传
export function uploadGrouponImage(data) {
  return request({
    url: '/mall/groupon/image/upload',
    method: 'post',
    data: data
  })
}

// 获取团购活动下已绑定的商品
export function listGrouponProducts(grouponId) {
  return request({
    url: '/mall/groupon/product/list',
    method: 'get',
    params: { grouponId }
  })
}

// 绑定商品到团购活动
export function bindGrouponProducts(data) {
  return request({
    url: '/mall/groupon/product/bind',
    method: 'post',
    data: data
  })
}

// 解绑团购活动商品
export function unbindGrouponProducts(data) {
  return request({
    url: '/mall/groupon/product/unbind',
    method: 'post',
    data: data
  })
}

// 商品列表（用于绑定团购活动）
export function listProduct(query) {
  return request({
    url: '/mall/product/list',
    method: 'get',
    params: query
  })
}

// ========== 团购商品明细 ==========

// 团购商品列表（按活动ID查询）
export function listGrouponItem(grouponId) {
  return request({
    url: '/mall/groupon/item/listByGroupon',
    method: 'get',
    params: { grouponId }
  })
}

// 团购商品列表（分页查询）
export function listGrouponItemPage(query) {
  return request({
    url: '/mall/groupon/item/list',
    method: 'get',
    params: query
  })
}

// 团购商品详情
export function getGrouponItem(id) {
  return request({
    url: '/mall/groupon/item/' + id,
    method: 'get'
  })
}

// 新增团购商品
export function addGrouponItem(data) {
  return request({
    url: '/mall/groupon/item',
    method: 'post',
    data: data
  })
}

// 修改团购商品
export function updateGrouponItem(data) {
  return request({
    url: '/mall/groupon/item',
    method: 'put',
    data: data
  })
}

// 删除团购商品
export function deleteGrouponItem(ids) {
  return request({
    url: '/mall/groupon/item/' + ids,
    method: 'delete'
  })
}

// 修改团购商品状态
export function changeGrouponItemStatus(data) {
  return request({
    url: '/mall/groupon/item/status',
    method: 'put',
    data: data
  })
}

// 团购商品图片上传
export function uploadGrouponItemImage(data) {
  return request({
    url: '/mall/groupon/item/image/upload',
    method: 'post',
    data: data
  })
}

// ========== 其他 ==========

// 商家列表（用于筛选）
export function listMerchantSimple() {
  return request({
    url: '/mall/merchant/list',
    method: 'get'
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
    data: data
  })
}

export function unbindGrouponProducts(data) {
  return request({
    url: '/mall/groupon/product/unbind',
    method: 'post',
    data: data
  })
}

export function listProduct(query) {
  return request({
    url: '/mall/product/list',
    method: 'get',
    params: query
  })
}
