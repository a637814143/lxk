<template>
  <section class="section">
    <header class="section__header">
      <div>
        <h2>基本资料</h2>
        <p class="muted">完善个人信息以便企业更好地了解你</p>
      </div>
      <span v-if="profileSaved" class="hint">资料已更新</span>
    </header>

    <form class="profile-form" @submit.prevent="saveProfile">
      <div class="profile-row">
        <label class="field">
          <span class="field-label">姓名</span>
          <input v-model="profileForm.name" required />
        </label>
        <label class="field">
          <span class="field-label">性别</span>
          <select v-model="profileForm.gender">
            <option value="">请选择</option>
            <option value="男">男</option>
            <option value="女">女</option>
            <option value="其他">其他 / 保密</option>
          </select>
        </label>
      </div>

      <div class="profile-row">
        <label class="field">
          <span class="field-label">学校</span>
          <input v-model="profileForm.school" />
        </label>
        <label class="field">
          <span class="field-label">专业</span>
          <input v-model="profileForm.major" />
        </label>
      </div>

      <div class="profile-row">
        <label class="field">
          <span class="field-label">年级</span>
          <select v-model="profileForm.grade">
            <option value="">请选择</option>
            <option value="大一">大一</option>
            <option value="大二">大二</option>
            <option value="大三">大三</option>
            <option value="大四">大四</option>
            <option value="研一">研一</option>
            <option value="研二">研二</option>
            <option value="研三">研三</option>
            <option value="其他">其他</option>
          </select>
        </label>
        <label class="field">
          <span class="field-label">学历</span>
          <select v-model="profileForm.education">
            <option value="">请选择</option>
            <option value="专科">专科</option>
            <option value="本科">本科</option>
            <option value="硕士">硕士</option>
            <option value="博士">博士</option>
            <option value="其他">其他</option>
          </select>
        </label>
      </div>

      <div class="profile-row profile-row-full">
        <label class="field">
          <span class="field-label">头像地址</span>
          <input v-model="profileForm.avatar" placeholder="可填写在线头像图片链接" />
        </label>
      </div>

      <div class="actions">
        <button class="primary" type="submit" :disabled="saving">
          {{ saving ? '保存中...' : '保存资料' }}
        </button>
      </div>
    </form>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">
      {{ feedback.message }}
    </p>
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
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.profile-row-full {
  flex-direction: column;
}

.field {
  flex: 1 1 220px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
}

.field-label {
  font-size: 14px;
}

.actions {
  display: flex;
  justify-content: flex-start;
  margin-top: 4px;
}

.profile-form input,
.profile-form select {
  border-radius: 999px;
  padding: 10px 14px;
}
</style>
