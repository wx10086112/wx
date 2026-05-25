<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>商家用户管理</span>
        <el-button style="float: right; padding: 3px 0;" type="text" icon="el-icon-plus" @click="handleAdd">新增用户</el-button>
      </div>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" size="small" class="search-form">
        <el-form-item label="所属商家">
          <el-select v-model="queryParams.merchantId" placeholder="请选择商家" clearable filterable>
            <el-option v-for="m in merchantList" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="queryParams.username" placeholder="请输入登录账号" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.realName" placeholder="请输入姓名" clearable @keyup.enter.native="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column label="所属商家" min-width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            {{ merchantNameMap[scope.row.merchantId] || scope.row.merchantId }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="登录账号" width="130" />
        <el-table-column prop="realName" label="姓名" width="100" align="center" />
        <el-table-column prop="phone" label="手机号" width="130" align="center" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.role === 'owner' ? 'danger' : 'primary'" size="small">
              {{ scope.row.role === 'owner' ? '管理员' : '成员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" size="small" icon="el-icon-key" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button v-if="scope.row.role !== 'owner'" type="text" size="small" icon="el-icon-delete" class="danger-btn" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" :close-on-click-modal="false">
      <el-form ref="userForm" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="所属商家" prop="merchantId">
          <el-select v-model="formData.merchantId" placeholder="请选择商家" filterable :disabled="isEdit" style="width: 100%">
            <el-option v-for="m in merchantList" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="formData.username" placeholder="请输入登录账号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="formData.password" placeholder="默认 123456" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员（全权限）" value="owner" />
            <el-option label="普通成员" value="member" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog title="重置密码" :visible.sync="resetPwdDialogVisible" width="400px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ resetPwdRow.realName }}（{{ resetPwdRow.username }}）</span>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="newPassword" placeholder="留空则默认 123456" show-password />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="resetPwdDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="resetPwdLoading" @click="submitResetPwd">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMerchantList } from '@/api/merchant'
import {
  getMerchantUserList,
  addMerchantUser,
  updateMerchantUser,
  deleteMerchantUser,
  resetMerchantUserPwd,
  changeMerchantUserStatus
} from '@/api/merchant'

export default {
  name: 'MerchantUser',
  data() {
    return {
      loading: false,
      tableList: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      queryParams: {
        merchantId: undefined,
        username: '',
        realName: '',
        status: undefined
      },
      merchantList: [],
      merchantNameMap: {},

      // 新增/编辑
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      submitLoading: false,
      formData: {
        id: undefined,
        merchantId: undefined,
        username: '',
        password: '',
        realName: '',
        phone: '',
        role: 'member',
        status: 1,
        remark: ''
      },
      formRules: {
        merchantId: [{ required: true, message: '请选择商家', trigger: 'change' }],
        username: [
          { required: true, message: '请输入登录账号', trigger: 'blur' },
          { min: 3, max: 30, message: '长度在 3 到 30 个字符', trigger: 'blur' }
        ],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
      },

      // 重置密码
      resetPwdDialogVisible: false,
      resetPwdRow: {},
      newPassword: '',
      resetPwdLoading: false
    }
  },
  created() {
    this.loadMerchants()
    this.fetchData()
  },
  methods: {
    async loadMerchants() {
      try {
        const res = await getMerchantList({ pageNum: 1, pageSize: 1000 })
        this.merchantList = res.rows || []
        this.merchantNameMap = {}
        this.merchantList.forEach(m => {
          this.merchantNameMap[m.id] = m.name
        })
      } catch (e) {
        console.error('获取商家列表失败')
      }
    },
    async fetchData() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          merchantId: this.queryParams.merchantId,
          username: this.queryParams.username,
          realName: this.queryParams.realName,
          status: this.queryParams.status
        }
        const res = await getMerchantUserList(params)
        this.tableList = res.rows || []
        this.total = res.total || 0
      } catch (e) {
        this.$message.error('获取用户列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pageNum = 1
      this.fetchData()
    },
    handleReset() {
      this.queryParams = { merchantId: undefined, username: '', realName: '', status: undefined }
      this.pageNum = 1
      this.fetchData()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
      this.fetchData()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.fetchData()
    },
    handleAdd() {
      this.isEdit = false
      this.dialogTitle = '新增商家用户'
      this.formData = {
        id: undefined,
        merchantId: undefined,
        username: '',
        password: '',
        realName: '',
        phone: '',
        role: 'member',
        status: 1,
        remark: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.userForm && this.$refs.userForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.isEdit = true
      this.dialogTitle = '编辑商家用户'
      this.formData = { ...row }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.userForm && this.$refs.userForm.clearValidate()
      })
    },
    async submitForm() {
      this.$refs.userForm.validate(async(valid) => {
        if (!valid) return
        this.submitLoading = true
        try {
          if (this.isEdit) {
            await updateMerchantUser(this.formData)
            this.$message.success('修改成功')
          } else {
            const data = { ...this.formData }
            if (!data.password) {
              data.password = '123456'
            }
            await addMerchantUser(data)
            this.$message.success('新增成功')
          }
          this.dialogVisible = false
          this.fetchData()
        } catch (e) {
          this.$message.error(this.isEdit ? '修改失败' : '新增失败')
        } finally {
          this.submitLoading = false
        }
      })
    },
    handleDelete(row) {
      this.$confirm(`确认删除用户「${row.realName}（${row.username}）」？`, '提示', {
        type: 'warning'
      }).then(async() => {
        try {
          await deleteMerchantUser(row.id)
          this.$message.success('删除成功')
          this.fetchData()
        } catch (e) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    async handleStatusChange(row) {
      try {
        await changeMerchantUserStatus(row.id, row.status)
        this.$message.success(row.status === 1 ? '已启用' : '已禁用')
      } catch (e) {
        this.$message.error('操作失败')
        row.status = row.status === 1 ? 0 : 1
      }
    },
    handleResetPwd(row) {
      this.resetPwdRow = row
      this.newPassword = ''
      this.resetPwdDialogVisible = true
    },
    async submitResetPwd() {
      this.resetPwdLoading = true
      try {
        await resetMerchantUserPwd(this.resetPwdRow.id, this.newPassword || '123456')
        this.$message.success('密码已重置为: ' + (this.newPassword || '123456'))
        this.resetPwdDialogVisible = false
      } catch (e) {
        this.$message.error('重置失败')
      } finally {
        this.resetPwdLoading = false
      }
    }
  }
}
</script>

<style scoped>
.search-form {
  margin-bottom: 10px;
}
.pagination {
  margin-top: 15px;
  text-align: right;
}
.danger-btn {
  color: #f56c6c;
}
</style>
