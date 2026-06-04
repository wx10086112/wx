import request from '@/utils/request'

// 平台流水列表
export function getPlatformFlowList(query) {
  return request({
    url: '/mall/finance/platform-flow/list',
    method: 'get',
    params: query
  })
}

// 分账列表
export function getProfitShareList(query) {
  return request({
    url: '/mall/finance/profit-share/list',
    method: 'get',
    params: query
  })
}

// 收益统计
export function getIncomeStats() {
  return request({
    url: '/mall/finance/income/stats',
    method: 'get'
  })
}

// 财务报表
export function getFinanceReport() {
  return request({
    url: '/mall/finance/report',
    method: 'get'
  })
}
