<template>
  <div class="dashboard-layout">
    <aside class="sidebar glass">
      <div class="sidebar__brand">
        <p class="badge">Tsuki 校园</p>
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
      <header class="topbar hero">
        <div>
          <p class="eyebrow">欢迎回来</p>
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
        <RouterView />
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
  { route: 'admin-messages', label: '消息中心' },
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
  padding: 28px 22px;
  gap: 20px;
  background: var(--bg-app);
}

.sidebar {
  position: relative;
  background: linear-gradient(155deg, rgba(15, 23, 42, 0.96), rgba(29, 78, 216, 0.88));
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
  background: rgba(255, 255, 255, 0.08);
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

.content {
  padding: 0 4px 16px;
  flex: 1;
  overflow-y: auto;
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
