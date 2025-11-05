<template>
  <section class="card">
    <div class="card__title">
      <h2>平台概览</h2>
      <button class="outline" @click="loadSummary">刷新</button>
    </div>

    <div v-if="summary" class="summary-grid">
      <div class="summary-item"><span>学生人数</span><strong>{{ summary.totalStudents }}</strong></div>
      <div class="summary-item"><span>企业数量</span><strong>{{ summary.totalCompanies }}</strong></div>
      <div class="summary-item"><span>待审企业</span><strong>{{ summary.pendingCompanies }}</strong></div>
      <div class="summary-item"><span>职位总数</span><strong>{{ summary.totalJobs }}</strong></div>
      <div class="summary-item"><span>已发布职位</span><strong>{{ summary.approvedJobs }}</strong></div>
      <div class="summary-item"><span>待审核职位</span><strong>{{ summary.pendingJobs }}</strong></div>
      <div class="summary-item"><span>投递总量</span><strong>{{ summary.totalApplications }}</strong></div>
      <div class="summary-item"><span>管理员未读消息</span><strong>{{ summary.unreadMessages }}</strong></div>
    </div>

    <div v-if="summary" class="status-breakdown">
      <h3>投递状态统计</h3>
      <ul>
        <li v-for="(value, key) in summary.statusBreakdown" :key="key">{{ key }}：{{ value }}</li>
      </ul>
    </div>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get } from '../../api/http';

const summary = ref(null);
const feedback = reactive({ message: '', type: 'info' });

onMounted(() => {
  loadSummary();
});

async function loadSummary() {
  try {
    summary.value = await get('/portal/admin/summary');
    feedback.message = '';
  } catch (error) {
    showFeedback(error.message ?? '加载概览失败', 'error');
  }
}

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}
</script>

<style scoped>
.summary-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.summary-item {
  background: #eff6ff;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #1e3a8a;
}

.summary-item span {
  font-size: 14px;
  color: #475569;
}

.summary-item strong {
  font-size: 22px;
}

.status-breakdown ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 8px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.feedback {
  padding: 10px 14px;
  border-radius: 12px;
  text-align: center;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}
</style>
