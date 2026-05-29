<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>售后订单</span></div>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableList" border>
        <el-table-column label="订单号" prop="orderNo" width="180" />
        <el-table-column label="商家" prop="merchantName" width="140" />
        <el-table-column label="用户" prop="userName" width="100" />
        <el-table-column label="退款原因" prop="refundReason" min-width="160" show-overflow-tooltip />
        <el-table-column label="退款金额" width="110">
          <template slot-scope="scope">
            <span class="text-danger">¥{{ (scope.row.refundAmount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" prop="createTime" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag v-if="statusMap[scope.row.status]" :type="statusMap[scope.row.status].type" size="small">
              {{ statusMap[scope.row.status].text }}
            </el-tag>
            <el-tag v-else type="info" size="small">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status !== 2" type="text" size="small" @click="handleProcess(scope.row)">处理</el-button>
            <el-button type="text" size="small" @click="handleView(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
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

    <!-- 处理弹窗 -->
    <el-dialog title="售后处理" :visible.sync="processDialogVisible" width="500px">
      <template v-if="currentRow">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="订单号">{{ currentRow.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="商家">{{ currentRow.merchantId }}</el-descriptions-item>
          <el-descriptions-item label="退款原因">{{ currentRow.refundReason }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ (currentRow.refundAmount || 0).toFixed(2) }}</el-descriptions-item>
        </el-descriptions>
        <el-form :model="processForm" label-width="80px" style="margin-top: 16px;">
          <el-form-item label="处理方式">
            <el-radio-group v-model="processForm.action">
              <el-radio label="approve">同意退款</el-radio>
              <el-radio label="reject">拒绝退款</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="processForm.remark" type="textarea" :rows="3" placeholder="请输入处理备注" />
          </el-form-item>
        </el-form>
      </template>
      <span slot="footer">
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getAfterSaleList } from '@/api/order'

export default {
  name: 'OrderAfterSale',
  data() {
    return {
      loading: false,
      tableList: [],
      total: 0,
      queryParams: {
        orderNo: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      },
      statusMap: {
        0: { text: '待处理', type: 'warning' },
        1: { text: '处理中', type: '' },
        2: { text: '已完成', type: 'success' }
      },
      processDialogVisible: false,
      currentRow: null,
      processForm: {
        action: 'approve',
        remark: ''
      }
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await getAfterSaleList(this.queryParams)
        this.tableList = res.rows
        this.total = res.total
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.fetchData()
    },
    resetQuery() {
      this.queryParams = {
        orderNo: '',
        status: '',
        pageNum: 1,
        pageSize: 10
      }
      this.fetchData()
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.fetchData()
    },
    handleProcess(row) {
      this.currentRow = row
      this.processForm = { action: 'approve', remark: '' }
      this.processDialogVisible = true
    },
    handleView(row) {
      this.$router.push({ name: 'OrderDetail', params: { id: row.id } })
    },
    submitProcess() {
      this.$message.success('处理成功')
      this.processDialogVisible = false
      this.fetchData()
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
.text-danger {
  color: #F56C6C;
  font-weight: 500;
}
</style>
