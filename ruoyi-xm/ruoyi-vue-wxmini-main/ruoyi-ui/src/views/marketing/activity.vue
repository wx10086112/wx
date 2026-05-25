<template>
  <div class="app-container">
    <!-- 筛选区 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" size="small" v-show="true">
      <el-form-item label="商家ID" prop="merchantId">
        <el-input v-model="queryParams.merchantId" placeholder="商家ID" clearable style="width: 140px;" />
      </el-form-item>
      <el-form-item label="活动名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入活动名称" clearable style="width: 180px;" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px;">
          <el-option label="未启用" :value="0" />
          <el-option label="进行中" :value="1" />
          <el-option label="已结束" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建来源" prop="sourceType">
        <el-select v-model="queryParams.sourceType" placeholder="全部" clearable style="width: 120px;">
          <el-option label="总后台" value="ADMIN" />
          <el-option label="商家端" value="MERCHANT" />
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
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd">新增团购活动</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">批量删除</el-button>
      </el-col>
    </el-row>

    <!-- 活动表格 -->
    <el-table v-loading="loading" :data="grouponList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="活动封面" width="90" align="center">
        <template slot-scope="scope">
          <el-image v-if="scope.row.coverImage" :src="scope.row.coverImage" style="width: 60px; height: 60px;" fit="cover" :preview-src-list="[scope.row.coverImage]" />
          <span v-else style="color: #ccc;">无</span>
        </template>
      </el-table-column>
      <el-table-column label="活动名称" prop="name" min-width="140" show-overflow-tooltip />
      <el-table-column label="所属商家" prop="merchantId" width="90" align="center" />
      <el-table-column label="活动时间" width="280" align="center">
        <template slot-scope="scope">
          {{ parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}') }} ~ {{ parseTime(scope.row.endTime, '{y}-{m}-{d} {h}:{i}') }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 1" type="success">进行中</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="info">已结束</el-tag>
          <el-tag v-else type="warning">未启用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="已售" prop="totalSold" width="70" align="center" />
      <el-table-column label="限购" width="80" align="center">
        <template slot-scope="scope">
          {{ scope.row.limitPerUser > 0 ? scope.row.limitPerUser + '/人' : '不限' }}
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sort" width="60" align="center" />
      <el-table-column label="来源" width="80" align="center">
        <template slot-scope="scope">
          <el-tag size="small" :type="scope.row.sourceType === 'ADMIN' ? '' : 'warning'">
            {{ scope.row.sourceType === 'ADMIN' ? '总后台' : '商家端' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160" align="center">
        <template slot-scope="scope">
          {{ parseTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="mini" type="text" :icon="scope.row.status === 1 ? 'el-icon-bottom' : 'el-icon-top'" @click="handleToggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-goods" @click="handleBindProduct(scope.row)">商品</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color: #F56C6C;" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="700px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商家ID" prop="merchantId">
              <el-input v-model="form.merchantId" placeholder="请输入商家ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入活动名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="每人限购" prop="limitPerUser">
              <el-input-number v-model="form.limitPerUser" :min="0" :controls="false" style="width: 100%;" placeholder="0不限" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="0">未启用</el-radio>
                <el-radio :label="1">进行中</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动封面" prop="coverImage">
          <el-upload
            class="image-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="(res) => handleImageSuccess(res, 'cover')"
            :before-upload="beforeImageUpload"
            :data="uploadData('cover')"
          >
            <img v-if="form.coverImage" :src="form.coverImage" class="image-preview">
            <i v-else class="el-icon-plus image-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">封面图，支持 jpg/jpeg/png/webp，不超过5MB</div>
        </el-form-item>
        <el-form-item label="活动海报">
          <el-upload
            class="image-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="(res) => handleImageSuccess(res, 'poster')"
            :before-upload="beforeImageUpload"
            :data="uploadData('poster')"
          >
            <img v-if="form.posterImage" :src="form.posterImage" class="image-preview">
            <i v-else class="el-icon-plus image-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">海报图，可选</div>
        </el-form-item>
        <el-form-item label="详情图">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="detailFileList"
            :on-success="(res) => handleDetailImageSuccess(res)"
            :before-upload="beforeImageUpload"
            :on-remove="handleDetailImageRemove"
            :data="uploadData('detail')"
          >
            <i class="el-icon-plus"></i>
          </el-upload>
          <div class="upload-tip">详情图，可上传多张</div>
        </el-form-item>
        <el-form-item label="活动说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动说明" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 绑定商品弹窗 -->
    <el-dialog title="绑定商品" :visible.sync="productDialogVisible" width="900px" append-to-body :close-on-click-modal="false">
      <el-row :gutter="16">
        <!-- 已绑定商品 -->
        <el-col :span="12">
          <div class="panel-title">已绑定商品</div>
          <el-table :data="boundProducts" size="small" max-height="400" border>
            <el-table-column label="ID" prop="id" width="60" align="center" />
            <el-table-column label="商品名称" prop="name" show-overflow-tooltip />
            <el-table-column label="价格" width="80" align="center">
              <template slot-scope="scope">¥{{ scope.row.price }}</template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template slot-scope="scope">
                <el-button size="mini" type="text" style="color: #F56C6C;" @click="handleUnbindProduct(scope.row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="boundProducts.length === 0" class="empty-tip">暂无绑定商品</div>
        </el-col>
        <!-- 可绑定商品 -->
        <el-col :span="12">
          <div class="panel-title">可绑定商品</div>
          <el-button size="mini" type="primary" icon="el-icon-refresh" @click="loadAvailableProducts" style="margin-bottom: 8px;">刷新</el-button>
          <el-table :data="availableProducts" size="small" max-height="400" @selection-change="handleProductSelectionChange" border>
            <el-table-column type="selection" width="40" align="center" />
            <el-table-column label="ID" prop="id" width="60" align="center" />
            <el-table-column label="商品名称" prop="name" show-overflow-tooltip />
            <el-table-column label="价格" width="80" align="center">
              <template slot-scope="scope">¥{{ scope.row.price }}</template>
            </el-table-column>
          </el-table>
          <div v-if="availableProducts.length === 0" class="empty-tip">暂无可用商品</div>
        </el-col>
      </el-row>
      <div slot="footer">
        <el-button @click="productDialogVisible = false">关 闭</el-button>
        <el-button type="primary" :disabled="selectedProducts.length === 0" @click="handleBatchBind">绑定选中商品</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listGroupon, getGroupon, addGroupon, updateGroupon, deleteGroupon, changeGrouponStatus, listGrouponProducts, bindGrouponProducts, unbindGrouponProducts, listProduct } from '@/api/marketing/groupon'
import { getToken } from '@/utils/auth'

export default {
  name: 'GrouponActivity',
  data() {
    return {
      // 遮罩
      loading: false,
      submitLoading: false,
      // 选中项
      ids: [],
      multiple: true,
      // 总条数
      total: 0,
      // 团购活动列表
      grouponList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        merchantId: null,
        name: null,
        status: null,
        sourceType: null
      },
      // 弹窗
      dialogVisible: false,
      dialogTitle: '',
      form: {},
      formRules: {
        merchantId: [{ required: true, message: '请输入商家ID', trigger: 'blur' }],
        name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
        coverImage: [{ required: true, message: '请上传活动封面', trigger: 'change' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
      },
      // 上传
      uploadUrl: process.env.VUE_APP_BASE_API + '/mall/groupon/image/upload',
      // 详情图文件列表
      detailFileList: [],
      // 绑定商品
      productDialogVisible: false,
      currentGroupon: null,
      boundProducts: [],
      availableProducts: [],
      selectedProducts: []
    }
  },
  computed: {
    uploadHeaders() {
      return { Authorization: 'Bearer ' + getToken() }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 获取列表 */
    getList() {
      this.loading = true
      listGroupon(this.queryParams).then(res => {
        this.grouponList = res.rows || []
        this.total = res.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    /** 查询 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置 */
    resetQuery() {
      this.$refs.queryForm.resetFields()
      this.queryParams.status = null
      this.queryParams.sourceType = null
      this.handleQuery()
    },
    /** 多选 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.multiple = !selection.length
    },
    /** 上传参数 */
    uploadData(imageType) {
      return {
        merchantId: this.form.merchantId || '',
        grouponId: this.form.id || '',
        tempToken: this.form.id ? '' : ('temp_' + Date.now()),
        imageType: imageType
      }
    },
    /** 新增 */
    handleAdd() {
      this.dialogTitle = '新增团购活动'
      this.detailFileList = []
      this.form = {
        merchantId: null,
        name: '',
        coverImage: '',
        posterImage: '',
        detailImages: '[]',
        description: '',
        startTime: null,
        endTime: null,
        limitPerUser: 0,
        sort: 0,
        status: 0
      }
      this.dialogVisible = true
    },
    /** 编辑 */
    handleEdit(row) {
      this.dialogTitle = '编辑团购活动'
      getGroupon(row.id).then(res => {
        this.form = { ...res.data }
        if (!this.form.detailImages) this.form.detailImages = '[]'
        this.detailFileList = JSON.parse(this.form.detailImages).map(url => ({ url, name: url.split('/').pop() }))
        this.dialogVisible = true
      })
    },
    /** 提交表单 */
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitLoading = true
        const data = { ...this.form }
        // 校验时间
        if (data.startTime && data.endTime && data.startTime >= data.endTime) {
          this.$message.error('开始时间必须早于结束时间')
          this.submitLoading = false
          return
        }
        const action = data.id ? updateGroupon : addGroupon
        action(data).then(() => {
          this.$message.success(data.id ? '修改成功' : '新增成功')
          this.dialogVisible = false
          this.getList()
        }).finally(() => {
          this.submitLoading = false
        })
      })
    },
    /** 上下架 */
    handleToggleStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const text = newStatus === 1 ? '上架' : '下架'
      this.$confirm('确认' + text + '该活动？', '提示', { type: 'warning' }).then(() => {
        changeGrouponStatus({ id: row.id, status: newStatus }).then(() => {
          this.$message.success(text + '成功')
          this.getList()
        })
      }).catch(() => {})
    },
    /** 删除 */
    handleDelete(row) {
      const ids = row.id ? [row.id] : this.ids
      this.$confirm('确认删除选中的活动？删除后不可恢复', '警告', { type: 'warning' }).then(() => {
        return deleteGroupon(ids.join(','))
      }).then(() => {
        this.getList()
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    /** 图片上传 */
    handleImageSuccess(res, type) {
      if (res.code === 200) {
        if (type === 'cover') {
          this.form.coverImage = res.data.url
        } else if (type === 'poster') {
          this.form.posterImage = res.data.url
        }
        this.$forceUpdate()
      } else {
        this.$message.error(res.msg || '上传失败')
      }
    },
    handleDetailImageSuccess(res) {
      if (res.code === 200) {
        const list = JSON.parse(this.form.detailImages || '[]')
        list.push(res.data.url)
        this.form.detailImages = JSON.stringify(list)
      } else {
        this.$message.error(res.msg || '上传失败')
      }
    },
    handleDetailImageRemove(file) {
      const list = JSON.parse(this.form.detailImages || '[]')
      const idx = list.indexOf(file.url)
      if (idx > -1) {
        list.splice(idx, 1)
        this.form.detailImages = JSON.stringify(list)
      }
    },
    beforeImageUpload(file) {
      const isImage = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isImage) this.$message.error('仅支持 jpg/png/webp 格式')
      if (!isLt5M) this.$message.error('文件大小不能超过5MB')
      return isImage && isLt5M
    },
    /** 绑定商品 */
    handleBindProduct(row) {
      this.currentGroupon = row
      this.productDialogVisible = true
      this.loadBoundProducts()
      this.loadAvailableProducts()
    },
    loadBoundProducts() {
      listGrouponProducts(this.currentGroupon.id).then(res => {
        this.boundProducts = res.data || []
      })
    },
    loadAvailableProducts() {
      listProduct({ merchantId: this.currentGroupon.merchantId, pageSize: 100 }).then(res => {
        const boundIds = this.boundProducts.map(p => p.id)
        this.availableProducts = (res.rows || []).filter(p => !boundIds.includes(p.id) && p.grouponId == null)
      })
    },
    handleProductSelectionChange(selection) {
      this.selectedProducts = selection
    },
    handleBatchBind() {
      const ids = this.selectedProducts.map(p => p.id)
      bindGrouponProducts({ grouponId: this.currentGroupon.id, productIds: ids }).then(() => {
        this.$message.success('绑定成功')
        this.loadBoundProducts()
        this.loadAvailableProducts()
      })
    },
    handleUnbindProduct(row) {
      this.$confirm('确认移除该商品？', '提示', { type: 'warning' }).then(() => {
        unbindGrouponProducts({ productIds: [row.id] }).then(() => {
          this.$message.success('移除成功')
          this.loadBoundProducts()
          this.loadAvailableProducts()
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.image-uploader {
  display: inline-block;
}
.image-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.image-uploader .el-upload:hover {
  border-color: #409EFF;
}
.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
}
.image-preview {
  width: 120px;
  height: 120px;
  display: block;
  border-radius: 6px;
  object-fit: cover;
}
.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
.panel-title {
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 14px;
}
.empty-tip {
  color: #909399;
  font-size: 13px;
  text-align: center;
  padding: 20px 0;
}
.mb8 {
  margin-bottom: 8px;
}
</style>
