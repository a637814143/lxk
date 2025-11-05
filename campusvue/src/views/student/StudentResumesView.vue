<template>
  <section class="section">
    <header class="section__header">
      <div>
        <h2>我的简历</h2>
        <p class="muted">创建或维护多份简历，并选择投递使用的版本</p>
      </div>
      <button class="outline" type="button" @click="loadResumes" :disabled="loading">
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </header>

    <form class="form-grid" @submit.prevent="submitResume">
      <label class="full">简历标题<input v-model="resumeForm.title" required /></label>
      <label class="full">教育经历<textarea v-model="resumeForm.educationExperience"></textarea></label>
      <label class="full">实习/工作经历<textarea v-model="resumeForm.workExperience"></textarea></label>
      <label class="full">技能特长<textarea v-model="resumeForm.skills"></textarea></label>
      <label class="full">自我评价<textarea v-model="resumeForm.selfEvaluation"></textarea></label>
      <label class="full file-input">
        附件上传
        <input ref="resumeFileInput" type="file" accept=".pdf,.doc,.docx" @change="handleResumeFile" />
        <small>支持 PDF/DOC/DOCX，最大15MB。上传后系统会生成附件链接。</small>
      </label>
      <label class="full">附件链接（可选）<input v-model="resumeForm.attachment" placeholder="也可填写已有附件链接" /></label>
      <div class="full actions">
        <button class="primary" type="submit">
          {{ editingResumeId ? '更新简历' : '新建简历' }}
        </button>
        <button class="outline" type="button" @click="resetForm">取消</button>
      </div>
    </form>

    <ul class="list" v-if="resumes.length">
      <li v-for="resume in resumes" :key="resume.id" class="list__item">
        <div>
          <h3>{{ resume.title }}</h3>
          <p class="muted">更新于：{{ formatDate(resume.updateTime) }}</p>
          <p class="muted" v-if="resume.attachment">附件：{{ resume.attachment }}</p>
        </div>
        <div class="list__actions">
          <button class="outline" @click="selectResume(resume.id)">
            {{ selectedResumeId === resume.id ? '当前投递' : '设为投递' }}
          </button>
          <button class="outline" @click="editResume(resume)">编辑</button>
          <button class="danger" @click="deleteResume(resume.id)">删除</button>
        </div>
      </li>
    </ul>
    <p v-else class="muted">还没有简历，填写上方表单即可新建。</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
  </template>

<script setup>
import { inject, onMounted, reactive, ref } from 'vue';
import { del, get, post, put, upload } from '../../api/http';

const resumeForm = reactive({
  title: '',
  educationExperience: '',
  workExperience: '',
  skills: '',
  selfEvaluation: '',
  attachment: ''
});

const resumes = ref([]);
const loading = ref(false);
const resumeFile = ref(null);
const resumeFileInput = ref(null);
const editingResumeId = ref(null);
const feedback = reactive({ message: '', type: 'info' });

const selectedResumeId = inject('selectedResumeId', ref(null));
const setSelectedResumeId = inject('setSelectedResumeId', (id) => {
  selectedResumeId.value = id;
});

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}

function resetForm() {
  resumeForm.title = '';
  resumeForm.educationExperience = '';
  resumeForm.workExperience = '';
  resumeForm.skills = '';
  resumeForm.selfEvaluation = '';
  resumeForm.attachment = '';
  editingResumeId.value = null;
  resumeFile.value = null;
  if (resumeFileInput.value) {
    resumeFileInput.value.value = '';
  }
}

function handleResumeFile(event) {
  const [file] = event.target.files ?? [];
  resumeFile.value = file ?? null;
}

async function loadResumes() {
  try {
    loading.value = true;
    resumes.value = await get('/portal/student/resumes');
    if (!selectedResumeId.value && resumes.value.length) {
      setSelectedResumeId(resumes.value[0].id);
    }
  } catch (error) {
    showFeedback(error.message, 'error');
  } finally {
    loading.value = false;
  }
}

async function submitResume() {
  try {
    if (editingResumeId.value) {
      const payload = { ...resumeForm };
      let updated = await put(`/portal/student/resumes/${editingResumeId.value}`, payload);
      if (resumeFile.value) {
        const formData = new FormData();
        formData.append('file', resumeFile.value);
        updated = await upload(`/portal/student/resumes/${editingResumeId.value}/attachment`, formData);
      }
      const index = resumes.value.findIndex(item => item.id === editingResumeId.value);
      if (index !== -1) {
        resumes.value[index] = updated;
      }
      setSelectedResumeId(updated.id);
      showFeedback('简历更新成功', 'success');
    } else {
      let attachmentPath = resumeForm.attachment;
      if (resumeFile.value) {
        const formData = new FormData();
        formData.append('file', resumeFile.value);
        const uploaded = await upload('/portal/student/resumes/attachments', formData);
        attachmentPath = uploaded.attachment;
      }
      const created = await post('/portal/student/resumes', {
        ...resumeForm,
        attachment: attachmentPath
      });
      resumes.value.unshift(created);
      setSelectedResumeId(created.id);
      showFeedback('简历创建成功', 'success');
    }
    resetForm();
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function editResume(resume) {
  Object.assign(resumeForm, {
    title: resume.title,
    educationExperience: resume.educationExperience,
    workExperience: resume.workExperience,
    skills: resume.skills,
    selfEvaluation: resume.selfEvaluation,
    attachment: resume.attachment
  });
  editingResumeId.value = resume.id;
  resumeFile.value = null;
  showFeedback('已加载简历内容，可修改后再次保存', 'info');
}

async function deleteResume(resumeId) {
  if (!confirm('确定要删除该简历吗？')) return;
  try {
    await del(`/portal/student/resumes/${resumeId}`);
    resumes.value = resumes.value.filter(item => item.id !== resumeId);
    if (selectedResumeId.value === resumeId) {
      setSelectedResumeId(resumes.value[0]?.id ?? null);
    }
    showFeedback('简历已删除', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function selectResume(resumeId) {
  setSelectedResumeId(resumeId);
  showFeedback('已设为投递简历', 'success');
}

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

onMounted(loadResumes);
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

.form-grid textarea,
.form-grid input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px;
  font-size: 14px;
}

.form-grid textarea {
  min-height: 80px;
  resize: vertical;
}

.form-grid .full {
  grid-column: 1 / -1;
}

.file-input input[type='file'] {
  padding: 8px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
}

.file-input small {
  color: #64748b;
  font-size: 12px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list__item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.list__actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.primary {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border: none;
  color: #fff;
  padding: 10px 18px;
  border-radius: 10px;
  cursor: pointer;
}

.outline {
  background: transparent;
  border: 1px solid #2563eb;
  color: #2563eb;
  padding: 8px 16px;
  border-radius: 10px;
  cursor: pointer;
}

.danger {
  background: transparent;
  border: 1px solid #dc2626;
  color: #dc2626;
  padding: 8px 16px;
  border-radius: 10px;
  cursor: pointer;
}

.muted {
  color: #6b7280;
  margin: 0;
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

.feedback.info {
  background: #e0f2fe;
  color: #0369a1;
}
</style>
