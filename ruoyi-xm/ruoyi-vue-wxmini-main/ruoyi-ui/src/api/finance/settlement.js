import request from '@/utils/request'

// ==================== 商家结算 ====================

export function getMerchantSettlementList(query) {
  return request({ url: '/mall/settlement/merchant/list', method: 'get', params: query })
}

export function getMerchantSettlementDetail(id) {
  return request({ url: '/mall/settlement/merchant/' + id, method: 'get' })
}

export function merchantBatchTransfer(ids) {
  return request({ url: '/mall/settlement/merchant/batch-transfer-real', method: 'post', data: ids })
}

export function merchantMarkArrived(ids) {
  return request({ url: '/mall/settlement/merchant/mark-arrived', method: 'post', data: ids })
}

export function merchantMarkFailed(data) {
  return request({ url: '/mall/settlement/merchant/mark-failed', method: 'post', data: data })
}

// ==================== 分销商结算 ====================

export function getDistributorSettlementList(query) {
  return request({ url: '/mall/settlement/distributor/list', method: 'get', params: query })
}

export function getDistributorSettlementDetail(id) {
  return request({ url: '/mall/settlement/distributor/' + id, method: 'get' })
}

export function distributorBatchArrived(ids) {
  return request({ url: '/mall/settlement/distributor/batch-arrived', method: 'post', data: ids })
}

export function distributorMarkFailed(data) {
  return request({ url: '/mall/settlement/distributor/mark-failed', method: 'post', data: data })
}

// ==================== 分账流水 ====================

export function getProfitLedgerList(query) {
  return request({ url: '/mall/settlement/profit-ledger/list', method: 'get', params: query })
}

export function getProfitLedgerDetail(id) {
  return request({ url: '/mall/settlement/profit-ledger/' + id, method: 'get' })
}

// ==================== 微信转账 ====================

export function merchantTransferReal(settlementId) {
  return request({ url: '/mall/settlement/merchant/transfer/' + settlementId, method: 'post' })
}

export function merchantBatchTransferReal(ids) {
  return request({ url: '/mall/settlement/merchant/batch-transfer-real', method: 'post', data: ids })
}

export function distributorTransferReal(settlementId) {
  return request({ url: '/mall/settlement/distributor/transfer/' + settlementId, method: 'post' })
}

export function distributorBatchTransferReal(ids) {
  return request({ url: '/mall/settlement/distributor/batch-transfer-real', method: 'post', data: ids })
}

// ==================== 转账记录 ====================

export function getTransferRecordList(query) {
  return request({ url: '/mall/settlement/transfer/list', method: 'get', params: query })
}
