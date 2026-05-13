<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" class="search-form">
      <el-form-item label="商家名称">
        <el-input v-model="queryParams.keyword" placeholder="请输入商家ID" clearable
          @keyup.enter.native="handleSearch" style="width: 220px;" />
      </el-form-item>
      <el-form-item label="排序方式">
        <el-select v-model="queryParams.sortBy" placeholder="请选择" @change="handleSearch" style="width: 150px;">
          <el-option label="销售额" value="sales" />
          <el-option label="订单数" value="orders" />
          <el-option label="评分" value="rating" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-card shadow="hover" v-loading="loading">
      <el-table :data="rankList" stripe border style="width: 100%">
        <el-table-column label="排名" width="80" align="center">
          <template slot-scope="scope">
            <el-badge v-if="scope.$index < 3" :value="scope.$index + 1"
              class="rank-badge" :class="'rank-' + (scope.$index + 1)">
              <span class="rank-icon">
                <i v-if="scope.$index === 0" class="el-icon-trophy" style="color: #FFD700;" />
                <i v-else-if="scope.$index === 1" class="el-icon-trophy" style="color: #C0C0C0;" />
                <i v-else class="el-icon-trophy" style="color: #CD7F32;" />
              </span>
            </el-badge>
            <span v-else>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商家名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sales" label="销售额(元)" width="160" align="right" sortable>
          <template slot-scope="scope">
            <span class="sales-value">¥{{ formatAmount(scope.row.sales) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orders" label="订单数" width="120" align="right" sortable>
          <template slot-scope="scope">
            <span>{{ scope.row.orders }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="150" align="center" sortable>
          <template slot-scope="scope">
            <el-rate :value="scope.row.rating" disabled :max="5" allow-half
              :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" class="pagination"
        :current-page="queryParams.pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange" />
    </el-card>
  </div>
</template>

<script>
import { getMerchantRankList } from '@/api/analysis'

export default {
  name: 'SalesRank',
  data() {
    return {
      loading: false,
      total: 0,
      queryParams: {
        keyword: '',
        sortBy: 'sales',
        pageNum: 1,
        pageSize: 10
      },
      rankList: [],
      allData: []
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.loading = true
      getMerchantRankList({
        keyword: this.queryParams.keyword,
        sortBy: this.queryParams.sortBy
      }).then(res => {
        this.allData = res || this.getMockData()
        this.filterAndPaginate()
      }).catch(() => {
        this.allData = this.getMockData()
        this.filterAndPaginate()
      }).finally(() => {
        this.loading = false
      })
    },
    getMockData() {
      const merchants = []
      const names = [
        '海底捞火锅', '星巴克咖啡', '麦当劳', '肯德基', '必胜客',
        '西贝莜面村', '外婆家', '喜茶', '奈雪的茶', '呷哺呷哺',
        '味千拉面', '真功夫', '永和大王', '吉野家', '黄记煌',
        '海底小屋', '胖哥俩', '绿茶餐厅', '南京大牌档', '丰收日'
      ]
      for (let i = 0; i < names.length; i++) {
        merchants.push({
          id: i + 1,
          name: names[i],
          sales: Math.round((100000 - i * 4500) * (0.8 + Math.random() * 0.4)),
          orders: Math.round((5000 - i * 220) * (0.8 + Math.random() * 0.4)),
          rating: Math.round((5 - i * 0.1 + Math.random() * 0.3) * 10) / 10
        })
      }
      return merchants.sort((a, b) => b.sales - a.sales)
    },
    filterAndPaginate() {
      let filtered = this.allData
      if (this.queryParams.keyword) {
        const kw = this.queryParams.keyword.toLowerCase()
        filtered = filtered.filter(item => item.name.toLowerCase().includes(kw))
      }
      if (this.queryParams.sortBy === 'sales') {
        filtered.sort((a, b) => b.sales - a.sales)
      } else if (this.queryParams.sortBy === 'orders') {
        filtered.sort((a, b) => b.orders - a.orders)
      } else if (this.queryParams.sortBy === 'rating') {
        filtered.sort((a, b) => b.rating - a.rating)
      }
      this.total = filtered.length
      const start = (this.queryParams.pageNum - 1) * this.queryParams.pageSize
      const end = start + this.queryParams.pageSize
      this.rankList = filtered.slice(start, end)
    },
    handleSearch() {
      this.queryParams.pageNum = 1
      this.fetchData()
    },
    handleReset() {
      this.queryParams = {
        keyword: '',
        sortBy: 'sales',
        pageNum: 1,
        pageSize: 10
      }
      this.fetchData()
    },
    handleSizeChange(val) {
      this.queryParams.pageSize = val
      this.filterAndPaginate()
    },
    handleCurrentChange(val) {
      this.queryParams.pageNum = val
      this.filterAndPaginate()
    },
    formatAmount(val) {
      if (val == null) return '0.00'
      return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    }
  }
}
</script>

<style scoped>
.search-form {
  margin-bottom: 15px;
}
.rank-badge {
  display: inline-block;
}
.rank-icon {
  font-size: 20px;
  line-height: 1;
}
.rank-1 .el-badge__content {
  background-color: #FFD700;
}
.rank-2 .el-badge__content {
  background-color: #C0C0C0;
}
.rank-3 .el-badge__content {
  background-color: #CD7F32;
}
.sales-value {
  color: #F56C6C;
  font-weight: bold;
}
.pagination {
  margin-top: 15px;
  text-align: right;
}
</style>
