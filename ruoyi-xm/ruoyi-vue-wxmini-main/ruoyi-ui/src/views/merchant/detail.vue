<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="card-header">
        <span>商家详情</span>
        <el-button size="small" icon="el-icon-back" @click="handleBack">返回</el-button>
      </div>

      <div v-loading="loading">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="商家名称">{{ merchant.name }}</el-descriptions-item>
          <el-descriptions-item label="所属分销商">{{ merchant.distributorName || '无（平台直属）' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ merchant.contact }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ merchant.phone }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(merchant.status)" size="small">{{ statusText(merchant.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="入驻时间">{{ merchant.createTime }}</el-descriptions-item>
          <el-descriptions-item label="商品数">{{ merchant.products }}</el-descriptions-item>
          <el-descriptions-item label="月销售额">¥{{ (merchant.monthlySales || 0).toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="商家ID">{{ merchant.id }}</el-descriptions-item>
        </el-descriptions>

        <!-- 标签页 -->
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- Tab 1: 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border style="margin-top: 10px;">
              <el-descriptions-item label="商家ID">{{ merchant.id }}</el-descriptions-item>
              <el-descriptions-item label="商家名称">{{ merchant.name }}</el-descriptions-item>
              <el-descriptions-item label="所属分销商">{{ merchant.distributorName || '无（平台直属）' }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ merchant.contact }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ merchant.phone }}</el-descriptions-item>
              <el-descriptions-item label="入驻时间">{{ merchant.createTime }}</el-descriptions-item>
              <el-descriptions-item label="佣金比例">{{ merchant.commissionRate }}%</el-descriptions-item>
            </el-descriptions>

            <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center;">
              <h3 style="margin: 0;">小程序配置</h3>
              <el-button type="primary" icon="el-icon-edit" size="small" @click="handleEditMiniApp">编辑配置</el-button>
            </div>
            <el-descriptions :column="2" border style="margin-top: 10px;">
              <el-descriptions-item label="C端 AppID">{{ merchant.cMiniAppId || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="C端 Secret">{{ merchant.cMiniAppSecretConfigured ? '******' : '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="商家端 AppID">{{ merchant.mMiniAppId || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="商家端 Secret">{{ merchant.mMiniAppSecretConfigured ? '******' : '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="微信商户号">{{ merchant.wxPayMchId || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="支付API密钥">{{ merchant.wxPayApiKeyConfigured ? '******' : '未配置' }}</el-descriptions-item>
            </el-descriptions>

            <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center;">
              <h3 style="margin: 0;">腾讯地图认领</h3>
              <el-button type="primary" icon="el-icon-edit" size="small" @click="handleEditMapClaim">编辑配置</el-button>
            </div>
            <el-descriptions :column="2" border style="margin-top: 10px;">
              <el-descriptions-item label="认领状态">
                <el-tag :type="mapClaimStatusType(merchant.mapClaimStatus)" size="small">{{ mapClaimStatusText(merchant.mapClaimStatus) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="POI ID">{{ merchant.mapPoiId || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="认领链接">{{ merchant.mapClaimUrl || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="认领时间">{{ merchant.mapClaimTime || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ merchant.mapClaimRemark || '无' }}</el-descriptions-item>
            </el-descriptions>

            <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center;">
              <h3 style="margin: 0;">微信支付与三方分账</h3>
              <el-button type="primary" icon="el-icon-edit" size="small" @click="handleEditWxApplyment">编辑配置</el-button>
            </div>
            <el-descriptions :column="2" border style="margin-top: 10px;">
              <el-descriptions-item label="接入方式">{{ wxPaymentAccessTypeText(merchant.wxPaymentAccessType) }}</el-descriptions-item>
              <el-descriptions-item label="商家商户号">{{ merchant.effectiveMerchantWxMchId || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="商户名称">{{ merchant.merchantWxMchName || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="分账开关">
                <el-tag :type="merchant.wxProfitSharingEnabled === 1 ? 'success' : 'info'" size="small">{{ merchant.wxProfitSharingEnabled === 1 ? '已开启' : '未开启' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="商家留存">{{ percentText(merchant.merchantShareRate) }}</el-descriptions-item>
              <el-descriptions-item label="平台分账">{{ percentText(merchant.platformShareRate) }}</el-descriptions-item>
              <el-descriptions-item label="分销商分账">{{ percentText(merchant.distributorShareRate) }}</el-descriptions-item>
              <el-descriptions-item label="到账周期">{{ merchant.settlementCycle || 'T1' }}</el-descriptions-item>
              <el-descriptions-item label="平台接收方">{{ merchant.platformReceiverMchId || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="分销商接收方">{{ merchant.distributorReceiverMchId || '未配置' }}</el-descriptions-item>
              <el-descriptions-item label="运营准入" :span="2">
                <el-tag :type="merchant.canOperate ? 'success' : 'warning'" size="small">{{ merchant.canOperate ? '可运营' : (merchant.operateBlockReason || '配置未完成') }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <!-- Tab 2: 商品列表 -->
          <el-tab-pane label="商品列表" name="products">
            <div class="search-bar">
              <el-input v-model="productQuery.name" placeholder="商品名称" size="small" clearable style="width: 200px; margin-right: 10px;" @keyup.enter.native="loadProducts" />
              <el-select v-model="productQuery.status" placeholder="全部状态" size="small" clearable style="width: 120px; margin-right: 10px;">
                <el-option label="上架" :value="1" />
                <el-option label="下架" :value="0" />
              </el-select>
              <el-button type="primary" icon="el-icon-search" size="small" @click="loadProducts">搜索</el-button>
              <el-button type="success" icon="el-icon-plus" size="small" @click="handleAddProduct" style="margin-left: auto;">新增商品</el-button>
            </div>

            <el-table v-loading="productLoading" :data="productList" border size="small">
              <el-table-column label="商品名称" prop="name" min-width="180" show-overflow-tooltip />
              <el-table-column label="分类" prop="categoryId" width="80" />
              <el-table-column label="原价" width="90">
                <template slot-scope="scope">¥{{ scope.row.originalPrice.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="现价" width="90">
                <template slot-scope="scope">
                  <span class="text-danger">¥{{ scope.row.price.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="库存" prop="stock" width="70" align="center" />
              <el-table-column label="销量" prop="sales" width="70" align="center" />
              <el-table-column label="状态" width="80" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">{{ scope.row.status === 1 ? '上架' : '下架' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" align="center">
                <template slot-scope="scope">
                  <el-button type="text" size="mini" @click="handleEditProduct(scope.row)">编辑</el-button>
                  <el-button type="text" size="mini" @click="handleToggleStatus(scope.row)">{{ scope.row.status === 1 ? '下架' : '上架' }}</el-button>
                  <el-button type="text" size="mini" class="text-danger" @click="handleDeleteProduct(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              class="pagination"
              :current-page="productQuery.pageNum"
              :page-sizes="[10, 20]"
              :page-size="productQuery.pageSize"
              :total="productTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="handleProductSizeChange"
              @current-change="handleProductPageChange"
            />
          </el-tab-pane>

          <!-- Tab 3: 团购活动 -->
          <el-tab-pane label="团购活动" name="groupon">
            <div class="search-bar">
              <el-input v-model="grouponQuery.name" placeholder="活动名称" size="small" clearable style="width: 200px; margin-right: 10px;" @keyup.enter.native="loadGroupons" />
              <el-select v-model="grouponQuery.status" placeholder="全部状态" size="small" clearable style="width: 120px; margin-right: 10px;">
                <el-option label="未启用" :value="0" />
                <el-option label="进行中" :value="1" />
                <el-option label="已结束" :value="2" />
              </el-select>
              <el-button type="primary" icon="el-icon-search" size="small" @click="loadGroupons">搜索</el-button>
              <el-button type="success" icon="el-icon-plus" size="small" @click="handleAddGroupon" style="margin-left: auto;">新增团购</el-button>
            </div>

            <el-table v-loading="grouponLoading" :data="grouponList" border size="small">
              <el-table-column label="ID" prop="id" width="60" align="center" />
              <el-table-column label="封面" width="80" align="center">
                <template slot-scope="scope">
                  <el-image v-if="scope.row.coverImage" :src="scope.row.coverImage" style="width: 50px; height: 50px;" fit="cover" :preview-src-list="[scope.row.coverImage]" />
                  <span v-else style="color: #ccc;">无</span>
                </template>
              </el-table-column>
              <el-table-column label="活动名称" prop="name" min-width="130" show-overflow-tooltip />
              <el-table-column label="活动时间" width="260" align="center">
                <template slot-scope="scope">
                  {{ parseTime(scope.row.startTime, '{y}-{m}-{d} {h}:{i}') }} ~ {{ parseTime(scope.row.endTime, '{y}-{m}-{d} {h}:{i}') }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80" align="center">
                <template slot-scope="scope">
                  <el-tag v-if="scope.row.status === 1" type="success" size="mini">进行中</el-tag>
                  <el-tag v-else-if="scope.row.status === 2" type="info" size="mini">已结束</el-tag>
                  <el-tag v-else type="warning" size="mini">未启用</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="已售" prop="totalSold" width="60" align="center" />
              <el-table-column label="限购" width="70" align="center">
                <template slot-scope="scope">{{ scope.row.limitPerUser > 0 ? scope.row.limitPerUser + '/人' : '不限' }}</template>
              </el-table-column>
              <el-table-column label="排序" prop="sort" width="55" align="center" />
              <el-table-column label="操作" width="260" align="center">
                <template slot-scope="scope">
                  <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEditGroupon(scope.row)">编辑</el-button>
                  <el-button size="mini" type="text" :icon="scope.row.status === 1 ? 'el-icon-bottom' : 'el-icon-top'" @click="handleToggleGrouponStatus(scope.row)">{{ scope.row.status === 1 ? '下架' : '上架' }}</el-button>
                  <el-button size="mini" type="text" icon="el-icon-goods" @click="handleManageItems(scope.row)">商品</el-button>
                  <el-button size="mini" type="text" icon="el-icon-delete" style="color: #F56C6C;" @click="handleDeleteGroupon(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              class="pagination"
              :current-page="grouponQuery.pageNum"
              :page-sizes="[10, 20]"
              :page-size="grouponQuery.pageSize"
              :total="grouponTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="handleGrouponSizeChange"
              @current-change="handleGrouponPageChange"
            />
          </el-tab-pane>

          <!-- Tab 4: 订单记录 -->
          <el-tab-pane label="订单记录" name="orders">
            <el-table v-loading="orderLoading" :data="orderList" border size="small">
              <el-table-column label="订单号" prop="orderNo" width="180" />
              <el-table-column label="用户" prop="userId" width="100" />
              <el-table-column label="金额" width="100">
                <template slot-scope="scope">¥{{ scope.row.payAmount.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90" align="center">
                <template slot-scope="scope">
                  <el-tag :type="orderStatusMap[scope.row.status].type" size="small">{{ orderStatusMap[scope.row.status].text }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="下单时间" prop="createTime" width="160" />
            </el-table>

            <el-pagination
              class="pagination"
              :current-page="orderQuery.pageNum"
              :page-sizes="[10, 20]"
              :page-size="orderQuery.pageSize"
              :total="orderTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="handleOrderSizeChange"
              @current-change="handleOrderPageChange"
            />
          </el-tab-pane>

          <!-- Tab 5: 流水记录 -->
          <el-tab-pane label="流水记录" name="flow">
            <el-table v-loading="flowLoading" :data="flowList" border size="small">
              <el-table-column label="类型" prop="type" width="100" />
              <el-table-column label="金额" width="120">
                <template slot-scope="scope">
                  <span :class="scope.row.totalAmount >= 0 ? 'text-success' : 'text-danger'">
                    {{ scope.row.totalAmount >= 0 ? '+' : '' }}¥{{ scope.row.totalAmount.toFixed(2) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="关联订单" prop="orderNo" width="160" />
              <el-table-column label="时间" prop="createTime" width="160" />
            </el-table>

            <el-pagination
              class="pagination"
              :current-page="flowQuery.pageNum"
              :page-sizes="[10, 20]"
              :page-size="flowQuery.pageSize"
              :total="flowTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="handleFlowSizeChange"
              @current-change="handleFlowPageChange"
            />
          </el-tab-pane>

          <!-- Tab 6: 账户管理 -->
          <el-tab-pane label="账户管理" name="accounts">
            <div class="search-bar">
              <el-button type="success" icon="el-icon-plus" size="small" @click="handleAddAccount">新增账户</el-button>
            </div>

            <el-table v-loading="accountLoading" :data="accountList" border size="small">
              <el-table-column label="用户名" prop="username" width="150" />
              <el-table-column label="姓名" prop="realName" width="120" />
              <el-table-column label="手机号" prop="phone" width="130" />
              <el-table-column label="角色" width="100" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.role === 'owner' ? 'warning' : 'primary'" size="mini">
                    {{ scope.row.role === 'owner' ? '管理员' : '成员' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">{{ scope.row.status === 1 ? '正常' : '禁用' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="最后登录" prop="lastLoginTime" width="160" />
              <el-table-column label="操作" width="200" align="center">
                <template slot-scope="scope">
                  <el-button type="text" size="mini" @click="handleResetPwd(scope.row)">重置密码</el-button>
                  <el-button type="text" size="mini" @click="handleToggleAccountStatus(scope.row)">{{ scope.row.status === 1 ? '禁用' : '启用' }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>

    <!-- 新增/编辑商品弹窗 -->
    <el-dialog :title="productDialogTitle" :visible.sync="productDialogVisible" width="600px" append-to-body>
      <el-form ref="productForm" :model="productForm" :rules="productRules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-input v-model="productForm.categoryId" placeholder="请输入分类" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原价" prop="originalPrice">
              <el-input-number v-model="productForm.originalPrice" :min="0" :precision="2" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="现价" prop="price">
              <el-input-number v-model="productForm.price" :min="0" :precision="2" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="productForm.stock" :min="0" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期(天)" prop="validDays">
              <el-input-number v-model="productForm.validDays" :min="1" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品主图" prop="mainImage">
          <el-upload
            class="image-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
            :data="uploadData"
          >
            <img v-if="productForm.mainImage || productForm.coverImage" :src="productForm.mainImage || productForm.coverImage" class="image-preview">
            <i v-else class="el-icon-plus image-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">支持 jpg/jpeg/png/webp 格式，单张不超过5MB</div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="productDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitProductForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 新增账户弹窗 -->
    <el-dialog title="新增账户" :visible.sync="accountDialogVisible" width="500px" append-to-body>
      <el-form ref="accountForm" :model="accountForm" :rules="accountRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="accountForm.username" placeholder="请输入登录用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="accountForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="accountForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="accountForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="accountForm.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="管理员" value="owner" />
            <el-option label="成员" value="member" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="accountDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitAccountForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 编辑小程序配置弹窗 -->
    <el-dialog title="编辑小程序配置" :visible.sync="miniAppDialogVisible" width="600px" append-to-body>
      <el-form ref="miniAppForm" :model="miniAppForm" label-width="120px">
        <el-divider content-position="left">C端小程序（用户下单）</el-divider>
        <el-form-item label="C端 AppID">
          <el-input v-model="miniAppForm.cMiniAppId" placeholder="wx开头的AppID" />
        </el-form-item>
        <el-form-item label="C端 Secret">
          <el-input v-model="miniAppForm.cMiniAppSecret" placeholder="请输入Secret" />
        </el-form-item>
        <el-divider content-position="left">商家端小程序（管理/核销）</el-divider>
        <el-form-item label="商家端 AppID">
          <el-input v-model="miniAppForm.mMiniAppId" placeholder="wx开头的AppID" />
        </el-form-item>
        <el-form-item label="商家端 Secret">
          <el-input v-model="miniAppForm.mMiniAppSecret" placeholder="请输入Secret" />
        </el-form-item>
        <el-divider content-position="left">微信支付</el-divider>
        <el-form-item label="商户号">
          <el-input v-model="miniAppForm.wxPayMchId" placeholder="微信支付商户号" />
        </el-form-item>
        <el-form-item label="API密钥">
          <el-input v-model="miniAppForm.wxPayApiKey" placeholder="微信支付API密钥" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="miniAppDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitMiniAppForm">保 存</el-button>
      </div>
    </el-dialog>

    <!-- 编辑腾讯地图认领配置弹窗 -->
    <el-dialog title="编辑腾讯地图认领配置" :visible.sync="mapClaimDialogVisible" width="500px" append-to-body>
      <el-form ref="mapClaimForm" :model="mapClaimForm" label-width="100px">
        <el-form-item label="认领状态">
          <el-select v-model="mapClaimForm.mapClaimStatus" placeholder="请选择状态" style="width: 100%;">
            <el-option label="未认领" value="NOT_CLAIMED" />
            <el-option label="认领中" value="CLAIMING" />
            <el-option label="已认领" value="CLAIMED" />
            <el-option label="认领失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="POI ID">
          <el-input v-model="mapClaimForm.mapPoiId" placeholder="腾讯地图POI ID" />
        </el-form-item>
        <el-form-item label="认领链接">
          <el-input v-model="mapClaimForm.mapClaimUrl" placeholder="腾讯地图认领或门店链接" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="mapClaimForm.mapClaimRemark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="mapClaimDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitMapClaimForm">保 存</el-button>
      </div>
    </el-dialog>

    <!-- 编辑微信支付与三方分账配置弹窗 -->
    <el-dialog title="编辑微信支付与三方分账配置" :visible.sync="wxApplymentDialogVisible" width="680px" append-to-body>
      <el-form ref="wxApplymentForm" :model="wxApplymentForm" label-width="140px">
        <el-divider content-position="left">商家支付账号</el-divider>
        <el-form-item label="接入方式">
          <el-radio-group v-model="wxApplymentForm.wxPaymentAccessType">
            <el-radio label="EXISTING_MCH">已有微信支付商户号</el-radio>
            <el-radio label="APPLYMENT_ASSISTED">平台协助申请</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商家商户号">
          <el-input v-model="wxApplymentForm.merchantWxMchId" placeholder="商家自己的微信支付商户号" />
        </el-form-item>
        <el-form-item label="商户名称">
          <el-input v-model="wxApplymentForm.merchantWxMchName" placeholder="商家微信支付商户名称" />
        </el-form-item>
        <template v-if="wxApplymentForm.wxPaymentAccessType === 'APPLYMENT_ASSISTED'">
          <el-divider content-position="left">协助申请信息</el-divider>
          <el-form-item label="申请单号">
            <el-input v-model="wxApplymentForm.wxApplymentId" placeholder="平台协助申请时填写，已有商户号可不填" />
          </el-form-item>
          <el-form-item label="申请状态">
            <el-select v-model="wxApplymentForm.wxApplymentState" placeholder="请选择状态" style="width: 100%;">
              <el-option label="未提交" value="NOT_SUBMITTED" />
              <el-option label="已提交" value="SUBMITTED" />
              <el-option label="审核中" value="AUDITING" />
              <el-option label="待账户验证" value="NEED_VERIFY" />
              <el-option label="待签约" value="NEED_SIGN" />
              <el-option label="已完成" value="FINISHED" />
              <el-option label="已驳回" value="REJECTED" />
              <el-option label="已冻结" value="FROZEN" />
            </el-select>
          </el-form-item>
          <el-form-item label="驳回原因">
            <el-input v-model="wxApplymentForm.wxApplymentRejectReason" type="textarea" :rows="2" placeholder="平台协助申请失败时填写" />
          </el-form-item>
        </template>
        <el-divider content-position="left">微信分账配置</el-divider>
        <el-form-item label="启用分账">
          <el-switch v-model="wxApplymentForm.wxProfitSharingEnabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="商家留存比例">
          <el-input-number v-model="wxApplymentForm.merchantShareRate" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="平台分账比例">
          <el-input-number v-model="wxApplymentForm.platformShareRate" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="分销商分账比例">
          <el-input-number v-model="wxApplymentForm.distributorShareRate" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%;" />
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">三方比例合计必须等于100%，商家比例代表留存在商家微信支付账户的部分。</div>
        </el-form-item>
        <el-form-item label="平台接收方">
          <el-input v-model="wxApplymentForm.platformReceiverMchId" placeholder="平台作为分账接收方的微信商户号" />
        </el-form-item>
        <el-form-item label="分销商接收方">
          <el-input v-model="wxApplymentForm.distributorReceiverMchId" placeholder="分销商作为分账接收方的微信商户号" />
        </el-form-item>
        <el-form-item label="到账周期">
          <el-select v-model="wxApplymentForm.settlementCycle" style="width: 100%;">
            <el-option label="T+1" value="T1" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="wxApplymentDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitWxApplymentForm">保 存</el-button>
      </div>
    </el-dialog>

    <!-- 团购新增/编辑弹窗 -->
    <el-dialog :title="grouponDialogTitle" :visible.sync="grouponDialogVisible" width="700px" append-to-body :close-on-click-modal="false">
      <el-form ref="grouponForm" :model="grouponForm" :rules="grouponRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动名称" prop="name">
              <el-input v-model="grouponForm.name" placeholder="请输入活动名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="grouponForm.sort" :min="0" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker v-model="grouponForm.startTime" type="datetime" placeholder="选择开始时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker v-model="grouponForm.endTime" type="datetime" placeholder="选择结束时间" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="每人限购" prop="limitPerUser">
              <el-input-number v-model="grouponForm.limitPerUser" :min="0" :controls="false" style="width: 100%;" placeholder="0不限" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="grouponForm.status">
                <el-radio :label="0">未启用</el-radio>
                <el-radio :label="1">进行中</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动封面" prop="coverImage">
          <el-upload
            class="image-uploader"
            :action="grouponUploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="(res) => handleGrouponImageSuccess(res, 'cover')"
            :before-upload="beforeImageUpload"
            :data="grouponUploadData('cover')"
          >
            <img v-if="grouponForm.coverImage" :src="grouponForm.coverImage" class="image-preview">
            <i v-else class="el-icon-plus image-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">封面图，支持 jpg/jpeg/png/webp，不超过5MB</div>
        </el-form-item>
        <el-form-item label="活动海报">
          <el-upload
            class="image-uploader"
            :action="grouponUploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="(res) => handleGrouponImageSuccess(res, 'poster')"
            :before-upload="beforeImageUpload"
            :data="grouponUploadData('poster')"
          >
            <img v-if="grouponForm.posterImage" :src="grouponForm.posterImage" class="image-preview">
            <i v-else class="el-icon-plus image-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">海报图，可选</div>
        </el-form-item>
        <el-form-item label="详情图">
          <el-upload
            :action="grouponUploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="detailFileList"
            :on-success="handleDetailImageSuccess"
            :before-upload="beforeImageUpload"
            :on-remove="handleDetailImageRemove"
            :data="grouponUploadData('detail')"
          >
            <i class="el-icon-plus"></i>
          </el-upload>
          <div class="upload-tip">详情图，可上传多张</div>
        </el-form-item>
        <el-form-item label="活动说明">
          <el-input v-model="grouponForm.description" type="textarea" :rows="3" placeholder="请输入活动说明" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="grouponDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="grouponSubmitLoading" @click="submitGrouponForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 团购商品列表弹窗 -->
    <el-dialog title="团购商品管理" :visible.sync="itemsDialogVisible" width="95%" style="max-width: 1100px;" append-to-body :close-on-click-modal="false">
      <div class="search-bar">
        <span style="margin-right: 12px; color: #606266;">活动：{{ currentGroupon ? currentGroupon.name : '' }}</span>
        <el-button type="success" icon="el-icon-plus" size="small" @click="handleAddItem" style="margin-left: auto;">新增团购商品</el-button>
      </div>
      <el-table v-loading="itemLoading" :data="itemList" border size="small">
        <el-table-column label="ID" prop="id" width="55" align="center" />
        <el-table-column label="封面" width="70" align="center">
          <template slot-scope="scope">
            <el-image v-if="scope.row.coverImage" :src="scope.row.coverImage" style="width: 45px; height: 45px;" fit="cover" :preview-src-list="[scope.row.coverImage]" />
            <span v-else style="color: #ccc;">无</span>
          </template>
        </el-table-column>
        <el-table-column label="商品名称" prop="name" min-width="120" show-overflow-tooltip />
        <el-table-column label="套餐内容" prop="content" min-width="150" show-overflow-tooltip />
        <el-table-column label="原价" width="80" align="center">
          <template slot-scope="scope">{{ (scope.row.originalPrice / 100).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="团购价" width="80" align="center">
          <template slot-scope="scope"><span class="text-danger">{{ (scope.row.grouponPrice / 100).toFixed(2) }}</span></template>
        </el-table-column>
        <el-table-column label="折扣" width="65" align="center">
          <template slot-scope="scope">{{ scope.row.discountRate ? scope.row.discountRate.toFixed(1) + '折' : '-' }}</template>
        </el-table-column>
        <el-table-column label="库存" prop="stock" width="55" align="center" />
        <el-table-column label="已售" prop="sales" width="55" align="center" />
        <el-table-column label="状态" width="70" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">{{ scope.row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEditItem(scope.row)">编辑</el-button>
            <el-button size="mini" type="text" :icon="scope.row.status === 1 ? 'el-icon-bottom' : 'el-icon-top'" @click="handleToggleItemStatus(scope.row)">{{ scope.row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color: #F56C6C;" @click="handleDeleteItem(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="itemList.length === 0" class="empty-tip">暂无团购商品，点击上方"新增团购商品"添加</div>
      <div slot="footer">
        <el-button @click="itemsDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 团购商品新增/编辑弹窗 -->
    <el-dialog :title="itemDialogTitle" :visible.sync="itemDialogVisible" width="750px" append-to-body :close-on-click-modal="false">
      <el-form ref="itemForm" :model="itemForm" :rules="itemRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="itemForm.name" placeholder="请输入团购商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="展示标题" prop="title">
              <el-input v-model="itemForm.title" placeholder="可与名称一致" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="套餐内容" prop="content">
          <el-input v-model="itemForm.content" type="textarea" :rows="2" placeholder="请输入套餐内容/服务内容" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="原价(元)" prop="originalPriceYuan">
              <el-input-number v-model="itemForm.originalPriceYuan" :min="0.01" :precision="2" :controls="false" style="width: 100%;" placeholder="0.00" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="团购价(元)" prop="grouponPriceYuan">
              <el-input-number v-model="itemForm.grouponPriceYuan" :min="0.01" :precision="2" :controls="false" style="width: 100%;" placeholder="0.00" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="折扣" prop="discountRate">
              <el-input :value="computedDiscount" disabled style="width: 100%;">
                <template slot="append">折</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="itemForm.stock" :min="0" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每人限购" prop="limitPerUser">
              <el-input-number v-model="itemForm.limitPerUser" :min="0" :controls="false" style="width: 100%;" placeholder="0不限" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="有效期(天)" prop="validDays">
              <el-input-number v-model="itemForm.validDays" :min="1" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="itemForm.sort" :min="0" :controls="false" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="itemForm.status">
                <el-radio :label="0">下架</el-radio>
                <el-radio :label="1">上架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面图" prop="coverImage">
          <el-upload
            class="image-uploader"
            :action="itemUploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="(res) => handleItemImageSuccess(res, 'cover')"
            :before-upload="beforeImageUpload"
            :data="itemUploadData('cover')"
          >
            <img v-if="itemForm.coverImage" :src="itemForm.coverImage" class="image-preview">
            <i v-else class="el-icon-plus image-uploader-icon"></i>
          </el-upload>
          <div class="upload-tip">封面图，支持 jpg/jpeg/png/webp，不超过5MB</div>
        </el-form-item>
        <el-form-item label="详情图">
          <el-upload
            :action="itemUploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="itemDetailFileList"
            :on-success="handleItemDetailImageSuccess"
            :before-upload="beforeImageUpload"
            :on-remove="handleItemDetailImageRemove"
            :data="itemUploadData('detail')"
          >
            <i class="el-icon-plus"></i>
          </el-upload>
          <div class="upload-tip">详情图，可上传多张</div>
        </el-form-item>

        <!-- 菜品搭配 -->
        <el-divider content-position="left">菜品搭配</el-divider>
        <div class="dish-group-section">
          <div class="dish-group-toolbar">
            <el-button size="mini" type="primary" icon="el-icon-plus" @click="addDishGroup">添加菜品组</el-button>
            <el-switch v-model="itemForm.directTotalPrice" active-text="直接设置菜品总价" inactive-text="" style="margin-left: 16px;" />
            <el-input-number v-if="itemForm.directTotalPrice" v-model="itemForm.dishTotalPriceYuan" :min="0" :precision="2" :controls="false" size="small" style="width: 140px; margin-left: 10px;" placeholder="菜品总价" />
            <span v-if="itemForm.directTotalPrice" style="margin-left: 4px; color: #909399; font-size: 12px;">元</span>
            <span style="margin-left: auto; color: #909399; font-size: 12px;">共 {{ dishTotalCount }} 道菜品</span>
          </div>

          <div v-for="(group, gIdx) in itemForm.dishGroups" :key="gIdx" class="dish-group-card">
            <div class="dish-group-header">
              <el-input v-model="group.groupName" size="small" style="width: 160px;" placeholder="菜品组名称" maxlength="10" />
              <div style="margin-left: auto; display: flex; gap: 4px;">
                <el-button v-if="gIdx > 0" size="mini" type="text" icon="el-icon-top" @click="moveDishGroup(gIdx, -1)" />
                <el-button v-if="gIdx < itemForm.dishGroups.length - 1" size="mini" type="text" icon="el-icon-bottom" @click="moveDishGroup(gIdx, 1)" />
                <el-button size="mini" type="text" icon="el-icon-delete" style="color: #F56C6C;" @click="removeDishGroup(gIdx)" />
              </div>
            </div>

            <div v-for="(dish, dIdx) in group.items" :key="dIdx" class="dish-row">
              <el-input v-model="dish.dishName" size="small" style="width: 140px;" placeholder="菜品名称" />
              <el-input-number v-model="dish.quantity" size="small" :min="1" :controls="false" style="width: 60px;" />
              <el-select v-model="dish.unit" size="small" style="width: 65px;">
                <el-option label="份" value="份" />
                <el-option label="个" value="个" />
                <el-option label="杯" value="杯" />
                <el-option label="斤" value="斤" />
                <el-option label="盒" value="盒" />
                <el-option label="次" value="次" />
                <el-option label="项" value="项" />
              </el-select>
              <el-input-number v-model="dish.priceYuan" size="small" :min="0" :precision="2" :controls="false" style="width: 90px;" placeholder="单价" />
              <span style="color: #909399; font-size: 12px; margin-right: 4px;">元</span>
              <el-button size="mini" type="text" icon="el-icon-delete" style="color: #F56C6C;" @click="removeDish(gIdx, dIdx)" />
            </div>

            <div class="dish-group-actions">
              <el-button size="mini" type="text" icon="el-icon-plus" @click="addDish(gIdx)">添加</el-button>
            </div>
          </div>
        </div>

        <el-form-item label="商品说明">
          <el-input v-model="itemForm.description" type="textarea" :rows="2" placeholder="请输入商品说明" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="itemDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="itemSubmitLoading" @click="submitItemForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMerchantDetail, getProductList, addProduct, updateProduct, deleteProduct, getMerchantOrders, getMerchantFlowList, orderStatusMap, getMerchantUserList, addMerchantUser, resetMerchantUserPwd, changeMerchantUserStatus, updateMerchant } from '@/api/merchant'
import { listGroupon, getGroupon, addGroupon, updateGroupon, deleteGroupon, changeGrouponStatus, listGrouponItem, addGrouponItem, updateGrouponItem, deleteGrouponItem, changeGrouponItemStatus } from '@/api/marketing/groupon'
import { getToken } from '@/utils/auth'

export default {
  name: 'MerchantDetail',
  data() {
    return {
      loading: false,
      merchant: {},
      activeTab: 'products',
      merchantId: null,
      orderStatusMap: orderStatusMap,

      // 商品
      productLoading: false,
      productList: [],
      productTotal: 0,
      productQuery: { name: '', status: '', pageNum: 1, pageSize: 10 },
      productDialogVisible: false,
      productDialogTitle: '新增商品',
      productForm: { id: null, name: '', category: '', originalPrice: 0, price: 0, stock: 0, validDays: 30, mainImage: '', coverImage: '', description: '' },
      productRules: {
        name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
        price: [{ required: true, message: '请输入现价', trigger: 'blur' }]
      },

      // 订单
      orderLoading: false,
      orderList: [],
      orderTotal: 0,
      orderQuery: { pageNum: 1, pageSize: 10 },

      // 流水
      flowLoading: false,
      flowList: [],
      flowTotal: 0,
      flowQuery: { pageNum: 1, pageSize: 10 },

      // 账户管理
      accountLoading: false,
      accountList: [],
      accountDialogVisible: false,
      accountForm: { username: '', password: '', realName: '', phone: '', role: 'member' },
      accountRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
      },

      // 小程序配置
      miniAppDialogVisible: false,
      miniAppForm: { cMiniAppId: '', cMiniAppSecret: '', mMiniAppId: '', mMiniAppSecret: '', wxPayMchId: '', wxPayApiKey: '' },

      // 腾讯地图认领
      mapClaimDialogVisible: false,
      mapClaimForm: { mapClaimStatus: 'NOT_CLAIMED', mapPoiId: '', mapClaimUrl: '', mapClaimRemark: '' },

      // 微信特约商户
      wxApplymentDialogVisible: false,
      wxApplymentForm: {
        wxApplymentId: '', wxApplymentState: 'NOT_SUBMITTED', wxApplymentRejectReason: '',
        wxPaymentAccessType: 'EXISTING_MCH', merchantWxMchId: '', merchantWxMchName: '',
        wxProfitSharingEnabled: 0, platformReceiverMchId: '', distributorReceiverMchId: '',
        merchantShareRate: 100, platformShareRate: 0, distributorShareRate: 0, settlementCycle: 'T1'
      },

      // 团购活动
      grouponLoading: false,
      grouponList: [],
      grouponTotal: 0,
      grouponQuery: { name: '', status: '', pageNum: 1, pageSize: 10 },
      grouponDialogVisible: false,
      grouponDialogTitle: '新增团购活动',
      grouponSubmitLoading: false,
      grouponForm: { id: null, merchantId: null, name: '', coverImage: '', posterImage: '', detailImages: '[]', description: '', startTime: null, endTime: null, limitPerUser: 0, sort: 0, status: 0 },
      grouponRules: {
        name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
        coverImage: [{ required: true, message: '请上传活动封面', trigger: 'change' }],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
      },
      grouponUploadUrl: process.env.VUE_APP_BASE_API + '/mall/groupon/image/upload',
      detailFileList: [],
      // 团购商品管理
      itemsDialogVisible: false,
      currentGroupon: null,
      itemLoading: false,
      itemList: [],
      itemDialogVisible: false,
      itemDialogTitle: '新增团购商品',
      itemSubmitLoading: false,
      itemForm: { id: null, merchantId: null, grouponId: null, name: '', title: '', content: '', description: '', coverImage: '', detailImages: '[]', originalPriceYuan: 0, grouponPriceYuan: 0, stock: 0, limitPerUser: 0, validDays: 30, status: 0, sort: 0, dishGroups: [{ groupName: '', items: [{ dishName: '', quantity: 1, unit: '份', priceYuan: 0 }] }], directTotalPrice: false, dishTotalPriceYuan: 0 },
      itemRules: {
        name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        content: [{ required: true, message: '请输入套餐内容', trigger: 'blur' }],
        coverImage: [{ required: true, message: '请上传封面图', trigger: 'change' }],
        originalPriceYuan: [{ required: true, message: '请输入原价', trigger: 'blur' }],
        grouponPriceYuan: [{ required: true, message: '请输入团购价', trigger: 'blur' }],
        stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
        validDays: [{ required: true, message: '请输入有效期', trigger: 'blur' }]
      },
      itemUploadUrl: process.env.VUE_APP_BASE_API + '/mall/groupon/item/image/upload',
      itemDetailFileList: [],
      // 图片上传
      uploadUrl: process.env.VUE_APP_BASE_API + '/mall/product/image/upload',
    }
  },
  computed: {
    uploadData() {
      return {
        merchantId: this.merchantId,
        productId: this.productForm.id || '',
        imageType: 'main'
      }
    },
    uploadHeaders() {
      return {
        Authorization: 'Bearer ' + getToken()
      }
    },
    computedDiscount() {
      if (this.itemForm.originalPriceYuan > 0 && this.itemForm.grouponPriceYuan > 0) {
        return (this.itemForm.grouponPriceYuan / this.itemForm.originalPriceYuan * 10).toFixed(1)
      }
      return '-'
    },
    dishTotalCount() {
      if (!this.itemForm.dishGroups) return 0
      return this.itemForm.dishGroups.reduce((sum, g) => sum + (g.items ? g.items.length : 0), 0)
    },
    computedDishTotalPrice() {
      if (this.itemForm.directTotalPrice) return Math.round((this.itemForm.dishTotalPriceYuan || 0) * 100)
      if (!this.itemForm.dishGroups) return 0
      return this.itemForm.dishGroups.reduce((sum, g) => {
        return sum + (g.items || []).reduce((s, d) => s + Math.round((d.priceYuan || 0) * 100) * (d.quantity || 1), 0)
      }, 0)
    }
  },
  created() {
    this.merchantId = Number(this.$route.params.id)
    if (!this.merchantId) {
      this.$message.error('参数错误')
      return
    }
    this.fetchDetail()
    this.loadProducts()
  },
  methods: {
    // ========== 图片上传 ==========
    beforeImageUpload(file) {
      const isValidType = ['image/jpeg', 'image/png', 'image/webp'].includes(file.type)
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isValidType) {
        this.$message.error('仅支持 jpg/jpeg/png/webp 格式!')
      }
      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB!')
      }
      return isValidType && isLt5M
    },
    getUploadFileUrl(file) {
      return file.url || (file.response && file.response.data && file.response.data.url) || ''
    },
    handleImageSuccess(res) {
      if (res.code === 200) {
        this.productForm.mainImage = res.data.url
        this.productForm.coverImage = res.data.url
        this.$message.success('图片上传成功')
      } else {
        this.$message.error(res.msg || '上传失败')
      }
    },
    async fetchDetail() {
      this.loading = true
      try {
        const res = await getMerchantDetail(this.merchantId)
        this.merchant = res.data
      } catch (e) {
        this.$message.error('获取商家详情失败')
      } finally {
        this.loading = false
      }
    },

    // ========== 商品管理 ==========
    async loadProducts() {
      this.productLoading = true
      try {
        const res = await getProductList({ merchantId: this.merchantId, ...this.productQuery })
        this.productList = res.rows
        this.productTotal = res.total
      } finally {
        this.productLoading = false
      }
    },
    handleProductSizeChange(val) {
      this.productQuery.pageSize = val
      this.loadProducts()
    },
    handleProductPageChange(val) {
      this.productQuery.pageNum = val
      this.loadProducts()
    },
    handleAddProduct() {
      this.productDialogTitle = '新增商品'
      this.productForm = { id: null, name: '', category: '', originalPrice: 0, price: 0, stock: 0, validDays: 30, mainImage: '', coverImage: '', description: '' }
      this.productDialogVisible = true
      this.$nextTick(() => { this.$refs.productForm && this.$refs.productForm.clearValidate() })
    },
    handleEditProduct(row) {
      this.productDialogTitle = '编辑商品'
      this.productForm = { ...row }
      this.productDialogVisible = true
      this.$nextTick(() => { this.$refs.productForm && this.$refs.productForm.clearValidate() })
    },
    submitProductForm() {
      this.$refs.productForm.validate(async valid => {
        if (!valid) return
        this.productForm.merchantId = this.merchantId
        if (this.productForm.id) {
          await updateProduct(this.productForm)
          this.$message.success('修改成功')
        } else {
          this.productForm.status = 1
          await addProduct(this.productForm)
          this.$message.success('新增成功')
        }
        this.productDialogVisible = false
        this.loadProducts()
      })
    },
    handleToggleStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const text = newStatus === 1 ? '上架' : '下架'
      this.$confirm(`确认${text}该商品？`, '提示', { type: 'warning' }).then(async () => {
        await updateProduct({ id: row.id, status: newStatus })
        this.$message.success(`${text}成功`)
        this.loadProducts()
      }).catch(() => {})
    },
    handleDeleteProduct(row) {
      this.$confirm('确认删除该商品？', '提示', { type: 'warning' }).then(async () => {
        await deleteProduct(row.id)
        this.$message.success('删除成功')
        this.loadProducts()
      }).catch(() => {})
    },

    // ========== 订单记录 ==========
    async loadOrders() {
      this.orderLoading = true
      try {
        const res = await getMerchantOrders(this.merchantId, this.orderQuery)
        this.orderList = res.rows
        this.orderTotal = res.total
      } finally {
        this.orderLoading = false
      }
    },
    handleOrderSizeChange(val) {
      this.orderQuery.pageSize = val
      this.loadOrders()
    },
    handleOrderPageChange(val) {
      this.orderQuery.pageNum = val
      this.loadOrders()
    },

    // ========== 流水记录 ==========
    async loadFlow() {
      this.flowLoading = true
      try {
        const res = await getMerchantFlowList({ merchantId: this.merchantId, ...this.flowQuery })
        this.flowList = res.rows
        this.flowTotal = res.total
      } finally {
        this.flowLoading = false
      }
    },
    handleFlowSizeChange(val) {
      this.flowQuery.pageSize = val
      this.loadFlow()
    },
    handleFlowPageChange(val) {
      this.flowQuery.pageNum = val
      this.loadFlow()
    },

    // ========== 账户管理 ==========
    async loadAccounts() {
      this.accountLoading = true
      try {
        const res = await getMerchantUserList({ merchantId: this.merchantId, pageNum: 1, pageSize: 100 })
        this.accountList = res.rows || []
      } finally {
        this.accountLoading = false
      }
    },
    handleAddAccount() {
      this.accountForm = { username: '', password: '', realName: '', phone: '', role: 'member' }
      this.accountDialogVisible = true
      this.$nextTick(() => { this.$refs.accountForm && this.$refs.accountForm.clearValidate() })
    },
    submitAccountForm() {
      this.$refs.accountForm.validate(async valid => {
        if (!valid) return
        await addMerchantUser({ ...this.accountForm, merchantId: this.merchantId, status: 1 })
        this.$message.success('新增成功')
        this.accountDialogVisible = false
        this.loadAccounts()
      })
    },
    handleResetPwd(row) {
      this.$confirm(`确认将 ${row.username} 的密码重置为 123456？`, '提示', { type: 'warning' }).then(async () => {
        await resetMerchantUserPwd(row.id, '123456')
        this.$message.success('密码已重置为 123456')
      }).catch(() => {})
    },
    handleToggleAccountStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const text = newStatus === 1 ? '启用' : '禁用'
      this.$confirm(`确认${text}账号 ${row.username}？`, '提示', { type: 'warning' }).then(async () => {
        await changeMerchantUserStatus(row.id, newStatus)
        this.$message.success(`${text}成功`)
        this.loadAccounts()
      }).catch(() => {})
    },

    // ========== 小程序配置 ==========
    handleEditMiniApp() {
      this.miniAppForm = {
        id: this.merchant.id,
        cMiniAppId: this.merchant.cMiniAppId || '',
        cMiniAppSecret: this.merchant.cMiniAppSecretConfigured ? '******' : '',
        mMiniAppId: this.merchant.mMiniAppId || '',
        mMiniAppSecret: this.merchant.mMiniAppSecretConfigured ? '******' : '',
        wxPayMchId: this.merchant.wxPayMchId || '',
        wxPayApiKey: this.merchant.wxPayApiKeyConfigured ? '******' : ''
      }
      this.miniAppDialogVisible = true
    },
    async submitMiniAppForm() {
      await updateMerchant(this.miniAppForm)
      this.$message.success('保存成功')
      this.miniAppDialogVisible = false
      this.fetchDetail()
    },

    // ========== 腾讯地图认领 ==========
    mapClaimStatusText(status) {
      const map = { NOT_CLAIMED: '未认领', CLAIMING: '认领中', CLAIMED: '已认领', FAILED: '认领失败' }
      return map[status] || '未认领'
    },
    mapClaimStatusType(status) {
      const map = { NOT_CLAIMED: 'info', CLAIMING: 'warning', CLAIMED: 'success', FAILED: 'danger' }
      return map[status] || 'info'
    },
    handleEditMapClaim() {
      this.mapClaimForm = {
        id: this.merchant.id,
        mapClaimStatus: this.merchant.mapClaimStatus || 'NOT_CLAIMED',
        mapPoiId: this.merchant.mapPoiId || '',
        mapClaimUrl: this.merchant.mapClaimUrl || '',
        mapClaimRemark: this.merchant.mapClaimRemark || ''
      }
      this.mapClaimDialogVisible = true
    },
    async submitMapClaimForm() {
      await updateMerchant(this.mapClaimForm)
      this.$message.success('保存成功')
      this.mapClaimDialogVisible = false
      this.fetchDetail()
    },

    // ========== 微信特约商户 ==========
    wxApplymentStateText(state) {
      const map = {
        NOT_SUBMITTED: '未提交', SUBMITTED: '已提交', AUDITING: '审核中',
        NEED_VERIFY: '待账户验证', NEED_SIGN: '待签约', FINISHED: '已完成',
        REJECTED: '已驳回', FROZEN: '已冻结'
      }
      return map[state] || '未提交'
    },
    wxApplymentStateType(state) {
      const map = {
        NOT_SUBMITTED: 'info', SUBMITTED: 'warning', AUDITING: 'warning',
        NEED_VERIFY: 'warning', NEED_SIGN: 'warning', FINISHED: 'success',
        REJECTED: 'danger', FROZEN: 'danger'
      }
      return map[state] || 'info'
    },
    wxPaymentAccessTypeText(type) {
      const map = { EXISTING_MCH: '已有微信支付商户号', APPLYMENT_ASSISTED: '平台协助申请' }
      return map[type] || '已有微信支付商户号'
    },
    percentText(value) {
      return value === null || value === undefined || value === '' ? '未配置' : value + '%'
    },
    handleEditWxApplyment() {
      this.wxApplymentForm = {
        id: this.merchant.id,
        wxApplymentId: this.merchant.wxApplymentId || '',
        wxApplymentState: this.merchant.wxApplymentState || 'NOT_SUBMITTED',
        wxApplymentRejectReason: this.merchant.wxApplymentRejectReason || '',
        wxPaymentAccessType: this.merchant.wxPaymentAccessType || 'EXISTING_MCH',
        merchantWxMchId: this.merchant.merchantWxMchId || '',
        merchantWxMchName: this.merchant.merchantWxMchName || '',
        wxProfitSharingEnabled: this.merchant.wxProfitSharingEnabled === 1 ? 1 : 0,
        platformReceiverMchId: this.merchant.platformReceiverMchId || '',
        distributorReceiverMchId: this.merchant.distributorReceiverMchId || '',
        merchantShareRate: this.merchant.merchantShareRate !== undefined && this.merchant.merchantShareRate !== null ? this.merchant.merchantShareRate : 100,
        platformShareRate: this.merchant.platformShareRate !== undefined && this.merchant.platformShareRate !== null ? this.merchant.platformShareRate : 0,
        distributorShareRate: this.merchant.distributorShareRate !== undefined && this.merchant.distributorShareRate !== null ? this.merchant.distributorShareRate : 0,
        settlementCycle: this.merchant.settlementCycle || 'T1'
      }
      this.wxApplymentDialogVisible = true
    },
    async submitWxApplymentForm() {
      const total = Number(this.wxApplymentForm.merchantShareRate || 0) + Number(this.wxApplymentForm.platformShareRate || 0) + Number(this.wxApplymentForm.distributorShareRate || 0)
      if (Math.abs(total - 100) > 0.0001) {
        this.$message.error('商家、平台、分销商三方分账比例合计必须等于100%')
        return
      }
      await updateMerchant(this.wxApplymentForm)
      this.$message.success('保存成功')
      this.wxApplymentDialogVisible = false
      this.fetchDetail()
    },

    // ========== 团购活动 ==========
    async loadGroupons() {
      this.grouponLoading = true
      try {
        const res = await listGroupon({ merchantId: this.merchantId, ...this.grouponQuery })
        this.grouponList = res.rows || []
        this.grouponTotal = res.total || 0
      } finally {
        this.grouponLoading = false
      }
    },
    handleGrouponSizeChange(val) {
      this.grouponQuery.pageSize = val
      this.loadGroupons()
    },
    handleGrouponPageChange(val) {
      this.grouponQuery.pageNum = val
      this.loadGroupons()
    },
    handleAddGroupon() {
      this.grouponDialogTitle = '新增团购活动'
      this.detailFileList = []
      this.grouponForm = {
        id: null, merchantId: this.merchantId, name: '', coverImage: '', posterImage: '',
        detailImages: '[]', description: '', startTime: null, endTime: null,
        limitPerUser: 0, sort: 0, status: 0
      }
      this.grouponDialogVisible = true
    },
    handleEditGroupon(row) {
      this.grouponDialogTitle = '编辑团购活动'
      getGroupon(row.id).then(res => {
        this.grouponForm = { ...res.data }
        if (!this.grouponForm.detailImages) this.grouponForm.detailImages = '[]'
        this.detailFileList = JSON.parse(this.grouponForm.detailImages).map(url => ({ url, name: url.split('/').pop() }))
        this.grouponDialogVisible = true
      })
    },
    submitGrouponForm() {
      this.$refs.grouponForm.validate(valid => {
        if (!valid) return
        this.grouponSubmitLoading = true
        const data = { ...this.grouponForm, merchantId: this.merchantId }
        if (data.startTime && data.endTime && data.startTime >= data.endTime) {
          this.$message.error('开始时间必须早于结束时间')
          this.grouponSubmitLoading = false
          return
        }
        const action = data.id ? updateGroupon : addGroupon
        action(data).then(() => {
          this.$message.success(data.id ? '修改成功' : '新增成功')
          this.grouponDialogVisible = false
          this.loadGroupons()
        }).finally(() => {
          this.grouponSubmitLoading = false
        })
      })
    },
    handleToggleGrouponStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const text = newStatus === 1 ? '上架' : '下架'
      this.$confirm('确认' + text + '该活动？', '提示', { type: 'warning' }).then(() => {
        changeGrouponStatus({ id: row.id, status: newStatus }).then(() => {
          this.$message.success(text + '成功')
          this.loadGroupons()
        })
      }).catch(() => {})
    },
    handleDeleteGroupon(row) {
      this.$confirm('确认删除该活动？删除后不可恢复', '警告', { type: 'warning' }).then(() => {
        return deleteGroupon(row.id)
      }).then(() => {
        this.loadGroupons()
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    grouponUploadData(imageType) {
      return {
        merchantId: this.merchantId,
        grouponId: this.grouponForm.id || '',
        tempToken: this.grouponForm.id ? '' : ('temp_' + Date.now()),
        imageType: imageType
      }
    },
    handleGrouponImageSuccess(res, type) {
      if (res.code === 200) {
        if (type === 'cover') {
          this.grouponForm.coverImage = res.data.url
        } else if (type === 'poster') {
          this.grouponForm.posterImage = res.data.url
        }
        this.$forceUpdate()
      } else {
        this.$message.error(res.msg || '上传失败')
      }
    },
    handleDetailImageSuccess(res, file) {
      if (res.code === 200) {
        file.url = res.data.url
        const list = JSON.parse(this.grouponForm.detailImages || '[]')
        list.push(res.data.url)
        this.grouponForm.detailImages = JSON.stringify(list)
      } else {
        this.$message.error(res.msg || '上传失败')
      }
    },
    handleDetailImageRemove(file) {
      const list = JSON.parse(this.grouponForm.detailImages || '[]')
      const url = this.getUploadFileUrl(file)
      const idx = list.indexOf(url)
      if (idx > -1) {
        list.splice(idx, 1)
        this.grouponForm.detailImages = JSON.stringify(list)
      }
    },
    // ========== 团购商品管理 ==========
    handleManageItems(row) {
      this.currentGroupon = row
      this.itemsDialogVisible = true
      this.loadItems()
    },
    loadItems() {
      this.itemLoading = true
      listGrouponItem(this.currentGroupon.id).then(res => {
        this.itemList = res.data || []
      }).finally(() => {
        this.itemLoading = false
      })
    },
    handleAddItem() {
      this.itemDialogTitle = '新增团购商品'
      this.itemDetailFileList = []
      this.itemForm = {
        id: null, merchantId: this.merchantId, grouponId: this.currentGroupon.id,
        name: '', title: '', content: '', description: '',
        coverImage: '', detailImages: '[]',
        originalPriceYuan: 0, grouponPriceYuan: 0,
        stock: 0, limitPerUser: 0, validDays: 30, status: 0, sort: 0,
        dishGroups: [{ groupName: '', items: [{ dishName: '', quantity: 1, unit: '份', priceYuan: 0 }] }],
        directTotalPrice: false, dishTotalPriceYuan: 0
      }
      this.itemDialogVisible = true
    },
    handleEditItem(row) {
      this.itemDialogTitle = '编辑团购商品'
      this.itemDetailFileList = []
      let dishGroups = [{ groupName: '', items: [{ dishName: '', quantity: 1, unit: '份', priceYuan: 0 }] }]
      if (row.dishGroups) {
        try {
          dishGroups = typeof row.dishGroups === 'string' ? JSON.parse(row.dishGroups) : row.dishGroups
          dishGroups.forEach(g => {
            g.items.forEach(d => { d.priceYuan = d.price ? d.price / 100 : 0 })
          })
        } catch (e) { dishGroups = [{ groupName: '', items: [{ dishName: '', quantity: 1, unit: '份', priceYuan: 0 }] }] }
      }
      this.itemForm = {
        ...row,
        originalPriceYuan: row.originalPrice ? row.originalPrice / 100 : 0,
        grouponPriceYuan: row.grouponPrice ? row.grouponPrice / 100 : 0,
        dishTotalPriceYuan: row.dishTotalPrice ? row.dishTotalPrice / 100 : 0,
        directTotalPrice: !!row.directTotalPrice,
        dishGroups: dishGroups
      }
      if (!this.itemForm.detailImages) this.itemForm.detailImages = '[]'
      this.itemDetailFileList = JSON.parse(this.itemForm.detailImages).map(url => ({ url, name: url.split('/').pop() }))
      this.itemDialogVisible = true
    },
    submitItemForm() {
      this.$refs.itemForm.validate(valid => {
        if (!valid) return
        this.itemSubmitLoading = true

        // 处理菜品组数据
        const dishGroups = (this.itemForm.dishGroups || []).map(g => ({
          groupName: g.groupName,
          items: (g.items || []).map(d => ({
            dishName: d.dishName,
            quantity: d.quantity || 1,
            unit: d.unit || '份',
            price: Math.round((d.priceYuan || 0) * 100)
          }))
        })).filter(g => g.groupName && g.items.length > 0)

        const dishCount = dishGroups.reduce((s, g) => s + g.items.length, 0)
        const dishTotalPrice = this.itemForm.directTotalPrice
          ? Math.round((this.itemForm.dishTotalPriceYuan || 0) * 100)
          : dishGroups.reduce((s, g) => s + g.items.reduce((ss, d) => ss + d.price * d.quantity, 0), 0)

        const data = {
          ...this.itemForm,
          merchantId: this.merchantId,
          grouponId: this.currentGroupon.id,
          originalPrice: Math.round(this.itemForm.originalPriceYuan * 100),
          grouponPrice: Math.round(this.itemForm.grouponPriceYuan * 100),
          dishGroups: JSON.stringify(dishGroups),
          dishTotalPrice: dishTotalPrice,
          directTotalPrice: this.itemForm.directTotalPrice ? 1 : 0,
          dishCount: dishCount,
          availableDishCount: dishCount
        }
        if (data.grouponPrice > data.originalPrice) {
          this.$message.error('团购价不能大于原价')
          this.itemSubmitLoading = false
          return
        }
        delete data.originalPriceYuan
        delete data.grouponPriceYuan
        delete data.dishTotalPriceYuan
        const action = data.id ? updateGrouponItem : addGrouponItem
        action(data).then(() => {
          this.$message.success(data.id ? '修改成功' : '新增成功')
          this.itemDialogVisible = false
          this.loadItems()
        }).finally(() => {
          this.itemSubmitLoading = false
        })
      })
    },
    handleToggleItemStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const text = newStatus === 1 ? '上架' : '下架'
      this.$confirm('确认' + text + '该商品？', '提示', { type: 'warning' }).then(() => {
        changeGrouponItemStatus({ id: row.id, status: newStatus }).then(() => {
          this.$message.success(text + '成功')
          this.loadItems()
        })
      }).catch(() => {})
    },
    handleDeleteItem(row) {
      this.$confirm('确认删除该商品？删除后不可恢复', '警告', { type: 'warning' }).then(() => {
        return deleteGrouponItem(row.id)
      }).then(() => {
        this.loadItems()
        this.$message.success('删除成功')
      }).catch(() => {})
    },
    itemUploadData(imageType) {
      return {
        merchantId: this.merchantId,
        grouponId: this.currentGroupon ? this.currentGroupon.id : '',
        itemId: this.itemForm.id || '',
        tempToken: this.itemForm.id ? '' : ('temp_' + Date.now()),
        imageType: imageType
      }
    },
    handleItemImageSuccess(res, type) {
      if (res.code === 200) {
        if (type === 'cover') {
          this.itemForm.coverImage = res.data.url
        }
        this.$forceUpdate()
      } else {
        this.$message.error(res.msg || '上传失败')
      }
    },
    handleItemDetailImageSuccess(res, file) {
      if (res.code === 200) {
        file.url = res.data.url
        const list = JSON.parse(this.itemForm.detailImages || '[]')
        list.push(res.data.url)
        this.itemForm.detailImages = JSON.stringify(list)
      } else {
        this.$message.error(res.msg || '上传失败')
      }
    },
    handleItemDetailImageRemove(file) {
      const list = JSON.parse(this.itemForm.detailImages || '[]')
      const url = this.getUploadFileUrl(file)
      const idx = list.indexOf(url)
      if (idx > -1) {
        list.splice(idx, 1)
        this.itemForm.detailImages = JSON.stringify(list)
      }
    },

    // ========== 菜品组管理 ==========
    addDishGroup() {
      this.itemForm.dishGroups.push({
        groupName: '',
        items: [{ dishName: '', quantity: 1, unit: '份', priceYuan: 0 }]
      })
    },
    removeDishGroup(gIdx) {
      if (this.itemForm.dishGroups.length <= 1) {
        this.$message.warning('至少保留一个菜品组')
        return
      }
      this.itemForm.dishGroups.splice(gIdx, 1)
    },
    moveDishGroup(gIdx, dir) {
      const newIdx = gIdx + dir
      const groups = this.itemForm.dishGroups
      const temp = groups[gIdx]
      this.$set(groups, gIdx, groups[newIdx])
      this.$set(groups, newIdx, temp)
    },
    addDish(gIdx) {
      this.itemForm.dishGroups[gIdx].items.push({
        dishName: '', quantity: 1, unit: '份', priceYuan: 0
      })
    },
    removeDish(gIdx, dIdx) {
      const items = this.itemForm.dishGroups[gIdx].items
      if (items.length <= 1) {
        this.$message.warning('每组至少保留一道菜品')
        return
      }
      items.splice(dIdx, 1)
    },

    handleBack() {
      this.$router.push({ path: '/merchant/list' })
    },
    statusText(status) {
      const map = { 0: '禁用', 1: '正常', 2: '待审核' }
      return map[status] || '未知'
    },
    statusTagType(status) {
      const map = { 0: 'info', 1: 'success', 2: 'warning' }
      return map[status] || 'info'
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'groupon' && this.grouponList.length === 0) {
        this.loadGroupons()
      } else if (val === 'orders' && this.orderList.length === 0) {
        this.loadOrders()
      } else if (val === 'flow' && this.flowList.length === 0) {
        this.loadFlow()
      } else if (val === 'accounts' && this.accountList.length === 0) {
        this.loadAccounts()
      }
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.detail-tabs {
  margin-top: 20px;
}
.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.pagination {
  margin-top: 12px;
  text-align: right;
}
.text-danger {
  color: #F56C6C;
}
.text-success {
  color: #67C23A;
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
  width: 150px;
  height: 150px;
  line-height: 150px;
  text-align: center;
}
.image-preview {
  width: 150px;
  height: 150px;
  display: block;
  object-fit: cover;
}
.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
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
.dish-group-section {
  margin-bottom: 16px;
}
.dish-group-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}
.dish-group-card {
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 10px;
  background: #FAFAFA;
}
.dish-group-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.dish-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  padding-left: 12px;
}
.dish-group-actions {
  padding-left: 12px;
  margin-top: 4px;
}
</style>
