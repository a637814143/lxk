<template>
  <div class="dashboard-layout">
    <aside class="dashboard-sidebar">
      <h2>企业导航</h2>
      <p>通过侧边栏切换模块，高效完成招聘与交流。</p>
      <nav class="sidebar-nav">
        <button
          v-for="item in sections"
          :key="item.key"
          type="button"
          :class="['sidebar-button', { active: activeSection === item.key }]"
          @click="selectSection(item.key)"
        >
          <span aria-hidden="true">{{ item.icon }}</span>
          <div>
            {{ item.label }}
            <div v-if="item.description" class="muted" style="font-size: 0.75rem; margin-top: 2px;">
              {{ item.description }}
            </div>
          </div>
        </button>
      </nav>
    </aside>

    <div class="dashboard-main">
      <header class="dashboard__header">
        <div>
          <h1>企业工作台</h1>
          <p>欢迎回来，{{ authInfo?.roleDisplayName }} {{ authInfo?.username }}</p>
        </div>
        <button class="outline" @click="handleLogout">退出登录</button>
      </header>

      <div class="dashboard-content">
        <section v-if="activeSection === 'profile'" class="card">
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

        <section v-else-if="activeSection === 'jobs'" class="card">
          <div class="card__title">
            <h2>职位管理</h2>
            <button class="outline" @click="loadJobs(true)">刷新</button>
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
          <div style="display: grid; gap: 18px;">
            <PieChart :data="jobStatusChartData" title="职位状态分布" />
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
          </div>
        </section>

        <section v-else-if="activeSection === 'finance'" class="card">
          <div class="card__title">
            <h2>财务往来</h2>
            <button class="outline" @click="loadTransactions(true)">刷新</button>
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

        <section v-else-if="activeSection === 'applications'" class="card">
          <div class="card__title">
            <h2>简历投递</h2>
            <button class="outline" @click="loadApplications(true)">刷新</button>
          </div>
          <div style="display: grid; gap: 18px;">
            <PieChart :data="applicationChartData" title="投递状态分布" />
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
          </div>

          <section v-if="messageDialog.visible" class="card" style="margin-top: 12px;">
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
        </section>

        <section v-else-if="activeSection === 'discussions'" class="card">
          <div class="card__title">
            <h2>企业讨论区</h2>
            <button class="outline" @click="loadDiscussions(true)">刷新</button>
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

        <section v-else-if="activeSection === 'announcements'" class="card">
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
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { clearAuthInfo, getAuthInfo, get, post, put, patch, upload } from '../api/http';
import { notifyError, notifyInfo, notifySuccess } from '../composables/useNotifier';
import PieChart from '../components/PieChart.vue';

const router = useRouter();
const authInfo = getAuthInfo();
const sections = [
  { key: 'profile', label: '企业资料', icon: '🏢', description: '完善企业信息与资质' },
  { key: 'jobs', label: '职位管理', icon: '💼', description: '发布并维护招聘岗位' },
  { key: 'finance', label: '财务往来', icon: '💳', description: '查看平台费用往来记录' },
  { key: 'applications', label: '简历投递', icon: '📬', description: '跟进学生投递进度' },
  { key: 'discussions', label: '企业讨论', icon: '💬', description: '与平台审核后的讨论互动' },
  { key: 'announcements', label: '平台公告', icon: '📢', description: '了解最新平台通知' }
];
const activeSection = ref('profile');

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

const jobStatusChartData = computed(() => {
  if (!jobs.value.length) {
    return {};
  }
  return jobs.value.reduce((accumulator, job) => {
    const status = job.status || '未标记';
    accumulator[status] = (accumulator[status] ?? 0) + 1;
    return accumulator;
  }, {});
});

const applicationChartData = computed(() => {
  if (!applications.value.length) {
    return {};
  }
  return applications.value.reduce((accumulator, app) => {
    const status = app.status || '未标记';
    accumulator[status] = (accumulator[status] ?? 0) + 1;
    return accumulator;
  }, {});
});

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
    if (type === 'success') {
      notifySuccess(message);
    } else if (type === 'error') {
      notifyError(message);
    } else if (type === 'info') {
      notifyInfo(message);
    }
  }
}

function handleLogout() {
  clearAuthInfo();
  router.replace({ name: 'login' });
}

function selectSection(sectionKey) {
  activeSection.value = sectionKey;
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

async function loadJobs(showToast = false) {
  try {
    jobs.value = await get('/portal/company/jobs');
    if (showToast) {
      showFeedback('职位列表已刷新', 'success');
    }
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

async function loadApplications(showToast = false) {
  try {
    applications.value = await get('/portal/company/applications');
    if (showToast) {
      showFeedback('投递列表已刷新', 'success');
    }
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

async function loadTransactions(showToast = false) {
  try {
    transactions.value = await get('/portal/company/transactions');
    if (showToast) {
      showFeedback('财务往来已刷新', 'success');
    }
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

async function loadDiscussions(showToast = false) {
  try {
    discussions.value = await get('/portal/company/discussions');
    if (showToast) {
      showFeedback('讨论区内容已刷新', 'success');
    }
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

watch(activeSection, (section) => {
  const loaderMap = {
    profile: () => {
      if (!profileForm.companyName) {
        loadProfile();
      }
    },
    jobs: () => {
      if (!jobs.value.length) {
        loadJobs();
      }
    },
    finance: () => {
      if (!transactions.value.length) {
        loadTransactions();
      }
    },
    applications: () => {
      if (!applications.value.length) {
        loadApplications();
      }
    },
    discussions: () => {
      if (!discussions.value.length) {
        loadDiscussions();
      }
    },
    announcements: () => {
      if (!announcements.value.length) {
        loadAnnouncements();
      }
    }
  };

  loaderMap[section]?.();
});

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
.dashboard-content > * {
  animation: fade-in 0.25s ease;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
