<template>
  <section class="resume-section">
    <header class="resume-header">
      <div>
        <p class="eyebrow">我的简历</p>
        <h2>管理投递版本</h2>
        <p class="muted">创建或维护多份简历，选择一份作为默认投递。</p>
      </div>
      <div class="header-actions">
        <button class="outline" type="button" @click="loadResumes" :disabled="loading">
          {{ loading ? '刷新中...' : '刷新' }}
        </button>
      </div>
    </header>

    <div class="resume-layout">
      <form class="resume-card" @submit.prevent="submitResume">
        <div class="field-grid">
          <label class="field full">
            <span>简历标题</span>
            <input v-model="resumeForm.title" required placeholder="如：前端开发-校招简历" />
          </label>
          <label class="field full">
            <span>附件链接（可选）</span>
            <input v-model="resumeForm.attachment" placeholder="已有网盘/官网链接可直接粘贴" />
          </label>
        </div>

        <div class="field-grid">
          <label class="field full">
            <span>教育经历</span>
            <textarea
              v-model="resumeForm.educationExperience"
              placeholder="按时间倒序填写：学校、专业、起止时间、成绩/荣誉等"
            ></textarea>
          </label>
          <label class="field full">
            <span>实习 / 工作经历</span>
            <textarea
              v-model="resumeForm.workExperience"
              placeholder="描述公司、岗位、时间及负责的主要工作/成果"
            ></textarea>
          </label>
          <label class="field full">
            <span>技能特长</span>
            <textarea
              v-model="resumeForm.skills"
              placeholder="如：编程语言、工具/框架、证书、项目经验等"
            ></textarea>
          </label>
          <label class="field full">
            <span>自我评价</span>
            <textarea
              v-model="resumeForm.selfEvaluation"
              placeholder="概括个人优势、求职意向与职业目标"
            ></textarea>
          </label>
        </div>

        <div class="upload-box">
          <div class="upload-info">
            <p class="label">附件上传</p>
            <p class="muted">支持 PDF / DOC / DOCX（≤5MB），上传后生成可复用的附件链接。</p>
          </div>
          <label class="upload-input">
            <input ref="resumeFileInput" type="file" accept=".pdf,.doc,.docx" @change="handleResumeFile" />
            <span>{{ resumeFile?.name || '选择文件' }}</span>
          </label>
        </div>

        <div class="actions">
          <button class="primary" type="submit">
            {{ editingResumeId ? '更新简历' : '新建简历' }}
          </button>
          <button class="outline" type="button" @click="resetForm">取消</button>
        </div>

        <p v-if="feedback.message" :class="['feedback', feedback.type]">
          {{ feedback.message }}
        </p>
      </form>

      <aside class="resume-aside">
        <div class="aside-card">
          <p class="label">当前投递简历</p>
          <p class="value">{{ selectedResumeId ? `#${selectedResumeId}` : '未选择' }}</p>
          <p class="muted">点击列表中的“设为投递”可快速切换默认投递版本。</p>
        </div>
        <div class="aside-card tips">
          <p class="label">填写提示</p>
          <ul>
            <li>描述经历时使用 STAR 法（情境-任务-行动-结果）。</li>
            <li>技能与项目条目保持同一格式，方便 HR 快速浏览。</li>
            <li>上传附件后，可在外部投递时复用生成的链接。</li>
          </ul>
        </div>
      </aside>
    </div>

    <div class="resume-list" v-if="resumes.length">
      <article v-for="resume in resumes" :key="resume.id" class="resume-item">
        <div>
          <p class="eyebrow">简历</p>
          <h3>
            {{ resume.title }}
            <span v-if="selectedResumeId === resume.id" class="pill">当前投递</span>
          </h3>
          <p class="muted">更新于：{{ formatDate(resume.updateTime) }}</p>
          <p class="muted attachment" v-if="resume.attachment">附件：{{ resume.attachment }}</p>
        </div>
        <div class="item-actions">
          <button class="outline" @click="selectResume(resume.id)">
            {{ selectedResumeId === resume.id ? '已选中' : '设为投递' }}
          </button>
          <button class="outline" @click="editResume(resume)">编辑</button>
          <button class="danger" @click="deleteResume(resume.id)">删除</button>
        </div>
      </article>
    </div>
    <p v-else class="muted">还没有简历，填写左侧表单即可新建。</p>
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
const setSelectedResumeId = inject('setSelectedResumeId', id => {
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
.resume-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.eyebrow {
  margin: 0 0 4px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #2563eb;
  font-weight: 700;
}

.resume-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.resume-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  align-items: start;
}

.resume-card {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid var(--border-subtle);
  border-radius: 18px;
  box-shadow: var(--shadow-soft);
  padding: 18px 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-weight: 600;
}

.field span {
  color: var(--color-text-main);
  font-size: 14px;
}

.field.full {
  grid-column: 1 / -1;
}

.field input,
.field textarea {
  padding: 12px 13px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.7);
  background: #fff;
}

.field textarea {
  min-height: 120px;
}

.upload-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px dashed var(--border-subtle);
  background: #f8fafc;
}

.upload-input {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.8);
  background: #fff;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.upload-input input {
  position: absolute;
  opacity: 0;
  width: 100%;
  height: 100%;
  inset: 0;
}

.upload-info .label {
  margin: 0 0 4px;
  font-weight: 700;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.resume-aside {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.aside-card {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid var(--border-subtle);
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: var(--shadow-soft);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.aside-card .label {
  margin: 0;
  font-weight: 700;
}

.aside-card .value {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary-strong);
}

.aside-card ul {
  margin: 0;
  padding-left: 16px;
  color: #475569;
  line-height: 1.5;
}

.resume-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 12px;
}

.resume-item {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid var(--border-subtle);
  border-radius: 16px;
  padding: 16px 16px 14px;
  box-shadow: var(--shadow-soft);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.resume-item h3 {
  margin: 6px 0 4px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  color: var(--color-primary-strong);
  border: 1px solid var(--color-primary-border);
  font-size: 12px;
  font-weight: 700;
}

.item-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.attachment {
  word-break: break-all;
}

@media (max-width: 960px) {
  .resume-layout {
    grid-template-columns: 1fr;
  }
}
</style>
