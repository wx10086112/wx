import { mockSuccess, mockPage } from '@/mock'
import { platformFlowList, profitShareList, financeReport, incomeStats } from '@/mock/data'

export function getPlatformFlowList(query) {
  return mockPage(platformFlowList, query.pageNum, query.pageSize)
}

export function getProfitShareList(query) {
  return mockPage(profitShareList, query.pageNum, query.pageSize)
}

export function getWithdrawList(query) {
  const { withdrawList } = require('@/mock/data')
  return mockPage(withdrawList, query.pageNum, query.pageSize)
}

export function getFinanceReport() {
  return mockSuccess(financeReport)
}

export function getIncomeStats() {
  return mockSuccess(incomeStats)
}
