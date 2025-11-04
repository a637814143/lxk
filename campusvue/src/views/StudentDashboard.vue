<template>
  <div class="dashboard">
    <header class="dashboard__header">
      <div>
        <h1>学生工作台</h1>
        <p>欢迎回来，{{ authInfo?.roleDisplayName }} {{ authInfo?.username }}</p>
      </div>
      <button class="outline" @click="handleLogout">退出登录</button>
    </header>

    <div class="dashboard__content">
      <section class="card card--wide">
        <h2>学习概览</h2>
        <div class="summary-grid">
          <div class="summary-item">
            <span>我的简历</span>
            <strong>{{ resumeStats.total }}</strong>
          </div>
          <div class="summary-item">
            <span>选中简历</span>
            <strong>{{ resumeStats.selectedLabel }}</strong>
            <small v-if="resumeStats.selectedTitle">{{ resumeStats.selectedTitle }}</small>
          </div>
          <div class="summary-item">
            <span>投递记录</span>
            <strong>{{ resumeStats.applications }}</strong>
          </div>
          <div class="summary-item">
            <span>未读消息</span>
            <strong>{{ resumeStats.unreadMessages }}</strong>
          </div>
        </div>
      </section>

      <section class="card">
        <h2>基本资料</h2>
        <form class="form-grid" @submit.prevent="saveProfile">
          <label>姓名<input v-model="profileForm.name" required /></label>
          <label>性别<input v-model="profileForm.gender" placeholder="男/女/其他" /></label>
          <label>学校<input v-model="profileForm.school" /></label>
          <label>专业<input v-model="profileForm.major" /></label>
          <label>年级<input v-model="profileForm.grade" /></label>
          <label>学历<input v-model="profileForm.education" /></label>
          <label class="full">头像地址<input v-model="profileForm.avatar" /></label>
          <div class="full actions">
            <button class="primary" type="submit">保存资料</button>
            <span v-if="profileSaved" class="hint">资料已更新</span>
          </div>
        </form>
      </section>

    <section class="card card--wide resume-card">
      <div class="card__title">
        <h2>我的简历</h2>
        <div class="resume-card__actions">
          <button class="outline" @click="refreshResumes">刷新</button>
          <button class="outline" type="button" @click="resetResumeForm">重置表单</button>
        </div>
      </div>
      <div class="resume-card__grid">
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

        <div class="resume-card__list">
          <ul class="list" v-if="resumes.length">
            <li v-for="resume in resumes" :key="resume.id" class="list__item">
              <div>
                <h3>{{ resume.title }}</h3>
                <p class="muted">更新于 {{ formatDate(resume.updateTime) }}</p>
                <p v-if="resume.attachment" class="muted">
                  附件：<a :href="resume.attachment" target="_blank" rel="noopener">点击查看</a>
                </p>
              </div>
              <div class="list__actions">
                <button class="outline" @click="selectResumeForApply(resume.id)">
                  {{ resume.id === selectedResumeId ? '已选择' : '投递使用' }}
                </button>
                <button class="outline" @click="editResume(resume)">编辑</button>
                <button class="danger" @click="deleteResume(resume.id)">删除</button>
              </div>
            </li>
          </ul>
          <p v-else class="muted">还没有简历，填写左侧表单即可新建。</p>
        </div>
      </div>
    </section>

    <section class="card card--wide">
      <div class="card__title">
        <h2>职位浏览</h2>
        <div class="actions-inline">
          <button class="outline" type="button" @click="resetJobFilters">清空筛选</button>
          <button class="outline" @click="searchJobs">搜索</button>
        </div>
      </div>
      <form class="filters" @submit.prevent="searchJobs">
        <input v-model="jobFilters.keyword" placeholder="关键字" />
        <input v-model="jobFilters.company" placeholder="企业名称" />
        <input v-model="jobFilters.jobType" placeholder="职位类别" />
        <input v-model="jobFilters.location" placeholder="工作地点" />
        <input v-model="jobFilters.salaryRange" placeholder="薪资范围" />
      </form>
      <div v-if="jobs.length" class="job-grid">
        <article v-for="job in jobs" :key="job.id" class="job-card">
          <header>
            <div>
              <h3>{{ job.jobTitle }}</h3>
              <p class="muted">{{ job.companyName || '企业 #' + job.companyId }}</p>
            </div>
            <span class="tag">{{ job.jobType || '不限' }}</span>
          </header>
          <div class="job-card__meta">
            <span>地点：{{ job.location || '不限' }}</span>
            <span>薪资：{{ job.salaryRange || '面议' }}</span>
          </div>
          <p class="job-card__desc">{{ job.description || '暂无职位描述' }}</p>
          <footer>
            <button class="primary" :disabled="!selectedResumeId" @click="applyJob(job.id)">
              使用选中简历投递
            </button>
            <button
              class="outline"
              type="button"
              @click="loadCompanyDiscussions(job.companyId, job.companyName)"
            >
              查看企业讨论
            </button>
          </footer>
        </article>
      </div>
      <p v-else class="muted">请调整筛选条件或稍后再试，目前没有符合条件的职位。</p>
    </section>

    <section class="card">
      <div class="card__title">
        <h2>我的投递</h2>
        <button class="outline" @click="loadApplications">刷新</button>
      </div>
      <table v-if="applications.length" class="table">
        <thead>
          <tr><th>职位</th><th>企业</th><th>状态</th><th>投递时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="app in applications" :key="app.id">
            <td>{{ resolveJobTitle(app.jobId) }}</td>
            <td>{{ resolveCompanyName(app.companyId) }}</td>
            <td><span class="status">{{ app.status }}</span></td>
            <td>{{ formatDate(app.applyTime) }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">暂无投递记录</p>
    </section>

    <section class="card">
      <div class="card__title">
        <h2>消息中心</h2>
        <button class="outline" @click="loadMessages">刷新</button>
      </div>
      <ul class="list" v-if="messages.length">
        <li v-for="message in messages" :key="message.id" class="list__item">
          <div>
            <h3>{{ message.title }}</h3>
            <p class="muted">{{ formatDate(message.sendTime) }}</p>
            <p>{{ message.content }}</p>
          </div>
          <button v-if="!message.isRead" class="outline" @click="markMessageRead(message.id)">标记已读</button>
        </li>
      </ul>
      <p v-else class="muted">暂无消息</p>
    </section>

    <section class="card">
      <div class="card__title">
        <h2>平台公告</h2>
        <button class="outline" @click="loadAnnouncements">刷新</button>
      </div>
      <ul class="list" v-if="announcements.length">
        <li v-for="item in announcements" :key="item.id" class="list__item">
          <div>
            <h3>{{ item.title }}</h3>
            <p class="muted">发布时间：{{ formatDate(item.publishTime) }}</p>
            <p>{{ item.content }}</p>
          </div>
        </li>
      </ul>
      <p v-else class="muted">暂无公告</p>
    </section>

    <section class="card card--wide">
      <div class="card__title">
        <h2>企业讨论区</h2>
        <button v-if="currentDiscussionCompany.id" class="outline" type="button" @click="resetDiscussion">
          返回说明
        </button>
      </div>
      <p class="muted" v-if="discussionLoading">正在加载企业讨论，请稍候…</p>
      <template v-else>
        <p class="muted" v-if="!currentDiscussionCompany.id">
          点击职位卡片中的“查看企业讨论”即可查看企业与管理员审核通过的交流内容。
        </p>
        <div v-else>
          <h3>{{ currentDiscussionCompany.name }} 的公开讨论</h3>
          <p v-if="discussionFeedback.message" :class="['feedback', discussionFeedback.type]">
            {{ discussionFeedback.message }}
          </p>
          <ul class="list" v-if="discussions.length">
            <li v-for="post in discussions" :key="post.id" class="list__item">
              <div>
                <h3>{{ post.title }}</h3>
                <p class="muted">发布时间：{{ formatDate(post.createdAt) }}</p>
                <p>{{ post.sanitizedContent }}</p>
                <p v-if="post.reviewComment" class="muted">审核备注：{{ post.reviewComment }}</p>
              </div>
            </li>
          </ul>
          <p v-else class="muted">暂无公开讨论，欢迎稍后再来查看。</p>
        </div>
      </template>
    </section>
    </div>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { clearAuthInfo, del, get, getAuthInfo, post, put, upload } from '../api/http';

const router = useRouter();
const authInfo = getAuthInfo();
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

const resumeForm = reactive({
  title: '',
  educationExperience: '',
  workExperience: '',
  skills: '',
  selfEvaluation: '',
  attachment: ''
});
const resumes = ref([]);
const selectedResumeId = ref(null);
const editingResumeId = ref(null);
const resumeFile = ref(null);
const resumeFileInput = ref(null);

const jobFilters = reactive({
  keyword: '',
  company: '',
  jobType: '',
  location: '',
  salaryRange: ''
});
const jobs = ref([]);

const applications = ref([]);
const messages = ref([]);
const announcements = ref([]);
const discussions = ref([]);
const currentDiscussionCompany = reactive({ id: null, name: '' });
const discussionLoading = ref(false);
const discussionFeedback = reactive({ message: '', type: 'info' });

const feedback = reactive({ message: '', type: 'info' });

const resumeStats = computed(() => {
  const selectedResume = resumes.value.find(item => item.id === selectedResumeId.value);
  return {
    total: resumes.value.length,
    selectedLabel: selectedResume ? '已选择' : '未选择',
    selectedTitle: selectedResume?.title ?? '',
    applications: applications.value.length,
    unreadMessages: messages.value.filter(item => !item.isRead).length
  };
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

function handleLogout() {
  clearAuthInfo();
  router.replace({ name: 'login' });
}

function resetResumeForm() {
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

async function loadProfile() {
  try {
    const data = await get('/portal/student/profile');
    Object.assign(profileForm, data);
    profileSaved.value = false;
  } catch (error) {
    if (error.status === 404) {
      profileSaved.value = false;
      return;
    }
    showFeedback(error.message, 'error');
  }
}

async function saveProfile() {
  try {
    const data = await put('/portal/student/profile', profileForm);
    Object.assign(profileForm, data);
    profileSaved.value = true;
    showFeedback('资料保存成功', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function refreshResumes() {
  try {
    resumes.value = await get('/portal/student/resumes');
    if (!selectedResumeId.value && resumes.value.length) {
      selectedResumeId.value = resumes.value[0].id;
    }
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function createResume() {
  try {
    if (editingResumeId.value) {
      const payload = { ...resumeForm };
      const updated = await put(`/portal/student/resumes/${editingResumeId.value}`, payload);
      let finalResume = updated;
      if (resumeFile.value) {
        const formData = new FormData();
        formData.append('file', resumeFile.value);
        finalResume = await upload(`/portal/student/resumes/${editingResumeId.value}/attachment`, formData);
      }
      const index = resumes.value.findIndex(item => item.id === editingResumeId.value);
      if (index !== -1) {
        resumes.value[index] = finalResume;
      }
      selectedResumeId.value = finalResume.id;
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
      selectedResumeId.value = created.id;
      showFeedback('简历创建成功', 'success');
    }
    resetResumeForm();
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
  selectedResumeId.value = resume.id;
  editingResumeId.value = resume.id;
  showFeedback('已加载简历内容，可修改后再次保存创建', 'info');
}

async function deleteResume(resumeId) {
  if (!confirm('确定要删除该简历吗？')) return;
  try {
    await del(`/portal/student/resumes/${resumeId}`);
    resumes.value = resumes.value.filter(item => item.id !== resumeId);
    if (selectedResumeId.value === resumeId) {
      selectedResumeId.value = resumes.value[0]?.id ?? null;
    }
    showFeedback('简历已删除', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function selectResumeForApply(resumeId) {
  selectedResumeId.value = resumeId;
}

function buildQuery(params) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value) {
      search.append(key, value);
    }
  });
  const query = search.toString();
  return query ? `?${query}` : '';
}

async function searchJobs() {
  try {
    const query = buildQuery({
      keyword: jobFilters.keyword,
      company: jobFilters.company,
      jobType: jobFilters.jobType,
      location: jobFilters.location,
      salaryRange: jobFilters.salaryRange
    });
    jobs.value = await get(`/portal/student/jobs${query}`);
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function resetJobFilters() {
  jobFilters.keyword = '';
  jobFilters.company = '';
  jobFilters.jobType = '';
  jobFilters.location = '';
  jobFilters.salaryRange = '';
  searchJobs();
}

async function applyJob(jobId) {
  if (!selectedResumeId.value) {
    showFeedback('请先选择要投递的简历', 'error');
    return;
  }
  try {
    await post('/portal/student/applications', {
      jobId,
      resumeId: selectedResumeId.value
    });
    showFeedback('投递成功，稍后可在“我的投递”中查看进度', 'success');
    await loadApplications();
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadApplications() {
  try {
    applications.value = await get('/portal/student/applications');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadMessages() {
  try {
    messages.value = await get('/portal/student/messages');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function markMessageRead(messageId) {
  try {
    const updated = await post(`/portal/student/messages/${messageId}/read`);
    messages.value = messages.value.map(msg => (msg.id === updated.id ? updated : msg));
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadAnnouncements() {
  try {
    announcements.value = await get('/portal/student/announcements');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function loadCompanyDiscussions(companyId, companyName) {
  if (!companyId) {
    return;
  }
  currentDiscussionCompany.id = companyId;
  currentDiscussionCompany.name = companyName ?? `企业 #${companyId}`;
  discussionLoading.value = true;
  discussionFeedback.message = '';
  try {
    discussions.value = await get(`/public/discussions/company/${companyId}`);
    if (!discussions.value.length) {
      discussionFeedback.message = '暂无公开讨论记录';
      discussionFeedback.type = 'info';
    }
  } catch (error) {
    discussions.value = [];
    discussionFeedback.message = error.message ?? '加载讨论失败';
    discussionFeedback.type = 'error';
  } finally {
    discussionLoading.value = false;
  }
}

function resetDiscussion() {
  currentDiscussionCompany.id = null;
  currentDiscussionCompany.name = '';
  discussions.value = [];
  discussionFeedback.message = '';
  discussionFeedback.type = 'info';
}

function resolveJobTitle(jobId) {
  const job = jobs.value.find(item => item.id === jobId);
  return job ? job.jobTitle : '职位 #' + jobId;
}

function resolveCompanyName(companyId) {
  if (!companyId) return '企业';
  const job = jobs.value.find(item => item.companyId === companyId);
  return job?.companyName ?? `企业 #${companyId}`;
}

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

onMounted(async () => {
  await loadProfile();
  await refreshResumes();
  await searchJobs();
  await loadApplications();
  await loadMessages();
  await loadAnnouncements();
});
</script>

<style scoped>
.dashboard {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px 24px 64px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.dashboard__header {
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
  padding: 28px 32px;
  border-radius: 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.18);
}

.dashboard__header h1 {
  margin: 0 0 8px;
  font-size: 28px;
}

.dashboard__header p {
  margin: 0;
  font-size: 15px;
  opacity: 0.92;
}

.dashboard__header .outline {
  border-color: rgba(255, 255, 255, 0.7);
  color: #fff;
  background: rgba(255, 255, 255, 0.15);
}

.dashboard__header .outline:hover {
  background: rgba(255, 255, 255, 0.25);
}

.dashboard__content {
  display: grid;
  gap: 24px;
  align-items: start;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

.summary-item {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.12), rgba(14, 165, 233, 0.08));
  border-radius: 18px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.summary-item span {
  font-size: 13px;
  color: #1e293b;
  opacity: 0.8;
}

.summary-item strong {
  font-size: 22px;
  color: #0f172a;
  font-weight: 700;
  word-break: break-word;
}

.summary-item small {
  font-size: 12px;
  color: #334155;
  opacity: 0.75;
}

.card {
  background: rgba(255, 255, 255, 0.96);
  border-radius: 24px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(226, 232, 240, 0.7);
  backdrop-filter: blur(6px);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 26px 48px rgba(15, 23, 42, 0.12);
}

.card--wide {
  grid-column: 1 / -1;
}

.card__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.resume-card__actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.resume-card__grid {
  display: grid;
  gap: 24px;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
}

.resume-card__list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.actions-inline {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.card h2 {
  margin: 0;
  font-size: 22px;
  color: #0f172a;
}

.form-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.form-grid label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
  color: #1e293b;
}

.form-grid input,
.form-grid textarea,
.form-grid select {
  border: 1px solid #dbeafe;
  border-radius: 14px;
  padding: 10px 14px;
  font-size: 14px;
  background: #f8fbff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.form-grid input:focus,
.form-grid textarea:focus,
.form-grid select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.form-grid textarea {
  min-height: 120px;
  resize: vertical;
}

.form-grid .full {
  grid-column: 1 / -1;
}

.file-input input[type='file'] {
  padding: 9px 12px;
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: #f8fafc;
}

.file-input small {
  color: #64748b;
  font-size: 12px;
}

.file-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.list__item {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
  border-radius: 20px;
  background: #f8fafc;
  border: 1px solid rgba(226, 232, 240, 0.9);
}

.list__item > div {
  flex: 1;
}

.list__actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 120px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: inset 0 0 0 1px rgba(226, 232, 240, 0.8);
}

.table th {
  background: #f1f5f9;
  color: #1f2937;
}

.table th,
.table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
  font-size: 14px;
}

.table tbody tr:hover {
  background: #f8fafc;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.filters input {
  flex: 1 1 180px;
  border: 1px solid #dbeafe;
  border-radius: 14px;
  padding: 10px 14px;
  background: #f8fbff;
}

.job-grid {
  display: grid;
  gap: 18px;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.job-card {
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 20px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: linear-gradient(145deg, #ffffff 0%, #f5f9ff 100%);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.job-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.12);
}

.job-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.job-card__meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #1e293b;
  opacity: 0.8;
}

.job-card__desc {
  line-height: 1.6;
  color: #475569;
}

.job-card footer {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tag {
  background: linear-gradient(135deg, #bae6fd, #7dd3fc);
  color: #0369a1;
  border-radius: 999px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
}

.status {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.12);
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 600;
}

.muted {
  color: #64748b;
  margin: 0;
}

.primary,
.outline,
.danger {
  border-radius: 999px;
  padding: 9px 18px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.primary {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border: none;
  color: #fff;
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.25);
}

.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 32px rgba(37, 99, 235, 0.3);
}

.outline {
  background: transparent;
  border: 1px solid #2563eb;
  color: #1d4ed8;
}

.outline:hover {
  background: rgba(37, 99, 235, 0.08);
  transform: translateY(-1px);
}

.danger {
  background: rgba(220, 38, 38, 0.08);
  border: 1px solid #dc2626;
  color: #b91c1c;
}

.danger:hover {
  background: rgba(248, 113, 113, 0.16);
  transform: translateY(-1px);
}

.feedback {
  text-align: center;
  padding: 14px 18px;
  border-radius: 16px;
  max-width: 600px;
  margin: 0 auto 32px;
  font-weight: 600;
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

.hint {
  color: #10b981;
  font-weight: 500;
}

@media (max-width: 900px) {
  .dashboard__header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .dashboard__header .outline {
    align-self: stretch;
    text-align: center;
  }

  .card--wide {
    grid-column: 1 / -1;
  }

  .list__item {
    flex-direction: column;
  }

  .list__actions {
    flex-direction: row;
    flex-wrap: wrap;
  }
}

@media (max-width: 600px) {
  .dashboard {
    padding: 24px 16px 48px;
  }

  .card {
    padding: 20px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .job-card footer {
    flex-direction: column;
  }
}
</style>
