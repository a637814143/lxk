<template>
  <div class="dashboard-layout">
    <aside class="dashboard-sidebar">
      <h2>管理导航</h2>
      <p>通过侧边栏快速切换核心功能，提升审核效率。</p>
      <nav class="sidebar-nav">
        <button
          v-for="item in sections"
          :key="item.key"
          :class="['sidebar-button', { active: activeSection === item.key }]"
          type="button"
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
          <h1>系统管理员工作台</h1>
          <p>欢迎回来，{{ authInfo?.roleDisplayName }} {{ authInfo?.username }}</p>
        </div>
        <button class="outline" @click="handleLogout">退出登录</button>
      </header>

      <div class="dashboard-content">
        <section v-if="activeSection === 'overview'" class="card">
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
          <div v-if="summary" class="status-breakdown" style="display: grid; gap: 20px;">
            <PieChart :data="statusBreakdownChart" title="投递状态占比" />
            <ul>
              <li v-for="(value, key) in summary.statusBreakdown" :key="key">{{ key }}：{{ value }}</li>
            </ul>
          </div>
        </section>

        <section v-else-if="activeSection === 'companies'" class="card">
          <div class="card__title">
            <h2>企业审核</h2>
            <button class="outline" @click="loadPendingCompanies(true)">刷新</button>
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

        <section v-else-if="activeSection === 'jobs'" class="card">
          <div class="card__title">
            <h2>职位审核</h2>
            <button class="outline" @click="loadPendingJobs(true)">刷新</button>
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

        <section v-else-if="activeSection === 'discussions'" class="card">
          <div class="card__title">
            <h2>讨论审核</h2>
            <button class="outline" @click="loadPendingDiscussions(true)">刷新</button>
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

        <section v-else-if="activeSection === 'users'" class="card">
          <div class="card__title">
            <h2>用户管理</h2>
            <button class="outline" @click="loadUsers(true)">刷新</button>
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

        <section v-else-if="activeSection === 'finance'" class="card">
          <div class="card__title">
            <h2>财务记录管理</h2>
            <button class="outline" @click="loadTransactions(true)">刷新</button>
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

        <section v-else-if="activeSection === 'announcements'" class="card">
          <div class="card__title">
            <h2>公告管理</h2>
            <button class="outline" @click="loadAnnouncements(true)">刷新</button>
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

        <section v-else-if="activeSection === 'backups'" class="card">
          <div class="card__title">
            <h2>数据备份</h2>
            <button class="outline" @click="loadBackups(true)">刷新</button>
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

        <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { clearAuthInfo, getAuthInfo, get, patch, post, put, del } from '../api/http';
import { notifyError, notifyInfo, notifySuccess } from '../composables/useNotifier';
import PieChart from '../components/PieChart.vue';

const router = useRouter();
const authInfo = getAuthInfo();

const sections = [
  { key: 'overview', label: '平台概览', icon: '📊', description: '掌握当前用户与职位数据' },
  { key: 'companies', label: '企业审核', icon: '🏢', description: '审批企业入驻与资料' },
  { key: 'jobs', label: '职位审核', icon: '💼', description: '确认岗位信息后发布' },
  { key: 'discussions', label: '讨论审核', icon: '💬', description: '维护企业讨论区内容' },
  { key: 'users', label: '用户管理', icon: '👥', description: '封禁或恢复账号权限' },
  { key: 'finance', label: '财务往来', icon: '💳', description: '记录平台与企业的资金往来' },
  { key: 'announcements', label: '公告中心', icon: '📢', description: '发布与维护系统公告' },
  { key: 'backups', label: '数据备份', icon: '🗄️', description: '守护关键业务数据' }
];

const activeSection = ref('overview');

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

const statusBreakdownChart = computed(() => {
  if (!summary.value?.statusBreakdown) {
    return {};
  }
  return Object.entries(summary.value.statusBreakdown).reduce((accumulator, [key, value]) => {
    accumulator[key] = Number(value ?? 0);
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

async function loadSummary() {
  try {
    summary.value = await get('/portal/admin/summary');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadPendingCompanies(showToast = false) {
  try {
    pendingCompanies.value = await get('/portal/admin/companies/pending');
    if (showToast) {
      showFeedback('待审核企业列表已刷新', 'success');
    }
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

async function loadPendingJobs(showToast = false) {
  try {
    pendingJobs.value = await get('/portal/admin/jobs/pending');
    if (showToast) {
      showFeedback('待审核职位列表已刷新', 'success');
    }
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

async function loadUsers(showToast = false) {
  try {
    users.value = await get('/portal/admin/users');
    if (showToast) {
      showFeedback('用户列表已刷新', 'success');
    }
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

async function loadTransactions(showToast = false) {
  try {
    transactions.value = await get('/portal/admin/transactions');
    if (showToast) {
      showFeedback('财务记录已刷新', 'success');
    }
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

async function loadPendingDiscussions(showToast = false) {
  try {
    pendingDiscussions.value = await get('/portal/admin/discussions/pending');
    if (showToast) {
      showFeedback('待审核讨论已刷新', 'success');
    }
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

async function loadBackups(showToast = false) {
  try {
    backups.value = await get('/portal/admin/backups');
    if (showToast) {
      showFeedback('备份列表已刷新', 'success');
    }
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

async function loadAnnouncements(showToast = false) {
  try {
    announcements.value = await get('/portal/admin/announcements');
    if (showToast) {
      showFeedback('公告列表已刷新', 'success');
    }
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

watch(activeSection, (section) => {
  const loaderMap = {
    overview: () => {
      if (!summary.value) {
        loadSummary();
      }
    },
    companies: () => {
      if (!pendingCompanies.value.length) {
        loadPendingCompanies();
      }
    },
    jobs: () => {
      if (!pendingJobs.value.length) {
        loadPendingJobs();
      }
    },
    discussions: () => {
      if (!pendingDiscussions.value.length) {
        loadPendingDiscussions();
      }
    },
    users: () => {
      if (!users.value.length) {
        loadUsers();
      }
    },
    finance: () => {
      if (!transactions.value.length) {
        loadTransactions();
      }
    },
    announcements: () => {
      if (!announcements.value.length) {
        loadAnnouncements();
      }
    },
    backups: () => {
      if (!backups.value.length) {
        loadBackups();
      }
    }
  };

  loaderMap[section]?.();
});

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
