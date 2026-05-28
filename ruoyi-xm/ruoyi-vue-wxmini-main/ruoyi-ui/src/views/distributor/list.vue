<template>
  <div class="app-container">
    <!-- 筛选区 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" size="small" v-show="true">
      <el-form-item label="名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入分销商名称" clearable style="width: 180px;" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 150px;" />
      </el-form-item>
      <el-form-item label="区域" prop="regionName">
        <el-input v-model="queryParams.regionName" placeholder="请输入区域" clearable style="width: 150px;" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 100px;">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button v-hasPermi="['mall:distributor:add']" type="primary" icon="el-icon-plus" size="mini" @click="handleAdd">新增分销商</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-hasPermi="['mall:distributor:remove']" type="danger" icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">批量删除</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="ID" prop="id" width="60" align="center" />
      <el-table-column label="分销商名称" prop="name" min-width="140" show-overflow-tooltip />
      <el-table-column label="联系人" prop="contact" width="100" />
      <el-table-column label="联系电话" prop="phone" width="130" />
      <el-table-column label="登录账号" prop="username" width="130" />
      <el-table-column label="区域" prop="regionName" width="120" />
      <el-table-column label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">{{ scope.row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" width="160" align="center">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="300" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button v-hasPermi="['mall:distributor:edit']" size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button v-hasPermi="['mall:distributor:edit']" size="mini" type="text" icon="el-icon-key" @click="handleResetPwd(scope.row)">重置密码</el-button>
          <el-button v-hasPermi="['mall:distributor:edit']" size="mini" type="text" @click="handleToggleStatus(scope.row)">{{ scope.row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button v-if="isSuperAdmin" size="mini" type="text" icon="el-icon-view" @click="handleSwitch(scope.row)">切换视角</el-button>
          <el-button v-hasPermi="['mall:distributor:remove']" size="mini" type="text" icon="el-icon-delete" style="color: #F56C6C;" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分销商名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入分销商名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contact">
              <el-input v-model="form.contact" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="登录账号" prop="username">
              <el-input v-model="form.username" placeholder="请输入登录账号" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="!form.id">
          <el-col :span="12">
            <el-form-item label="初始密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入初始密码" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="区域名称" prop="regionName">
              <el-input v-model="form.regionName" placeholder="请输入区域名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区域编码" prop="regionCode">
              <el-input v-model="form.regionCode" placeholder="请输入区域编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDistributor, addDistributor, updateDistributor, deleteDistributor, changeDistributorStatus, resetDistributorPassword, switchDistributor } from '@/api/distributor'

export default {
  name: 'DistributorList',
  data() {
    return {
      loading: false,
      submitLoading: false,
      ids: [],
      multiple: true,
      total: 0,
      list: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        phone: null,
        regionName: null,
        status: null
      },
      dialogVisible: false,
      dialogTitle: '',
      form: {},
      formRules: {
        name: [{ required: true, message: '请输入分销商名称', trigger: 'blur' }],
        username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
        contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  computed: {
    isSuperAdmin() {
      return this.$store.getters.roles.includes('admin')
    }
  },
  methods: {
    getList() {
      this.loading = true
      listDistributor(this.queryParams).then(res => {
        this.list = res.rows || []
        this.total = res.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.$refs.queryForm.resetFields()
      this.queryParams.status = null
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.multiple = !selection.length
    },
    handleAdd() {
      this.dialogTitle = '新增分销商'
      this.form = {
        name: '', contact: '', phone: '', username: '', password: '',
        regionName: '', regionCode: '', status: 1, remark: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => { this.$refs.form && this.$refs.form.clearValidate() })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑分销商'
      this.form = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => { this.$refs.form && this.$refs.form.clearValidate() })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitLoading = true
        const action = this.form.id ? updateDistributor : addDistributor
        action(this.form).then(() => {
          this.$message.success(this.form.id ? '修改成功' : '新增成功')
          this.dialogVisible = false
          this.getList()
        }).catch(err => {
          this.$message.error(err.msg || '操作失败')
        }).finally(() => {
          this.submitLoading = false
        })
      })
    },
    handleToggleStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const text = newStatus === 1 ? '启用' : '禁用'
      this.$confirm('确认' + text + '该分销商？', '提示', { type: 'warning' }).then(() => {
        changeDistributorStatus({ id: row.id, status: newStatus }).then(() => {
          this.$message.success(text + '成功')
          this.getList()
        })
      }).catch(() => {})
    },
    handleResetPwd(row) {
      this.$prompt('请输入新密码', '重置密码 - ' + row.name, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^.{6,}$/,
        inputErrorMessage: '密码至少6位',
        inputValue: '123456'
      }).then(({ value }) => {
        resetDistributorPassword({ id: row.id, password: value }).then(() => {
          this.$message.success('密码已重置')
        })
      }).catch(() => {})
    },
    handleSwitch(row) {
      this.$confirm('确认切换为分销商「' + row.name + '」的视角？切换后将看到该分销商的数据。', '切换视角', { type: 'warning' }).then(() => {
        switchDistributor(row.id).then(res => {
          this.$message.success(res.msg || '切换成功')
          // 刷新页面以应用新视角
          setTimeout(() => { location.reload() }, 500)
        })
      }).catch(() => {})
    },
    handleDelete(row) {
      const ids = row.id ? [row.id] : this.ids
      this.$confirm('确认删除选中的分销商？', '警告', { type: 'warning' }).then(() => {
        return deleteDistributor(ids.join(','))
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
</style>
