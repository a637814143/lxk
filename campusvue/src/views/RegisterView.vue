<template>
  <div class="register-page">
    <section class="register-card">
      <header class="register-header">
        <h1>创建 Tsuki 校园招聘账号</h1>
        <p>选择角色后完善资料即可登录系统</p>
      </header>
      <form class="register-form" @submit.prevent="handleSubmit">
        <label>
          <span>用户名</span>
          <input v-model.trim="form.username" required maxlength="50" autocomplete="username" />
        </label>
        <label>
          <span>邮箱</span>
          <input v-model.trim="form.email" required type="email" autocomplete="email" />
        </label>
        <label>
          <span>手机号</span>
          <input v-model.trim="form.phone" placeholder="可选" autocomplete="tel" />
        </label>
        <label>
          <span>密码</span>
          <input v-model="form.password" required type="password" minlength="6" autocomplete="new-password" />
        </label>
        <label>
          <span>用户角色</span>
          <select v-model="form.role" required>
            <option disabled value="">请选择角色</option>
            <option value="STUDENT">学生</option>
            <option value="COMPANY">企业</option>
            <option value="ADMIN">系统管理员</option>
          </select>
        </label>
        <label v-if="needsDisplayName">
          <span>{{ form.role === 'ADMIN' ? '管理员姓名' : '学生姓名' }}</span>
          <input v-model.trim="form.displayName" maxlength="50" required />
        </label>
        <label v-if="form.role === 'COMPANY'">
          <span>企业名称</span>
          <input v-model.trim="form.companyName" maxlength="100" required />
        </label>
        <button class="primary" type="submit" :disabled="loading">
          {{ loading ? '正在注册…' : '注册并跳转登录' }}
        </button>
      </form>
      <router-link class="back" :to="{ name: 'login' }">已有账号？返回登录</router-link>
      <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { post } from '../api/http';

const router = useRouter();
const loading = ref(false);
const feedback = reactive({ message: '', type: 'info' });

const form = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  role: '',
  displayName: '',
  companyName: ''
});

const needsDisplayName = computed(() => form.role === 'STUDENT' || form.role === 'ADMIN');

watch(
  () => form.role,
  role => {
    if (role !== 'COMPANY') {
      form.companyName = '';
    }
    if (role !== 'STUDENT' && role !== 'ADMIN') {
      form.displayName = '';
    }
  }
);

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}

async function handleSubmit() {
  if (loading.value) return;
  loading.value = true;
  try {
    if (needsDisplayName.value && !form.displayName) {
      showFeedback('请填写姓名', 'error');
      loading.value = false;
      return;
    }
    if (form.role === 'COMPANY' && !form.companyName) {
      showFeedback('请填写企业名称', 'error');
      loading.value = false;
      return;
    }
    await post('/auth/register', {
      username: form.username,
      email: form.email,
      phone: form.phone,
      password: form.password,
      role: form.role,
      displayName: needsDisplayName.value ? form.displayName : null,
      companyName: form.role === 'COMPANY' ? form.companyName : null
    });
    showFeedback('注册成功，请使用账号登录', 'success');
    setTimeout(() => {
      router.push({ name: 'login', query: { username: form.username } });
    }, 600);
  } catch (error) {
    showFeedback(error.message ?? '注册失败，请稍后重试', 'error');
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
  background: linear-gradient(135deg, #eff6ff, #e0f2fe);
  padding: 32px;
}

.register-card {
  width: min(520px, 100%);
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 25px 60px rgba(30, 64, 175, 0.18);
  padding: 36px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.register-header h1 {
  margin: 0;
  color: #1e3a8a;
}

.register-header p {
  margin: 6px 0 0;
  color: #475569;
}

.register-form {
  display: grid;
  gap: 18px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-weight: 600;
  color: #1f2937;
}

input,
select {
  border: 1px solid rgba(59, 130, 246, 0.35);
  border-radius: 12px;
  padding: 12px;
  font-size: 15px;
}

button.primary {
  margin-top: 12px;
  padding: 12px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: box-shadow 0.2s ease;
}

button.primary:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.back {
  color: #2563eb;
  text-align: center;
}

.feedback {
  margin: 0;
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 14px;
}

.feedback.success {
  background: #dcfce7;
  color: #15803d;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}

.feedback.info {
  background: #dbeafe;
  color: #1d4ed8;
}
</style>
