<template>
  <DashboardLayout
    title="学生工作台"
    :subtitle="subtitle"
    :nav-items="navItems"
    @logout="handleLogout"
  >
    <router-view />
    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </DashboardLayout>
</template>

<script setup>
import { computed, onMounted, provide, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import DashboardLayout from '../components/dashboard/DashboardLayout.vue';
import { clearAuthInfo, del, get, getAuthInfo, post, put, upload } from '../api/http';

const router = useRouter();
const authInfo = getAuthInfo();

const subtitle = computed(() => {
  if (!authInfo) return '';
  const roleName = authInfo.roleDisplayName ?? '';
  const username = authInfo.username ?? '';
  return `欢迎回来，${roleName} ${username}`.trim();
});

const navItems = [
  { label: '基本资料', to: '/student/profile' },
  { label: '我的简历', to: '/student/resumes' },
  { label: '职位浏览', to: '/student/jobs' },
  { label: '我的投递', to: '/student/applications' },
  { label: '消息中心', to: '/student/messages' },
  { label: '平台公告', to: '/student/announcements' },
  { label: '企业讨论区', to: '/student/discussions' }
];

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

const studentContext = {
  authInfo,
  profileForm,
  profileSaved,
  resumeForm,
  resumes,
  selectedResumeId,
  editingResumeId,
  resumeFile,
  resumeFileInput,
  jobFilters,
  jobs,
  applications,
  messages,
  announcements,
  discussions,
  currentDiscussionCompany,
  discussionLoading,
  discussionFeedback,
  feedback,
  loadProfile,
  saveProfile,
  refreshResumes,
  createResume,
  resetResumeForm,
  handleResumeFile,
  editResume,
  deleteResume,
  selectResumeForApply,
  searchJobs,
  applyJob,
  loadApplications,
  loadMessages,
  markMessageRead,
  loadAnnouncements,
  loadCompanyDiscussions,
  resetDiscussion,
  resolveJobTitle,
  resolveCompanyName,
  formatDate,
  showFeedback
};

provide('studentContext', studentContext);

onMounted(async () => {
  await loadProfile();
  await refreshResumes();
  await searchJobs();
  await loadApplications();
  await loadMessages();
  await loadAnnouncements();
});
</script>
