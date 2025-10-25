<template>
  <div class="dashboard-layout">
    <aside class="dashboard-sidebar">
      <h2>学生导航</h2>
      <p>从侧边栏选择功能，快速处理求职流程。</p>
      <nav class="sidebar-nav">
        <button
          v-for="item in sections"
          :key="item.key"
          type="button"
          :class="['sidebar-button', { active: activeSection === item.key }]"
          @click="selectSection(item.key)"
        >
          <span aria-hidden="true">{{ item.icon }}</span>
          <div>
            {{ item.label }}
            <div v-if="item.description" class="muted" style="font-size: 0.75rem; margin-top: 2px;">
              {{ item.description }}
            </div>
          </div>
        </button>
      </nav>
    </aside>

    <div class="dashboard-main">
      <header class="dashboard__header">
        <div>
          <h1>学生工作台</h1>
          <p>欢迎回来，{{ authInfo?.roleDisplayName }} {{ authInfo?.username }}</p>
        </div>
        <button class="outline" @click="handleLogout">退出登录</button>
      </header>

      <div class="dashboard-content">
        <section v-if="activeSection === 'profile'" class="card">
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

        <section v-else-if="activeSection === 'resumes'" class="card">
          <div class="card__title">
            <h2>我的简历</h2>
            <button class="outline" @click="refreshResumes(true)">刷新</button>
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

        <section v-else-if="activeSection === 'jobs'" class="card">
          <div class="card__title">
            <h2>职位浏览</h2>
            <button class="outline" @click="searchJobs(true)">搜索</button>
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
                <button class="outline" type="button" @click="loadCompanyDiscussions(job.companyId, job.companyName)">
                  查看企业讨论
                </button>
              </footer>
            </article>
          </div>
          <p v-else class="muted">请调整筛选条件或稍后再试，目前没有符合条件的职位。</p>
        </section>

        <section v-else-if="activeSection === 'applications'" class="card">
          <div class="card__title">
            <h2>我的投递</h2>
            <button class="outline" @click="loadApplications(true)">刷新</button>
          </div>
          <div style="display: grid; gap: 18px;">
            <PieChart :data="applicationChartData" title="投递状态分布" />
            <table v-if="applications.length" class="table">
              <thead>
                <tr><th>职位</th><th>企业</th><th>状态</th><th>投递时间</th></tr>
              </thead>
              <tbody>
                <tr v-for="app in applications" :key="app.id">
                  <td>{{ resolveJobTitle(app.jobId) }}</td>
                  <td>{{ resolveCompanyName(app.companyId) }}</td>
                  <td>{{ app.status }}</td>
                  <td>{{ formatDate(app.applyTime) }}</td>
                </tr>
              </tbody>
            </table>
            <p v-else class="muted">暂无投递记录</p>
          </div>
        </section>

        <section v-else-if="activeSection === 'messages'" class="card">
          <div class="card__title">
            <h2>消息中心</h2>
            <button class="outline" @click="loadMessages(true)">刷新</button>
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

        <section v-else-if="activeSection === 'announcements'" class="card">
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

        <section v-else-if="activeSection === 'discussions'" class="card">
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

        <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { clearAuthInfo, del, get, getAuthInfo, post, put, upload } from '../api/http';
import { notifyError, notifyInfo, notifySuccess } from '../composables/useNotifier';
import PieChart from '../components/PieChart.vue';

const router = useRouter();
const authInfo = getAuthInfo();
const sections = [
  { key: 'profile', label: '个人资料', icon: '🧑‍🎓', description: '更新联系方式与就读信息' },
  { key: 'resumes', label: '我的简历', icon: '📄', description: '维护履历并选择投递版本' },
  { key: 'jobs', label: '职位浏览', icon: '💼', description: '搜索合适的岗位机会' },
  { key: 'applications', label: '投递进度', icon: '📊', description: '查看申请状态与统计' },
  { key: 'messages', label: '消息中心', icon: '✉️', description: '及时查收通知提醒' },
  { key: 'announcements', label: '平台公告', icon: '📢', description: '了解最新平台动态' },
  { key: 'discussions', label: '企业讨论', icon: '💬', description: '阅读企业公开讨论' }
];
const activeSection = ref('profile');
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

const applicationChartData = computed(() => {
  if (!applications.value.length) {
    return {};
  }
  return applications.value.reduce((accumulator, application) => {
    const status = application.status || '未标记';
    accumulator[status] = (accumulator[status] ?? 0) + 1;
    return accumulator;
  }, {});
});

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
    if (type === 'success') {
      notifySuccess(message);
    } else if (type === 'error') {
      notifyError(message);
    } else if (type === 'info') {
      notifyInfo(message);
    }
  }
}

function handleLogout() {
  clearAuthInfo();
  router.replace({ name: 'login' });
}

function selectSection(sectionKey) {
  activeSection.value = sectionKey;
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

async function refreshResumes(showToast = false) {
  try {
    resumes.value = await get('/portal/student/resumes');
    if (!selectedResumeId.value && resumes.value.length) {
      selectedResumeId.value = resumes.value[0].id;
    }
    if (showToast) {
      showFeedback('简历列表已刷新', 'success');
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

async function searchJobs(showToast = false) {
  try {
    const query = buildQuery({
      keyword: jobFilters.keyword,
      company: jobFilters.company,
      jobType: jobFilters.jobType,
      location: jobFilters.location,
      salaryRange: jobFilters.salaryRange
    });
    jobs.value = await get(`/portal/student/jobs${query}`);
    if (showToast) {
      showFeedback('职位列表已更新', 'success');
    }
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
    applications.value = await get('/portal/student/applications');
  } catch (error) {
    applications.value = [];
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
    showFeedback('消息已标记为已读', 'success');
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

watch(activeSection, (section) => {
  const loaderMap = {
    profile: () => {
      if (!profileSaved.value && !profileForm.name) {
        loadProfile();
      }
    },
    resumes: () => {
      if (!resumes.value.length) {
        refreshResumes();
      }
    },
    jobs: () => {
      if (!jobs.value.length) {
        searchJobs();
      }
    },
    applications: () => {
      if (!applications.value.length) {
        loadApplications();
      }
    },
    messages: () => {
      if (!messages.value.length) {
        loadMessages();
      }
    },
    announcements: () => {
      if (!announcements.value.length) {
        loadAnnouncements();
      }
    },
    discussions: () => {
      if (currentDiscussionCompany.id && !discussions.value.length) {
        loadCompanyDiscussions(currentDiscussionCompany.id, currentDiscussionCompany.name);
      }
    }
  };

  loaderMap[section]?.();
});

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
.dashboard-content > * {
  animation: fade-in 0.25s ease;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
