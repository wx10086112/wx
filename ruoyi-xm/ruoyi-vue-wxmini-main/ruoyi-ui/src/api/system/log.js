import { mockSuccess, mockPage } from '@/mock'
import { loginLogList, operationLogList } from '@/mock/data'

export function getLoginLogList(query) {
  return mockPage(loginLogList, query.pageNum, query.pageSize)
}

export function getOperationLogList(query) {
  return mockPage(operationLogList, query.pageNum, query.pageSize)
}
