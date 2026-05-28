<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>异常订单</span></div>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
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
        <el-table-column label="订单金额" width="110">
          <template slot-scope="scope">
            <span>¥{{ scope.row.totalAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 0 ? 'danger' : 'success'" size="small">
              {{ scope.row.status === 0 ? '未处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 0" type="text" size="small" @click="handleProcess(scope.row)">处理</el-button>
            <span v-else class="text-info">已处理</span>
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
    <el-dialog title="异常订单处理" :visible.sync="processDialogVisible" width="500px">
      <template v-if="currentRow">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="订单号">{{ currentRow.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="商家">{{ currentRow.merchantId }}</el-descriptions-item>
          <el-descriptions-item label="异常原因">{{ currentRow.issue }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentRow.totalAmount.toFixed(2) }}</el-descriptions-item>
        </el-descriptions>
        <el-form :model="processForm" label-width="80px" style="margin-top: 16px;">
          <el-form-item label="处理方式">
            <el-radio-group v-model="processForm.action">
              <el-radio label="fix">修复订单</el-radio>
              <el-radio label="cancel">取消订单</el-radio>
              <el-radio label="ignore">标记已处理</el-radio>
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
import { getAbnormalOrderList } from '@/api/order'

export default {
  name: 'OrderAbnormal',
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
      processDialogVisible: false,
      currentRow: null,
      processForm: {
        action: 'fix',
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
        const res = await getAbnormalOrderList(this.queryParams)
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
      this.processForm = { action: 'fix', remark: '' }
      this.processDialogVisible = true
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
.text-info {
  color: #909399;
  font-size: 12px;
}
</style>
