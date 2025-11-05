<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="sidebar__brand">
        <h1>系统后台</h1>
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
          <h2>管理员工作台</h2>
          <p class="muted">查看平台状态，审核内容并管理财务</p>
        </div>
        <div class="topbar__actions">
          <div class="wallet" v-if="walletSummary">
            <span>平台钱包</span>
            <strong>￥{{ formatMoney(walletSummary.balance) }}</strong>
            <button class="outline" @click="refreshWallet" :disabled="loadingWallet">
              {{ loadingWallet ? '刷新中' : '刷新余额' }}
            </button>
          </div>
          <button class="outline" @click="handleLogout">退出登录</button>
        </div>
      </header>
      <main class="content">
        <RouterView @request-wallet-refresh="refreshWallet" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { onMounted, provide, ref } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { clearAuthInfo, getAuthInfo, get } from '../../api/http';

const router = useRouter();
const route = useRoute();
const authInfo = getAuthInfo();

const walletSummary = ref(null);
const loadingWallet = ref(false);

const navItems = [
  { route: 'admin-overview', label: '平台概览' },
  { route: 'admin-companies', label: '企业审核' },
  // 职位审核入口已移除
  { route: 'admin-discussions', label: '讨论审核' },
  { route: 'admin-users', label: '用户管理' },
  { route: 'admin-finance', label: '财务管理' },
  { route: 'admin-announcements', label: '公告管理' },
  { route: 'admin-invites', label: '邀请码管理' },
  { route: 'admin-backups', label: '数据备份' }
];

provide('walletSummary', walletSummary);
provide('refreshWallet', refreshWallet);
provide('authInfo', authInfo);

onMounted(() => {
  refreshWallet();
});

async function refreshWallet() {
  try {
    loadingWallet.value = true;
    walletSummary.value = await get('/portal/admin/wallet');
  } catch (error) {
    console.error('加载管理员钱包失败', error);
  } finally {
    loadingWallet.value = false;
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
</script>

<style scoped>
.dashboard-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
  background: #f8fafc;
}

.sidebar {
  background: linear-gradient(160deg, #0f172a, #1e3a8a);
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
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
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
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #1d4ed8;
}

.content {
  padding: 32px;
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.card {
  background: #fff;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card__title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.form-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.form-grid label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-weight: 600;
  color: #1f2937;
}

.form-grid input,
.form-grid textarea,
.table select,
.table input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px;
  font-size: 14px;
}

.form-grid textarea {
  min-height: 80px;
  resize: vertical;
}

.form-grid .full {
  grid-column: 1 / -1;
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

.list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
