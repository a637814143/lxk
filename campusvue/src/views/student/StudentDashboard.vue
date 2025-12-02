<template>
  <div class="dashboard-layout">
    <aside class="sidebar glass">
      <div class="sidebar__brand">
        <p class="badge">Tsuki 校园</p>
        <h1>学生中心</h1>
        <p class="muted">{{ authInfo?.roleDisplayName }} · {{ authInfo?.username }}</p>
      </div>
      <nav class="sidebar__nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.route"
          :to="{ name: item.route }"
          class="sidebar__link"
          :class="{ active: route.name === item.route }"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <div class="main">
      <header class="topbar hero">
        <div>
          <p class="eyebrow">欢迎回来</p>
          <h2>学生工作台</h2>
          <p class="muted">管理个人资料、简历、投递及消息，随时掌握最新进度。</p>
        </div>
        <div class="topbar__actions">
          <div class="selected-resume" v-if="selectedResumeId">
            <span>当前投递简历</span>
            <strong>#{{ selectedResumeId }}</strong>
          </div>
          <button class="outline pill-btn clickable" v-if="unreadCount !== null" @click="goMessages">
            <span>未读 {{ unreadCount }}</span>
          </button>
          <button class="outline" @click="handleLogout">退出登录</button>
        </div>
      </header>
      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { clearAuthInfo, getAuthInfo, get } from '../../api/http';
import { UNREAD_POLL_INTERVAL, shouldPoll } from '../../config/ui';

const router = useRouter();
const route = useRoute();
const authInfo = getAuthInfo();

const selectedResumeId = ref(null);
const unreadCount = ref(null);
const loadingUnread = ref(false);
let poller = null;
let visHandler = null;

function setSelectedResumeId(id) {
  selectedResumeId.value = id ?? null;
}

const navItems = [
  { route: 'student-home', label: '首页' },
  { route: 'student-profile', label: '基本资料' },
  { route: 'student-resumes', label: '我的简历' },
  { route: 'student-jobs', label: '职位浏览' },
  { route: 'student-applications', label: '我的投递' },
  { route: 'student-messages', label: '消息中心' },
  { route: 'student-discussions', label: '企业讨论区' }
];

provide('authInfo', authInfo);
provide('selectedResumeId', selectedResumeId);
provide('setSelectedResumeId', setSelectedResumeId);
provide('refreshUnreadCount', refreshUnreadCount);

function handleLogout() {
  clearAuthInfo();
  router.replace({ name: 'login' });
}

function goMessages() {
  router.push({ name: 'student-messages' });
}

async function refreshUnreadCount() {
  try {
    loadingUnread.value = true;
    const count = await get('/portal/student/messages/unread-count');
    unreadCount.value = Number(count ?? 0);
  } catch (e) {
    // ignore
  } finally {
    loadingUnread.value = false;
  }
}

onMounted(() => {
  refreshUnreadCount();
  poller = setInterval(() => { if (shouldPoll()) refreshUnreadCount(); }, UNREAD_POLL_INTERVAL);
  visHandler = () => { if (shouldPoll()) { refreshUnreadCount(); } };
  document.addEventListener('visibilitychange', visHandler);
});

onUnmounted(() => {
  if (poller) clearInterval(poller);
  if (visHandler) document.removeEventListener('visibilitychange', visHandler);
});

watch(() => route.fullPath, () => {
  refreshUnreadCount();
});
</script>

<style scoped>
.dashboard-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
  padding: 28px 22px;
  gap: 20px;
  background: var(--bg-app);
}

.sidebar {
  position: relative;
  background: linear-gradient(155deg, rgba(31, 41, 55, 0.96), rgba(37, 99, 235, 0.85));
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: 22px;
  padding: 26px 22px;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 20px 44px rgba(15, 23, 42, 0.25);
  overflow: hidden;
}

.sidebar::after {
  content: '';
  position: absolute;
  width: 160px;
  height: 160px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  filter: blur(40px);
  bottom: -60px;
  right: -40px;
}

.sidebar__brand {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sidebar__brand .badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  font-weight: 700;
  font-size: 12px;
  letter-spacing: 0.2px;
  width: fit-content;
}

.sidebar__brand h1 {
  margin: 0;
  font-size: 22px;
}

.sidebar__brand p {
  margin: 0;
  color: rgba(255, 255, 255, 0.75);
}

.sidebar__nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.sidebar__link {
  padding: 11px 14px;
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  transition: all 0.16s ease;
}

.sidebar__link:hover,
.sidebar__link.active {
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.18);
  transform: translateY(-1px);
}

.main {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  gap: 18px;
}

.topbar {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 26px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid var(--border-subtle);
  border-radius: 20px;
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(10px);
  overflow: hidden;
}

.topbar::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(37, 99, 235, 0.08), rgba(59, 130, 246, 0.05));
  z-index: 0;
}

.topbar > * {
  position: relative;
  z-index: 1;
}

.topbar__actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.selected-resume {
  background: var(--color-primary-soft);
  color: var(--color-primary-strong);
  padding: 10px 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
  border: 1px solid var(--color-primary-border);
}

.selected-resume strong {
  font-size: 16px;
}

.unread {
  display: flex;
  align-items: center;
  gap: 10px;
}

.content {
  flex: 1;
  padding: 0 4px 16px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.eyebrow {
  margin: 0 0 4px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #2563eb;
  font-weight: 700;
}

.pill-btn {
  border-radius: 999px;
  padding: 10px 14px;
}

.clickable {
  cursor: pointer;
}

@media (max-width: 960px) {
  .dashboard-layout {
    grid-template-columns: 1fr;
    padding: 18px 14px;
  }

  .topbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .topbar__actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>
