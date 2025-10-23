<template>
  <div class="login-page">
    <section class="login-card">
      <header class="login-header">
        <h1>Tsuki 校园招聘平台</h1>
        <p>当前仅开放系统管理员登录入口</p>
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
      <p class="note">企业与学生用户请通过前端门户提交资料，由管理员统一开通权限。</p>
      <p v-if="feedback.message" :class="['feedback', feedback.type]">
        {{ feedback.message }}
      </p>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { post, setAuthInfo, clearAuthInfo } from '../api/http';

const router = useRouter();
const username = ref('');
const password = ref('');
const loading = ref(false);
const feedback = reactive({ message: '', type: 'info' });

const presetUsername = router.currentRoute.value.query.username;
if (typeof presetUsername === 'string' && presetUsername) {
  username.value = presetUsername;
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
      token: data.token
    };
    setAuthInfo(payload);
    if (data.role !== 'ADMIN') {
      throw new Error('当前系统仅支持系统管理员登录');
    }
    showFeedback(`登录成功，欢迎管理员 ${data.username}`, 'success');
    setTimeout(() => {
      const redirect = router.currentRoute.value.query.redirect;
      if (typeof redirect === 'string' && redirect) {
        router.replace(redirect);
      } else {
        router.push({ name: 'admin-dashboard' });
      }
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
  padding: 24px;
}

.login-card {
  width: min(420px, 100%);
  border-radius: 24px;
  padding: 36px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 48px rgba(30, 64, 175, 0.22);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.login-header h1 {
  margin: 0;
  font-size: 26px;
  color: #1e3a8a;
}

.login-header p {
  margin: 4px 0 0;
  color: #475569;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #1f2937;
  font-weight: 600;
}

input {
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(59, 130, 246, 0.35);
  font-size: 15px;
}

input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.25);
}

button.primary {
  margin-top: 12px;
  padding: 12px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #ffffff;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

button.primary:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

button.primary:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 25px rgba(37, 99, 235, 0.35);
}

.feedback {
  margin: 0;
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 14px;
}

.feedback.info {
  background: #dbeafe;
  color: #1d4ed8;
}

.feedback.success {
  background: #dcfce7;
  color: #15803d;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}

.note {
  margin: 0;
  text-align: center;
  font-size: 13px;
  color: #4b5563;
}
</style>
