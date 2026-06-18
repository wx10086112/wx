<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>商家结算管理</span></div>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="商家名称">
          <el-input v-model="queryParams.merchantName" placeholder="请输入商家名称" clearable />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="待T+1" value="WAITING_T1" />
            <el-option label="打款中" value="TRANSFERRING" />
            <el-option label="已到账" value="ARRIVED" />
            <el-option label="失败" value="FAILED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="退款处理中" value="REFUND_PROCESSING" />
            <el-option label="已冲正" value="REVERSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 工具栏 -->
      <el-row :gutter="10" class="toolbar">
        <el-button type="warning" size="small" :disabled="multipleSelection.length === 0" @click="handleWxBatchTransfer">
          微信批量打款
        </el-button>
      </el-row>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableList" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="结算单号" prop="settlementNo" width="200" />
        <el-table-column label="订单号" prop="orderNo" width="180" />
        <el-table-column label="标题" prop="title" min-width="160" show-overflow-tooltip />
        <el-table-column label="订单金额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.orderAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="商家金额" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.merchantAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="平台手续费" width="120" align="right">
          <template slot-scope="scope">
            ¥{{ formatAmount(scope.row.platformFeeAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130" align="center">
          <template slot-scope="scope">
            <el-tag :type="statusMap[scope.row.status] || 'info'" size="small">
              {{ statusTextMap[scope.row.status] || scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" prop="applyTime" width="160" />
        <el-table-column label="预计打款时间" prop="expectedTransferTime" width="160" />
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
import { getMerchantSettlementList, merchantBatchTransferReal } from '@/api/finance/settlement'

export default {
  name: 'MerchantSettlement',
  data() {
    return {
      loading: false,
      tableList: [],
      total: 0,
      multipleSelection: [],
      queryParams: {
        merchantName: '',
        orderNo: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      },
      statusMap: {
        WAITING_T1: 'warning',
        TRANSFERRING: 'primary',
        ARRIVED: 'success',
        FAILED: 'danger',
        CANCELLED: 'info',
        REFUND_PROCESSING: 'warning',
        REVERSED: 'info'
      },
      statusTextMap: {
        WAITING_T1: '待T+1',
        TRANSFERRING: '打款中',
        ARRIVED: '已到账',
        FAILED: '失败',
        CANCELLED: '已取消',
        REFUND_PROCESSING: '退款处理中',
        REVERSED: '已冲正'
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
        const res = await getMerchantSettlementList(this.queryParams)
        this.tableList = res.rows
        this.total = res.total
      } finally {
        this.loading = false
      }
    },
    formatAmount(val) {
      return Number(val || 0).toFixed(2)
    },
    handleSelectionChange(selection) {
      this.multipleSelection = selection
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        merchantName: '',
        orderNo: '',
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
    },
    handleWxBatchTransfer() {
      const ids = this.multipleSelection.map(item => item.id)
      this.$confirm('确认通过微信对选中的 ' + ids.length + ' 条记录发起打款?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await merchantBatchTransferReal(ids)
        this.$message.success('微信打款已发起')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
