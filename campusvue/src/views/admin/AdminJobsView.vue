<template>
  <section class="card">
    <div class="card__title">
      <h2>职位概览</h2>
      <button class="outline" @click="loadJobs">刷新</button>
    </div>

    <p class="muted">
      职位发布现已自动生效，无需管理员审核。如需处理异常职位，可直接联系发布企业。
    </p>

    <table v-if="jobs.length" class="table">
      <thead>
        <tr><th>职位名称</th><th>企业ID</th><th>状态</th><th>更新时间</th></tr>
      </thead>
      <tbody>
        <tr v-for="job in jobs" :key="job.id">
          <td>{{ job.jobTitle }}</td>
          <td>{{ job.companyId }}</td>
          <td>{{ renderStatus(job.status) }}</td>
          <td>{{ formatDate(job.updateTime) }}</td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无职位数据。</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get } from '../../api/http';

const jobs = ref([]);
const feedback = reactive({ message: '', type: 'info' });

onMounted(() => {
  loadJobs();
});

async function loadJobs() {
  try {
    jobs.value = await get('/jobs');
  } catch (error) {
    showFeedback(error.message ?? '加载职位失败', 'error');
  }
}

function renderStatus(status) {
  switch ((status || '').toLowerCase()) {
    case 'approved':
      return '已发布';
    case 'closed':
      return '已关闭';
    case 'pending':
      return '待处理';
    case 'rejected':
      return '已驳回';
    default:
      return status || '—';
  }
}

function formatDate(value) {
  if (!value) {
    return '-';
  }
  return new Date(value).toLocaleString();
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
.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}

.feedback {
  padding: 10px 14px;
  border-radius: 12px;
  text-align: center;
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
