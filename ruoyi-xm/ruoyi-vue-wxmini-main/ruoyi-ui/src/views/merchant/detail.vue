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
          <el-descriptions-item label="商品数">{{ merchant.productCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="累计收入">¥{{ Number(merchant.totalIncome || 0).toLocaleString() }}</el-descriptions-item>
          <el-descriptions-item label="今日营收">¥{{ Number(merchant.todayIncome || 0).toLocaleString() }}</el-descriptions-item>
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
            </el-descriptions>

            <div class="section-header carousel-section-header">
              <h3 class="section-title">
                店铺轮播图
                <el-tooltip content="最多上传 5 张，拖动可调整顺序，第一张作为小程序首页封面" placement="top">
                  <i class="el-icon-question carousel-help" />
                </el-tooltip>
              </h3>
              <el-button
                type="primary"
                icon="el-icon-check"
                size="small"
                :loading="carouselSaving"
                @click="saveCarouselImages"
              >保存轮播图</el-button>
            </div>
            <div class="carousel-upload-panel">
              <ImageUpload
                v-model="carouselForm.carouselImages"
                :limit="5"
                :file-size="5"
                :file-type="['jpg', 'jpeg', 'png', 'webp']"
              />
            </div>

            <div class="section-header">
              <h3 class="section-title">商户小程序与商家后台入口</h3>
              <el-button type="primary" icon="el-icon-edit" size="small" @click="handleEditMiniApp">编辑配置</el-button>
            </div>
            <el-descriptions :column="2" border style="margin-top: 10px;">
              <el-descriptions-item label="商户 C 端 AppID">{{ configText(merchant.cMiniAppId) }}</el-descriptions-item>
              <el-descriptions-item label="商户 C 端 Secret">{{ secretStatusText(merchant.cMiniAppSecretConfigured) }}</el-descriptions-item>
              <el-descriptions-item label="入口使用 AppID">{{ configText(merchant.cMiniAppId) }}</el-descriptions-item>
              <el-descriptions-item label="配置状态">
                <el-tag :type="miniAppConfigStatus.type" size="small">{{ miniAppConfigStatus.text }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="入口码状态">
                <el-tag :type="entryCodeStatus.type" size="small">{{ entryCodeStatus.text }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <div class="entry-panel">
              <div class="entry-panel__preview">
                <el-image
                  v-if="merchantEntry.qrCodeUrl"
                  :src="merchantEntry.qrCodeUrl"
                  fit="cover"
                  :preview-src-list="[merchantEntry.qrCodeUrl]"
                  class="entry-panel__image"
                />
                <div v-else class="entry-panel__empty">
                  <i class="el-icon-mobile-phone" />
                  <span>{{ canGenerateEntryCode ? '点击生成商家后台入口码' : '先配置小程序 AppID 和 Secret' }}</span>
                </div>
              </div>

              <div class="entry-panel__main">
                <div class="entry-panel__title-row">
                  <div>
                    <div class="entry-panel__title">商家后台入口码</div>
                    <div class="entry-panel__desc">给店长或员工扫码使用，进入该商户小程序内的 B 端登录页，不落到 C 端首页。</div>
                  </div>
                  <el-tag type="success" size="small">商户 AppID / BC 合并</el-tag>
                </div>

                <div class="entry-panel__path">
                  <span class="entry-panel__path-label">入口页</span>
                  <span class="entry-panel__path-value">{{ entryLoginPage }}</span>
                </div>
                <div class="entry-panel__path">
                  <span class="entry-panel__path-label">请求参数</span>
                  <span class="entry-panel__path-value">merchantId={{ merchantId }}</span>
                </div>

                <div class="entry-panel__steps">
                  <div class="entry-step">
                    <span class="entry-step__index">1</span>
                    <span>后台先在“账户管理”创建店长或员工账号。</span>
                  </div>
                  <div class="entry-step">
                    <span class="entry-step__index">2</span>
                    <span>把入口码发给商家，员工扫码进入对应商家的后台登录页。</span>
                  </div>
                  <div class="entry-step">
                    <span class="entry-step__index">3</span>
                    <span>员工用商家账号密码登录，权限按 owner/member 控制。</span>
                  </div>
                </div>

                <div class="entry-panel__actions">
                  <el-button type="primary" size="small" :loading="entryQrLoading" :disabled="!canGenerateEntryCode" @click="loadMerchantEntryQrCode(false)">
                    {{ merchantEntry.qrCodeUrl ? '重新生成入口码' : '生成入口码' }}
                  </el-button>
                  <el-button size="small" @click="copyEntryLoginPage">复制登录页路径</el-button>
                  <el-button size="small" @click="activeTab = 'accounts'; loadAccounts()">去建账号</el-button>
                </div>
              </div>
            </div>

            <div class="section-header">
              <h3 class="section-title">腾讯地图认领</h3>
              <el-button type="primary" icon="el-icon-edit" size="small" @click="handleEditMapClaim">编辑配置</el-button>
            </div>
            <el-descriptions :column="2" border style="margin-top: 10px;">
              <el-descriptions-item label="认领状态">
                <el-tag :type="mapClaimStatusType(merchant.mapClaimStatus)" size="small">{{ mapClaimStatusText(merchant.mapClaimStatus) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="POI ID">{{ configText(merchant.mapPoiId) }}</el-descriptions-item>
              <el-descriptions-item label="认领链接">{{ configText(merchant.mapClaimUrl) }}</el-descriptions-item>
              <el-descriptions-item label="认领时间">{{ configText(merchant.mapClaimTime, '待认领完成') }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ merchant.mapClaimRemark || '无' }}</el-descriptions-item>
            </el-descriptions>

            <div class="section-header">
              <h3 class="section-title">微信支付与结算配置</h3>
              <el-button type="primary" icon="el-icon-edit" size="small" @click="handleEditWxApplyment">编辑配置</el-button>
            </div>
            <el-descriptions :column="2" border style="margin-top: 10px;">
              <el-descriptions-item label="接入方式">{{ wxPaymentAccessTypeText(merchant.wxPaymentAccessType) }}</el-descriptions-item>
              <el-descriptions-item label="商家商户号">{{ configText(merchant.effectiveMerchantWxMchId || merchant.merchantWxMchId, '资料未填写') }}</el-descriptions-item>
              <el-descriptions-item label="商户名称">{{ configText(merchant.merchantWxMchName) }}</el-descriptions-item>
              <el-descriptions-item label="微信分账">
                <el-tag :type="merchant.wxProfitSharingEnabled === 1 ? 'success' : 'info'" size="small">{{ merchant.wxProfitSharingEnabled === 1 ? '已启用' : '未启用' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="合同版本">{{ configText(merchant.profitSharingContractVersion, '未记录') }}</el-descriptions-item>
              <el-descriptions-item label="商家覆盖比例">{{ percentText(merchant.merchantShareRate) }}</el-descriptions-item>
              <el-descriptions-item label="平台覆盖比例">{{ percentText(merchant.platformShareRate) }}</el-descriptions-item>
              <el-descriptions-item label="分销商覆盖比例">{{ percentText(merchant.distributorShareRate) }}</el-descriptions-item>
              <el-descriptions-item label="运营准入" :span="2">
                <el-tag :type="merchant.canOperate ? 'success' : 'warning'" size="small">{{ merchant.canOperate ? '可运营' : (merchant.operateBlockReason || '配置未完成') }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <el-tab-pane label="门店信息" name="store">
            <div class="store-toolbar">
              <h3 class="section-title">主门店</h3>
              <el-button
                v-if="primaryStore"
                v-hasPermi="['mall:merchant:edit']"
                type="primary"
                icon="el-icon-edit"
                size="small"
                @click="openStoreDialog"
              >编辑门店</el-button>
            </div>

            <div v-loading="storeLoading" class="store-content">
              <el-empty v-if="!storeLoading && !primaryStore" description="尚未创建主门店">
                <el-button
                  v-hasPermi="['mall:merchant:add']"
                  type="primary"
                  icon="el-icon-plus"
                  size="small"
                  @click="openStoreDialog"
                >创建主门店</el-button>
              </el-empty>

              <el-descriptions v-else-if="primaryStore" :column="2" border>
                <el-descriptions-item label="门店图片">
                  <el-image
                    v-if="primaryStore.avatar"
                    :src="displayImageUrl(primaryStore.avatar)"
                    :preview-src-list="[displayImageUrl(primaryStore.avatar)]"
                    fit="cover"
                    class="store-avatar"
                  />
                  <span v-else>-</span>
                </el-descriptions-item>
                <el-descriptions-item label="门店状态">
                  <el-tag :type="primaryStore.status === 1 ? 'success' : 'info'" size="small">
                    {{ primaryStore.status === 1 ? '营业中' : '已停用' }}
                  </el-tag>
                  <el-tag type="primary" size="small" style="margin-left: 8px;">主门店</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="门店名称">{{ primaryStore.name || '-' }}</el-descriptions-item>
                <el-descriptions-item label="联系人">{{ primaryStore.contact || '-' }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ primaryStore.phone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="营业时间">{{ primaryStore.businessHours || '-' }}</el-descriptions-item>
                <el-descriptions-item label="门店地址" :span="2">{{ primaryStore.address || '-' }}</el-descriptions-item>
                <el-descriptions-item label="经度">{{ coordinateText(primaryStore.longitude) }}</el-descriptions-item>
                <el-descriptions-item label="纬度">{{ coordinateText(primaryStore.latitude) }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ primaryStore.createTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ primaryStore.updateTime || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
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
              <el-table-column label="主图" width="80" align="center">
                <template slot-scope="scope">
                  <el-image
                    v-if="scope.row.mainImage || scope.row.coverImage"
                    :src="displayImageUrl(scope.row.mainImage || scope.row.coverImage)"
                    style="width: 50px; height: 50px;"
                    fit="cover"
                    :preview-src-list="[displayImageUrl(scope.row.mainImage || scope.row.coverImage)]"
                  />
                  <span v-else style="color: #ccc;">无</span>
                </template>
              </el-table-column>
              <el-table-column label="商品名称" prop="name" min-width="180" show-overflow-tooltip />
              <el-table-column label="分类" width="110">
                <template slot-scope="scope">{{ scope.row.categoryName || categoryNameText(scope.row.categoryId) }}</template>
              </el-table-column>
              <el-table-column label="原价" width="90">
                <template slot-scope="scope">¥{{ moneyText(scope.row.originalPrice) }}</template>
              </el-table-column>
              <el-table-column label="现价" width="90">
                <template slot-scope="scope">
                  <span class="text-danger">¥{{ moneyText(scope.row.price) }}</span>
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
                  <el-image v-if="scope.row.coverImage" :src="displayImageUrl(scope.row.coverImage)" style="width: 50px; height: 50px;" fit="cover" :preview-src-list="[displayImageUrl(scope.row.coverImage)]" />
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
              <el-table-column label="用户" min-width="120">
                <template slot-scope="scope">{{ scope.row.userName || '未知用户' }}</template>
              </el-table-column>
              <el-table-column label="金额" width="100">
                <template slot-scope="scope">¥{{ moneyText(scope.row.payAmount) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90" align="center">
                <template slot-scope="scope">
                  <el-tag :type="orderStatusInfo(scope.row.status).type" size="small">{{ orderStatusInfo(scope.row.status).text }}</el-tag>
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
                    {{ Number(scope.row.totalAmount || 0) >= 0 ? '+' : '' }}¥{{ moneyText(scope.row.totalAmount) }}
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

    <el-dialog :title="primaryStore ? '编辑主门店' : '创建主门店'" :visible.sync="storeDialogVisible" width="680px" append-to-body>
      <el-form ref="storeForm" :model="storeForm" :rules="storeRules" label-width="96px">
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="门店名称" prop="name">
              <el-input v-model="storeForm.name" maxlength="100" placeholder="请输入门店名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="storeForm.contact" maxlength="50" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="storeForm.phone" maxlength="20" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="营业时间">
              <el-input v-model="storeForm.businessHours" maxlength="100" placeholder="例如 10:00-22:00" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="门店地址">
          <el-input v-model="storeForm.address" maxlength="255" placeholder="请输入门店地址" />
        </el-form-item>
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input-number
                v-model="storeForm.longitude"
                :min="-180"
                :max="180"
                :precision="7"
                :controls="false"
                placeholder="例如 108.9530980"
                class="store-coordinate-input"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input-number
                v-model="storeForm.latitude"
                :min="-90"
                :max="90"
                :precision="7"
                :controls="false"
                placeholder="例如 34.2778000"
                class="store-coordinate-input"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="门店图片">
          <ImageUpload
            v-model="storeForm.avatar"
            :limit="1"
            :file-size="5"
            :file-type="['jpg', 'jpeg', 'png', 'webp']"
          />
        </el-form-item>
        <el-form-item label="营业状态">
          <el-switch
            v-model="storeForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="营业中"
            inactive-text="已停用"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="storeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="storeSaving" @click="submitStoreForm">保存</el-button>
      </div>
    </el-dialog>

    <!-- 新增/编辑商品弹窗 -->
    <el-dialog :title="productDialogTitle" :visible.sync="productDialogVisible" width="600px" append-to-body>
      <el-form ref="productForm" :model="productForm" :rules="productRules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <div class="category-picker">
            <el-select v-model="productForm.categoryId" filterable placeholder="请选择分类" style="flex: 1;">
              <el-option
                v-for="item in productCategoryOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
            <el-button type="text" icon="el-icon-plus" @click="handleAddCategory">新增分类</el-button>
          </div>
          <div v-if="!productCategoryOptions.length" class="form-tip">暂无分类，请先新增分类后再保存商品。</div>
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
            <img v-if="productForm.mainImage || productForm.coverImage" :src="displayImageUrl(productForm.mainImage || productForm.coverImage)" class="image-preview">
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
    <el-dialog title="编辑商户小程序配置" :visible.sync="miniAppDialogVisible" width="600px" append-to-body>
      <el-form ref="miniAppForm" :model="miniAppForm" label-width="120px">
        <el-divider content-position="left">商户小程序（C/B 共用）</el-divider>
        <el-form-item label="商户 AppID">
          <el-input v-model="miniAppForm.cMiniAppId" placeholder="该商户小程序的 wx 开头 AppID" />
          <div class="form-tip">小程序请求会自动带 X-Wx-AppId，登录接口会带 appid；后端用这里的 AppID 匹配商户。</div>
        </el-form-item>
        <el-form-item label="商户 Secret">
          <el-input v-model="miniAppForm.cMiniAppSecret" placeholder="该商户小程序 Secret" />
          <div class="form-tip">修改 AppID 时必须重新填写对应 Secret，避免新 AppID 误用旧 Secret。</div>
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

    <!-- 编辑微信支付与结算配置弹窗 -->
    <el-dialog title="编辑微信支付与结算配置" :visible.sync="wxApplymentDialogVisible" width="680px" append-to-body>
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
        <el-alert
          title="服务商模式下，用户付款结算到商家的微信支付子商户号；普通商家收款不需要填写 OpenID。"
          type="success"
          :closable="false"
          style="margin-bottom: 16px;"
        />
        <el-alert
          v-if="wxApplymentForm.wxPaymentAccessType === 'APPLYMENT_ASSISTED'"
          title="平台仅协助商家完成微信支付商户号申请，申请完成后在此填写商户号即可。"
          type="info"
          :closable="false"
          style="margin-bottom: 16px;"
        />
        <el-divider content-position="left">商户合同与分账比例</el-divider>
        <el-alert
          title="开启微信分账后，支付下单会标记分账订单；后台按本商户合同版本和三方比例生成账务，并在微信分账开关打开时发起分账。"
          type="info"
          :closable="false"
          style="margin-bottom: 16px;"
        />
        <el-form-item label="启用微信分账">
          <el-switch
            v-model="wxApplymentForm.wxProfitSharingEnabled"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="关闭"
          />
        </el-form-item>
        <el-form-item label="合同版本">
          <el-input v-model="wxApplymentForm.profitSharingContractVersion" placeholder="如：2026-07服务商合同/商户单独协议编号" />
        </el-form-item>
        <el-form-item label="商家到账比例">
          <el-input-number v-model="wxApplymentForm.merchantShareRate" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="平台留存比例">
          <el-input-number v-model="wxApplymentForm.platformShareRate" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="分销商到账比例">
          <el-input-number v-model="wxApplymentForm.distributorShareRate" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%;" />
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">三方比例合计必须等于100%。启用商户覆盖比例后，实际账务优先按这里保存的商户合同口径计算。</div>
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
            <img v-if="grouponForm.coverImage" :src="displayImageUrl(grouponForm.coverImage)" class="image-preview">
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
            <img v-if="grouponForm.posterImage" :src="displayImageUrl(grouponForm.posterImage)" class="image-preview">
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
          <el-image v-if="scope.row.coverImage" :src="displayImageUrl(scope.row.coverImage)" style="width: 45px; height: 45px;" fit="cover" :preview-src-list="[displayImageUrl(scope.row.coverImage)]" />
            <span v-else style="color: #ccc;">无</span>
          </template>
        </el-table-column>
        <el-table-column label="商品名称" prop="name" min-width="120" show-overflow-tooltip />
        <el-table-column label="套餐内容" prop="content" min-width="150" show-overflow-tooltip />
        <el-table-column label="原价" width="80" align="center">
          <template slot-scope="scope">{{ centMoneyText(scope.row.originalPrice) }}</template>
        </el-table-column>
        <el-table-column label="团购价" width="80" align="center">
          <template slot-scope="scope"><span class="text-danger">{{ centMoneyText(scope.row.grouponPrice) }}</span></template>
        </el-table-column>
        <el-table-column label="折扣" width="65" align="center">
          <template slot-scope="scope">{{ discountText(scope.row.discountRate) }}</template>
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
            <img v-if="itemForm.coverImage" :src="displayImageUrl(itemForm.coverImage)" class="image-preview">
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
import { getMerchantDetail, getMerchantStore, createMerchantStore, updateMerchantStore, getMerchantLiveStats, getMerchantEntryQrCode, getProductList, getProductCategoryList, addProductCategory, addProduct, updateProduct, deleteProduct, getMerchantOrders, getMerchantFlowList, orderStatusMap, getMerchantUserList, addMerchantUser, resetMerchantUserPwd, changeMerchantUserStatus, updateMerchant } from '@/api/merchant'
import { listGroupon, getGroupon, addGroupon, updateGroupon, deleteGroupon, changeGrouponStatus, listGrouponItem, addGrouponItem, updateGrouponItem, deleteGrouponItem, changeGrouponItemStatus } from '@/api/marketing/groupon'
import { getToken } from '@/utils/auth'

const MERCHANT_DETAIL_REFRESH_INTERVAL = 15000

export default {
  name: 'MerchantDetail',
  data() {
    return {
      loading: false,
      merchant: {},
      activeTab: 'basic',
      merchantId: null,
      carouselSaving: false,
      carouselForm: { id: null, carouselImages: '' },
      storeLoading: false,
      storeLoaded: false,
      primaryStore: null,
      storeDialogVisible: false,
      storeSaving: false,
      storeForm: {
        name: '', contact: '', phone: '', address: '', businessHours: '',
        avatar: '', longitude: null, latitude: null, status: 1
      },
      storeRules: {
        name: [{ required: true, message: '请输入门店名称', trigger: 'blur' }]
      },
      orderStatusMap: orderStatusMap,
      entryQrLoading: false,
      merchantEntry: {
        qrCodeUrl: '',
        loginPage: '',
        entryAppId: '',
        scene: ''
      },

      // 商品
      productLoading: false,
      productList: [],
      productTotal: 0,
      productQuery: { name: '', status: '', pageNum: 1, pageSize: 10 },
      productCategoryOptions: [],
      productDialogVisible: false,
      productDialogTitle: '新增商品',
      productForm: { id: null, name: '', categoryId: null, originalPrice: 0, price: 0, stock: 0, validDays: 30, mainImage: '', coverImage: '', description: '' },
      productRules: {
        name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
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
      miniAppForm: { cMiniAppId: '', cMiniAppSecret: '' },

      // 腾讯地图认领
      mapClaimDialogVisible: false,
      mapClaimForm: { mapClaimStatus: 'NOT_CLAIMED', mapPoiId: '', mapClaimUrl: '', mapClaimRemark: '' },

      // 微信特约商户
      wxApplymentDialogVisible: false,
      wxApplymentForm: {
        wxPaymentAccessType: 'EXISTING_MCH', merchantWxMchId: '', merchantWxMchName: '',
        wxProfitSharingEnabled: 0, receiverOpenid: '', profitSharingContractVersion: '',
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
      uploadUrl: process.env.VUE_APP_BASE_API + '/mall/product/image/upload'
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
    entryLoginPage() {
      if (this.merchantEntry && this.merchantEntry.loginPage) {
        return this.merchantEntry.loginPage
      }
      if (this.merchantId) {
        return `/pages/merchant/login/login?merchantId=${this.merchantId}`
      }
      return '/pages/merchant/login/login'
    },
    canGenerateEntryCode() {
      return Boolean(this.merchant && this.merchant.cMiniAppId && this.merchant.cMiniAppSecretConfigured)
    },
    miniAppConfigStatus() {
      if (this.canGenerateEntryCode) {
        return { type: 'success', text: '小程序已配置' }
      }
      if (this.merchant && this.merchant.cMiniAppId) {
        return { type: 'warning', text: 'Secret 待填写' }
      }
      return { type: 'warning', text: '小程序待填写' }
    },
    entryCodeStatus() {
      if (!this.canGenerateEntryCode) {
        return { type: 'info', text: '需先配置小程序' }
      }
      return this.merchantEntry && this.merchantEntry.qrCodeUrl
        ? { type: 'success', text: '已生成' }
        : { type: 'warning', text: '待生成' }
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
    this.loadProductCategories()
    this.loadProducts()
    this.startLiveStatsRefresh()
  },
  activated() {
    this.startLiveStatsRefresh()
    this.refreshLiveStats()
  },
  deactivated() {
    this.stopLiveStatsRefresh()
  },
  beforeDestroy() {
    this.stopLiveStatsRefresh()
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
      return file.rawUrl || (file.response && file.response.data && file.response.data.url) || file.url || ''
    },
    displayImageUrl(url) {
      if (!url || /^(https?:)?\/\//i.test(url) || url.startsWith('data:') || url.startsWith('blob:')) {
        return url || ''
      }
      const baseApi = (process.env.VUE_APP_BASE_API || '').replace(/\/+$/, '')
      return baseApi + (url.startsWith('/') ? url : '/' + url)
    },
    moneyText(value) {
      const amount = Number(value || 0)
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    centMoneyText(value) {
      const amount = Number(value || 0) / 100
      return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
    },
    discountText(value) {
      const discount = Number(value)
      return Number.isFinite(discount) && discount > 0 ? discount.toFixed(1) + '折' : '-'
    },
    orderStatusInfo(status) {
      return this.orderStatusMap[status] || { text: '未知状态', type: 'info' }
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
        this.merchant = this.normalizeMerchantDetail(res.data)
        this.carouselForm = {
          id: this.merchantId,
          carouselImages: this.merchant.carouselImages || ''
        }
        await this.loadMerchantEntryQrCode(true)
      } catch (e) {
        this.$message.error('获取商家详情失败')
      } finally {
        this.loading = false
      }
    },
    normalizeMerchantDetail(data) {
      const merchant = data || {}
      return {
        ...merchant,
        productCount: Number(merchant.productCount || 0),
        totalIncome: Number(merchant.totalIncome || 0),
        todayIncome: Number(merchant.todayIncome || 0),
        wxProfitSharingEnabled: Number(merchant.wxProfitSharingEnabled || 0),
        merchantShareRate: merchant.merchantShareRate === null || merchant.merchantShareRate === undefined ? 100 : Number(merchant.merchantShareRate),
        platformShareRate: merchant.platformShareRate === null || merchant.platformShareRate === undefined ? 0 : Number(merchant.platformShareRate),
        distributorShareRate: merchant.distributorShareRate === null || merchant.distributorShareRate === undefined ? 0 : Number(merchant.distributorShareRate),
        profitSharingContractVersion: merchant.profitSharingContractVersion || '',
        settlementCycle: merchant.settlementCycle || 'T1'
      }
    },
    startLiveStatsRefresh() {
      this.stopLiveStatsRefresh()
      this._liveStatsRefreshTimer = setInterval(() => this.refreshLiveStats(), MERCHANT_DETAIL_REFRESH_INTERVAL)
    },
    stopLiveStatsRefresh() {
      if (this._liveStatsRefreshTimer) {
        clearInterval(this._liveStatsRefreshTimer)
        this._liveStatsRefreshTimer = null
      }
    },
    async refreshLiveStats() {
      if (!this.merchantId) return
      try {
        const res = await getMerchantLiveStats(this.merchantId)
        const stats = res.data || {}
        this.merchant = {
          ...this.merchant,
          productCount: Number(stats.productCount || 0),
          totalIncome: Number(stats.totalIncome || 0),
          todayIncome: Number(stats.todayIncome || 0)
        }
      } catch (e) {
        // Polling failures must not interrupt merchant detail operations.
      }
    },
    applySavedMerchant(res) {
      if (res && res.data) {
        this.merchant = this.normalizeMerchantDetail({ ...this.merchant, ...res.data })
        this.carouselForm = {
          id: this.merchantId,
          carouselImages: this.merchant.carouselImages || ''
        }
        this.refreshLiveStats()
        return true
      }
      return false
    },

    async saveCarouselImages() {
      this.carouselSaving = true
      try {
        const res = await updateMerchant({
          id: this.merchantId,
          carouselImages: this.carouselForm.carouselImages || ''
        })
        if (!this.applySavedMerchant(res)) await this.fetchDetail()
        this.$message.success('店铺轮播图保存成功')
      } finally {
        this.carouselSaving = false
      }
    },

    async loadPrimaryStore() {
      this.storeLoading = true
      try {
        const res = await getMerchantStore(this.merchantId)
        this.primaryStore = res.data || null
        this.storeLoaded = true
      } finally {
        this.storeLoading = false
      }
    },
    coordinateText(value) {
      if (value === null || value === undefined || value === '') return '-'
      const coordinate = Number(value)
      return Number.isFinite(coordinate) ? coordinate.toFixed(7) : '-'
    },
    normalizeCoordinate(value) {
      if (value === null || value === undefined || value === '') return null
      const coordinate = Number(value)
      return Number.isFinite(coordinate) ? coordinate : null
    },
    openStoreDialog() {
      const source = this.primaryStore || {}
      this.storeForm = {
        name: source.name || this.merchant.name || '',
        contact: source.contact || this.merchant.contact || '',
        phone: source.phone || this.merchant.phone || '',
        address: source.address || this.merchant.address || '',
        businessHours: source.businessHours || this.merchant.businessHours || '',
        avatar: source.avatar || this.merchant.avatar || this.merchant.logo || '',
        longitude: this.normalizeCoordinate(source.longitude),
        latitude: this.normalizeCoordinate(source.latitude),
        status: source.status === 0 ? 0 : 1
      }
      this.storeDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.storeForm) this.$refs.storeForm.clearValidate()
      })
    },
    submitStoreForm() {
      this.$refs.storeForm.validate(async valid => {
        if (!valid) return
        this.storeSaving = true
        try {
          const creating = !this.primaryStore
          const payload = {
            ...this.storeForm,
            longitude: this.normalizeCoordinate(this.storeForm.longitude),
            latitude: this.normalizeCoordinate(this.storeForm.latitude)
          }
          const res = creating
            ? await createMerchantStore(this.merchantId, payload)
            : await updateMerchantStore(this.merchantId, payload)
          this.primaryStore = res.data || payload
          this.storeLoaded = true
          this.storeDialogVisible = false
          this.merchant = { ...this.merchant, storeCount: 1 }
          this.$message.success(creating ? '主门店已创建' : '门店信息已保存')
        } finally {
          this.storeSaving = false
        }
      })
    },

    async loadMerchantEntryQrCode(silent = false) {
      if (!this.canGenerateEntryCode) {
        this.merchantEntry = { qrCodeUrl: '', loginPage: '', entryAppId: '', scene: '' }
        if (!silent) {
          this.$message.warning('请先配置商户小程序 AppID 和 Secret')
        }
        return
      }
      if (!silent) {
        this.entryQrLoading = true
      }
      try {
        const res = await getMerchantEntryQrCode(this.merchantId)
        this.merchantEntry = res.data || {}
      } catch (e) {
        if (!silent) {
          this.$message.error((e && e.message) || '生成入口码失败')
        }
      } finally {
        if (!silent) {
          this.entryQrLoading = false
        }
      }
    },
    copyEntryLoginPage() {
      const text = this.entryLoginPage
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success('登录页路径已复制')
        }).catch(() => {
          this.$message.info(text)
        })
        return
      }
      this.$message.info(text)
    },

    // ========== 商品管理 ==========
    async loadProductCategories() {
      const res = await getProductCategoryList({ merchantId: this.merchantId, status: 1 })
      const list = Array.isArray(res.data) ? res.data : []
      this.productCategoryOptions = list.map(item => ({
        ...item,
        id: Number(item.id),
        sort: Number(item.sort || 0)
      }))
    },
    categoryNameText(categoryId) {
      const id = Number(categoryId)
      const category = this.productCategoryOptions.find(item => Number(item.id) === id)
      return category ? category.name : (categoryId || '-')
    },
    async handleAddCategory() {
      try {
        const { value } = await this.$prompt('请输入分类名称，例如：水果、套餐、饮品', '新增商品分类', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '请输入分类名称',
          inputValidator: val => {
            const name = String(val || '').trim()
            if (!name) return '分类名称不能为空'
            if (name.length > 50) return '分类名称不能超过50个字符'
            return true
          }
        })
        const name = String(value || '').trim()
        const res = await addProductCategory({ merchantId: this.merchantId, name, sort: 0, status: 1 })
        await this.loadProductCategories()
        if (res && res.data && res.data.id) {
          this.productForm.categoryId = Number(res.data.id)
        }
        this.$message.success('分类已准备好')
      } catch (e) {
        // 用户取消输入时不提示错误。
      }
    },
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
      this.loadProductCategories()
      this.productDialogTitle = '新增商品'
      this.productForm = { id: null, merchantId: this.merchantId, name: '', categoryId: null, originalPrice: 0, price: 0, stock: 0, validDays: 30, mainImage: '', coverImage: '', description: '' }
      this.productDialogVisible = true
      this.$nextTick(() => { this.$refs.productForm && this.$refs.productForm.clearValidate() })
    },
    handleEditProduct(row) {
      this.loadProductCategories()
      this.productDialogTitle = '编辑商品'
      this.productForm = {
        ...row,
        categoryId: row.categoryId === null || row.categoryId === undefined ? null : Number(row.categoryId),
        originalPrice: Number(row.originalPrice || 0),
        price: Number(row.price || 0),
        stock: Number(row.stock || 0),
        validDays: Number(row.validDays || 30)
      }
      this.productDialogVisible = true
      this.$nextTick(() => { this.$refs.productForm && this.$refs.productForm.clearValidate() })
    },
    submitProductForm() {
      this.$refs.productForm.validate(async valid => {
        if (!valid) return
        const payload = {
          ...this.productForm,
          merchantId: this.merchantId,
          categoryId: Number(this.productForm.categoryId)
        }
        if (!Number.isFinite(payload.categoryId)) {
          this.$message.error('请选择正确的商品分类')
          return
        }
        if (payload.id) {
          await updateProduct(payload)
          this.$message.success('修改成功')
        } else {
          payload.status = 1
          await addProduct(payload)
          this.$message.success('新增成功')
        }
        this.productDialogVisible = false
        await this.loadProducts()
        await this.refreshLiveStats()
      })
    },
    handleToggleStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const text = newStatus === 1 ? '上架' : '下架'
      this.$confirm(`确认${text}该商品？`, '提示', { type: 'warning' }).then(async() => {
        await updateProduct({ id: row.id, status: newStatus })
        this.$message.success(`${text}成功`)
        await this.loadProducts()
        await this.refreshLiveStats()
      }).catch(() => {})
    },
    handleDeleteProduct(row) {
      this.$confirm('确认删除该商品？', '提示', { type: 'warning' }).then(async() => {
        await deleteProduct(row.id)
        this.$message.success('删除成功')
        await this.loadProducts()
        await this.refreshLiveStats()
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
      this.$confirm(`确认重置 ${row.username} 的密码为系统默认密码？`, '提示', { type: 'warning' }).then(async() => {
        await resetMerchantUserPwd(row.id, '')
        this.$message.success('密码已重置为系统默认密码')
      }).catch(() => {})
    },
    handleToggleAccountStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      const text = newStatus === 1 ? '启用' : '禁用'
      this.$confirm(`确认${text}账号 ${row.username}？`, '提示', { type: 'warning' }).then(async() => {
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
        cMiniAppSecret: this.merchant.cMiniAppSecretConfigured ? '******' : ''
      }
      this.miniAppDialogVisible = true
    },
    async submitMiniAppForm() {
      const payload = { ...this.miniAppForm }
      const res = await updateMerchant(payload)
      if (!this.applySavedMerchant(res)) await this.fetchDetail()
      await this.loadMerchantEntryQrCode(true)
      this.miniAppDialogVisible = false
      this.$message.success('保存成功，详情已刷新')
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
      const res = await updateMerchant(this.mapClaimForm)
      if (!this.applySavedMerchant(res)) await this.fetchDetail()
      this.mapClaimDialogVisible = false
      this.$message.success('保存成功，详情已刷新')
    },

    // ========== 微信特约商户 ==========
    wxPaymentAccessTypeText(type) {
      const map = { EXISTING_MCH: '已有微信支付商户号', APPLYMENT_ASSISTED: '平台协助申请' }
      return map[type] || '已有微信支付商户号'
    },
    percentText(value) {
      return value === null || value === undefined || value === '' ? '待填写' : value + '%'
    },
    configText(value, emptyText = '待填写') {
      return value === null || value === undefined || String(value).trim() === '' ? emptyText : value
    },
    secretStatusText(configured) {
      return configured ? '已配置' : '待填写'
    },
    handleEditWxApplyment() {
      this.wxApplymentForm = {
        id: this.merchant.id,
        wxPaymentAccessType: this.merchant.wxPaymentAccessType || 'EXISTING_MCH',
        merchantWxMchId: this.merchant.merchantWxMchId || '',
        merchantWxMchName: this.merchant.merchantWxMchName || '',
        wxProfitSharingEnabled: Number(this.merchant.wxProfitSharingEnabled || 0),
        receiverOpenid: '',
        profitSharingContractVersion: this.merchant.profitSharingContractVersion || '',
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
      const payload = {
        ...this.wxApplymentForm,
        wxProfitSharingEnabled: Number(this.wxApplymentForm.wxProfitSharingEnabled || 0),
        receiverOpenid: ''
      }
      const res = await updateMerchant(payload)
      if (!this.applySavedMerchant(res)) await this.fetchDetail()
      this.wxApplymentDialogVisible = false
      this.$message.success('保存成功，详情已刷新')
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
        this.detailFileList = JSON.parse(this.grouponForm.detailImages).map(url => ({ rawUrl: url, url: this.displayImageUrl(url), name: url.split('/').pop() }))
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
        file.rawUrl = res.data.url
        file.url = this.displayImageUrl(res.data.url)
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
      this.itemDetailFileList = JSON.parse(this.itemForm.detailImages).map(url => ({ rawUrl: url, url: this.displayImageUrl(url), name: url.split('/').pop() }))
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
        file.rawUrl = res.data.url
        file.url = this.displayImageUrl(res.data.url)
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
      const map = { 0: '禁用', 1: '正常', 2: '待审核', 3: '停止合作' }
      return map[status] || '未知'
    },
    statusTagType(status) {
      const map = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
      return map[status] || 'info'
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'store' && !this.storeLoaded) {
        this.loadPrimaryStore()
      } else if (val === 'groupon' && this.grouponList.length === 0) {
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
.store-toolbar {
  min-height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.store-content {
  min-height: 240px;
}
.store-avatar {
  width: 96px;
  height: 72px;
  border-radius: 4px;
  overflow: hidden;
  vertical-align: middle;
}
.store-coordinate-input {
  width: 100%;
}
.section-header {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-title {
  margin: 0;
}
.carousel-section-header {
  margin-top: 24px;
}
.carousel-help {
  margin-left: 5px;
  color: #909399;
  font-size: 15px;
  cursor: help;
}
.carousel-upload-panel {
  min-height: 168px;
  margin-top: 10px;
  padding: 18px 18px 6px;
  border: 1px solid #EBEEF5;
  background: #FAFAFA;
}
.entry-panel {
  margin-top: 12px;
  padding: 18px;
  border: 1px solid #D9E6FF;
  border-radius: 12px;
  display: flex;
  gap: 18px;
  background: linear-gradient(135deg, #F7FBFF 0%, #FFFFFF 62%);
}
.entry-panel__main {
  flex: 1;
  min-width: 0;
}
.entry-panel__title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.entry-panel__title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.entry-panel__desc {
  margin-top: 8px;
  line-height: 1.7;
  color: #606266;
}
.entry-panel__path {
  margin-top: 14px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #F2F7FF;
  display: flex;
  gap: 10px;
  font-size: 13px;
}
.entry-panel__path-label {
  color: #5C7EA8;
  flex-shrink: 0;
}
.entry-panel__path-value {
  color: #303133;
  word-break: break-all;
}
.entry-panel__steps {
  margin-top: 14px;
  display: grid;
  gap: 8px;
}
.entry-step {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 13px;
}
.entry-step__index {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #1677FF;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}
.entry-panel__actions {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.entry-panel__preview {
  width: 188px;
  min-width: 188px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.entry-panel__image {
  width: 172px;
  height: 172px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #EBEEF5;
  background: #fff;
}
.entry-panel__empty {
  width: 172px;
  height: 172px;
  border: 1px dashed #B9CEF2;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  justify-content: center;
  padding: 16px;
  box-sizing: border-box;
  text-align: center;
  color: #6D87AD;
  background: #fff;
}
.entry-panel__empty i {
  font-size: 28px;
  color: #1677FF;
}
.category-picker {
  display: flex;
  align-items: center;
  gap: 10px;
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
.form-tip {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
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
