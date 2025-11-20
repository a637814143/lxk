<template>
  <div class="register-page">
    <section class="register-card">
      <header class="register-header">
        <h1>创建 Tsuki 校园招聘账号</h1>
        <p>支持学生、企业和系统管理员注册，信息将用于角色初始化</p>
      </header>
      <form class="register-form" @submit.prevent="handleSubmit">
        <label>
          <span>用户名</span>
          <input v-model.trim="form.username" type="text" maxlength="50" autocomplete="username" required />
        </label>
        <div class="grid">
          <label>
            <span>密码</span>
            <input v-model="form.password" type="password" minlength="6" maxlength="64" autocomplete="new-password" required />
          </label>
          <label>
            <span>确认密码</span>
            <input v-model="form.confirmPassword" type="password" minlength="6" maxlength="64" autocomplete="new-password" required />
          </label>
        </div>
        <label>
          <span>邮箱</span>
          <input v-model.trim="form.email" type="email" maxlength="100" autocomplete="email" required />
        </label>
        <label>
          <span>手机号（选填）</span>
          <input v-model.trim="form.phone" type="tel" maxlength="20" placeholder="例如：13800000000" />
        </label>
        <div class="role-field">
          <span>选择角色</span>
          <div class="role-options">
            <label>
              <input v-model="form.role" type="radio" value="STUDENT" /> 学生
            </label>
            <label>
              <input v-model="form.role" type="radio" value="COMPANY" /> 企业
            </label>
            <label>
              <input v-model="form.role" type="radio" value="ADMIN" /> 系统管理员
            </label>
          </div>
        </div>
        <label v-if="requiresDisplayName">
          <span>{{ form.role === 'ADMIN' ? '管理员姓名' : '学生姓名' }}</span>
          <input v-model.trim="form.displayName" type="text" maxlength="50" required />
        </label>
        <label v-if="requiresCompanyName">
          <span>企业名称</span>
          <input v-model.trim="form.companyName" type="text" maxlength="100" required />
        </label>
        <label v-if="requiresInviteCode">
          <span>企业邀请码</span>
          <input v-model.trim="form.inviteCode" type="text" maxlength="64" required />
        </label>
        <button class="primary" type="submit" :disabled="loading">
          {{ loading ? '提交中…' : '注册' }}
        </button>
      </form>
      <p class="note">已有账号？<RouterLink :to="{ name: 'login' }">前往登录</RouterLink></p>
      <p v-if="feedback.message" :class="['feedback', feedback.type]">
        {{ feedback.message }}
      </p>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';
import { useRouter, RouterLink } from 'vue-router';
import { post } from '../api/http';

const router = useRouter();

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  role: 'STUDENT',
  displayName: '',
  companyName: '',
  inviteCode: ''
});

const loading = ref(false);
const feedback = reactive({ message: '', type: 'info' });

const requiresDisplayName = computed(() => form.role === 'STUDENT' || form.role === 'ADMIN');
const requiresCompanyName = computed(() => false);
const requiresInviteCode = computed(() => false);

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
}

function validateForm() {
  if (!form.username) {
    return '请输入用户名';
  }
  if (!form.password || !form.confirmPassword) {
    return '请输入并确认密码';
  }
  if (form.password !== form.confirmPassword) {
    return '两次输入的密码不一致';
  }
  if (!form.email) {
    return '请输入邮箱';
  }
  if (requiresDisplayName.value && !form.displayName) {
    return '请输入姓名信息';
  }
  if (requiresCompanyName.value && !form.companyName) {
    return '请输入企业名称';
  }
  if (requiresInviteCode.value && !form.inviteCode) {
    return '请输入企业邀请码';
  }
  return null;
}

async function handleSubmit() {
  if (loading.value) {
    return;
  }
  const validationMessage = validateForm();
  if (validationMessage) {
    showFeedback(validationMessage, 'error');
    return;
  }
  loading.value = true;
  showFeedback('正在提交注册信息…', 'info');
  try {
    const payload = {
      username: form.username,
      password: form.password,
      email: form.email,
      phone: form.phone || null,
      role: form.role,
      displayName: requiresDisplayName.value ? form.displayName : null,
      companyName: requiresCompanyName.value ? form.companyName : null,
      inviteCode: requiresInviteCode.value ? form.inviteCode : null
    };
    await post('/auth/register', payload);
    showFeedback('注册成功，请使用新账户登录', 'success');
    setTimeout(() => {
      router.push({
        name: 'login',
        query: {
          username: form.username,
          registered: '1'
        }
      });
    }, 500);
  } catch (error) {
    console.error(error);
    showFeedback(error.message ?? '注册失败，请稍后再试', 'error');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
}

.register-card {
  width: min(720px, 100%);
  padding: 32px 28px;
  border-radius: var(--radius-xl);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(20px);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.register-header h1 {
  margin: 0;
  font-size: 28px;
  color: var(--color-primary-strong);
}

.register-header p {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--color-text-muted);
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
  color: var(--color-text-main);
  font-size: 14px;
}

.grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.role-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: var(--color-text-main);
  font-weight: 600;
}

.role-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 24px;
}

.role-options label {
  flex-direction: row;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.note {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-muted);
}

.note a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 600;
}

.note a:hover {
  text-decoration: underline;
}

.feedback {
  text-align: center;
}

@media (max-width: 640px) {
  .register-card {
    padding: 28px 20px;
  }
}
</style>
