<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>商家等级</span></div>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableList" border style="width: 100%">
        <el-table-column prop="name" label="等级名称" width="120" align="center" />
        <el-table-column prop="commission" label="佣金比例(%)" width="130" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.commission }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="merchantCount" label="商家数量" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" type="primary">{{ scope.row.merchantCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="small" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog :title="'编辑等级 - ' + editForm.name" :visible.sync="dialogVisible" width="500px" :close-on-click-modal="false">
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入等级名称" />
        </el-form-item>
        <el-form-item label="佣金比例" prop="commission">
          <el-input-number v-model="editForm.commission" :min="0" :max="100" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitEdit">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMerchantLevels } from '@/api/merchant'

export default {
  name: 'MerchantLevel',
  data() {
    return {
      loading: false,
      tableList: [],
      dialogVisible: false,
      submitLoading: false,
      editForm: {
        id: undefined,
        name: '',
        commission: 0,
        description: ''
      },
      rules: {
        name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
        commission: [{ required: true, message: '请输入佣金比例', trigger: 'blur' }],
        description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
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
        const res = await getMerchantLevels()
        this.tableList = res.data
      } catch (e) {
        this.$message.error('获取等级列表失败')
      } finally {
        this.loading = false
      }
    },
    handleEdit(row) {
      this.editForm = {
        id: row.id,
        name: row.name,
        commission: row.commission,
        description: row.description
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.editFormRef) {
          this.$refs.editFormRef.clearValidate()
        }
      })
    },
    submitEdit() {
      this.$refs.editFormRef.validate(valid => {
        if (!valid) return
        this.submitLoading = true
        // 模拟提交
        setTimeout(() => {
          const index = this.tableList.findIndex(item => item.id === this.editForm.id)
          if (index !== -1) {
            this.tableList.splice(index, 1, { ...this.tableList[index], ...this.editForm })
          }
          this.$message.success('修改成功')
          this.dialogVisible = false
          this.submitLoading = false
        }, 500)
      })
    }
  }
}
</script>

<style scoped>
</style>
