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

// 商家排行
export function getMerchantRankList() {
  return request({
    url: '/mall/dashboard/merchant-rank',
    method: 'get'
  })
}

// 销售统计
export function getSalesStats() {
  return request({
    url: '/mall/dashboard/sales-stats',
    method: 'get'
  })
}

// 订单统计
export function getOrderStats() {
  return request({
    url: '/mall/dashboard/order-stats',
    method: 'get'
  })
}
