<template>
<div class="auth-page login-page">
    <div class="auth-wrapper">
      <section class="auth-hero">
        <p class="hero-badge">Tsuki 校园招聘</p>
        <h1>让每一次投递都有回应</h1>
        <p class="hero-lede">使用注册账号登录，系统会根据角色自动进入对应工作台。</p>
        <ul class="hero-highlights">
          <li>
            <strong>岗位实时更新</strong>
            <span>对接校企发布的最新职位与公告</span>
          </li>
          <li>
            <strong>进度一目了然</strong>
            <span>投递、面试、消息集中跟踪</span>
          </li>
          <li>
            <strong>安全登录</strong>
            <span>支持多角色切换与个性化工作台</span>
          </li>
        </ul>
        <div class="hero-actions">
          <RouterLink class="outline" :to="{ name: 'register' }">还没有账号？立即注册</RouterLink>
          <p class="muted">新用户将根据所选角色进入学生或企业端</p>
        </div>
      </section>

      <section class="auth-card">
        <header class="card-header">
          <p class="eyebrow">欢迎回来</p>
          <h2>登录账号</h2>
          <p class="muted">输入信息后即可进入对应工作台</p>
        </header>

        <form class="auth-form" @submit.prevent="handleSubmit">
          <label>
            <span>用户名</span>
            <input v-model.trim="username" type="text" placeholder="学号 / 企业账号" autocomplete="username" required />
          </label>
          <label>
            <div class="label-row">
              <span>密码</span>
              <small class="hint">至少 6 位，区分大小写</small>
            </div>
            <input v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" required />
          </label>
          <button class="primary" type="submit" :disabled="loading">
            {{ loading ? '正在登录...' : '登录' }}
          </button>
        </form>

        <div class="form-footnote">
          <p class="note">
            还没有账号？
            <RouterLink :to="{ name: 'register' }">立即注册</RouterLink>
          </p>
        </div>

        <p v-if="feedback.message" :class="['feedback', feedback.type]">
          {{ feedback.message }}
        </p>
      </section>
    </div>
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
  showFeedback('正在验证，请稍候...', 'info');
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
    showFeedback(`登录成功，欢迎${data.roleDisplayName} ${data.username}`, 'success');
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
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 18px 56px;
  background: radial-gradient(circle at 20% 20%, rgba(59, 130, 246, 0.15), transparent 32%),
    radial-gradient(circle at 80% 0%, rgba(236, 72, 153, 0.12), transparent 32%),
    linear-gradient(135deg, #f8fafc 0%, #eef2ff 30%, #fdf2f8 100%);
  position: relative;
}

.auth-page::after {
  content: '';
  position: absolute;
  inset: 40px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.45);
  filter: blur(50px);
  z-index: 0;
}

.auth-wrapper {
  width: min(1180px, 100%);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 22px;
  align-items: stretch;
  position: relative;
  z-index: 1;
}

.auth-hero {
  position: relative;
  background: linear-gradient(140deg, rgba(37, 99, 235, 0.12), rgba(59, 130, 246, 0.1)),
    radial-gradient(circle at 80% 0%, rgba(236, 72, 153, 0.08), transparent 45%);
  border: 1px solid rgba(37, 99, 235, 0.2);
  border-radius: 22px;
  padding: 28px;
  box-shadow: 0 20px 40px rgba(37, 99, 235, 0.12);
  overflow: hidden;
}

.auth-hero::before,
.auth-hero::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  filter: blur(48px);
  opacity: 0.6;
}

.auth-hero::before {
  width: 180px;
  height: 180px;
  background: rgba(59, 130, 246, 0.3);
  top: -40px;
  right: -60px;
}

.auth-hero::after {
  width: 200px;
  height: 200px;
  background: rgba(59, 130, 246, 0.22);
  bottom: -60px;
  left: -50px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 999px;
  color: var(--color-primary-strong);
  font-weight: 700;
  letter-spacing: 0.2px;
  position: relative;
}

.auth-hero h1 {
  margin: 12px 0 8px;
  font-size: 30px;
  color: #0f172a;
}

.hero-lede {
  margin: 0 0 16px;
  color: #334155;
  line-height: 1.7;
}

.hero-highlights {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
  position: relative;
  z-index: 1;
}

.hero-highlights li {
  background: rgba(255, 255, 255, 0.88);
  border-radius: 16px;
  padding: 12px 14px;
  border: 1px solid rgba(37, 99, 235, 0.15);
  display: flex;
  flex-direction: column;
  gap: 4px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
}

.hero-highlights strong {
  color: var(--color-primary-strong);
}

.hero-highlights span {
  color: #475569;
  font-size: 13px;
}

.hero-actions {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.hero-actions .outline {
  border-color: var(--color-primary-border);
  background: rgba(255, 255, 255, 0.92);
}

.auth-card {
  background: rgba(255, 255, 255, 0.96);
  border-radius: 22px;
  padding: 28px 26px;
  border: 1px solid var(--border-strong);
  box-shadow: 0 22px 48px rgba(37, 99, 235, 0.12);
  backdrop-filter: blur(12px);
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.card-header h2 {
  margin: 8px 0 6px;
  font-size: 24px;
  color: var(--color-text-main);
}

.card-header .eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--color-primary-strong);
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.2px;
}

.card-header .muted {
  margin: 0;
  color: var(--color-text-muted);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: var(--color-text-main);
  font-weight: 600;
  font-size: 14px;
}

.label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

input {
  padding: 12px 13px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.7);
  background: #ffffff;
  font-size: 14px;
  transition: border-color 0.15s ease, box-shadow 0.15s ease, transform 0.1s ease;
}

input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
  outline: none;
  transform: translateY(-1px);
}

button.primary {
  width: 100%;
  padding: 12px 18px;
  font-size: 15px;
}

.form-footnote {
  border-top: 1px dashed var(--border-subtle);
  padding-top: 12px;
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
  font-weight: 700;
  text-decoration: none;
}

.note a:hover {
  text-decoration: underline;
}

@media (max-width: 720px) {
  .auth-page {
    padding-top: 24px;
  }

  .auth-hero,
  .auth-card {
    padding: 22px 20px;
  }

  .hero-highlights {
    grid-template-columns: 1fr;
  }
}
</style>
