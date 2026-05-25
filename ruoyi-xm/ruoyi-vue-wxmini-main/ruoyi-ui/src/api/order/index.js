import request from '@/utils/request'

// 订单列表
export function getOrderList(query) {
  return request({
    url: '/mall/order/list',
    method: 'get',
    params: query
  })
}

// 订单详情
export function getOrderDetail(id) {
  return request({
    url: '/mall/order/' + id,
    method: 'get'
  })
}

// 修改订单
export function updateOrder(data) {
  return request({
    url: '/mall/order',
    method: 'put',
    data: data
  })
}

// 售后列表
export function getAfterSaleList(query) {
  return request({
    url: '/mall/after-sale/list',
    method: 'get',
    params: query
  })
}

// 售后详情
export function getAfterSaleDetail(id) {
  return request({
    url: '/mall/after-sale/' + id,
    method: 'get'
  })
}

// 处理售后
export function handleAfterSale(id, status, data) {
  return request({
    url: '/mall/after-sale/handle/' + id + '/' + status,
    method: 'post',
    data: data
  })
}

// 异常订单列表
export function getAbnormalOrderList(query) {
  return request({
    url: '/mall/order/list',
    method: 'get',
    params: query
  })
}
