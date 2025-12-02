<template>
  <div class="dashboard-layout">
    <aside class="sidebar glass">
      <div class="sidebar__brand">
        <p class="badge">Tsuki 校园</p>
        <h1>企业中心</h1>
        <p class="muted">{{ authInfo?.roleDisplayName }} · {{ authInfo?.username }}</p>
      </div>
      <nav class="sidebar__nav">
        <RouterLink
          v-for="item in navMenu"
          :key="item.name"
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
          <h2>企业工作台</h2>
          <p class="muted">管理企业资料、招聘流程与财务账单</p>
          <div class="status-pills">
            <span class="status-pill" :class="auditStatusClass">{{ auditStatusText }}</span>
            <span class="status-pill" :class="{ success: isActivated, warning: !isActivated }">
              {{ isActivated ? '邀请码已激活' : '待激活邀请码' }}
            </span>
          </div>
        </div>
        <div class="topbar__actions">
          <div class="wallet" v-if="walletSummary">
            <span>钱包余额</span>
            <strong>￥{{ formatMoney(walletSummary.balance) }}</strong>
            <button class="outline" @click="refreshWallet" :disabled="loadingWallet">
              {{ loadingWallet ? '刷新中' : '刷新余额' }}
            </button>
          </div>
          <button class="outline pill-btn clickable" v-if="unreadCount !== null" @click="goMessages">
            未读 {{ unreadCount }}
          </button>
          <button class="outline" @click="handleLogout">退出登录</button>
        </div>
      </header>
      <main class="content">
        <div v-if="!isApproved" class="notice">
          <strong>企业资料待审核</strong>：完成基本信息并等待管理员审核通过后，方可发布职位、参与讨论、批阅学生简历。
          <template v-if="companyProfile && companyProfile.auditStatus === 'rejected'">
            审核未通过<span v-if="companyProfile.auditReason">（{{ companyProfile.auditReason }}）</span>，请修改资料后重新提交。
          </template>
        </div>
        <RouterView @request-wallet-refresh="refreshWallet" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { clearAuthInfo, getAuthInfo, get } from '../../api/http';
import { UNREAD_POLL_INTERVAL, shouldPoll } from '../../config/ui';

const router = useRouter();
const route = useRoute();
const authInfo = getAuthInfo();

const walletSummary = ref(null);
const companyProfile = ref(null);
const isApproved = computed(() => (companyProfile.value?.auditStatus ?? 'pending') === 'approved');
const isActivated = computed(() => !!companyProfile.value?.inviteActivated);
const canUseFeatures = computed(() => isApproved.value && isActivated.value);
const auditStatusText = computed(() => {
  const status = companyProfile.value?.auditStatus ?? 'pending';
  if (status === 'approved') return '资料已通过';
  if (status === 'rejected') return '审核被驳回';
  return '资料审核中';
});
const auditStatusClass = computed(() => {
  const status = companyProfile.value?.auditStatus ?? 'pending';
  return {
    success: status === 'approved',
    danger: status === 'rejected',
    warning: status !== 'approved' && status !== 'rejected'
  };
});
const loadingWallet = ref(false);
const unreadCount = ref(null);
const loadingUnread = ref(false);
let poller = null;
let visHandler = null;

const navItems = [
  { name: 'profile', route: 'company-profile', label: '企业资料' },
  { name: 'jobs', route: 'company-jobs', label: '职位管理' },
  { name: 'finance', route: 'company-finance', label: '财务往来' },
  { name: 'applications', route: 'company-applications', label: '简历投递' },
  { name: 'messages', route: 'company-messages', label: '消息中心' },
  { name: 'discussions', route: 'company-discussions', label: '企业讨论区' },
  { name: 'announcements', route: 'company-announcements', label: '平台公告' }
];

const navMenu = computed(() => (canUseFeatures.value ? navItems : navItems.filter(i => i.name === 'profile')));

provide('walletSummary', walletSummary);
provide('refreshWallet', refreshWallet);
provide('authInfo', authInfo);
provide('refreshUnreadCount', refreshUnreadCount);

onMounted(() => {
  refreshWallet();
  loadCompanyProfile();
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

async function refreshWallet() {
  try {
    loadingWallet.value = true;
    walletSummary.value = await get('/portal/company/wallet');
  } catch (error) {
    console.error('加载钱包余额失败', error);
  } finally {
    loadingWallet.value = false;
  }
}

async function loadCompanyProfile() {
  try {
    companyProfile.value = await get('/portal/company/profile');
  } catch (error) {
    companyProfile.value = null;
  }
}

function handleLogout() {
  clearAuthInfo();
  router.replace({ name: 'login' });
}

function goMessages() {
  router.push({ name: 'company-messages' });
}

function formatMoney(value) {
  const amount = Number(value ?? 0);
  return amount.toFixed(2);
}

async function refreshUnreadCount() {
  try {
    loadingUnread.value = true;
    const count = await get('/portal/company/messages/unread-count');
    unreadCount.value = Number(count ?? 0);
  } catch (error) {
    // ignore
  } finally {
    loadingUnread.value = false;
  }
}
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
  background: linear-gradient(155deg, rgba(30, 58, 138, 0.96), rgba(37, 99, 235, 0.88));
  color: #fff;
  display: flex;
  flex-direction: column;
  padding: 26px 22px;
  gap: 22px;
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
  font-size: 24px;
}

.sidebar__brand p {
  margin: 0;
  color: rgba(255, 255, 255, 0.75);
}

.sidebar__nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  padding: 22px 26px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.topbar h2 {
  margin: 0;
  font-size: 24px;
  color: #1e293b;
}

.topbar__actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.wallet {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 14px;
  background: var(--color-primary-soft);
  border: 1px solid var(--color-primary-border);
  color: var(--color-primary-strong);
}

.wallet strong {
  font-size: 18px;
}

.status-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #e2e8f0;
  color: #0f172a;
  font-weight: 600;
  font-size: 12px;
  border: 1px solid rgba(148, 163, 184, 0.6);
}

.status-pill.success {
  background: var(--color-success-soft);
  border-color: rgba(22, 163, 74, 0.3);
  color: var(--color-success);
}

.status-pill.warning {
  background: #fef3c7;
  border-color: rgba(251, 191, 36, 0.4);
  color: #b45309;
}

.status-pill.danger {
  background: var(--color-danger-soft);
  border-color: rgba(185, 28, 28, 0.3);
  color: var(--color-danger);
}

.notice {
  background: #fff7ed;
  border: 1px dashed rgba(251, 146, 60, 0.6);
  color: #b45309;
  padding: 14px 16px;
  border-radius: 14px;
}

.unread {
  display: flex;
  align-items: center;
  gap: 10px;
}

.content {
  padding: 0 4px 16px;
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.clickable {
  cursor: pointer;
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
    justify-content: flex-start;
  }
}
</style>
