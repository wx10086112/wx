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

