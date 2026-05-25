import request from '@/utils/request'

// 首页统计数据
export function getDashboardStats() {
  return request({
    url: '/mall/dashboard/stats',
    method: 'get'
  })
}

// 趋势数据
export function getTrendData() {
  return request({
    url: '/mall/dashboard/trend',
    method: 'get'
  })
}

// 订单状态分布
export function getOrderStatusData() {
  return request({
    url: '/mall/dashboard/order-status',
    method: 'get'
  })
}

// 热销商品
export function getHotProducts() {
  return request({
    url: '/mall/dashboard/hot-products',
    method: 'get'
  })
}

// 商家排行
export function getMerchantRank() {
  return request({
    url: '/mall/dashboard/merchant-rank',
    method: 'get'
  })
}

// 用户列表
export function getUserList(query) {
  return request({
    url: '/mall/user/list',
    method: 'get',
    params: query
  })
}
