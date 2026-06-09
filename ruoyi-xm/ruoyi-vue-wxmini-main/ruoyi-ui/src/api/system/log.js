import request from '@/utils/request'
import { addDateRange } from '@/utils/ruoyi'

const businessTypeMap = {
  0: '其它',
  1: '新增',
  2: '修改',
  3: '删除',
  4: '授权',
  5: '导出',
  6: '导入',
  7: '强退',
  8: '生成代码',
  9: '清空数据'
}

function normalizeStatus(status) {
  return String(status) === '0' ? 0 : 1
}

function normalizeLoginRows(rows = []) {
  return rows.map(item => ({
    ...item,
    userId: item.userName || item.userId || '',
    ip: item.ipaddr || item.ip || '',
    location: item.loginLocation || item.location || '',
    status: normalizeStatus(item.status)
  }))
}

function normalizeOperationRows(rows = []) {
  return rows.map(item => ({
    ...item,
    operator: item.operName || item.operator || '',
    module: item.title || item.module || '',
    operation: businessTypeMap[item.businessType] || item.operation || item.businessType || '其它',
    ip: item.operIp || item.ip || '',
    createTime: item.operTime || item.createTime || '',
    status: normalizeStatus(item.status),
    costTime: item.costTime || 0
  }))
}

export function getLoginLogList(query, dateRange = []) {
  const params = { ...query, userName: query.userName || query.userId }
  delete params.userId
  return request({
    url: '/monitor/logininfor/list',
    method: 'get',
    params: addDateRange(params, dateRange)
  }).then(res => ({
    ...res,
    rows: normalizeLoginRows(res.rows)
  }))
}

export function getOperationLogList(query, dateRange = []) {
  const params = { ...query, operName: query.operName || query.operator, title: query.title || query.module }
  delete params.operator
  delete params.module
  return request({
    url: '/monitor/operlog/list',
    method: 'get',
    params: addDateRange(params, dateRange)
  }).then(res => ({
    ...res,
    rows: normalizeOperationRows(res.rows)
  }))
}
