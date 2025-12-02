<template>
  <section class="home">
    <section class="hero-card">
      <div class="hero__copy">
        <p class="eyebrow">欢迎回来，{{ authInfo?.username || '同学' }}</p>
        <h2>开启你的求职旅程</h2>
        <p class="muted">
          快速完善简历、查看投递进度，捕捉最新岗位与平台公告。
        </p>
        <div class="hero__actions">
          <button class="primary" type="button" @click="go('student-jobs')">去找职位</button>
          <button class="outline" type="button" @click="go('student-resumes')">管理简历</button>
        </div>
        <div class="hero__chips">
          <span class="chip">智能推荐</span>
          <span class="chip ghost">宣讲会订阅</span>
          <span class="chip ghost">进度提醒</span>
        </div>
      </div>
      <div class="hero__stats">
        <div class="stat-card">
          <span>我的投递</span>
          <strong>{{ stats.totalApplications }}</strong>
          <small>待处理 {{ stats.pendingApplications }}</small>
        </div>
        <div class="stat-card">
          <span>未读消息</span>
          <strong>{{ stats.unreadMessages }}</strong>
          <small>及时查看企业反馈</small>
        </div>
        <div class="stat-card">
          <span>简历数量</span>
          <strong>{{ stats.resumes }}</strong>
          <small>保持最新，提高通过率</small>
        </div>
      </div>
    </section>

    <div class="focus-strip">
      <div class="focus-item">
        <span class="label">快速入口</span>
        <p>浏览职位 / 查看投递 / 消息中心</p>
      </div>
      <div class="focus-item">
        <span class="label">安全同步</span>
        <p>登录后自动保存最新资料与简历</p>
      </div>
      <div class="focus-item">
        <span class="label">每日亮点</span>
        <p>平台公告与讨论区精选话题</p>
      </div>
    </div>

    <div class="panel-grid">
      <section class="card quick-actions">
        <div class="card__title">
          <h3>快捷操作</h3>
          <button class="outline" type="button" @click="reload" :disabled="loading">
            {{ loading ? '刷新中...' : '刷新' }}
          </button>
        </div>
        <div class="actions">
          <button class="ghost" @click="go('student-jobs')">浏览职位</button>
          <button class="ghost" @click="go('student-applications')">查看投递</button>
          <button class="ghost" @click="go('student-messages')">消息中心</button>
          <button class="ghost" @click="go('student-discussions')">企业讨论</button>
        </div>
      </section>

      <section class="card profile-card">
        <div class="card__title">
          <h3>我的资料</h3>
          <button class="outline" type="button" @click="go('student-profile')">完善资料</button>
        </div>
        <div v-if="profile" class="profile-grid">
          <div><strong>姓名：</strong>{{ profile.name || '未填写' }}</div>
          <div><strong>学校：</strong>{{ profile.school || '未填写' }}</div>
          <div><strong>专业：</strong>{{ profile.major || '未填写' }}</div>
          <div><strong>学历：</strong>{{ profile.education || '未填写' }}</div>
        </div>
        <p v-else class="muted">暂无资料，请前往完善。</p>
      </section>

      <section class="card gradient">
        <div class="card__title">
          <h3>我的简历</h3>
          <button class="outline" type="button" @click="go('student-resumes')">管理简历</button>
        </div>
        <ul class="list" v-if="resumes.length">
          <li v-for="resume in resumes" :key="resume.id" class="list__item">
            <div>
              <h4>{{ resume.title }}</h4>
              <p class="muted">更新于 {{ formatDate(resume.updateTime) }}</p>
            </div>
            <button class="outline" type="button" @click="go('student-resumes')">查看</button>
          </li>
        </ul>
        <p v-else class="muted">暂无简历，去创建一份吧。</p>
      </section>

      <section class="card gradient-soft">
        <div class="card__title">
          <h3>近期投递</h3>
          <button class="outline" type="button" @click="go('student-applications')">查看全部</button>
        </div>
        <ul class="list" v-if="applications.length">
          <li v-for="app in applications" :key="app.id" class="list__item">
            <div>
              <h4>职位 #{{ app.jobId }}</h4>
              <p class="muted">状态：{{ app.status }} · 投递时间 {{ formatDate(app.applyTime) }}</p>
              <p class="muted">备注：{{ app.decisionNote || '—' }}</p>
            </div>
            <span :class="['pill', statusClass(app.status)]">{{ app.status }}</span>
          </li>
        </ul>
        <p v-else class="muted">暂无投递记录。</p>
      </section>

      <section class="card gradient">
        <div class="card__title">
          <h3>最新职位</h3>
          <button class="outline" type="button" @click="go('student-jobs')">更多</button>
        </div>
        <ul class="list" v-if="jobs.length">
          <li v-for="job in jobs" :key="job.id" class="list__item">
            <div>
              <h4>{{ job.jobTitle }}</h4>
              <p class="muted">
                {{ job.companyName }} · {{ job.location || '不限' }} · {{ job.salaryRange || '薪资面议' }}
              </p>
            </div>
            <button class="outline" type="button" @click="go('student-jobs')">查看</button>
          </li>
        </ul>
        <p v-else class="muted">暂无职位，稍后再来看看。</p>
      </section>

      <section class="card gradient-soft">
        <div class="card__title">
          <h3>最新公告</h3>
          <span class="muted">更多公告请前往公告页查看</span>
        </div>
        <ul class="list" v-if="announcements.length">
          <li v-for="item in announcements" :key="item.id" class="list__item">
            <div>
              <h4>{{ item.title }}</h4>
              <p class="muted">{{ formatDate(item.publishTime) }}</p>
              <p class="muted content">{{ item.content }}</p>
            </div>
          </li>
        </ul>
        <p v-else class="muted">暂无公告。</p>
      </section>

      <section class="card gradient">
        <div class="card__title">
          <h3>最近消息</h3>
          <button class="outline" type="button" @click="go('student-messages')">前往消息中心</button>
        </div>
        <ul class="list" v-if="recentMessages.length">
          <li v-for="msg in recentMessages" :key="msg.id" class="list__item">
            <div>
              <h4>{{ msg.title }}</h4>
              <p class="muted">{{ formatDate(msg.sendTime) }}</p>
              <p class="muted content">{{ msg.content }}</p>
            </div>
            <span class="pill" :class="{ unread: !msg.isRead, success: msg.isRead }">
              {{ msg.isRead ? '已读' : '未读' }}
            </span>
          </li>
        </ul>
        <p v-else class="muted">暂无消息。</p>
      </section>
    </div>
  </section>
</template>

<script setup>
import { inject, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { get } from '../../api/http';

const router = useRouter();
const authInfo = inject('authInfo', null);
const refreshUnreadCount = inject('refreshUnreadCount', () => {});

const loading = ref(false);
const stats = reactive({
  totalApplications: 0,
  pendingApplications: 0,
  unreadMessages: 0,
  resumes: 0
});

const profile = ref(null);
const resumes = ref([]);
const applications = ref([]);
const jobs = ref([]);
const announcements = ref([]);
const recentMessages = ref([]);

function go(routeName) {
  router.push({ name: routeName });
}

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

function statusClass(status) {
  const s = (status || '').toLowerCase();
  if (s.includes('待') || s.includes('pending')) return 'warning';
  if (s.includes('通') || s.includes('pass') || s.includes('approved')) return 'success';
  if (s.includes('拒') || s.includes('fail') || s.includes('reject')) return 'danger';
  return '';
}

async function loadStats() {
  const [apps, messages, resumeList, profileData] = await Promise.all([
    get('/portal/student/applications'),
    get('/portal/student/messages'),
    get('/portal/student/resumes'),
    get('/portal/student/profile').catch(() => null)
  ]);
  profile.value = profileData;
  applications.value = apps.slice(0, 3);
  resumes.value = resumeList.slice(0, 2);
  stats.totalApplications = apps.length;
  stats.pendingApplications = apps.filter(app => (app.status || '').includes('待')).length;
  stats.unreadMessages = messages.filter(m => !m.isRead).length;
  stats.resumes = resumeList.length;
  recentMessages.value = messages.slice(0, 3);
  try {
    refreshUnreadCount();
  } catch {
    /* ignore */
  }
}

async function loadJobsAndAnnouncements() {
  const [jobList, annList] = await Promise.all([
    get('/portal/student/jobs').catch(() => []),
    get('/portal/student/announcements').catch(() => [])
  ]);
  jobs.value = jobList.slice(0, 3);
  announcements.value = annList.slice(0, 3);
}

async function reload() {
  try {
    loading.value = true;
    await Promise.all([loadStats(), loadJobsAndAnnouncements()]);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  reload();
});
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-card {
  position: relative;
  background: linear-gradient(135deg, rgba(14, 165, 233, 0.12), rgba(37, 99, 235, 0.08)),
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.25), transparent 40%),
    radial-gradient(circle at 90% 10%, rgba(79, 70, 229, 0.16), transparent 40%);
  border: 1px solid rgba(37, 99, 235, 0.16);
  border-radius: 20px;
  padding: 22px 22px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
  box-shadow: 0 18px 40px rgba(37, 99, 235, 0.16);
  overflow: hidden;
}

.hero-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(255, 255, 255, 0.32), transparent 55%);
  pointer-events: none;
}

.hero__copy h2 {
  margin: 6px 0 8px;
  font-size: 28px;
  color: #0f172a;
}

.hero__copy .muted {
  color: #1e293b;
}

.eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 12px;
  color: #2563eb;
  font-weight: 700;
}

.hero__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.hero__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.3);
  color: #0f172a;
  font-size: 13px;
  font-weight: 600;
}

.chip.ghost {
  background: rgba(37, 99, 235, 0.08);
  color: #1d4ed8;
  border-color: rgba(37, 99, 235, 0.2);
}

.hero__stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  align-self: center;
}

.stat-card {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(37, 99, 235, 0.18);
  border-radius: 14px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
}

.stat-card strong {
  font-size: 22px;
  color: var(--color-primary-strong);
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.focus-strip {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-soft);
}

.focus-item {
  padding: 8px 10px;
}

.focus-item .label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.1);
  color: #1d4ed8;
  font-weight: 700;
  font-size: 12px;
}

.focus-item p {
  margin: 8px 0 0;
  color: #475569;
  font-weight: 600;
}

.quick-actions .actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-actions .ghost {
  background: #f8fafc;
  border: 1px solid var(--border-subtle);
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease, border-color 0.12s ease;
}

.quick-actions .ghost:hover {
  border-color: var(--color-primary);
  box-shadow: 0 12px 28px rgba(37, 99, 235, 0.16);
  transform: translateY(-1px);
}

.card.gradient {
  background: linear-gradient(160deg, rgba(37, 99, 235, 0.08), rgba(59, 130, 246, 0.05));
  border: 1px solid rgba(37, 99, 235, 0.14);
}

.card.gradient-soft {
  background: linear-gradient(160deg, rgba(236, 72, 153, 0.08), rgba(99, 102, 241, 0.06));
  border: 1px solid rgba(236, 72, 153, 0.12);
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
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-subtle);
}

.pill {
  align-self: center;
  padding: 6px 10px;
  border-radius: 999px;
  background: #e2e8f0;
  color: #0f172a;
  font-size: 12px;
  border: 1px solid rgba(148, 163, 184, 0.7);
}

.pill.success {
  background: var(--color-success-soft);
  border-color: rgba(22, 163, 74, 0.3);
  color: var(--color-success);
}

.pill.warning {
  background: #fef3c7;
  border-color: rgba(251, 191, 36, 0.4);
  color: #b45309;
}

.pill.danger {
  background: var(--color-danger-soft);
  border-color: rgba(185, 28, 28, 0.3);
  color: var(--color-danger);
}

.pill.unread {
  background: #fef3c7;
  color: #b45309;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px 12px;
}

.content {
  max-width: 560px;
}

@media (max-width: 768px) {
  .hero-card {
    grid-template-columns: 1fr;
  }
}
</style>
