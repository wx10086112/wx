import { mockSuccess } from '@/mock'
import { dashboardStats, trendData, merchantRankList } from '@/mock/data'

export function getDashboardStats() {
  return mockSuccess(dashboardStats)
}

export function getTrendData() {
  return mockSuccess(trendData)
}

export function getMerchantRankList() {
  return mockSuccess(merchantRankList)
}
