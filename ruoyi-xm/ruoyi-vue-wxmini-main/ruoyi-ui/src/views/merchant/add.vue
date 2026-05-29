<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>添加商户</span></div>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" style="max-width: 700px; margin: 0 auto;">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商家名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商家名称" />
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
            <el-form-item label="所属分销商">
              <el-select v-if="isPlatform" v-model="form.distributorId" placeholder="请选择分销商（可选）" clearable filterable style="width: 100%;">
                <el-option label="无（平台直属）" :value="null" />
                <el-option v-for="d in distributorOptions" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
              <el-input v-else :value="currentDistributorName" disabled />
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
            <el-form-item label="开户银行" prop="bankName">
              <el-input v-model="form.bankName" placeholder="请输入开户银行" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="银行账号" prop="bankAccount">
              <el-input v-model="form.bankAccount" placeholder="请输入银行账号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商家地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入商家地址" />
        </el-form-item>
        <el-form-item label="商家简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商家简介" />
        </el-form-item>
        <el-divider content-position="left">小程序配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="C端AppID">
              <el-input v-model="form.cMiniAppId" placeholder="C端小程序AppID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="C端Secret">
              <el-input v-model="form.cMiniAppSecret" placeholder="C端小程序Secret" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商家端AppID">
              <el-input v-model="form.mMiniAppId" placeholder="商家端小程序AppID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商家端Secret">
              <el-input v-model="form.mMiniAppSecret" placeholder="商家端小程序Secret" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">支付配置（预留）</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="微信商户号">
              <el-input v-model="form.wxPayMchId" placeholder="微信支付商户号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付API密钥">
              <el-input v-model="form.wxPayApiKey" placeholder="微信支付API密钥" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { addMerchant } from '@/api/merchant'
import { listDistributor } from '@/api/distributor'

export default {
  name: 'MerchantAdd',
  data() {
    return {
      loading: false,
      form: {
        name: '',
        contact: '',
        phone: '',
        distributorId: null,
        bankName: '',
        bankAccount: '',
        address: '',
        description: '',
        cMiniAppId: '',
        cMiniAppSecret: '',
        mMiniAppId: '',
        mMiniAppSecret: '',
        wxPayMchId: '',
        wxPayApiKey: ''
      },
      rules: {
        name: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],
        contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      },
      distributorOptions: []
    }
  },
  computed: {
    isPlatform() {
      return this.$store.state.user.accountType !== 'DISTRIBUTOR'
    },
    currentDistributorName() {
      return this.$store.state.user.name || '当前分销商'
    }
  },
  created() {
    if (this.isPlatform) {
      this.loadDistributors()
    }
  },
  methods: {
    async loadDistributors() {
      try {
        const res = await listDistributor({ pageSize: 500 })
        this.distributorOptions = res.rows || []
      } catch (e) {
        this.distributorOptions = []
      }
    },
    handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        this.loading = true
        try {
          await addMerchant(this.form)
          this.$message.success('添加成功')
          this.$router.push({ path: '/merchant/list' })
        } catch (e) {
          this.$message.error('添加失败')
        } finally {
          this.loading = false
        }
      })
    },
    handleCancel() {
      this.$router.push({ path: '/merchant/list' })
    }
  }
}
</script>
