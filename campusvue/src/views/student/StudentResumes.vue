<template>
  <section class="card">
    <div class="card__title">
      <h2>我的简历</h2>
      <button class="outline" type="button" @click="refreshResumes">刷新</button>
    </div>
    <form class="form-grid" @submit.prevent="createResume">
      <label class="full">简历标题<input v-model="resumeForm.title" required /></label>
      <label class="full">教育经历<textarea v-model="resumeForm.educationExperience"></textarea></label>
      <label class="full">实习/工作经历<textarea v-model="resumeForm.workExperience"></textarea></label>
      <label class="full">技能特长<textarea v-model="resumeForm.skills"></textarea></label>
      <label class="full">自我评价<textarea v-model="resumeForm.selfEvaluation"></textarea></label>
      <label class="full file-input">
        附件上传
        <input ref="resumeFileInput" type="file" accept=".pdf,.doc,.docx" @change="handleResumeFile" />
        <small>支持 PDF/DOC/DOCX，最大 15MB。上传后系统会生成附件链接。</small>
      </label>
      <label class="full">附件链接（可选）<input v-model="resumeForm.attachment" placeholder="也可填写已有附件链接" /></label>
      <div class="full actions">
        <button class="primary" type="submit">{{ editingResumeId ? '更新简历' : '新建简历' }}</button>
        <button class="outline" type="button" @click="resetResumeForm">取消</button>
      </div>
    </form>
    <ul class="list" v-if="resumes.length">
      <li v-for="resume in resumes" :key="resume.id" class="list__item">
        <div>
          <h3>{{ resume.title }}</h3>
          <p class="muted">更新于 {{ formatDate(resume.updateTime) }}</p>
        </div>
        <div class="list__actions">
          <button class="outline" type="button" @click="selectResumeForApply(resume.id)">
            {{ resume.id === selectedResumeId ? '已选择' : '投递使用' }}
          </button>
          <button class="outline" type="button" @click="editResume(resume)">编辑</button>
          <button class="danger" type="button" @click="deleteResume(resume.id)">删除</button>
        </div>
      </li>
    </ul>
    <p v-else class="muted">还没有简历，填写上方表单即可新建。</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const student = inject('studentContext');
if (!student) {
  throw new Error('studentContext not provided');
}

const {
  resumeForm,
  resumes,
  selectedResumeId,
  editingResumeId,
  resumeFileInput,
  refreshResumes,
  createResume,
  resetResumeForm,
  handleResumeFile,
  selectResumeForApply,
  editResume,
  deleteResume,
  formatDate
} = student;
</script>

<style scoped>
.file-input input[type='file'] {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 8px;
}

.file-input small {
  display: block;
  margin-top: 6px;
  color: #64748b;
}
</style>
