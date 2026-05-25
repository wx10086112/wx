<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>参数配置</span></div>
      <el-table :data="[]" border style="width: 100%">
        <el-table-column prop="configName" label="参数名称" />
        <el-table-column prop="configKey" label="参数键名" />
        <el-table-column prop="configValue" label="参数键值" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
      <div class="placeholder" style="margin-top: 20px;">参数配置页面开发中...</div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ParamConfig'
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
