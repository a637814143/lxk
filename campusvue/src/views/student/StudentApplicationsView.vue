<template>
  <section class="section">
    <header class="section__header">
      <div>
        <h2>我的投递</h2>
        <p class="muted">跟踪各岗位的投递状态与企业反馈</p>
      </div>
      <button class="outline" type="button" @click="refresh" :disabled="loading">
        {{ loading ? '刷新中…' : '刷新' }}
      </button>
    </header>

    <table v-if="applications.length" class="table">
      <thead>
        <tr>
          <th>职位</th>
          <th>企业</th>
          <th>状态</th>
          <th>企业备注</th>
          <th>投递时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="app in applications" :key="app.id">
          <td>{{ resolveJobTitle(app.jobId) }}</td>
          <td>{{ resolveCompanyName(app.companyId) }}</td>
          <td>{{ app.status }}</td>
          <td>{{ app.decisionNote || '—' }}</td>
          <td>{{ formatDate(app.applyTime) }}</td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无投递记录</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get } from '../../api/http';

const applications = ref([]);
const jobs = ref([]);
const loading = ref(false);
const feedback = reactive({ message: '', type: 'info' });

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}

async function loadApplications() {
  try {
    loading.value = true;
    const items = await get('/portal/student/applications');
    applications.value = items.map(item => ({
      ...item,
      decisionNote: item.decisionNote ?? ''
    }));
  } catch (error) {
    showFeedback(error.message, 'error');
  } finally {
    loading.value = false;
  }
}

async function loadJobs() {
  try {
    jobs.value = await get('/portal/student/jobs');
  } catch (error) {
    console.warn('加载职位信息失败，用于显示投递表格', error);
  }
}

function resolveJobTitle(jobId) {
  const job = jobs.value.find(item => item.id === jobId);
  return job ? job.jobTitle : `职位 #${jobId}`;
}

function resolveCompanyName(companyId) {
  const job = jobs.value.find(item => item.companyId === companyId);
  return job?.companyName ?? `企业 #${companyId}`;
}

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

async function refresh() {
  await Promise.all([loadApplications(), loadJobs()]);
}

onMounted(refresh);
</script>

<style scoped>
.section {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  text-align: left;
  padding: 10px 12px;
  border-bottom: 1px solid #e5e7eb;
}

.muted {
  color: #6b7280;
  margin: 0;
}

.outline {
  background: transparent;
  border: 1px solid #2563eb;
  color: #2563eb;
  padding: 8px 16px;
  border-radius: 10px;
  cursor: pointer;
}

.feedback {
  text-align: center;
  padding: 12px;
  border-radius: 12px;
}

.feedback.success {
  background: #dcfce7;
  color: #15803d;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}
</style>
