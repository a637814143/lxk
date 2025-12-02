<template>
  <div class="auth-page register-page">
    <div class="auth-wrapper">
      <section class="auth-hero">
        <p class="hero-badge">快速创建账号</p>
        <h1>完善信息，精准匹配招聘与投递</h1>
        <p class="hero-lede">注册后即可发布/投递岗位、跟踪消息与面试安排。</p>

        <div class="hero-stats">
          <div class="stat">
            <strong>3 步</strong>
            <span>完成基础资料</span>
          </div>
          <div class="stat">
            <strong>双向</strong>
            <span>企业与学生即时沟通</span>
          </div>
        </div>

        <ul class="hero-highlights">
          <li>支持学生与企业双角色</li>
          <li>数据加密存储，可随时编辑完善</li>
          <li>注册成功后可直接登录</li>
        </ul>
      </section>

      <section class="auth-card">
        <header class="card-header">
          <p class="eyebrow">创建 Tsuki 账号</p>
          <h2>填写注册信息</h2>
          <p class="muted">信息仅用于角色初始化与联络</p>
        </header>

        <form class="auth-form" @submit.prevent="handleSubmit">
          <label>
            <span>用户名</span>
            <input
              v-model.trim="form.username"
              type="text"
              maxlength="50"
              placeholder="建议使用学号或企业账号"
              autocomplete="username"
              required
            />
          </label>
          <div class="grid">
            <label>
              <div class="label-row">
                <span>密码</span>
                <small class="hint">至少 6 位，推荐含数字与字母</small>
              </div>
              <input
                v-model="form.password"
                type="password"
                minlength="6"
                maxlength="64"
                autocomplete="new-password"
                required
              />
            </label>
            <label>
              <span>确认密码</span>
              <input
                v-model="form.confirmPassword"
                type="password"
                minlength="6"
                maxlength="64"
                autocomplete="new-password"
                required
              />
            </label>
          </div>
          <label>
            <span>邮箱</span>
            <input v-model.trim="form.email" type="email" maxlength="100" autocomplete="email" required />
          </label>
          <label class="with-hint">
            <div class="label-row">
              <span>手机号（选填）</span>
              <small class="hint">用于接收通知或找回账号</small>
            </div>
            <input v-model.trim="form.phone" type="tel" maxlength="20" placeholder="例如：13800000000" />
          </label>
          <div class="role-field">
            <span>选择角色</span>
            <div class="role-options">
              <label :class="{ active: form.role === 'STUDENT' }">
                <input v-model="form.role" type="radio" value="STUDENT" />
                <div>
                  <strong>学生</strong>
                  <span>投递岗位、管理简历</span>
                </div>
              </label>
              <label :class="{ active: form.role === 'COMPANY' }">
                <input v-model="form.role" type="radio" value="COMPANY" />
                <div>
                  <strong>企业</strong>
                  <span>发布岗位、查看投递</span>
                </div>
              </label>
            </div>
          </div>
          <label v-if="requiresDisplayName">
            <span>学生姓名</span>
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
            {{ loading ? '提交中...' : '注册' }}
          </button>
        </form>

        <div class="form-footnote">
          <p class="note">
            已有账号？
            <RouterLink :to="{ name: 'login' }">前往登录</RouterLink>
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

const requiresDisplayName = computed(() => form.role === 'STUDENT');
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
  showFeedback('正在提交注册信息...', 'info');
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
    showFeedback('注册成功，请使用新账号登录', 'success');
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
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 18px 56px;
  background: radial-gradient(circle at 12% 18%, rgba(59, 130, 246, 0.12), transparent 30%),
    radial-gradient(circle at 85% 0%, rgba(236, 72, 153, 0.12), transparent 30%),
    linear-gradient(135deg, #f8fafc 0%, #eef2ff 30%, #fdf2f8 100%);
  position: relative;
}

.auth-page::after {
  content: '';
  position: absolute;
  inset: 36px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.45);
  filter: blur(52px);
  z-index: 0;
}

.auth-wrapper {
  width: min(1180px, 100%);
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
  gap: 24px;
  align-items: stretch;
  position: relative;
  z-index: 1;
}

.auth-hero {
  position: relative;
  background: linear-gradient(140deg, rgba(37, 99, 235, 0.12), rgba(59, 130, 246, 0.1)),
    radial-gradient(circle at 20% 0%, rgba(16, 185, 129, 0.08), transparent 45%);
  border: 1px solid rgba(59, 130, 246, 0.18);
  border-radius: 22px;
  padding: 28px;
  box-shadow: 0 22px 48px rgba(59, 130, 246, 0.12);
  overflow: hidden;
}

.auth-hero::before,
.auth-hero::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  filter: blur(48px);
  opacity: 0.7;
}

.auth-hero::before {
  width: 200px;
  height: 200px;
  background: rgba(14, 165, 233, 0.28);
  top: -60px;
  right: -40px;
}

.auth-hero::after {
  width: 170px;
  height: 170px;
  background: rgba(59, 130, 246, 0.25);
  bottom: -50px;
  left: -40px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 999px;
  color: var(--color-primary-strong);
  font-weight: 700;
  letter-spacing: 0.2px;
  position: relative;
  z-index: 1;
}

.auth-hero h1 {
  margin: 12px 0 8px;
  font-size: 30px;
  color: #0f172a;
}

.hero-lede {
  margin: 0 0 18px;
  color: #334155;
  line-height: 1.7;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
  margin-bottom: 12px;
  position: relative;
  z-index: 1;
}

.stat {
  background: rgba(255, 255, 255, 0.88);
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(59, 130, 246, 0.18);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
}

.stat strong {
  display: block;
  font-size: 20px;
  color: var(--color-primary-strong);
}

.stat span {
  color: #475569;
  font-size: 13px;
}

.hero-highlights {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 8px;
  position: relative;
  z-index: 1;
}

.hero-highlights li {
  background: rgba(255, 255, 255, 0.88);
  border-radius: 12px;
  padding: 10px 12px;
  border: 1px dashed rgba(59, 130, 246, 0.3);
  color: #334155;
  font-weight: 600;
}

.auth-card {
  background: rgba(255, 255, 255, 0.96);
  border-radius: 22px;
  padding: 28px 26px;
  border: 1px solid var(--border-strong);
  box-shadow: 0 24px 54px rgba(14, 165, 233, 0.12);
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

.with-hint .hint {
  color: var(--color-text-muted);
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

.grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

.role-field {
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: var(--color-text-main);
  font-weight: 700;
}

.role-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 10px;
}

.role-options label {
  position: relative;
  flex-direction: row;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.7);
  background: #f8fafc;
  cursor: pointer;
  transition: border-color 0.12s ease, box-shadow 0.12s ease, transform 0.1s ease, background 0.12s ease;
}

.role-options label.active {
  border-color: var(--color-primary);
  background: #e0ecff;
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.14);
  transform: translateY(-1px);
}

.role-options label input {
  position: absolute;
  opacity: 0;
  inset: 0;
}

.role-options label strong {
  display: block;
  color: var(--color-primary-strong);
}

.role-options label span {
  color: var(--color-text-muted);
  font-weight: 500;
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
