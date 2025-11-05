<template>
  <section class="card">
    <div class="card__title">
      <h2>职位管理</h2>
      <button class="outline" @click="loadJobs">刷新</button>
    </div>

    <form class="form-grid" @submit.prevent="submitJob">
      <label class="full">职位名称<input v-model="jobForm.jobTitle" required maxlength="100" /></label>
      <label>职位类型<input v-model="jobForm.jobType" maxlength="50" /></label>
      <label>薪资范围<input v-model="jobForm.salaryRange" maxlength="50" /></label>
      <label>工作地点<input v-model="jobForm.location" maxlength="100" /></label>
      <label class="full">岗位要求<textarea v-model="jobForm.requirement" maxlength="255"></textarea></label>
      <label class="full">职位描述<textarea v-model="jobForm.description" maxlength="255"></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">{{ jobForm.jobId ? '更新职位' : '发布职位' }}</button>
        <button class="outline" type="button" @click="resetJobForm">重置</button>
      </div>
    </form>

    <table v-if="jobs.length" class="table">
      <thead>
        <tr><th>职位</th><th>类型</th><th>地点</th><th>状态</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="job in jobs" :key="job.id">
          <td>{{ job.jobTitle }}</td>
          <td>{{ job.jobType || '不限' }}</td>
          <td>{{ job.location || '不限' }}</td>
          <td><span class="status">{{ renderStatus(job.status) }}</span></td>
          <td class="actions">
            <button class="outline" type="button" @click="prefillJob(job)">编辑</button>
            <select v-model="job.status" @change="updateJobStatus(job)">
              <option value="approved">已发布</option>
              <option value="closed">已关闭</option>
            </select>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂未发布职位</p>

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
      showFeedback('职位已发布，学生可立即投递', 'success');
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

function renderStatus(status) {
  switch ((status || '').toLowerCase()) {
    case 'approved':
      return '已发布';
    case 'closed':
      return '已关闭';
    default:
      return status || '—';
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
</script>

<style scoped>
.status {
  padding: 4px 10px;
  background: #e0f2fe;
  color: #0369a1;
  border-radius: 999px;
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
