<template>
  <section class="profile-section">
    <header class="profile-header">
      <div>
        <p class="eyebrow">个人信息</p>
        <h2>完善基础资料</h2>
        <p class="muted">补充学校与专业等信息，便于企业快速了解你。</p>
      </div>
      <span v-if="profileSaved" class="hint chip">资料已更新</span>
    </header>

    <div class="profile-card">
      <div class="card-meta">
        <div class="meta-block">
          <p class="label">头像链接</p>
          <p class="value muted">{{ profileForm.avatar || '未填写' }}</p>
        </div>
        <div class="meta-block">
          <p class="label">当前学历</p>
          <p class="value muted">{{ profileForm.education || '未选择' }}</p>
        </div>
      </div>

      <form class="profile-form" @submit.prevent="saveProfile">
        <div class="field-grid">
          <label class="field">
            <span>姓名</span>
            <input v-model="profileForm.name" required placeholder="请输入姓名" />
          </label>
          <label class="field">
            <span>性别</span>
            <select v-model="profileForm.gender">
              <option value="">请选择</option>
              <option value="男">男</option>
              <option value="女">女</option>
              <option value="其他">其他 / 保密</option>
            </select>
          </label>
          <label class="field">
            <span>学校</span>
            <input v-model="profileForm.school" placeholder="如：XX大学" />
          </label>
          <label class="field">
            <span>专业</span>
            <input v-model="profileForm.major" placeholder="如：计算机科学与技术" />
          </label>
          <label class="field">
            <span>年级</span>
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
            <span>学历</span>
            <select v-model="profileForm.education">
              <option value="">请选择</option>
              <option value="专科">专科</option>
              <option value="本科">本科</option>
              <option value="硕士">硕士</option>
              <option value="博士">博士</option>
              <option value="其他">其他</option>
            </select>
          </label>
          <label class="field full">
            <span>头像地址</span>
            <input v-model="profileForm.avatar" placeholder="填写在线头像图片链接（可选）" />
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
    </div>
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
.profile-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.eyebrow {
  margin: 0 0 4px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #2563eb;
  font-weight: 700;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.profile-header h2 {
  margin: 6px 0;
}

.chip {
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--color-success-soft);
  color: var(--color-success);
  font-weight: 700;
  border: 1px solid rgba(22, 163, 74, 0.2);
}

.profile-card {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid var(--border-subtle);
  border-radius: 18px;
  padding: 20px 18px 22px;
  box-shadow: var(--shadow-soft);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 10px;
  padding: 10px 12px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px dashed var(--border-subtle);
}

.meta-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-block .label {
  margin: 0;
  color: #475569;
  font-weight: 600;
}

.meta-block .value {
  margin: 0;
  font-size: 14px;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-weight: 600;
}

.field span {
  font-size: 14px;
  color: var(--color-text-main);
}

.field.full {
  grid-column: 1 / -1;
}

.field input,
.field select {
  padding: 12px 13px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.7);
  background: #fff;
}

.actions {
  display: flex;
  justify-content: flex-start;
  margin-top: 4px;
}

@media (max-width: 720px) {
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
