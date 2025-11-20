<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="sidebar__brand">
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
      <header class="topbar">
        <div>
          <h2>企业工作台</h2>
          <p class="muted">管理企业资料、招聘流程与财务账单</p>
        </div>
        <div class="topbar__actions">
          <div class="wallet" v-if="walletSummary">
            <span>钱包余额</span>
            <strong>￥{{ formatMoney(walletSummary.balance) }}</strong>
            <button class="outline" @click="refreshWallet" :disabled="loadingWallet">
              {{ loadingWallet ? '刷新中' : '刷新余额' }}
            </button>
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
}

.sidebar {
  background: linear-gradient(160deg, #1e3a8a, #1d4ed8);
  color: #fff;
  display: flex;
  flex-direction: column;
  padding: 28px 24px;
  gap: 24px;
}

.sidebar__brand h1 {
  margin: 0;
  font-size: 24px;
}

.sidebar__brand p {
  margin: 6px 0 0;
  color: rgba(255, 255, 255, 0.75);
}

.sidebar__nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
}

.main {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.topbar {
  padding: 24px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border-bottom: 1px solid var(--border-subtle);
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
}

.wallet {
  display: flex;
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

.unread {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-radius: 12px;
  background: #fef3c7;
  border: 1px solid #fde68a;
  color: #b45309;
}

.content {
  padding: 32px;
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
</style>
