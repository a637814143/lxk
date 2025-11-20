<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="sidebar__brand">
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
      <header class="topbar">
        <div>
          <h2>学生工作台</h2>
          <p class="muted">管理个人资料、简历、投递及消息</p>
        </div>
        <div class="topbar__actions">
          <div class="selected-resume" v-if="selectedResumeId">
            <span>当前投递简历</span>
            <strong>#{{ selectedResumeId }}</strong>
          </div>
          <div class="unread" v-if="unreadCount !== null">
            <span>未读消息</span>
            <strong>{{ unreadCount }}</strong>
            <button class="outline" @click="refreshUnreadCount" :disabled="loadingUnread">
              {{ loadingUnread ? '刷新中' : '刷新' }}
            </button>
          </div>
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
  { route: 'student-profile', label: '基本资料' },
  { route: 'student-resumes', label: '我的简历' },
  { route: 'student-jobs', label: '职位浏览' },
  { route: 'student-applications', label: '我的投递' },
  { route: 'student-messages', label: '消息中心' },
  { route: 'student-announcements', label: '平台公告' },
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
  grid-template-columns: 240px 1fr;
  min-height: 100vh;
}

.sidebar {
  background: linear-gradient(145deg, #0f172a, #1f2937);
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 28px 24px;
}

.sidebar__brand h1 {
  margin: 0;
  font-size: 22px;
}

.sidebar__brand p {
  margin: 6px 0 0;
  color: rgba(255, 255, 255, 0.75);
}

.sidebar__nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar__link {
  padding: 10px 14px;
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  transition: background 0.2s ease;
}

.sidebar__link:hover,
.sidebar__link.active {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.main {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: #ffffff;
  border-bottom: 1px solid var(--border-subtle);
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
}

.selected-resume strong {
  font-size: 16px;
}

.unread {
  background: #fef3c7;
  color: #b45309;
  padding: 10px 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  border: 1px solid #fde68a;
}

.content {
  flex: 1;
  padding: 32px;
}
</style>
