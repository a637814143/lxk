<template>
  <DashboardLayout
    title="系统管理员工作台"
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
import { clearAuthInfo, getAuthInfo, get, patch, post, put, del } from '../api/http';

const router = useRouter();
const authInfo = getAuthInfo();

const subtitle = computed(() => {
  if (!authInfo) return '';
  const roleName = authInfo.roleDisplayName ?? '';
  const username = authInfo.username ?? '';
  return `欢迎回来，${roleName} ${username}`.trim();
});

const navItems = [
  { label: '平台概览', to: '/admin/overview' },
  { label: '企业审核', to: '/admin/company-review' },
  { label: '职位审核', to: '/admin/job-review' },
  { label: '讨论审核', to: '/admin/discussion-review' },
  { label: '用户管理', to: '/admin/users' },
  { label: '财务管理', to: '/admin/finance' },
  { label: '公告管理', to: '/admin/announcements' },
  { label: '数据备份', to: '/admin/backups' }
];

const summary = ref(null);
const pendingCompanies = ref([]);
const pendingJobs = ref([]);
const users = ref([]);
const announcements = ref([]);
const transactions = ref([]);
const pendingDiscussions = ref([]);
const backups = ref([]);
const quarterFee = ref(0);
const durationOptions = [
  { value: 1, label: '1个季度（3个月）' },
  { value: 2, label: '2个季度（6个月）' },
  { value: 4, label: '4个季度（12个月）' }
];

const announcementForm = reactive({
  id: null,
  title: '',
  content: '',
  target: 'all'
});

const transactionForm = reactive({
  companyId: '',
  durationQuarters: 1,
  type: '季度服务费',
  reference: '',
  notes: ''
});

const backupForm = reactive({
  backupType: 'system',
  message: ''
});

const feedback = reactive({ message: '', type: 'info' });

const calculatedAmount = computed(() => {
  const quarters = Number(transactionForm.durationQuarters || 0);
  const fee = Number(quarterFee.value || 0);
  return Number.isFinite(quarters) && Number.isFinite(fee) ? fee * quarters : 0;
});

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

async function loadSummary() {
  try {
    summary.value = await get('/portal/admin/summary');
  } catch (error) {
    showFeedback(error.message, 'error');
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

async function loadPendingCompanies() {
  try {
    pendingCompanies.value = await get('/portal/admin/companies/pending');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function reviewCompany(companyId, status) {
  const reason = status === 'rejected' ? prompt('请输入驳回原因', '') ?? '' : '';
  try {
    await patch(`/portal/admin/companies/${companyId}/review`, { status, reason });
    showFeedback('企业审核结果已提交', 'success');
    await Promise.all([loadPendingCompanies(), loadSummary()]);
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadPendingJobs() {
  try {
    pendingJobs.value = await get('/portal/admin/jobs/pending');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function reviewJob(jobId, status) {
  const reason = status === 'rejected' ? prompt('请输入驳回原因', '') ?? '' : '';
  try {
    await patch(`/portal/admin/jobs/${jobId}/review`, { status, reason });
    showFeedback('职位审核结果已提交', 'success');
    await Promise.all([loadPendingJobs(), loadSummary()]);
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadUsers() {
  try {
    users.value = await get('/portal/admin/users');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function toggleUserStatus(user) {
  try {
    const newStatus = user.status === 1 ? 0 : 1;
    const updated = await patch(`/portal/admin/users/${user.id}/status`, { status: newStatus });
    Object.assign(user, updated);
    showFeedback('用户状态已更新', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadTransactions() {
  try {
    transactions.value = await get('/portal/admin/transactions');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function resetTransactionForm() {
  transactionForm.companyId = '';
  transactionForm.durationQuarters = 1;
  transactionForm.type = '季度服务费';
  transactionForm.reference = '';
  transactionForm.notes = '';
}

async function createTransaction() {
  if (!transactionForm.companyId || !transactionForm.durationQuarters) {
    showFeedback('请填写企业ID并选择使用时长', 'error');
    return;
  }
  try {
    await post('/portal/admin/transactions', {
      companyId: Number(transactionForm.companyId),
      durationQuarters: transactionForm.durationQuarters,
      type: transactionForm.type,
      reference: transactionForm.reference,
      notes: transactionForm.notes
    });
    showFeedback('已创建季度扣款记录', 'success');
    resetTransactionForm();
    await loadTransactions();
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function updateTransactionStatus(transaction, status) {
  try {
    const updated = await patch(`/portal/admin/transactions/${transaction.id}`, {
      status,
      notes: transaction.notes
    });
    Object.assign(transaction, updated);
    showFeedback('交易状态已更新', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
    await loadTransactions();
  }
}

async function loadPendingDiscussions() {
  try {
    pendingDiscussions.value = await get('/portal/admin/discussions/pending');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function handleDiscussionReview(discussion, status) {
  const comment = status === 'rejected' ? prompt('请输入驳回原因', '') ?? '' : '';
  try {
    await post(`/portal/admin/discussions/${discussion.id}/review`, { status, comment });
    showFeedback('讨论审核结果已提交', 'success');
    await loadPendingDiscussions();
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadBackups() {
  try {
    backups.value = await get('/portal/admin/backups');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function triggerBackup() {
  try {
    await post('/portal/admin/backups', backupForm);
    showFeedback('备份任务已创建', 'success');
    backupForm.message = '';
    await loadBackups();
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadAnnouncements() {
  try {
    announcements.value = await get('/portal/admin/announcements');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function resetAnnouncementForm() {
  announcementForm.id = null;
  announcementForm.title = '';
  announcementForm.content = '';
  announcementForm.target = 'all';
}

function editAnnouncement(item) {
  announcementForm.id = item.id;
  announcementForm.title = item.title;
  announcementForm.content = item.content;
  announcementForm.target = item.target;
}

async function saveAnnouncement() {
  try {
    if (announcementForm.id) {
      await put(`/portal/admin/announcements/${announcementForm.id}`, {
        title: announcementForm.title,
        content: announcementForm.content,
        target: announcementForm.target
      });
      showFeedback('公告已更新', 'success');
    } else {
      await post('/portal/admin/announcements', {
        title: announcementForm.title,
        content: announcementForm.content,
        target: announcementForm.target
      });
      showFeedback('公告发布成功', 'success');
    }
    resetAnnouncementForm();
    await loadAnnouncements();
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function deleteAnnouncement(id) {
  if (!confirm('确定删除该公告吗？')) return;
  try {
    await del(`/portal/admin/announcements/${id}`);
    showFeedback('公告已删除', 'success');
    await loadAnnouncements();
  } catch (error) {
    showFeedback(error.message, 'error');
  }
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

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

const adminContext = {
  authInfo,
  summary,
  pendingCompanies,
  pendingJobs,
  users,
  announcements,
  transactions,
  pendingDiscussions,
  backups,
  quarterFee,
  durationOptions,
  announcementForm,
  transactionForm,
  backupForm,
  feedback,
  calculatedAmount,
  loadSummary,
  loadPendingCompanies,
  reviewCompany,
  loadPendingJobs,
  reviewJob,
  loadUsers,
  toggleUserStatus,
  loadTransactions,
  createTransaction,
  resetTransactionForm,
  updateTransactionStatus,
  loadPendingDiscussions,
  handleDiscussionReview,
  loadBackups,
  triggerBackup,
  loadAnnouncements,
  saveAnnouncement,
  resetAnnouncementForm,
  editAnnouncement,
  deleteAnnouncement,
  showFeedback,
  formatDuration,
  formatCurrency,
  formatDate,
  loadBillingConfig
};

provide('adminContext', adminContext);

onMounted(async () => {
  await loadBillingConfig();
  await Promise.all([
    loadSummary(),
    loadPendingCompanies(),
    loadPendingJobs(),
    loadUsers(),
    loadAnnouncements(),
    loadTransactions(),
    loadPendingDiscussions(),
    loadBackups()
  ]);
});
</script>
