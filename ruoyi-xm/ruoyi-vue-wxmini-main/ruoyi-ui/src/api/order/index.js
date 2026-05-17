import { mockSuccess, mockPage } from '@/mock'
import { orderList, afterSaleList, abnormalOrderList } from '@/mock/data'

export function getOrderList(query) {
  let list = orderList
  if (query.status !== undefined && query.status !== '') {
    list = list.filter(o => o.status === Number(query.status))
  }
  return mockPage(list, query.pageNum, query.pageSize)
}

export function getOrderDetail(id) {
  const order = orderList.find(o => o.id === id)
  return mockSuccess(order || orderList[0])
}

export function getAfterSaleList(query) {
  return mockPage(afterSaleList, query.pageNum, query.pageSize)
}

export function getAbnormalOrderList(query) {
  return mockPage(abnormalOrderList, query.pageNum, query.pageSize)
}
