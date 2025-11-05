<template>
  <section class="section">
    <header class="section__header">
      <div>
        <h2>基本资料</h2>
        <p class="muted">完善个人信息以便企业更好地了解你</p>
      </div>
      <span v-if="profileSaved" class="hint">资料已更新</span>
    </header>

    <form class="form-grid" @submit.prevent="saveProfile">
      <label>姓名<input v-model="profileForm.name" required /></label>
      <label>性别<input v-model="profileForm.gender" placeholder="男/女/其他" /></label>
      <label>学校<input v-model="profileForm.school" /></label>
      <label>专业<input v-model="profileForm.major" /></label>
      <label>年级<input v-model="profileForm.grade" /></label>
      <label>学历<input v-model="profileForm.education" /></label>
      <label class="full">头像地址<input v-model="profileForm.avatar" /></label>
      <div class="full actions">
        <button class="primary" type="submit" :disabled="saving">
          {{ saving ? '保存中...' : '保存资料' }}
        </button>
      </div>
    </form>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, put } from '../../api/http';

const profileForm = reactive({
  name: '',
  gender: '',
  school: '',
  major: '',
  grade: '',
  education: '',
  avatar: ''
});

const profileSaved = ref(false);
const saving = ref(false);
const feedback = reactive({ message: '', type: 'info' });

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}

async function loadProfile() {
  try {
    const data = await get('/portal/student/profile');
    Object.assign(profileForm, data);
    profileSaved.value = false;
  } catch (error) {
    if (error.status !== 404) {
      showFeedback(error.message, 'error');
    }
  }
}

async function saveProfile() {
  try {
    saving.value = true;
    const updated = await put('/portal/student/profile', profileForm);
    Object.assign(profileForm, updated);
    profileSaved.value = true;
    showFeedback('资料保存成功', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
  } finally {
    saving.value = false;
  }
}

onMounted(loadProfile);
</script>

<style scoped>
.section {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.form-grid label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
}

.form-grid input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px;
  font-size: 14px;
}

.form-grid .full {
  grid-column: 1 / -1;
}

.actions {
  display: flex;
  justify-content: flex-start;
}

.primary {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border: none;
  color: #fff;
  padding: 10px 18px;
  border-radius: 10px;
  cursor: pointer;
}

.primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.muted {
  color: #6b7280;
  margin: 0;
}

.hint {
  color: #10b981;
}

.feedback {
  text-align: center;
  padding: 12px;
  border-radius: 12px;
}

.feedback.success {
  background: #dcfce7;
  color: #15803d;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}
</style>
