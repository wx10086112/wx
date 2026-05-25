<template>
  <div class="app-container">
    <el-card>
      <div slot="header"><span>已完成</span></div>
      <div class="placeholder">已完成页面开发中...</div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'OrderCompleted'
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
