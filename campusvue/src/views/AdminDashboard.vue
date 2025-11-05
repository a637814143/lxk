<template>
  <div class="dashboard">
    <header class="dashboard__header">
      <div>
        <h1>系统管理员工作台</h1>
        <p>欢迎回来，{{ authInfo?.roleDisplayName }} {{ authInfo?.username }}</p>
      </div>
      <button class="outline" @click="handleLogout">退出登录</button>
    </header>

    <div class="dashboard__content">
      <section class="card card--wide">
        <h2>平台概览</h2>
        <div class="summary-grid" v-if="summary">
          <div class="summary-item"><span>学生人数</span><strong>{{ summary.totalStudents }}</strong></div>
        <div class="summary-item"><span>企业数量</span><strong>{{ summary.totalCompanies }}</strong></div>
        <div class="summary-item"><span>待审企业</span><strong>{{ summary.pendingCompanies }}</strong></div>
        <div class="summary-item"><span>职位总数</span><strong>{{ summary.totalJobs }}</strong></div>
        <div class="summary-item"><span>已发布职位</span><strong>{{ summary.approvedJobs }}</strong></div>
        <div class="summary-item"><span>待审核职位</span><strong>{{ summary.pendingJobs }}</strong></div>
        <div class="summary-item"><span>投递总量</span><strong>{{ summary.totalApplications }}</strong></div>
        <div class="summary-item"><span>管理员未读消息</span><strong>{{ summary.unreadMessages }}</strong></div>
      </div>
      <div class="status-breakdown" v-if="summary">
        <h3>投递状态统计</h3>
        <ul>
          <li v-for="(value, key) in summary.statusBreakdown" :key="key">{{ key }}：{{ value }}</li>
        </ul>
      </div>
    </section>

      <section class="card card--wide">
        <div class="card__title">
          <h2>企业审核</h2>
          <button class="outline" @click="loadPendingCompanies">刷新</button>
      </div>
      <table v-if="pendingCompanies.length" class="table">
        <thead><tr><th>企业名称</th><th>行业</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="company in pendingCompanies" :key="company.id">
            <td>{{ company.companyName }}</td>
            <td>{{ company.industry || '未填写' }}</td>
            <td>{{ company.auditStatus }}</td>
            <td class="actions">
              <button class="primary" @click="reviewCompany(company.id, 'approved')">通过</button>
              <button class="danger" @click="reviewCompany(company.id, 'rejected')">驳回</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">暂无待审核企业</p>
    </section>

      <section class="card card--wide">
        <div class="card__title">
          <h2>职位审核</h2>
          <button class="outline" @click="loadPendingJobs">刷新</button>
      </div>
      <table v-if="pendingJobs.length" class="table">
        <thead><tr><th>职位名称</th><th>企业ID</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="job in pendingJobs" :key="job.id">
            <td>{{ job.jobTitle }}</td>
            <td>{{ job.companyId }}</td>
            <td>{{ job.status }}</td>
            <td class="actions">
              <button class="primary" @click="reviewJob(job.id, 'approved')">通过</button>
              <button class="danger" @click="reviewJob(job.id, 'rejected')">驳回</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">暂无待审核职位</p>
    </section>

      <section class="card">
        <div class="card__title">
          <h2>讨论审核</h2>
          <button class="outline" @click="loadPendingDiscussions">刷新</button>
      </div>
      <ul class="list" v-if="pendingDiscussions.length">
        <li v-for="item in pendingDiscussions" :key="item.id" class="list__item">
          <div>
            <h3>{{ item.title }} <small class="muted">企业 #{{ item.companyId }}</small></h3>
            <p class="muted">提交时间：{{ formatDate(item.createdAt) }}</p>
            <p>{{ item.sanitizedContent || item.content }}</p>
            <p v-if="item.reviewComment" class="muted">备注：{{ item.reviewComment }}</p>
          </div>
          <div class="list__actions">
            <button class="primary" @click="handleDiscussionReview(item, 'approved')">通过</button>
            <button class="danger" @click="handleDiscussionReview(item, 'rejected')">驳回</button>
          </div>
        </li>
      </ul>
      <p v-else class="muted">暂无待审核的讨论内容</p>
    </section>

      <section class="card card--wide">
        <div class="card__title">
          <h2>用户管理</h2>
          <button class="outline" @click="loadUsers">刷新</button>
      </div>
      <table v-if="users.length" class="table">
        <thead><tr><th>ID</th><th>用户名</th><th>角色</th><th>状态</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.role }}</td>
            <td>{{ user.status === 1 ? '启用' : '禁用' }}</td>
            <td class="actions">
              <button class="outline" @click="toggleUserStatus(user)">
                {{ user.status === 1 ? '禁用账号' : '恢复账号' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">暂无用户数据</p>
    </section>

      <section class="card card--wide">
        <div class="card__title">
          <h2>财务记录管理</h2>
          <button class="outline" @click="loadTransactions">刷新</button>
      </div>
      <form class="form-grid" @submit.prevent="createTransaction">
        <label>企业ID<input v-model="transactionForm.companyId" type="number" min="1" required /></label>
        <label>金额（元）<input v-model="transactionForm.amount" type="number" min="0" step="0.01" required /></label>
        <label>币种<input v-model="transactionForm.currency" placeholder="默认 CNY" /></label>
        <label class="full">费用用途<input v-model="transactionForm.type" required placeholder="例如：平台服务费" /></label>
        <label class="full">业务编号<input v-model="transactionForm.reference" placeholder="可选的内部参考编号" /></label>
        <label class="full">备注<textarea v-model="transactionForm.notes" placeholder="补充说明（可选）"></textarea></label>
        <div class="full actions">
          <button class="primary" type="submit">创建记录</button>
          <button class="outline" type="button" @click="resetTransactionForm">重置</button>
        </div>
      </form>
      <table v-if="transactions.length" class="table">
        <thead><tr><th>ID</th><th>企业ID</th><th>用途</th><th>金额</th><th>状态</th><th>更新时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in transactions" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.companyId }}</td>
            <td>{{ item.type }}</td>
            <td>{{ Number(item.amount ?? 0).toFixed(2) }} {{ item.currency || 'CNY' }}</td>
            <td>{{ item.status }}</td>
            <td>{{ formatDate(item.updatedAt || item.createdAt) }}</td>
            <td class="actions">
              <select v-model="item.status" @change="updateTransactionStatus(item, item.status)">
                <option value="pending">待处理</option>
                <option value="completed">已完成</option>
                <option value="cancelled">已取消</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">暂无财务记录</p>
    </section>

      <section class="card card--wide">
        <div class="card__title">
          <h2>公告管理</h2>
          <button class="outline" @click="loadAnnouncements">刷新</button>
      </div>
      <form class="form-grid" @submit.prevent="saveAnnouncement">
        <label>标题<input v-model="announcementForm.title" required /></label>
        <label>目标
          <select v-model="announcementForm.target" required>
            <option value="all">全部用户</option>
            <option value="student">学生</option>
            <option value="company">企业</option>
          </select>
        </label>
        <label class="full">内容<textarea v-model="announcementForm.content" required></textarea></label>
        <div class="full actions">
          <button class="primary" type="submit">{{ announcementForm.id ? '更新公告' : '发布公告' }}</button>
          <button class="outline" type="button" @click="resetAnnouncementForm">取消编辑</button>
        </div>
      </form>
      <ul class="list" v-if="announcements.length">
        <li v-for="item in announcements" :key="item.id" class="list__item">
          <div>
            <h3>{{ item.title }} <small class="muted">({{ item.target }})</small></h3>
            <p class="muted">发布时间：{{ formatDate(item.publishTime) }}</p>
            <p>{{ item.content }}</p>
          </div>
          <div class="list__actions">
            <button class="outline" @click="editAnnouncement(item)">编辑</button>
            <button class="danger" @click="deleteAnnouncement(item.id)">删除</button>
          </div>
        </li>
      </ul>
      <p v-else class="muted">暂无公告</p>
    </section>

      <section class="card card--wide">
        <div class="card__title">
          <h2>数据备份</h2>
          <button class="outline" @click="loadBackups">刷新</button>
      </div>
      <form class="form-grid" @submit.prevent="triggerBackup">
        <label>备份类型<input v-model="backupForm.backupType" placeholder="例如 daily/system" /></label>
        <label class="full">备注<textarea v-model="backupForm.message" placeholder="可选备注"></textarea></label>
        <div class="full actions">
          <button class="primary" type="submit">立即备份</button>
          <button class="outline" type="button" @click="backupForm.message = ''">清空备注</button>
        </div>
      </form>
      <table v-if="backups.length" class="table">
        <thead><tr><th>ID</th><th>类型</th><th>状态</th><th>创建时间</th><th>文件</th></tr></thead>
        <tbody>
          <tr v-for="item in backups" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.backupType || 'system' }}</td>
            <td>{{ item.status }}</td>
            <td>{{ formatDate(item.createdAt) }}</td>
            <td>
              <a v-if="item.downloadUrl" :href="item.downloadUrl" target="_blank" rel="noopener">下载</a>
              <span v-else class="muted">生成中</span>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">暂无备份记录</p>
    </section>
    </div>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { clearAuthInfo, getAuthInfo, get, patch, post, put, del } from '../api/http';

const router = useRouter();
const authInfo = getAuthInfo();

const summary = ref(null);
const pendingCompanies = ref([]);
const pendingJobs = ref([]);
const users = ref([]);
const announcements = ref([]);
const transactions = ref([]);
const pendingDiscussions = ref([]);
const backups = ref([]);

const announcementForm = reactive({
  id: null,
  title: '',
  content: '',
  target: 'all'
});

const transactionForm = reactive({
  companyId: '',
  amount: '',
  type: '',
  currency: 'CNY',
  reference: '',
  notes: ''
});

const backupForm = reactive({
  backupType: 'system',
  message: ''
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

async function loadSummary() {
  try {
    summary.value = await get('/portal/admin/summary');
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
  transactionForm.amount = '';
  transactionForm.type = '';
  transactionForm.currency = 'CNY';
  transactionForm.reference = '';
  transactionForm.notes = '';
}

async function createTransaction() {
  if (!transactionForm.companyId || !transactionForm.amount || !transactionForm.type) {
    showFeedback('请填写企业ID、金额和用途', 'error');
    return;
  }
  try {
    await post('/portal/admin/transactions', {
      companyId: Number(transactionForm.companyId),
      amount: transactionForm.amount,
      type: transactionForm.type,
      currency: transactionForm.currency || 'CNY',
      reference: transactionForm.reference,
      notes: transactionForm.notes
    });
    showFeedback('已创建财务记录', 'success');
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

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

onMounted(async () => {
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
  background: linear-gradient(135deg, #1d4ed8, #6366f1);
  color: #fff;
  padding: 28px 32px;
  border-radius: 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.18);
}

.dashboard__header h1 {
  margin: 0 0 8px;
  font-size: 28px;
}

.dashboard__header p {
  margin: 0;
  font-size: 15px;
  opacity: 0.9;
}

.dashboard__header .outline {
  border-color: rgba(255, 255, 255, 0.7);
  color: #fff;
  background: rgba(255, 255, 255, 0.15);
}

.dashboard__header .outline:hover {
  background: rgba(255, 255, 255, 0.25);
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

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.summary-item {
  background: linear-gradient(135deg, #eef2ff, #dbeafe);
  padding: 18px;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #312e81;
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.12);
}

.summary-item strong {
  font-size: 22px;
}

.status-breakdown {
  background: #f8fafc;
  border-radius: 18px;
  padding: 18px;
  border: 1px solid rgba(226, 232, 240, 0.7);
}

.status-breakdown h3 {
  margin: 0 0 12px;
  font-size: 18px;
  color: #0f172a;
}

.status-breakdown ul {
  padding-left: 18px;
  margin: 0;
  color: #475569;
  display: grid;
  gap: 6px;
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
  border: 1px solid #dbeafe;
  border-radius: 14px;
  padding: 10px 14px;
  font-size: 14px;
  background: #f8fbff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.form-grid input:focus,
.form-grid textarea:focus,
.form-grid select:focus,
.card select:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.18);
}

.form-grid textarea {
  min-height: 120px;
  resize: vertical;
}

.form-grid .full {
  grid-column: 1 / -1;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
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
  border-radius: 20px;
  padding: 18px;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  background: #f8fafc;
  border: 1px solid rgba(226, 232, 240, 0.9);
}

.list__item > div {
  flex: 1;
}

.list__actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 140px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: inset 0 0 0 1px rgba(226, 232, 240, 0.8);
}

.table th {
  background: #eef2ff;
  color: #312e81;
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

.muted {
  color: #64748b;
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
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  border: none;
  color: #fff;
  box-shadow: 0 12px 24px rgba(79, 70, 229, 0.25);
}

.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 32px rgba(79, 70, 229, 0.3);
}

.outline {
  background: transparent;
  border: 1px solid #4f46e5;
  color: #4338ca;
}

.outline:hover {
  background: rgba(79, 70, 229, 0.08);
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

.feedback.info {
  background: #e0f2fe;
  color: #0369a1;
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

  .list__item {
    flex-direction: column;
  }

  .list__actions {
    flex-direction: row;
    flex-wrap: wrap;
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
