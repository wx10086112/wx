<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>平台转账记录</span></div>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="转账单号">
          <el-input v-model="queryParams.transferNo" placeholder="请输入转账单号" clearable />
        </el-form-item>
        <el-form-item label="结算单号">
          <el-input v-model="queryParams.settlementNo" placeholder="请输入结算单号" clearable />
        </el-form-item>
        <el-form-item label="目标类型">
          <el-select v-model="queryParams.targetType" placeholder="全部" clearable>
            <el-option label="商家" value="MERCHANT" />
            <el-option label="分销商" value="DISTRIBUTOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="待处理" value="WAITING" />
            <el-option label="转账中" value="TRANSFERRING" />
            <el-option label="已到账" value="ARRIVED" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableList" border>
        <el-table-column label="转账单号" prop="transferNo" width="200" show-overflow-tooltip />
        <el-table-column label="结算单号" prop="settlementNo" width="200" show-overflow-tooltip />
        <el-table-column label="目标类型" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.targetType === 'MERCHANT' ? 'success' : 'primary'" size="small">
              {{ scope.row.targetType === 'MERCHANT' ? '商家' : '分销商' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商家名称" prop="merchantName" width="120" show-overflow-tooltip />
        <el-table-column label="分销商名称" prop="distributorName" width="120" show-overflow-tooltip />
        <el-table-column label="订单号" prop="orderNo" width="180" show-overflow-tooltip />
        <el-table-column label="转账金额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column label="收款openid" prop="receiverOpenid" width="150" show-overflow-tooltip />
        <el-table-column label="微信批次号" prop="wechatBatchNo" width="180" show-overflow-tooltip />
        <el-table-column label="微信明细号" prop="wechatDetailNo" width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="statusMap[scope.row.status] || 'info'" size="small">
              {{ statusTextMap[scope.row.status] || scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" prop="applyTime" width="160" />
        <el-table-column label="转账时间" prop="transferTime" width="160" />
        <el-table-column label="到账时间" prop="arriveTime" width="160" />
        <el-table-column label="失败原因" prop="failReason" min-width="150" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        :current-page="queryParams.pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script>
import { getTransferRecordList } from '@/api/finance/settlement'

export default {
  name: 'PlatformTransfer',
  data() {
    return {
      loading: false,
      tableList: [],
      total: 0,
      queryParams: {
        transferNo: '',
        settlementNo: '',
        targetType: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      },
      statusMap: {
        WAITING: 'info',
        TRANSFERRING: 'primary',
        ARRIVED: 'success',
        FAILED: 'danger',
        CANCELLED: 'info'
      },
      statusTextMap: {
        WAITING: '待处理',
        TRANSFERRING: '转账中',
        ARRIVED: '已到账',
        FAILED: '失败',
        CANCELLED: '已取消'
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    async getList() {
      this.loading = true
      try {
        const res = await getTransferRecordList(this.queryParams)
        this.tableList = res.rows
        this.total = res.total
      } finally {
        this.loading = false
      }
    },
    formatAmount(val) {
      return Number(val || 0).toFixed(2)
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        transferNo: '',
        settlementNo: '',
        targetType: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      }
      this.getList()
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.getList()
    },
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.getList()
    }
  }
}
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
