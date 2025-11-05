<template>
  <section class="card">
    <div class="card__title">
      <h2>简历投递</h2>
      <button class="outline" @click="reload">刷新</button>
    </div>

    <table v-if="applications.length" class="table">
      <thead>
        <tr><th>职位</th><th>学生ID</th><th>状态</th><th>备注</th><th>更新时间</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="app in applications" :key="app.id">
          <td>{{ resolveJobTitle(app.jobId) }}</td>
          <td>{{ app.studentId }}</td>
          <td>{{ app.status }}</td>
          <td>
            <input v-model="app.decisionNote" maxlength="500" placeholder="备注（可选）" @change="updateStatus(app)" />
          </td>
          <td>{{ formatDate(app.updateTime) }}</td>
          <td class="actions">
            <select v-model="app.status" @change="updateStatus(app)">
              <option value="待查阅">待查阅</option>
              <option value="已查阅">已查阅</option>
              <option value="面试">面试</option>
              <option value="录用">录用</option>
              <option value="拒绝">拒绝</option>
            </select>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无投递</p>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { get, patch } from '../../api/http';
import { useToast } from '../../ui/toast';

const applications = ref([]);
const jobs = ref([]);
const toast = useToast();

const jobMap = computed(() => {
  const map = new Map();
  jobs.value.forEach(job => { map.set(job.id, job.jobTitle); });
  return map;
});

onMounted(reload);

async function reload() { await Promise.all([loadJobs(), loadApplications()]); }

async function loadJobs() {
  try { jobs.value = await get('/portal/company/jobs'); } catch {}
}

async function loadApplications() {
  try {
    const items = await get('/portal/company/applications');
    applications.value = items.map(normalizeApplication);
  } catch (error) {
    toast.error(error.message ?? '加载投递记录失败');
  }
}

function normalizeApplication(item) {
  return { id: item.id, jobId: item.jobId, studentId: item.studentId, status: item.status, decisionNote: item.decisionNote || '', updateTime: item.updateTime };
}

async function updateStatus(app) {
  try {
    const updated = await patch(`/portal/company/applications/${app.id}`, { status: app.status, decisionNote: app.decisionNote });
    Object.assign(app, normalizeApplication(updated));
    toast.success('投递状态已更新');
  } catch (error) {
    toast.error(error.message ?? '更新状态失败');
  }
}

function resolveJobTitle(jobId) { return jobMap.value.get(jobId) || `职位 #${jobId}`; }
function formatDate(value) { if (!value) return '-'; return new Date(value).toLocaleString(); }
</script>

<style scoped>
.table input { min-width: 160px; }
.actions { display: flex; gap: 12px; }
</style>

