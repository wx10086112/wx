import { mockSuccess } from '@/mock'
import { merchantRankList, salesStats, orderStats } from '@/mock/data'

export function getMerchantRankList() {
  return mockSuccess(merchantRankList)
}

export function getSalesStats() {
  return mockSuccess(salesStats)
}

export function getOrderStats() {
  return mockSuccess(orderStats)
}
