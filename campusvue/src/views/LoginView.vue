<template>
  <div class="login-page">
    <section class="login-card">
      <header class="login-header">
        <h1>校园招聘平台</h1>
        <p>使用注册账号登录，系统会根据角色跳转到对应工作台</p>
      </header>
      <form class="login-form" @submit.prevent="handleSubmit">
        <label>
          <span>用户名</span>
          <input v-model.trim="username" type="text" autocomplete="username" required />
        </label>
        <label>
          <span>密码</span>
          <input v-model="password" type="password" autocomplete="current-password" required />
        </label>
        <button class="primary" type="submit" :disabled="loading">
          {{ loading ? '正在登录…' : '登录' }}
        </button>
      </form>
      <p class="note">
        还没有账号？<RouterLink :to="{ name: 'register' }">立即注册</RouterLink>
      </p>
      <p v-if="feedback.message" :class="['feedback', feedback.type]">
        {{ feedback.message }}
      </p>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter, useRoute, RouterLink } from 'vue-router';
import { post, setAuthInfo, clearAuthInfo } from '../api/http';

const router = useRouter();
const route = useRoute();
const username = ref('');
const password = ref('');
const loading = ref(false);
const feedback = reactive({ message: '', type: 'info' });

const roleRoutes = {
  ADMIN: { name: 'admin-overview' },
  STUDENT: { name: 'student-dashboard' },
  COMPANY: { name: 'company-profile' }
};

const presetUsername = typeof route.query.username === 'string' ? route.query.username : '';
if (presetUsername) {
  username.value = presetUsername;
}

if (route.query.registered) {
  feedback.message = '注册成功，请使用新账号登录';
  feedback.type = 'success';
}

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
}

async function handleSubmit() {
  if (loading.value) {
    return;
  }
  loading.value = true;
  showFeedback('正在验证，请稍候…', 'info');
  try {
    const data = await post('/auth/login', {
      username: username.value,
      password: password.value
    });
    const payload = {
      userId: data.userId,
      username: data.username,
      role: data.role,
      roleDisplayName: data.roleDisplayName,
      token: data.token,
      redirectPath: data.redirectPath
    };
    setAuthInfo(payload);
    showFeedback(`登录成功，欢迎 ${data.roleDisplayName} ${data.username}`, 'success');
    setTimeout(() => {
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '';
      if (redirect) {
        router.replace(redirect);
        return;
      }
      const target = roleRoutes[data.role];
      if (!target) {
        showFeedback('当前角色暂不支持登录，请联系管理员', 'error');
        clearAuthInfo();
        return;
      }
      router.push(target);
    }, 300);
  } catch (error) {
    console.error(error);
    showFeedback(error.message ?? '登录失败，请稍后重试', 'error');
    clearAuthInfo();
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
}

.login-card {
  width: min(420px, 100%);
  padding: 32px 28px;
  border-radius: var(--radius-xl);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(18px);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.login-header h1 {
  margin: 0;
  font-size: 26px;
  color: var(--color-primary-strong);
}

.login-header p {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--color-text-muted);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: var(--color-text-main);
  font-weight: 600;
  font-size: 14px;
}

.feedback {
  text-align: center;
}

.note {
  margin: 0;
  text-align: center;
  font-size: 13px;
  color: var(--color-text-muted);
}

.note a {
  color: var(--color-primary);
  font-weight: 600;
  text-decoration: none;
}

.note a:hover {
  text-decoration: underline;
}
</style>
