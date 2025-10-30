<template>
  <section class="card">
    <h2>平台概览</h2>
    <div class="summary-grid" v-if="summary">
      <div class="summary-item"><span>学生人数</span><strong>{{ summary.totalStudents }}</strong></div>
      <div class="summary-item"><span>企业数量</span><strong>{{ summary.totalCompanies }}</strong></div>
      <div class="summary-item"><span>待审企业</span><strong>{{ summary.pendingCompanies }}</strong></div>
      <div class="summary-item"><span>职位总数</span><strong>{{ summary.totalJobs }}</strong></div>
      <div class="summary-item"><span>已发布职位</span><strong>{{ summary.approvedJobs }}</strong></div>
      <div class="summary-item"><span>待审核职位</span><strong>{{ summary.pendingJobs }}</strong></div>
      <div class="summary-item"><span>投递总量</span><strong>{{ summary.totalApplications }}</strong></div>
      <div class="summary-item"><span>管理员未读消息</span><strong>{{ summary.unreadMessages }}</strong></div>
    </div>
    <p v-else class="muted">正在加载平台概览信息…</p>
  </section>
  <section class="card" v-if="summary">
    <div class="status-breakdown">
      <h3>投递状态统计</h3>
      <ul>
        <li v-for="(value, key) in summary.statusBreakdown" :key="key">{{ key }}：{{ value }}</li>
      </ul>
    </div>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const admin = inject('adminContext');
if (!admin) {
  throw new Error('adminContext not provided');
}

const { summary } = admin;
</script>

<style scoped>
.status-breakdown ul {
  padding-left: 18px;
  margin: 0;
  color: #475569;
}
</style>
