<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>菜单管理</span></div>
      <el-table :data="[]" border style="width: 100%">
        <el-table-column prop="menuName" label="菜单名称" />
        <el-table-column prop="icon" label="图标" width="80" />
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column prop="path" label="路由地址" />
        <el-table-column prop="component" label="组件路径" />
        <el-table-column prop="status" label="状态" width="80" />
      </el-table>
      <div class="placeholder" style="margin-top: 20px;">菜单管理页面开发中...</div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'MenuConfig'
}
</script>

<style scoped>
.placeholder {
  padding: 40px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style>
