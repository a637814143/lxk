<template>
  <div class="dashboard">
    <header class="dashboard__header">
      <div>
        <h1>企业工作台</h1>
        <p>欢迎回来，{{ authInfo?.roleDisplayName }} {{ authInfo?.username }}</p>
      </div>
      <button class="outline" @click="handleLogout">退出登录</button>
    </header>

    <section class="card">
      <h2>企业资料</h2>
      <form class="form-grid" @submit.prevent="saveProfile">
        <label>企业名称<input v-model="profileForm.companyName" required /></label>
        <label>营业执照号<input v-model="profileForm.licenseNumber" /></label>
        <label>行业类别<input v-model="profileForm.industry" /></label>
        <label>企业地址<input v-model="profileForm.address" /></label>
        <label>企业官网<input v-model="profileForm.website" /></label>
        <label class="full">企业简介<textarea v-model="profileForm.description"></textarea></label>
        <label class="full">Logo 链接<input v-model="profileForm.logo" /></label>
        <div class="full actions">
          <button class="primary" type="submit">保存资料</button>
        </div>
      </form>
    </section>

    <section class="card">
      <div class="card__title">
        <h2>职位管理</h2>
        <button class="outline" @click="loadJobs">刷新</button>
      </div>
      <form class="form-grid" @submit.prevent="createJob">
        <label class="full">职位名称<input v-model="jobForm.jobTitle" required /></label>
        <label>职位类型<input v-model="jobForm.jobType" /></label>
        <label>薪资范围<input v-model="jobForm.salaryRange" /></label>
        <label>工作地点<input v-model="jobForm.location" /></label>
        <label class="full">岗位要求<textarea v-model="jobForm.requirement"></textarea></label>
        <label class="full">职位描述<textarea v-model="jobForm.description"></textarea></label>
        <div class="full actions">
          <button class="primary" type="submit">发布职位</button>
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
            <td><span class="status">{{ job.status }}</span></td>
            <td class="actions">
              <button class="outline" @click="prefillJob(job)">编辑</button>
              <select v-model="job.status" @change="changeJobStatus(job)">
                <option value="pending">待审核</option>
                <option value="approved">已发布</option>
                <option value="rejected">已拒绝</option>
                <option value="closed">已关闭</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">暂未发布职位</p>
    </section>

    <section class="card">
      <div class="card__title">
        <h2>简历投递</h2>
        <button class="outline" @click="loadApplications">刷新</button>
      </div>
      <table v-if="applications.length" class="table">
        <thead>
          <tr><th>职位</th><th>学生ID</th><th>状态</th><th>更新时间</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="app in applications" :key="app.id">
            <td>{{ resolveJobTitle(app.jobId) }}</td>
            <td>{{ app.studentId }}</td>
            <td>{{ app.status }}</td>
            <td>{{ formatDate(app.updateTime) }}</td>
            <td class="actions">
              <select v-model="app.status" @change="updateApplicationStatus(app)">
                <option value="待查看">待查看</option>
                <option value="已查看">已查看</option>
                <option value="面试中">面试中</option>
                <option value="录用">录用</option>
                <option value="拒绝">拒绝</option>
              </select>
              <button class="outline" @click="openMessageDialog(app)">发送消息</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">暂无投递</p>
    </section>

    <section v-if="messageDialog.visible" class="card">
      <h2>向学生发送消息</h2>
      <form class="form-grid" @submit.prevent="sendMessage">
        <label>标题<input v-model="messageDialog.form.title" required /></label>
        <label class="full">内容<textarea v-model="messageDialog.form.content" required></textarea></label>
        <div class="full actions">
          <button class="primary" type="submit">发送</button>
          <button class="outline" type="button" @click="closeMessageDialog">取消</button>
        </div>
      </form>
    </section>

    <section class="card">
      <h2>平台公告</h2>
      <ul class="list" v-if="announcements.length">
        <li v-for="item in announcements" :key="item.id" class="list__item">
          <div>
            <h3>{{ item.title }}</h3>
            <p class="muted">发布时间：{{ formatDate(item.publishTime) }}</p>
            <p>{{ item.content }}</p>
          </div>
        </li>
      </ul>
      <p v-else class="muted">暂无公告</p>
    </section>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { clearAuthInfo, getAuthInfo, get, post, put, patch } from '../api/http';

const router = useRouter();
const authInfo = getAuthInfo();

const profileForm = reactive({
  companyName: '',
  licenseNumber: '',
  industry: '',
  address: '',
  website: '',
  description: '',
  logo: ''
});

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
const applications = ref([]);
const announcements = ref([]);

const messageDialog = reactive({
  visible: false,
  applicationId: null,
  status: '',
  form: {
    title: '',
    content: ''
  }
});

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

function handleLogout() {
  clearAuthInfo();
  router.replace({ name: 'login' });
}

async function loadProfile() {
  try {
    const data = await get('/portal/company/profile');
    Object.assign(profileForm, data);
  } catch (error) {
    if (error.status !== 404) {
      showFeedback(error.message, 'error');
    }
  }
}

async function saveProfile() {
  try {
    await put('/portal/company/profile', profileForm);
    showFeedback('企业资料已保存', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadJobs() {
  try {
    jobs.value = await get('/portal/company/jobs');
  } catch (error) {
    showFeedback(error.message, 'error');
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

async function createJob() {
  try {
    const payload = {
      jobTitle: jobForm.jobTitle,
      jobType: jobForm.jobType,
      salaryRange: jobForm.salaryRange,
      location: jobForm.location,
      requirement: jobForm.requirement,
      description: jobForm.description
    };
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
    showFeedback(error.message, 'error');
  }
}

function prefillJob(job) {
  Object.assign(jobForm, job, { jobId: job.id });
}

async function changeJobStatus(job) {
  try {
    const updated = await patch(`/portal/company/jobs/${job.id}/status`, { status: job.status });
    Object.assign(job, updated);
    showFeedback('职位状态已更新', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadApplications() {
  try {
    applications.value = await get('/portal/company/applications');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function updateApplicationStatus(app) {
  try {
    const updated = await patch(`/portal/company/applications/${app.id}`, { status: app.status });
    Object.assign(app, updated);
    showFeedback('投递状态已更新', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function openMessageDialog(app) {
  messageDialog.visible = true;
  messageDialog.applicationId = app.id;
  messageDialog.status = app.status;
  messageDialog.form.title = `关于职位 #${app.jobId}`;
  messageDialog.form.content = '';
}

function closeMessageDialog() {
  messageDialog.visible = false;
  messageDialog.applicationId = null;
  messageDialog.status = '';
  messageDialog.form.title = '';
  messageDialog.form.content = '';
}

async function sendMessage() {
  try {
    const app = applications.value.find(item => item.id === messageDialog.applicationId);
    if (!app) {
      throw new Error('未找到对应的投递记录');
    }
    const updated = await patch(`/portal/company/applications/${messageDialog.applicationId}`, {
      status: messageDialog.status || app.status,
      messageTitle: messageDialog.form.title,
      messageContent: messageDialog.form.content
    });
    Object.assign(app, updated);
    showFeedback('消息发送成功', 'success');
    closeMessageDialog();
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadAnnouncements() {
  try {
    announcements.value = await get('/portal/company/announcements');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function resolveJobTitle(jobId) {
  const job = jobs.value.find(item => item.id === jobId);
  return job ? job.jobTitle : `职位 #${jobId}`;
}

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

onMounted(async () => {
  await loadProfile();
  await loadJobs();
  await loadApplications();
  await loadAnnouncements();
});
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.form-grid label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
}

.form-grid input,
.form-grid textarea,
.table select {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px;
  font-size: 14px;
}

.form-grid textarea {
  min-height: 80px;
  resize: vertical;
}

.form-grid .full {
  grid-column: 1 / -1;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  text-align: left;
  padding: 8px 12px;
  border-bottom: 1px solid #e5e7eb;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list__item {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.muted {
  color: #6b7280;
}

.primary {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border: none;
  color: #fff;
  padding: 10px 18px;
  border-radius: 10px;
  cursor: pointer;
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

.status {
  padding: 4px 10px;
  background: #e0f2fe;
  color: #0369a1;
  border-radius: 999px;
}
</style>
