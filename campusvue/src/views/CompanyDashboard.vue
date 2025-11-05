<template>
  <div class="dashboard">
    <header class="dashboard__header">
      <div>
        <h1>企业工作台</h1>
        <p>欢迎回来，{{ authInfo?.roleDisplayName }} {{ authInfo?.username }}</p>
      </div>
      <button class="outline" @click="handleLogout">退出登录</button>
    </header>

    <div class="dashboard__content">
      <section class="card card--wide">
        <h2>企业资料</h2>
        <form class="form-grid" @submit.prevent="saveProfile">
          <label>企业名称<input v-model="profileForm.companyName" required /></label>
        <label>营业执照号<input v-model="profileForm.licenseNumber" /></label>
        <label>行业类别<input v-model="profileForm.industry" /></label>
        <label>企业地址<input v-model="profileForm.address" /></label>
        <label>企业官网<input v-model="profileForm.website" /></label>
        <label class="full">企业简介<textarea v-model="profileForm.description"></textarea></label>
        <label class="full">Logo 链接<input v-model="profileForm.logo" /></label>
        <label class="full file-input">
          营业执照文件
          <div class="file-row">
            <input ref="licenseFileInput" type="file" accept=".pdf,.jpg,.jpeg,.png" @change="handleLicenseFile" />
            <button class="outline" type="button" @click="uploadLicense" :disabled="!licenseFile">上传</button>
          </div>
          <small v-if="profileForm.licenseDocument">
            当前文件：
            <a :href="profileForm.licenseDocument" target="_blank" rel="noopener">点击查看</a>
          </small>
        </label>
        <div class="full actions">
          <button class="primary" type="submit">保存资料</button>
        </div>
      </form>
    </section>

      <section class="card card--wide">
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

      <section class="card card--wide">
        <div class="card__title">
          <h2>财务往来</h2>
          <button class="outline" @click="loadTransactions">刷新</button>
      </div>
      <form class="form-grid" @submit.prevent="submitTransaction">
        <label>金额（元）<input v-model="transactionForm.amount" type="number" min="0" step="0.01" required /></label>
        <label>币种<input v-model="transactionForm.currency" placeholder="默认 CNY" /></label>
        <label class="full">费用用途<input v-model="transactionForm.type" required placeholder="例如：平台服务费" /></label>
        <label class="full">业务编号<input v-model="transactionForm.reference" placeholder="可选的内部参考编号" /></label>
        <label class="full">备注<textarea v-model="transactionForm.notes" placeholder="补充说明（可选）"></textarea></label>
        <div class="full actions">
          <button class="primary" type="submit">提交审核</button>
          <button class="outline" type="button" @click="resetTransactionForm">清空</button>
        </div>
      </form>
      <table v-if="transactions.length" class="table">
        <thead>
          <tr><th>用途</th><th>金额</th><th>币种</th><th>状态</th><th>更新时间</th><th>备注</th></tr>
        </thead>
        <tbody>
          <tr v-for="item in transactions" :key="item.id">
            <td>{{ item.type }}</td>
            <td>{{ Number(item.amount ?? 0).toFixed(2) }}</td>
            <td>{{ item.currency || 'CNY' }}</td>
            <td>{{ item.status }}</td>
            <td>{{ formatDate(item.updatedAt || item.createdAt) }}</td>
            <td>{{ item.notes || '-' }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">暂无财务记录</p>
    </section>

      <section class="card card--wide">
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

      <section class="card card--wide">
        <div class="card__title">
          <h2>企业讨论区</h2>
          <button class="outline" @click="loadDiscussions">刷新</button>
      </div>
      <form class="form-grid" @submit.prevent="createDiscussion">
        <label class="full">讨论主题<input v-model="discussionForm.title" required /></label>
        <label class="full">讨论内容<textarea v-model="discussionForm.content" required></textarea></label>
        <div class="full actions">
          <button class="primary" type="submit">提交审核</button>
          <button class="outline" type="button" @click="resetDiscussionForm">清空</button>
        </div>
      </form>
      <ul class="list" v-if="discussions.length">
        <li v-for="item in discussions" :key="item.id" class="list__item">
          <div>
            <h3>{{ item.title }}</h3>
            <p class="muted">状态：{{ translateDiscussionStatus(item.status) }} · 提交时间：{{ formatDate(item.createdAt) }}</p>
            <p>{{ item.sanitizedContent || item.content }}</p>
            <p v-if="item.reviewComment" class="muted">审核备注：{{ item.reviewComment }}</p>
          </div>
        </li>
      </ul>
      <p v-else class="muted">暂无讨论内容，欢迎提交与学生交流的话题。</p>
    </section>

      <section v-if="messageDialog.visible" class="card card--wide">
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
    </div>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { clearAuthInfo, getAuthInfo, get, post, put, patch, upload } from '../api/http';

const router = useRouter();
const authInfo = getAuthInfo();

const profileForm = reactive({
  companyName: '',
  licenseNumber: '',
  industry: '',
  address: '',
  website: '',
  description: '',
  logo: '',
  licenseDocument: ''
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
const transactions = ref([]);
const discussions = ref([]);

const messageDialog = reactive({
  visible: false,
  applicationId: null,
  status: '',
  form: {
    title: '',
    content: ''
  }
});

const transactionForm = reactive({
  amount: '',
  type: '',
  currency: 'CNY',
  reference: '',
  notes: ''
});

const discussionForm = reactive({
  title: '',
  content: ''
});

const licenseFile = ref(null);
const licenseFileInput = ref(null);

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
    const data = await put('/portal/company/profile', profileForm);
    Object.assign(profileForm, data);
    showFeedback('企业资料已保存', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function handleLicenseFile(event) {
  const [file] = event.target.files ?? [];
  licenseFile.value = file ?? null;
}

async function uploadLicense() {
  if (!licenseFile.value) {
    showFeedback('请先选择要上传的营业执照文件', 'error');
    return;
  }
  try {
    const formData = new FormData();
    formData.append('file', licenseFile.value);
    const data = await upload('/portal/company/profile/license', formData);
    Object.assign(profileForm, data);
    showFeedback('营业执照上传成功，等待管理员审核', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
  } finally {
    licenseFile.value = null;
    if (licenseFileInput.value) {
      licenseFileInput.value.value = '';
    }
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

async function loadTransactions() {
  try {
    transactions.value = await get('/portal/company/transactions');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function resetTransactionForm() {
  transactionForm.amount = '';
  transactionForm.type = '';
  transactionForm.currency = 'CNY';
  transactionForm.reference = '';
  transactionForm.notes = '';
}

async function submitTransaction() {
  if (!transactionForm.amount || !transactionForm.type) {
    showFeedback('请填写金额和费用用途', 'error');
    return;
  }
  try {
    await post('/portal/company/transactions', {
      amount: transactionForm.amount,
      type: transactionForm.type,
      currency: transactionForm.currency || 'CNY',
      reference: transactionForm.reference,
      notes: transactionForm.notes
    });
    showFeedback('财务申请已提交，等待管理员处理', 'success');
    resetTransactionForm();
    await loadTransactions();
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadDiscussions() {
  try {
    discussions.value = await get('/portal/company/discussions');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function resetDiscussionForm() {
  discussionForm.title = '';
  discussionForm.content = '';
}

async function createDiscussion() {
  if (!discussionForm.title || !discussionForm.content) {
    showFeedback('请填写讨论主题和内容', 'error');
    return;
  }
  try {
    const created = await post('/portal/company/discussions', discussionForm);
    discussions.value.unshift(created);
    showFeedback('讨论已提交审核', 'success');
    resetDiscussionForm();
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function translateDiscussionStatus(status) {
  switch ((status || '').toLowerCase()) {
    case 'approved':
      return '已发布';
    case 'rejected':
      return '已驳回';
    case 'pending':
    default:
      return '待审核';
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
  await loadTransactions();
  await loadDiscussions();
  await loadAnnouncements();
});
</script>

<style scoped>
.dashboard {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px 64px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.dashboard__header {
  background: linear-gradient(135deg, #0ea5e9, #22d3ee);
  color: #0f172a;
  padding: 28px 32px;
  border-radius: 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 24px 48px rgba(14, 165, 233, 0.22);
}

.dashboard__header h1 {
  margin: 0 0 8px;
  font-size: 28px;
}

.dashboard__header p {
  margin: 0;
  font-size: 15px;
  opacity: 0.85;
}

.dashboard__header .outline {
  border-color: rgba(15, 23, 42, 0.2);
  color: #0f172a;
  background: rgba(255, 255, 255, 0.35);
}

.dashboard__header .outline:hover {
  background: rgba(255, 255, 255, 0.45);
}

.dashboard__content {
  display: grid;
  gap: 24px;
  align-items: start;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
}

.card {
  background: rgba(255, 255, 255, 0.96);
  border-radius: 24px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(226, 232, 240, 0.7);
  backdrop-filter: blur(6px);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 26px 48px rgba(15, 23, 42, 0.12);
}

.card--wide {
  grid-column: 1 / -1;
}

.card__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card h2 {
  margin: 0;
  font-size: 22px;
  color: #0f172a;
}

.form-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.form-grid label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
  color: #1e293b;
}

.form-grid input,
.form-grid textarea,
.form-grid select,
.card select {
  border: 1px solid #bae6fd;
  border-radius: 14px;
  padding: 10px 14px;
  font-size: 14px;
  background: #f0f9ff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.form-grid input:focus,
.form-grid textarea:focus,
.form-grid select:focus,
.card select:focus {
  outline: none;
  border-color: #0ea5e9;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.18);
}

.form-grid textarea {
  min-height: 120px;
  resize: vertical;
}

.form-grid .full {
  grid-column: 1 / -1;
}

.file-input .file-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 6px;
}

.file-input input[type='file'] {
  flex: 1;
  padding: 9px 12px;
  border-radius: 14px;
  border: 1px solid #bae6fd;
  background: #f0f9ff;
}

.file-input small {
  display: block;
  margin-top: 6px;
  color: #64748b;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.table {
  width: 100%;
  border-collapse: collapse;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: inset 0 0 0 1px rgba(226, 232, 240, 0.8);
}

.table th {
  background: #e0f2fe;
  color: #0f172a;
}

.table th,
.table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
  font-size: 14px;
}

.table tbody tr:hover {
  background: #f8fafc;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.list__item {
  padding: 18px;
  border-radius: 20px;
  background: #f8fafc;
  border: 1px solid rgba(226, 232, 240, 0.9);
}

.muted {
  color: #64748b;
}

.status {
  padding: 6px 14px;
  background: #cffafe;
  color: #0f172a;
  border-radius: 999px;
  font-weight: 600;
  display: inline-block;
}

.primary,
.outline,
.danger {
  border-radius: 999px;
  padding: 9px 18px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.primary {
  background: linear-gradient(135deg, #0ea5e9, #0284c7);
  border: none;
  color: #fff;
  box-shadow: 0 12px 24px rgba(14, 165, 233, 0.25);
}

.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 32px rgba(14, 165, 233, 0.3);
}

.outline {
  background: transparent;
  border: 1px solid #0ea5e9;
  color: #0284c7;
}

.outline:hover {
  background: rgba(14, 165, 233, 0.08);
  transform: translateY(-1px);
}

.danger {
  background: rgba(220, 38, 38, 0.08);
  border: 1px solid #dc2626;
  color: #b91c1c;
}

.danger:hover {
  background: rgba(248, 113, 113, 0.16);
  transform: translateY(-1px);
}

.feedback {
  text-align: center;
  padding: 14px 18px;
  border-radius: 16px;
  max-width: 600px;
  margin: 0 auto 32px;
  font-weight: 600;
}

.feedback.success {
  background: #dcfce7;
  color: #15803d;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}

@media (max-width: 900px) {
  .dashboard__header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .dashboard__header .outline {
    align-self: stretch;
    text-align: center;
  }
}

@media (max-width: 600px) {
  .dashboard {
    padding: 24px 16px 48px;
  }

  .card {
    padding: 20px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
