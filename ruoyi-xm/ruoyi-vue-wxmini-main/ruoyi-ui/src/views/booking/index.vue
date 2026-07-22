<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="card-header">
        <span>预点单管理</span>
        <el-button size="mini" icon="el-icon-refresh" @click="getList">刷新</el-button>
      </div>

      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="预点单号">
          <el-input v-model="queryParams.bookingNo" placeholder="请输入预点单号" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="商家名称">
          <el-input v-model="queryParams.merchantName" placeholder="请输入商家名称" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="套餐名称">
          <el-input v-model="queryParams.productName" placeholder="请输入套餐名称" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="用户名称">
          <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="queryParams.contactPhone" placeholder="请输入联系电话" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="bookingList" border>
        <el-table-column label="预点单号" prop="bookingNo" min-width="170" show-overflow-tooltip />
        <el-table-column label="商家" prop="merchantName" min-width="140" show-overflow-tooltip />
        <el-table-column label="套餐" prop="productName" min-width="180" show-overflow-tooltip />
        <el-table-column label="用户" min-width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <span>{{ scope.row.userName || scope.row.contactName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="联系电话" prop="contactPhone" min-width="120" />
        <el-table-column label="人数" prop="peopleCount" width="70" align="center" />
        <el-table-column label="预点单时间" prop="bookingTime" min-width="160" />
        <el-table-column label="过期时间" prop="expireTime" min-width="160">
          <template slot-scope="scope">
            <span>{{ scope.row.expireTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="95" align="center">
          <template slot-scope="scope">
            <el-tag :type="statusMap[scope.row.status] ? statusMap[scope.row.status].type : 'info'" size="small">
              {{ statusMap[scope.row.status] ? statusMap[scope.row.status].text : '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip />
        <el-table-column label="创建时间" prop="createTime" min-width="160" />
        <el-table-column label="操作" width="210" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 'PENDING'" type="text" size="small" @click="handleConfirm(scope.row)">确认</el-button>
            <el-button v-if="scope.row.status === 'CONFIRMED'" type="text" size="small" @click="handleComplete(scope.row)">完成</el-button>
            <el-button v-if="canCancel(scope.row)" type="text" size="small" @click="handleCancel(scope.row)">取消</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" type="text" size="small" @click="handleExpire(scope.row)">置过期</el-button>
            <el-button type="text" size="small" @click="handleDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        :current-page="queryParams.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <el-dialog title="预点单详情" :visible.sync="detailOpen" width="620px">
      <el-descriptions v-if="currentBooking" :column="2" border>
        <el-descriptions-item label="预点单号">{{ currentBooking.bookingNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[currentBooking.status] ? statusMap[currentBooking.status].text : currentBooking.status }}</el-descriptions-item>
        <el-descriptions-item label="商家">{{ currentBooking.merchantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="套餐">{{ currentBooking.productName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentBooking.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentBooking.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ currentBooking.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="人数">{{ currentBooking.peopleCount || 1 }}</el-descriptions-item>
        <el-descriptions-item label="预点单时间">{{ currentBooking.bookingTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">{{ currentBooking.expireTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentBooking.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <span slot="footer">
        <el-button @click="detailOpen = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  listBooking,
  getBooking,
  confirmBooking,
  completeBooking,
  cancelBooking,
  expireBooking
} from '@/api/booking'

export default {
  name: 'BookingManage',
  data() {
    return {
      loading: false,
      bookingList: [],
      total: 0,
      detailOpen: false,
      currentBooking: null,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        bookingNo: '',
        merchantName: '',
        productName: '',
        userName: '',
        contactPhone: '',
        status: ''
      },
      statusOptions: [
        { label: '待处理', value: 'PENDING' },
        { label: '已确认', value: 'CONFIRMED' },
        { label: '已完成', value: 'COMPLETED' },
        { label: '已取消', value: 'CANCELLED' },
        { label: '已过期', value: 'EXPIRED' }
      ],
      statusMap: {
        PENDING: { text: '待处理', type: 'warning' },
        CONFIRMED: { text: '已确认', type: 'primary' },
        COMPLETED: { text: '已完成', type: 'success' },
        CANCELLED: { text: '已取消', type: 'info' },
        EXPIRED: { text: '已过期', type: 'danger' }
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
        const params = { ...this.queryParams }
        if (this.dateRange && this.dateRange.length === 2) {
          params.beginTime = this.dateRange[0]
          params.endTime = this.dateRange[1]
        }
        const res = await listBooking(params)
        this.bookingList = res.rows || []
        this.total = res.total || 0
      } finally {
        this.loading = false
      }
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        bookingNo: '',
        merchantName: '',
        productName: '',
        userName: '',
        contactPhone: '',
        status: ''
      }
      this.dateRange = []
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
    canCancel(row) {
      return row.status === 'PENDING' || row.status === 'CONFIRMED'
    },
    async handleDetail(row) {
      const res = await getBooking(row.id)
      this.currentBooking = res.data
      this.detailOpen = true
    },
    handleConfirm(row) {
      this.confirmAction('确认该预点单？', () => confirmBooking(row.id))
    },
    handleComplete(row) {
      this.confirmAction('标记该预点单为已完成？', () => completeBooking(row.id))
    },
    handleCancel(row) {
      this.confirmAction('取消该预点单？', () => cancelBooking(row.id))
    },
    handleExpire(row) {
      this.confirmAction('将该预点单置为已过期？', () => expireBooking(row.id))
    },
    confirmAction(message, action) {
      this.$modal.confirm(message).then(() => action()).then(() => {
        this.$modal.msgSuccess('操作成功')
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.search-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
