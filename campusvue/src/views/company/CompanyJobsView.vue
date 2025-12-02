<template>
  <section class="card jobs-page">
    <header class="card__title header">
      <div>
        <p class="eyebrow">岗位发布 · 管理</p>
        <h2>职位管理</h2>
        <p class="muted">创建、更新职位，实时查看状态。</p>
      </div>
      <div class="head-actions">
        <button class="outline" @click="loadJobs">刷新</button>
        <button class="ghost" type="button" @click="resetJobForm">清空表单</button>
      </div>
    </header>

    <form class="form-grid" @submit.prevent="submitJob">
      <label class="full">
        职位名称
        <input v-model="jobForm.jobTitle" required maxlength="100" placeholder="如：前端实习生" />
      </label>
      <label>
        职位类型
        <input v-model="jobForm.jobType" maxlength="50" placeholder="全职 / 实习 / 校招" />
      </label>
      <label>
        薪资范围
        <input v-model="jobForm.salaryRange" maxlength="50" placeholder="例：15k-25k · 13 薪" />
      </label>
      <label>
        工作地点
        <input v-model="jobForm.location" maxlength="100" placeholder="城市 · 办公形式" />
      </label>
      <label class="full">
        岗位要求
        <textarea v-model="jobForm.requirement" maxlength="255" placeholder="核心技能、经验要求"></textarea>
      </label>
      <label class="full">
        职位描述
        <textarea v-model="jobForm.description" maxlength="255" placeholder="岗位亮点、团队介绍"></textarea>
      </label>
      <div class="full actions">
        <button class="primary" type="submit">{{ jobForm.jobId ? '更新职位' : '发布职位' }}</button>
        <button class="outline" type="button" @click="resetJobForm">重置</button>
      </div>
    </form>

    <div class="table-wrapper" v-if="jobs.length">
      <table class="table">
        <thead>
          <tr>
            <th>职位</th>
            <th>类型</th>
            <th>地点</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="job in jobs" :key="job.id">
            <td>
              <div class="job-info">
                <strong>{{ job.jobTitle }}</strong>
                <small class="muted">{{ job.salaryRange || '薪资面议' }}</small>
              </div>
            </td>
            <td>{{ job.jobType || '不限' }}</td>
            <td>{{ job.location || '不限' }}</td>
            <td>
              <span class="status-pill" :class="statusClass(job.status)">{{ statusLabel(job.status) }}</span>
            </td>
            <td class="actions">
              <button class="outline" type="button" @click="prefillJob(job)">编辑</button>
              <select v-model="job.status" @change="updateJobStatus(job)">
                <option value="pending">待审核</option>
                <option value="approved">已发布</option>
                <option value="rejected">已拒绝</option>
                <option value="closed">已关闭</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else class="muted">暂无已发布职位</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, post, put, patch } from '../../api/http';

const jobForm = reactive({
  jobId: null,
  jobTitle: '',
  jobType: '',
  salaryRange: '',
  location: '',
  requirement: '',
  description: ''
});

const jobs = ref([]);
const feedback = reactive({ message: '', type: 'info' });

onMounted(() => {
  loadJobs();
});

async function loadJobs() {
  try {
    jobs.value = await get('/portal/company/jobs');
  } catch (error) {
    showFeedback(error.message ?? '加载职位失败', 'error');
  }
}

async function submitJob() {
  if (!jobForm.jobTitle) {
    showFeedback('请输入职位名称', 'error');
    return;
  }
  const payload = {
    jobTitle: jobForm.jobTitle,
    jobType: jobForm.jobType,
    salaryRange: jobForm.salaryRange,
    location: jobForm.location,
    requirement: jobForm.requirement,
    description: jobForm.description
  };
  try {
    if (jobForm.jobId) {
      await put(`/portal/company/jobs/${jobForm.jobId}`, payload);
      showFeedback('职位已更新', 'success');
    } else {
      await post('/portal/company/jobs', payload);
      showFeedback('职位已提交审核', 'success');
    }
    resetJobForm();
    await loadJobs();
  } catch (error) {
    showFeedback(error.message ?? '保存职位失败', 'error');
  }
}

function prefillJob(job) {
  Object.assign(jobForm, job, { jobId: job.id });
}

async function updateJobStatus(job) {
  try {
    const updated = await patch(`/portal/company/jobs/${job.id}/status`, { status: job.status });
    Object.assign(job, updated);
    showFeedback('职位状态已更新', 'success');
  } catch (error) {
    showFeedback(error.message ?? '更新职位状态失败', 'error');
  }
}

function resetJobForm() {
  jobForm.jobId = null;
  jobForm.jobTitle = '';
  jobForm.jobType = '';
  jobForm.salaryRange = '';
  jobForm.location = '';
  jobForm.requirement = '';
  jobForm.description = '';
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

function statusLabel(status) {
  if (status === 'approved') return '已发布';
  if (status === 'rejected') return '已拒绝';
  if (status === 'closed') return '已关闭';
  return '待审核';
}

function statusClass(status) {
  return {
    success: status === 'approved',
    danger: status === 'rejected',
    warning: status !== 'approved' && status !== 'rejected'
  };
}
</script>

<style scoped>
.jobs-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.eyebrow {
  margin: 0;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #2563eb;
  font-weight: 700;
}

.head-actions {
  display: flex;
  gap: 10px;
}

.ghost {
  border: 1px dashed var(--border-subtle, #d1d5db);
  background: transparent;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 10px;
}

.form-grid input,
.form-grid textarea {
  border-radius: 12px;
  border: 1px solid #d1d5db;
  padding: 10px 12px;
  font-size: 14px;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.form-grid input:focus,
.form-grid textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.25);
  outline: none;
}

textarea {
  min-height: 90px;
  resize: vertical;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.table-wrapper {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.job-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.6);
  background: #f1f5f9;
  color: #0f172a;
  font-weight: 700;
  font-size: 12px;
}

.status-pill.success {
  background: #dcfce7;
  border-color: rgba(34, 197, 94, 0.4);
  color: #15803d;
}

.status-pill.warning {
  background: #fef3c7;
  border-color: rgba(251, 191, 36, 0.4);
  color: #b45309;
}

.status-pill.danger {
  background: #fee2e2;
  border-color: rgba(248, 113, 113, 0.4);
  color: #b91c1c;
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
