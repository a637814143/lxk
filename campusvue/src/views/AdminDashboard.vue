<template>
  <div class="dashboard">
    <header class="dashboard__header">
      <div>
        <h1>系统管理员工作台</h1>
        <p>欢迎回来，{{ authInfo?.roleDisplayName }} {{ authInfo?.username }}</p>
      </div>
      <button class="outline" @click="handleLogout">退出登录</button>
    </header>

    <section class="card">
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

    <section class="card">
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

    <section class="card">
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

    <section class="card">
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

const announcementForm = reactive({
  id: null,
  title: '',
  content: '',
  target: 'all'
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
    loadAnnouncements()
  ]);
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

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.summary-item {
  background: #eff6ff;
  padding: 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #1e3a8a;
}

.summary-item strong {
  font-size: 20px;
}

.status-breakdown ul {
  padding-left: 18px;
  margin: 0;
  color: #475569;
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
.form-grid select {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px;
  font-size: 14px;
}

.form-grid textarea {
  min-height: 100px;
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

.list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list__item {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.list__actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
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

.danger {
  background: transparent;
  border: 1px solid #dc2626;
  color: #dc2626;
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
</style>
