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
                      <option value="approved">已发布</option>
                      <option value="closed">已关闭</option>
                      <option value="rejected" disabled>已拒绝</option>
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
              <button class="primary" type="submit">提交记录</button>
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
          <div class="grid-stack">
            <PieChart :data="applicationChartData" title="投递状态分布" />
            <table v-if="applications.length" class="table">
              <thead>
                <tr><th>职位</th><th>学生</th><th>状态</th><th>更新时间</th><th>操作</th></tr>
              </thead>
              <tbody>
                <tr v-for="app in applications" :key="app.id" :class="{ active: app.id === selectedApplicationId }">
                  <td>{{ app.jobTitle || resolveJobTitle(app.jobId) }}</td>
                  <td>{{ app.studentName || `学生 #${app.studentId}` }}</td>
                  <td>{{ app.status }}</td>
                  <td>{{ formatDate(app.updateTime) }}</td>
                  <td class="actions">
                    <button class="outline" type="button" @click="viewApplication(app)">
                      {{ app.id === selectedApplicationId ? '已展开' : '查看详情' }}
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <p v-else class="muted">暂无投递</p>
          </div>

          <section v-if="selectedApplication" class="card secondary-card">
            <div class="application-detail__header">
              <div>
                <h3>{{ selectedApplication.resume?.title || '未命名简历' }}</h3>
                <p class="muted">
                  投递职位：{{ selectedApplication.jobTitle || resolveJobTitle(selectedApplication.jobId) }} · 当前状态：{{ selectedApplication.status }}
                </p>
                <p class="muted">
                  学生：{{ selectedApplication.studentName || `学生 #${selectedApplication.studentId}` }}
                </p>
              </div>
              <button class="outline" type="button" @click="clearSelectedApplication">收起</button>
            </div>

            <div class="application-detail__content">
              <article class="resume-pane">
                <h4>简历概要</h4>
                <dl>
                  <div>
                    <dt>教育经历</dt>
                    <dd>
                      <ul>
                        <li v-for="(item, index) in splitLines(selectedApplication.resume?.educationExperience)" :key="`edu-${index}`">
                          {{ item }}
                        </li>
                      </ul>
                    </dd>
                  </div>
                  <div>
                    <dt>实习 / 项目</dt>
                    <dd>
                      <ul>
                        <li v-for="(item, index) in splitLines(selectedApplication.resume?.workExperience)" :key="`work-${index}`">
                          {{ item }}
                        </li>
                      </ul>
                    </dd>
                  </div>
                  <div>
                    <dt>技能特长</dt>
                    <dd>
                      <ul class="tag-list">
                        <li v-for="(item, index) in splitLines(selectedApplication.resume?.skills)" :key="`skill-${index}`">
                          {{ item }}
                        </li>
                      </ul>
                    </dd>
                  </div>
                  <div v-if="selectedApplication.resume?.selfEvaluation">
                    <dt>自我评价</dt>
                    <dd>{{ selectedApplication.resume.selfEvaluation }}</dd>
                  </div>
                  <div v-if="selectedApplication.resume?.attachment" class="attachment-row">
                    <dt>附件下载</dt>
                    <dd>
                      <a :href="selectedApplication.resume.attachment" target="_blank" rel="noopener">
                        点击查看附件简历
                      </a>
                    </dd>
                  </div>
                </dl>
              </article>

              <article class="decision-pane">
                <h4>处理结果</h4>
                <p class="muted">选择后提交即可同步学生端，并自动推送站内消息。</p>
                <form class="decision-form" @submit.prevent="submitDecision">
                  <div class="decision-options">
                    <label><input type="radio" value="面试中" v-model="decisionForm.status" /> 邀约面试</label>
                    <label><input type="radio" value="录用" v-model="decisionForm.status" /> 录用通过</label>
                    <label><input type="radio" value="拒绝" v-model="decisionForm.status" /> 投递不通过</label>
                  </div>
                  <label class="full">
                    补充说明
                    <textarea
                      v-model="decisionForm.note"
                      :placeholder="decisionForm.status === '拒绝' ? '请填写具体拒绝原因，学生端会看到该内容' : '可填写面试安排或录用说明'"
                      rows="4"
                    ></textarea>
                  </label>
                  <label class="toggle">
                    <input type="checkbox" v-model="decisionForm.notifyStudent" /> 同步发送站内消息
                  </label>
                  <div class="form-actions">
                    <button class="primary" type="submit">提交结果</button>
                    <button class="outline" type="button" @click="resetDecisionForm">重置</button>
                  </div>
                  <p v-if="selectedApplication.decisionNote && decisionForm.note !== selectedApplication.decisionNote" class="muted" style="margin-top: 8px;">
                    上次反馈：{{ selectedApplication.decisionNote }}
                  </p>
                </form>
              </article>
            </div>
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
  { key: 'jobs', label: '职位管理', icon: '💼', description: '发布并即时上线招聘岗位' },
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
const selectedApplicationId = ref(null);
const decisionForm = reactive({
  status: '面试中',
  note: '',
  notifyStudent: true
});

const selectedApplication = computed(() => {
  return applications.value.find(item => item.id === selectedApplicationId.value) || null;
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
    showFeedback('营业执照上传成功，管理员可查看最新资料', 'success');
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
      showFeedback('职位已发布，学生端已同步', 'success');
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
    const previousId = selectedApplicationId.value;
    applications.value = await get('/portal/company/applications');
    if (previousId) {
      const stillExists = applications.value.some(item => item.id === previousId);
      selectedApplicationId.value = stillExists ? previousId : null;
    }
    if (showToast) {
      showFeedback('投递列表已刷新', 'success');
    }
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function viewApplication(app) {
  selectedApplicationId.value = app.id;
}

function clearSelectedApplication() {
  selectedApplicationId.value = null;
}

function hydrateDecisionForm(app) {
  if (!app) {
    decisionForm.status = '面试中';
    decisionForm.note = '';
    decisionForm.notifyStudent = true;
    return;
  }
  const supportedStatuses = ['面试中', '录用', '拒绝'];
  decisionForm.status = supportedStatuses.includes(app.status) ? app.status : '面试中';
  decisionForm.note = app.decisionNote || '';
  decisionForm.notifyStudent = true;
}

function resetDecisionForm() {
  hydrateDecisionForm(selectedApplication.value);
}

async function submitDecision() {
  if (!selectedApplication.value) {
    showFeedback('请选择需要处理的投递', 'error');
    return;
  }
  const note = decisionForm.note.trim();
  if (decisionForm.status === '拒绝' && !note) {
    showFeedback('请填写具体的拒绝原因', 'error');
    return;
  }
  try {
    const payload = {
      status: decisionForm.status,
      decisionNote: note || null,
      notifyStudent: decisionForm.notifyStudent
    };
    const updated = await patch(`/portal/company/applications/${selectedApplication.value.id}`, payload);
    const index = applications.value.findIndex(item => item.id === updated.id);
    if (index !== -1) {
      applications.value[index] = updated;
    }
    selectedApplicationId.value = updated.id;
    showFeedback('处理结果已同步', 'success');
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

function splitLines(text) {
  if (!text) {
    return [];
  }
  return text
    .split(/\r?\n/)
    .map(item => item.trim())
    .filter(Boolean);
}

function resolveJobTitle(jobId) {
  const direct = applications.value.find(item => item.jobId === jobId && item.jobTitle);
  if (direct?.jobTitle) {
    return direct.jobTitle;
  }
  const job = jobs.value.find(item => item.id === jobId);
  return job ? job.jobTitle : `职位 #${jobId}`;
}

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

watch(selectedApplication, (app) => {
  hydrateDecisionForm(app);
}, { immediate: true });

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
