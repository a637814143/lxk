<template>
  <div class="dashboard">
    <header class="dashboard__header">
      <div>
        <h1>学生工作台</h1>
        <p>欢迎回来，{{ authInfo?.roleDisplayName }} {{ authInfo?.username }}</p>
      </div>
      <button class="outline" @click="handleLogout">退出登录</button>
    </header>

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

    <section class="card">
      <div class="card__title">
        <h2>我的简历</h2>
        <button class="outline" @click="refreshResumes">刷新</button>
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
            <button class="outline" @click="selectResumeForApply(resume.id)">
              {{ resume.id === selectedResumeId ? '已选择' : '投递使用' }}
            </button>
            <button class="outline" @click="editResume(resume)">编辑</button>
            <button class="danger" @click="deleteResume(resume.id)">删除</button>
          </div>
        </li>
      </ul>
      <p v-else class="muted">还没有简历，填写上方表单即可新建。</p>
    </section>

    <section class="card">
      <div class="card__title">
        <h2>职位浏览</h2>
        <button class="outline" @click="searchJobs">搜索</button>
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
            <h3>{{ job.jobTitle }}</h3>
            <span class="tag">{{ job.jobType || '不限' }}</span>
          </header>
          <p class="muted">工作地点：{{ job.location || '不限' }}</p>
          <p class="muted">薪资范围：{{ job.salaryRange || '面议' }}</p>
          <p class="job-card__desc">{{ job.description || '暂无职位描述' }}</p>
          <footer>
            <button class="primary" :disabled="!selectedResumeId" @click="applyJob(job.id)">
              使用选中简历投递
            </button>
            <button class="outline" type="button"
              @click="loadCompanyDiscussions(job.companyId, job.companyName)">
              查看企业讨论
            </button>
          </footer>
        </article>
      </div>
      <p v-else class="muted">请调整筛选条件或稍后再试，目前没有符合条件的职位。</p>
    </section>

    <section class="card">
      <h2>我的投递</h2>
      <table v-if="applications.length" class="table">
        <thead>
          <tr><th>职位</th><th>企业</th><th>状态</th><th>企业备注</th><th>投递时间</th></tr>
        </thead>
        <tbody>
          <tr v-for="app in applications" :key="app.id">
            <td>{{ resolveJobTitle(app.jobId) }}</td>
            <td>{{ resolveCompanyName(app.companyId) }}</td>
            <td>{{ app.status }}</td>
            <td>{{ app.decisionNote || '—' }}</td>
            <td>{{ formatDate(app.applyTime) }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">暂无投递记录</p>
    </section>

    <section class="card">
      <h2>消息中心</h2>
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
      <h2>平台公告</h2>
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

    <section class="card">
      <div class="card__title">
        <h2>企业讨论区</h2>
        <button v-if="currentDiscussionCompany.id" class="outline" type="button" @click="resetDiscussion">
          返回职位列表
        </button>
      </div>
      <p class="muted" v-if="discussionLoading">正在加载企业讨论，请稍候…</p>
      <template v-else>
        <p class="muted" v-if="!currentDiscussionCompany.id">
          点击职位卡片中的“查看企业讨论”即可浏览企业与同学的公开交流，并参与回复。
        </p>
        <div v-else>
          <h3>{{ currentDiscussionCompany.name }} 的公开讨论</h3>
          <form class="discussion-form" @submit.prevent="submitDiscussion">
            <label v-if="!replyTarget" class="full">
              讨论主题（可选）
              <input v-model="discussionForm.title" maxlength="100" placeholder="为你的评论添加一个主题" />
            </label>
            <label class="full">
              评论内容<textarea v-model="discussionForm.content" required maxlength="1000"
                placeholder="分享你的想法或向企业提问"></textarea>
            </label>
            <div class="discussion-actions">
              <span v-if="replyTarget" class="reply-hint">
                正在回复：{{ replyTarget.title || replyTarget.sanitizedContent || replyTarget.content }}
              </span>
              <span class="spacer"></span>
              <button v-if="replyTarget" class="outline" type="button" @click="cancelReply">取消回复</button>
              <button class="primary" type="submit">发布评论</button>
            </div>
          </form>
          <p v-if="discussionFeedback.message" :class="['feedback', discussionFeedback.type]">
            {{ discussionFeedback.message }}
          </p>
          <ul v-if="discussionThreads.length" class="discussion-list">
            <li v-for="thread in discussionThreads" :key="thread.id" class="discussion-thread">
              <article class="discussion-post">
                <header>
                  <h4>{{ thread.title }}</h4>
                  <p class="muted">{{ resolveAuthorLabel(thread) }} · {{ formatDate(thread.createdAt) }}</p>
                </header>
                <p>{{ thread.sanitizedContent || thread.content }}</p>
                <footer>
                  <button class="text" type="button" @click="setReplyTarget(thread)">回复</button>
                </footer>
              </article>
              <ul v-if="thread.replies.length" class="reply-list">
                <li v-for="reply in thread.replies" :key="reply.id" class="discussion-reply">
                  <div class="reply-header">
                    <strong>{{ resolveAuthorLabel(reply) }}</strong>
                    <span class="muted">{{ formatDate(reply.createdAt) }}</span>
                  </div>
                  <p>{{ reply.sanitizedContent || reply.content }}</p>
                  <button class="text" type="button" @click="setReplyTarget(reply)">回复</button>
                </li>
              </ul>
            </li>
          </ul>
          <p v-else class="muted">还没有评论，欢迎发表第一条。</p>
        </div>
      </template>
    </section>

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
const discussionThreads = computed(() => buildDiscussionThreads(discussions.value));
const currentDiscussionCompany = reactive({ id: null, name: '' });
const discussionLoading = ref(false);
const discussionFeedback = reactive({ message: '', type: 'info' });
const discussionForm = reactive({ title: '', content: '' });
const replyTarget = ref(null);

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

function showDiscussionFeedback(message, type = 'info') {
  discussionFeedback.message = message;
  discussionFeedback.type = type;
  if (message) {
    setTimeout(() => {
      discussionFeedback.message = '';
    }, 4000);
  }
}

function buildDiscussionThreads(items) {
  if (!Array.isArray(items)) {
    return [];
  }
  const nodes = items.map(item => ({ ...item, replies: [] }));
  const map = new Map(nodes.map(node => [node.id, node]));
  const roots = [];
  nodes.forEach(node => {
    if (node.parentId && map.has(node.parentId)) {
      map.get(node.parentId).replies.push(node);
    } else {
      roots.push(node);
    }
  });
  return roots;
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
    const items = await get('/portal/student/applications');
    applications.value = items.map(item => ({
      ...item,
      decisionNote: item.decisionNote ?? ''
    }));
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
  showDiscussionFeedback('正在加载讨论，请稍候…', 'info');
  discussionForm.title = '';
  discussionForm.content = '';
  replyTarget.value = null;
  try {
    const items = await get(`/public/discussions/company/${companyId}`);
    discussions.value = items;
    if (!items.length) {
      showDiscussionFeedback('暂无评论，欢迎发表第一条', 'info');
    } else {
      showDiscussionFeedback('', 'info');
    }
  } catch (error) {
    discussions.value = [];
    showDiscussionFeedback(error.message ?? '加载讨论失败', 'error');
  } finally {
    discussionLoading.value = false;
  }
}

function resetDiscussion() {
  currentDiscussionCompany.id = null;
  currentDiscussionCompany.name = '';
  discussions.value = [];
  discussionForm.title = '';
  discussionForm.content = '';
  replyTarget.value = null;
  showDiscussionFeedback('', 'info');
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

async function submitDiscussion() {
  if (!currentDiscussionCompany.id) {
    showDiscussionFeedback('请先选择要查看的企业讨论', 'error');
    return;
  }
  if (!discussionForm.content.trim()) {
    showDiscussionFeedback('请填写评论内容', 'error');
    return;
  }
  const payload = {
    companyId: currentDiscussionCompany.id,
    title: discussionForm.title?.trim() || undefined,
    content: discussionForm.content.trim(),
    parentId: replyTarget.value?.id ?? null
  };
  try {
    const created = await post('/portal/student/discussions', payload);
    discussions.value = [...discussions.value, created];
    discussionForm.title = '';
    discussionForm.content = '';
    replyTarget.value = null;
    showDiscussionFeedback('评论已发布', 'success');
  } catch (error) {
    showDiscussionFeedback(error.message ?? '发布评论失败', 'error');
  }
}

function setReplyTarget(post) {
  replyTarget.value = post;
  showDiscussionFeedback(`正在回复 ${post.authorName ?? '该评论'}`, 'info');
}

function cancelReply() {
  replyTarget.value = null;
  showDiscussionFeedback('', 'info');
}

function resolveAuthorLabel(post) {
  return post.authorRole ?? '用户';
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
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.form-grid input,
.form-grid textarea {
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

.discussion-form {
  display: grid;
  gap: 12px;
}

.discussion-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.discussion-actions .spacer {
  flex: 1;
}

.reply-hint {
  color: #475569;
  font-size: 14px;
}

.discussion-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.discussion-thread {
  display: flex;
  flex-direction: column;
  gap: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
}

.discussion-post header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.discussion-post footer {
  margin-top: 8px;
}

.reply-list {
  list-style: none;
  margin: 0;
  padding-left: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.discussion-reply {
  border-left: 3px solid #bfdbfe;
  padding-left: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #475569;
}

button.text {
  background: none;
  border: none;
  color: #2563eb;
  cursor: pointer;
  padding: 0;
  font-size: 14px;
}

button.text:hover {
  text-decoration: underline;
}

.list__actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  text-align: left;
  padding: 8px 12px;
  border-bottom: 1px solid #e5e7eb;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.filters input {
  flex: 1 1 180px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 8px 10px;
}

.job-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.job-card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.job-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.job-card__desc {
  line-height: 1.5;
  color: #475569;
}

.tag {
  background: #e0f2fe;
  color: #0369a1;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
}

.muted {
  color: #6b7280;
  margin: 0;
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

.hint {
  color: #10b981;
}
</style>
