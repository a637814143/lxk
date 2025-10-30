<template>
  <DashboardLayout
    title="企业工作台"
    :subtitle="subtitle"
    :nav-items="navItems"
    @logout="handleLogout"
  >
    <router-view />
    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </DashboardLayout>
</template>

<script setup>
import { computed, onMounted, provide, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import DashboardLayout from '../components/dashboard/DashboardLayout.vue';
import { clearAuthInfo, getAuthInfo, get, post, put, patch, upload } from '../api/http';

const router = useRouter();
const authInfo = getAuthInfo();

const subtitle = computed(() => {
  if (!authInfo) return '';
  const roleName = authInfo.roleDisplayName ?? '';
  const username = authInfo.username ?? '';
  return `欢迎回来，${roleName} ${username}`.trim();
});

const navItems = [
  { label: '企业资料', to: '/company/profile' },
  { label: '职位管理', to: '/company/jobs' },
  { label: '财务往来', to: '/company/finance' },
  { label: '简历投递', to: '/company/applications' },
  { label: '企业讨论区', to: '/company/discussions' },
  { label: '平台公告', to: '/company/announcements' }
];

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
const walletBalance = ref(0);
const quarterFee = ref(0);
const durationOptions = [
  { value: 1, label: '1个季度（3个月）' },
  { value: 2, label: '2个季度（6个月）' },
  { value: 4, label: '4个季度（12个月）' }
];

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
  durationQuarters: 1,
  type: '季度服务费',
  reference: '',
  notes: ''
});

const calculatedAmount = computed(() => {
  const quarters = Number(transactionForm.durationQuarters || 0);
  const fee = Number(quarterFee.value || 0);
  return Number.isFinite(quarters) && Number.isFinite(fee) ? fee * quarters : 0;
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

function applyProfileData(data = {}) {
  Object.assign(profileForm, {
    companyName: data.companyName ?? '',
    licenseNumber: data.licenseNumber ?? '',
    industry: data.industry ?? '',
    address: data.address ?? '',
    website: data.website ?? '',
    description: data.description ?? '',
    logo: data.logo ?? '',
    licenseDocument: data.licenseDocument ?? ''
  });
  walletBalance.value = Number(data.walletBalance ?? 0);
}

function handleLogout() {
  clearAuthInfo();
  router.replace({ name: 'login' });
}

async function loadProfile() {
  try {
    const data = await get('/portal/company/profile');
    applyProfileData(data);
  } catch (error) {
    if (error.status !== 404) {
      showFeedback(error.message, 'error');
    }
  }
}

async function loadBillingConfig() {
  try {
    const data = await get('/billing/config');
    quarterFee.value = Number(data?.quarterFee ?? 0);
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function saveProfile() {
  try {
    const data = await put('/portal/company/profile', profileForm);
    applyProfileData(data);
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
    applyProfileData(data);
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
  transactionForm.durationQuarters = 1;
  transactionForm.type = '季度服务费';
  transactionForm.reference = '';
  transactionForm.notes = '';
}

async function submitTransaction() {
  if (!transactionForm.durationQuarters) {
    showFeedback('请选择使用时长', 'error');
    return;
  }
  const amount = calculatedAmount.value;
  if (walletBalance.value < amount) {
    showFeedback('钱包余额不足，请先充值', 'error');
    return;
  }
  try {
    await post('/portal/company/transactions', {
      durationQuarters: transactionForm.durationQuarters,
      type: transactionForm.type,
      reference: transactionForm.reference,
      notes: transactionForm.notes
    });
    showFeedback('季度服务费已扣款，感谢使用', 'success');
    resetTransactionForm();
    await Promise.all([loadTransactions(), loadProfile()]);
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

function formatDuration(months) {
  if (!months) return '—';
  const quarters = months / 3;
  if (Number.isInteger(quarters)) {
    return `${quarters}个季度（${months}个月）`;
  }
  return `${months}个月`;
}

function formatCurrency(value) {
  return Number(value ?? 0).toFixed(2);
}

const companyContext = {
  authInfo,
  profileForm,
  jobForm,
  jobs,
  applications,
  announcements,
  transactions,
  discussions,
  walletBalance,
  quarterFee,
  durationOptions,
  messageDialog,
  transactionForm,
  calculatedAmount,
  discussionForm,
  licenseFile,
  licenseFileInput,
  feedback,
  loadProfile,
  saveProfile,
  loadJobs,
  resetJobForm,
  createJob,
  prefillJob,
  changeJobStatus,
  loadApplications,
  updateApplicationStatus,
  openMessageDialog,
  closeMessageDialog,
  sendMessage,
  loadTransactions,
  resetTransactionForm,
  submitTransaction,
  loadDiscussions,
  resetDiscussionForm,
  createDiscussion,
  translateDiscussionStatus,
  loadAnnouncements,
  loadBillingConfig,
  showFeedback,
  applyProfileData,
  handleLicenseFile,
  uploadLicense,
  formatDate,
  formatDuration,
  formatCurrency,
  resolveJobTitle
};

provide('companyContext', companyContext);

onMounted(async () => {
  await loadBillingConfig();
  await loadProfile();
  await loadJobs();
  await loadApplications();
  await loadTransactions();
  await loadDiscussions();
  await loadAnnouncements();
});
</script>
