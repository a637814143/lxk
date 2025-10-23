<template>
  <div class="dashboard" v-if="!loading">
    <div class="container">
      <header class="hero" v-if="user">
        <div>
          <h1>欢迎回来，{{ user.username }}</h1>
          <p>当前角色：{{ user.roleDisplayName ?? '系统管理员' }} · 用户ID：{{ user.userId }}</p>
        </div>
        <button class="ghost" @click="logout">退出登录</button>
      </header>

      <section v-if="error" class="error-banner">{{ error }}</section>

      <section v-else class="content">
        <div v-if="actionMessage.message" :class="['toast', actionMessage.type]">
          {{ actionMessage.message }}
        </div>

        <section class="summary">
          <article>
            <h3>用户</h3>
            <p>{{ summaryStats.userCount }}</p>
          </article>
          <article>
            <h3>管理员</h3>
            <p>{{ summaryStats.adminCount }}</p>
          </article>
          <article>
            <h3>企业</h3>
            <p>{{ summaryStats.companyCount }}</p>
          </article>
          <article>
            <h3>学生</h3>
            <p>{{ summaryStats.studentCount }}</p>
          </article>
          <article>
            <h3>职位</h3>
            <p>{{ summaryStats.jobCount }}</p>
          </article>
          <article>
            <h3>投递</h3>
            <p>{{ summaryStats.applicationCount }}</p>
          </article>
        </section>

        <section class="panel-grid">
          <article class="panel">
            <header>
              <h2>管理员档案</h2>
              <p>维护当前系统管理员信息，可用于发布公告和记录日志</p>
            </header>
            <form class="form-grid" @submit.prevent="saveAdminProfile">
              <label>
                <span>用户ID</span>
                <input v-model="adminProfileForm.userId" type="number" :readonly="!!adminProfile" required />
              </label>
              <label>
                <span>姓名</span>
                <input v-model="adminProfileForm.name" type="text" required />
              </label>
              <label>
                <span>管理员等级</span>
                <input v-model="adminProfileForm.level" type="number" min="1" />
              </label>
              <div class="form-actions">
                <button class="primary" type="submit">
                  {{ adminProfile ? '更新档案' : '创建档案' }}
                </button>
                <button v-if="adminProfile" class="ghost" type="button" @click="resetAdminProfileForm">
                  取消
                </button>
              </div>
            </form>
            <p class="muted" v-if="adminProfile">
              管理员ID：{{ adminProfile.id }} · 姓名：{{ adminProfile.name }} · 等级：{{ adminProfile.level ?? '未设置' }}
            </p>
          </article>

          <article class="panel">
            <header>
              <h2>用户管理</h2>
              <p>创建或调整系统中的账户</p>
            </header>
            <form class="form-grid" @submit.prevent="submitUser">
              <label><span>用户名</span><input v-model="forms.user.username" type="text" /></label>
              <label><span>密码</span><input v-model="forms.user.password" type="password" :required="!editing.user" /></label>
              <label><span>邮箱</span><input v-model="forms.user.email" type="email" /></label>
              <label><span>手机号</span><input v-model="forms.user.phone" type="text" /></label>
              <label><span>角色</span>
                <select v-model="forms.user.role">
                  <option value="ADMIN">系统管理员</option>
                  <option value="COMPANY">企业</option>
                  <option value="STUDENT">学生</option>
                </select>
              </label>
              <label><span>状态</span><input v-model="forms.user.status" type="number" min="0" max="1" /></label>
              <div class="form-actions">
                <button class="primary" type="submit">{{ editing.user ? '保存修改' : '新增用户' }}</button>
                <button v-if="editing.user" class="ghost" type="button" @click="resetForm('user')">取消编辑</button>
              </div>
            </form>
            <ul class="list">
              <li v-for="item in lists.users" :key="item.id">
                <div>
                  <strong>#{{ item.id }} · {{ item.username }}</strong>
                  <p>角色：{{ item.role }} · 状态：{{ item.status }} · 邮箱：{{ item.email }}</p>
                </div>
                <div class="item-actions">
                  <button @click="startEditUser(item)">编辑</button>
                  <button class="danger" @click="removeUser(item.id)">删除</button>
                </div>
              </li>
            </ul>
          </article>

          <article class="panel">
            <header>
              <h2>系统管理员</h2>
              <p>管理所有管理员档案</p>
            </header>
            <form class="form-grid" @submit.prevent="submitAdmin">
              <label><span>用户ID</span><input v-model="forms.admin.userId" type="number" /></label>
              <label><span>姓名</span><input v-model="forms.admin.name" type="text" /></label>
              <label><span>等级</span><input v-model="forms.admin.level" type="number" min="1" /></label>
              <div class="form-actions">
                <button class="primary" type="submit">{{ editing.admin ? '保存修改' : '新增管理员' }}</button>
                <button v-if="editing.admin" class="ghost" type="button" @click="resetForm('admin')">取消编辑</button>
              </div>
            </form>
            <ul class="list">
              <li v-for="item in lists.admins" :key="item.id">
                <div>
                  <strong>#{{ item.id }} · {{ item.name }}</strong>
                  <p>用户ID：{{ item.userId }} · 等级：{{ item.level ?? '未设置' }}</p>
                </div>
                <div class="item-actions">
                  <button @click="startEditAdmin(item)">编辑</button>
                  <button class="danger" @click="removeAdmin(item.id)">删除</button>
                </div>
              </li>
            </ul>
          </article>

          <article class="panel">
            <header>
              <h2>企业管理</h2>
              <p>维护企业资料与审核状态</p>
            </header>
            <form class="form-grid" @submit.prevent="submitCompany">
              <label><span>用户ID</span><input v-model="forms.company.userId" type="number" /></label>
              <label><span>企业名称</span><input v-model="forms.company.companyName" type="text" /></label>
              <label><span>营业执照</span><input v-model="forms.company.licenseNumber" type="text" /></label>
              <label><span>行业</span><input v-model="forms.company.industry" type="text" /></label>
              <label><span>地址</span><input v-model="forms.company.address" type="text" /></label>
              <label><span>官网</span><input v-model="forms.company.website" type="text" /></label>
              <label class="full"><span>企业简介</span><textarea v-model="forms.company.description"></textarea></label>
              <label><span>Logo</span><input v-model="forms.company.logo" type="text" /></label>
              <label><span>审核状态</span>
                <select v-model="forms.company.auditStatus">
                  <option value="pending">待审核</option>
                  <option value="approved">已通过</option>
                  <option value="rejected">已驳回</option>
                  <option value="closed">已关闭</option>
                </select>
              </label>
              <label class="full"><span>审核备注</span><textarea v-model="forms.company.auditReason"></textarea></label>
              <div class="form-actions">
                <button class="primary" type="submit">{{ editing.company ? '保存修改' : '新增企业' }}</button>
                <button v-if="editing.company" class="ghost" type="button" @click="resetForm('company')">取消编辑</button>
              </div>
            </form>
            <ul class="list">
              <li v-for="item in lists.companies" :key="item.id">
                <div>
                  <strong>#{{ item.id }} · {{ item.companyName }}</strong>
                  <p>用户ID：{{ item.userId }} · 行业：{{ item.industry ?? '未设置' }} · 状态：{{ item.auditStatus }}</p>
                </div>
                <div class="item-actions">
                  <button @click="startEditCompany(item)">编辑</button>
                  <button class="danger" @click="removeCompany(item.id)">删除</button>
                </div>
              </li>
            </ul>
          </article>

          <article class="panel">
            <header>
              <h2>学生管理</h2>
              <p>维护学生档案信息</p>
            </header>
            <form class="form-grid" @submit.prevent="submitStudent">
              <label><span>用户ID</span><input v-model="forms.student.userId" type="number" /></label>
              <label><span>姓名</span><input v-model="forms.student.name" type="text" /></label>
              <label><span>性别</span><input v-model="forms.student.gender" type="text" placeholder="男/女/其他" /></label>
              <label><span>学校</span><input v-model="forms.student.school" type="text" /></label>
              <label><span>专业</span><input v-model="forms.student.major" type="text" /></label>
              <label><span>年级</span><input v-model="forms.student.grade" type="text" /></label>
              <label><span>学历</span><input v-model="forms.student.education" type="text" /></label>
              <label><span>头像</span><input v-model="forms.student.avatar" type="text" /></label>
              <div class="form-actions">
                <button class="primary" type="submit">{{ editing.student ? '保存修改' : '新增学生' }}</button>
                <button v-if="editing.student" class="ghost" type="button" @click="resetForm('student')">取消编辑</button>
              </div>
            </form>
            <ul class="list">
              <li v-for="item in lists.students" :key="item.id">
                <div>
                  <strong>#{{ item.id }} · {{ item.name }}</strong>
                  <p>用户ID：{{ item.userId }} · 学校：{{ item.school ?? '未填写' }} · 专业：{{ item.major ?? '未填写' }}</p>
                </div>
                <div class="item-actions">
                  <button @click="startEditStudent(item)">编辑</button>
                  <button class="danger" @click="removeStudent(item.id)">删除</button>
                </div>
              </li>
            </ul>
          </article>

          <article class="panel">
            <header>
              <h2>职位管理</h2>
              <p>发布与更新招聘岗位</p>
            </header>
            <form class="form-grid" @submit.prevent="submitJob">
              <label><span>企业ID</span><input v-model="forms.job.companyId" type="number" /></label>
              <label><span>职位名称</span><input v-model="forms.job.jobTitle" type="text" /></label>
              <label><span>职位类别</span><input v-model="forms.job.jobType" type="text" /></label>
              <label><span>薪资范围</span><input v-model="forms.job.salaryRange" type="text" /></label>
              <label><span>工作地点</span><input v-model="forms.job.location" type="text" /></label>
              <label class="full"><span>岗位要求</span><textarea v-model="forms.job.requirement"></textarea></label>
              <label class="full"><span>职位描述</span><textarea v-model="forms.job.description"></textarea></label>
              <label><span>状态</span>
                <select v-model="forms.job.status">
                  <option value="pending">待审核</option>
                  <option value="approved">已发布</option>
                  <option value="rejected">已驳回</option>
                  <option value="closed">已关闭</option>
                </select>
              </label>
              <div class="form-actions">
                <button class="primary" type="submit">{{ editing.job ? '保存修改' : '新增职位' }}</button>
                <button v-if="editing.job" class="ghost" type="button" @click="resetForm('job')">取消编辑</button>
              </div>
            </form>
            <ul class="list">
              <li v-for="item in lists.jobs" :key="item.id">
                <div>
                  <strong>#{{ item.id }} · {{ item.jobTitle }}</strong>
                  <p>企业ID：{{ item.companyId }} · 地点：{{ item.location ?? '未填写' }} · 状态：{{ item.status }}</p>
                </div>
                <div class="item-actions">
                  <button @click="startEditJob(item)">编辑</button>
                  <button class="danger" @click="removeJob(item.id)">删除</button>
                </div>
              </li>
            </ul>
          </article>

          <article class="panel">
            <header>
              <h2>简历管理</h2>
              <p>维护学生简历信息</p>
            </header>
            <form class="form-grid" @submit.prevent="submitResume">
              <label><span>学生ID</span><input v-model="forms.resume.studentId" type="number" /></label>
              <label><span>简历标题</span><input v-model="forms.resume.title" type="text" /></label>
              <label class="full"><span>教育经历</span><textarea v-model="forms.resume.educationExperience"></textarea></label>
              <label class="full"><span>工作经历</span><textarea v-model="forms.resume.workExperience"></textarea></label>
              <label class="full"><span>技能特长</span><textarea v-model="forms.resume.skills"></textarea></label>
              <label class="full"><span>自我评价</span><textarea v-model="forms.resume.selfEvaluation"></textarea></label>
              <label><span>附件链接</span><input v-model="forms.resume.attachment" type="text" /></label>
              <div class="form-actions">
                <button class="primary" type="submit">{{ editing.resume ? '保存修改' : '新增简历' }}</button>
                <button v-if="editing.resume" class="ghost" type="button" @click="resetForm('resume')">取消编辑</button>
              </div>
            </form>
            <ul class="list">
              <li v-for="item in lists.resumes" :key="item.id">
                <div>
                  <strong>#{{ item.id }} · {{ item.title }}</strong>
                  <p>学生ID：{{ item.studentId }} · 更新于：{{ formatDate(item.updateTime) }}</p>
                </div>
                <div class="item-actions">
                  <button @click="startEditResume(item)">编辑</button>
                  <button class="danger" @click="removeResume(item.id)">删除</button>
                </div>
              </li>
            </ul>
          </article>

          <article class="panel">
            <header>
              <h2>投递管理</h2>
              <p>跟踪学生投递记录</p>
            </header>
            <form class="form-grid" @submit.prevent="submitApplication">
              <label><span>学生ID</span><input v-model="forms.application.studentId" type="number" /></label>
              <label><span>简历ID</span><input v-model="forms.application.resumeId" type="number" /></label>
              <label><span>职位ID</span><input v-model="forms.application.jobId" type="number" /></label>
              <label><span>企业ID</span><input v-model="forms.application.companyId" type="number" /></label>
              <label><span>状态</span>
                <select v-model="forms.application.status">
                  <option value="待查看">待查看</option>
                  <option value="已查看">已查看</option>
                  <option value="面试中">面试中</option>
                  <option value="录用">录用</option>
                  <option value="拒绝">拒绝</option>
                </select>
              </label>
              <div class="form-actions">
                <button class="primary" type="submit">{{ editing.application ? '保存修改' : '新增投递' }}</button>
                <button v-if="editing.application" class="ghost" type="button" @click="resetForm('application')">取消编辑</button>
              </div>
            </form>
            <ul class="list">
              <li v-for="item in lists.applications" :key="item.id">
                <div>
                  <strong>#{{ item.id }} · 状态：{{ item.status }}</strong>
                  <p>学生ID：{{ item.studentId }} · 简历ID：{{ item.resumeId }} · 职位ID：{{ item.jobId }} · 企业ID：{{ item.companyId }}</p>
                </div>
                <div class="item-actions">
                  <button @click="startEditApplication(item)">编辑</button>
                  <button class="danger" @click="removeApplication(item.id)">删除</button>
                </div>
              </li>
            </ul>
          </article>

          <article class="panel">
            <header>
              <h2>站内消息</h2>
              <p>向学生或企业发送通知</p>
            </header>
            <form class="form-grid" @submit.prevent="submitMessage">
              <label><span>发送者ID</span><input v-model="forms.message.senderId" type="number" placeholder="可留空表示系统" /></label>
              <label><span>接收者ID</span><input v-model="forms.message.receiverId" type="number" /></label>
              <label><span>标题</span><input v-model="forms.message.title" type="text" /></label>
              <label class="full"><span>内容</span><textarea v-model="forms.message.content"></textarea></label>
              <label class="checkbox"><input v-model="forms.message.isRead" type="checkbox" /> 标记为已读</label>
              <div class="form-actions">
                <button class="primary" type="submit">{{ editing.message ? '保存修改' : '发送消息' }}</button>
                <button v-if="editing.message" class="ghost" type="button" @click="resetForm('message')">取消编辑</button>
              </div>
            </form>
            <ul class="list">
              <li v-for="item in lists.messages" :key="item.id">
                <div>
                  <strong>#{{ item.id }} · {{ item.title }}</strong>
                  <p>来自：{{ item.senderId ?? '系统' }} → 发送至：{{ item.receiverId }} · 状态：{{ item.isRead ? '已读' : '未读' }}</p>
                </div>
                <div class="item-actions">
                  <button @click="startEditMessage(item)">编辑</button>
                  <button class="danger" @click="removeMessage(item.id)">删除</button>
                </div>
              </li>
            </ul>
          </article>

          <article class="panel">
            <header>
              <h2>公告中心</h2>
              <p>发布面向指定群体的公告</p>
            </header>
            <form class="form-grid" @submit.prevent="submitAnnouncement">
              <label><span>管理员ID</span><input v-model="forms.announcement.adminId" type="number" :placeholder="adminProfile ? `默认使用 ${adminProfile.id}` : '请输入管理员ID'" /></label>
              <label><span>标题</span><input v-model="forms.announcement.title" type="text" /></label>
              <label class="full"><span>内容</span><textarea v-model="forms.announcement.content"></textarea></label>
              <label><span>目标</span>
                <select v-model="forms.announcement.target">
                  <option value="all">全部用户</option>
                  <option value="student">学生</option>
                  <option value="company">企业</option>
                </select>
              </label>
              <div class="form-actions">
                <button class="primary" type="submit">{{ editing.announcement ? '保存修改' : '发布公告' }}</button>
                <button v-if="editing.announcement" class="ghost" type="button" @click="resetForm('announcement')">取消编辑</button>
              </div>
            </form>
            <ul class="list">
              <li v-for="item in lists.announcements" :key="item.id">
                <div>
                  <strong>#{{ item.id }} · {{ item.title }}</strong>
                  <p>管理员ID：{{ item.adminId }} · 目标：{{ item.target }} · 发布时间：{{ formatDate(item.publishTime) }}</p>
                </div>
                <div class="item-actions">
                  <button @click="startEditAnnouncement(item)">编辑</button>
                  <button class="danger" @click="removeAnnouncement(item.id)">删除</button>
                </div>
              </li>
            </ul>
          </article>

          <article class="panel">
            <header>
              <h2>审核日志</h2>
              <p>记录平台关键操作</p>
            </header>
            <form class="form-grid" @submit.prevent="submitAuditLog">
              <label><span>管理员ID</span><input v-model="forms.auditLog.adminId" type="number" :placeholder="adminProfile ? `默认使用 ${adminProfile.id}` : '请输入管理员ID'" /></label>
              <label><span>操作行为</span><input v-model="forms.auditLog.action" type="text" /></label>
              <label><span>目标类型</span><input v-model="forms.auditLog.targetType" type="text" placeholder="企业/职位/用户" /></label>
              <label><span>目标ID</span><input v-model="forms.auditLog.targetId" type="number" /></label>
              <label class="full"><span>结果描述</span><textarea v-model="forms.auditLog.result"></textarea></label>
              <div class="form-actions">
                <button class="primary" type="submit">{{ editing.auditLog ? '保存修改' : '记录操作' }}</button>
                <button v-if="editing.auditLog" class="ghost" type="button" @click="resetForm('auditLog')">取消编辑</button>
              </div>
            </form>
            <ul class="list">
              <li v-for="item in lists.auditLogs" :key="item.id">
                <div>
                  <strong>#{{ item.id }} · {{ item.action }}</strong>
                  <p>管理员ID：{{ item.adminId }} · 目标：{{ item.targetType ?? '无' }} / {{ item.targetId ?? '-' }} · 时间：{{ formatDate(item.timestamp) }}</p>
                </div>
                <div class="item-actions">
                  <button @click="startEditAuditLog(item)">编辑</button>
                  <button class="danger" @click="removeAuditLog(item.id)">删除</button>
                </div>
              </li>
            </ul>
          </article>
        </section>
      </section>
    </div>
  </div>
  <div v-else class="loading-state">正在加载数据…</div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { del, get, post, put } from '../api/http';

const router = useRouter();
const loading = ref(true);
const error = ref('');
const user = ref(null);
const adminProfile = ref(null);
const actionMessage = reactive({ message: '', type: 'success' });

const lists = reactive({
  users: [],
  admins: [],
  companies: [],
  students: [],
  jobs: [],
  resumes: [],
  applications: [],
  announcements: [],
  auditLogs: [],
  messages: []
});

const adminProfileForm = reactive({
  userId: '',
  name: '',
  level: 1
});

const editing = reactive({
  user: null,
  admin: null,
  company: null,
  student: null,
  job: null,
  resume: null,
  application: null,
  message: null,
  announcement: null,
  auditLog: null
});

function defaultUserForm() {
  return { username: '', password: '', email: '', phone: '', role: 'ADMIN', status: 1 };
}

function defaultAdminForm() {
  return { userId: '', name: '', level: 1 };
}

function defaultCompanyForm() {
  return {
    userId: '',
    companyName: '',
    licenseNumber: '',
    industry: '',
    address: '',
    website: '',
    description: '',
    logo: '',
    auditStatus: 'pending',
    auditReason: ''
  };
}

function defaultStudentForm() {
  return {
    userId: '',
    name: '',
    gender: '',
    school: '',
    major: '',
    grade: '',
    education: '',
    avatar: ''
  };
}

function defaultJobForm() {
  return {
    companyId: '',
    jobTitle: '',
    jobType: '',
    salaryRange: '',
    location: '',
    requirement: '',
    description: '',
    status: 'pending'
  };
}

function defaultResumeForm() {
  return {
    studentId: '',
    title: '',
    educationExperience: '',
    workExperience: '',
    skills: '',
    selfEvaluation: '',
    attachment: ''
  };
}

function defaultApplicationForm() {
  return {
    studentId: '',
    resumeId: '',
    jobId: '',
    companyId: '',
    status: '待查看'
  };
}

function defaultMessageForm() {
  return {
    senderId: '',
    receiverId: '',
    title: '',
    content: '',
    isRead: false
  };
}

function defaultAnnouncementForm() {
  return {
    adminId: '',
    title: '',
    content: '',
    target: 'all'
  };
}

function defaultAuditLogForm() {
  return {
    adminId: '',
    action: '',
    targetType: '',
    targetId: '',
    result: ''
  };
}

const forms = reactive({
  user: defaultUserForm(),
  admin: defaultAdminForm(),
  company: defaultCompanyForm(),
  student: defaultStudentForm(),
  job: defaultJobForm(),
  resume: defaultResumeForm(),
  application: defaultApplicationForm(),
  message: defaultMessageForm(),
  announcement: defaultAnnouncementForm(),
  auditLog: defaultAuditLogForm()
});

const summaryStats = computed(() => ({
  userCount: lists.users.length,
  adminCount: lists.admins.length,
  companyCount: lists.companies.length,
  studentCount: lists.students.length,
  jobCount: lists.jobs.length,
  applicationCount: lists.applications.length
}));

function showMessage(message, type = 'success') {
  actionMessage.message = message;
  actionMessage.type = type;
  if (message) {
    setTimeout(() => {
      actionMessage.message = '';
    }, 3200);
  }
}

function requiredString(value, label) {
  const normalized = (value ?? '').toString().trim();
  if (!normalized) {
    throw new Error(`${label}不能为空`);
  }
  return normalized;
}

function optionalString(value) {
  const normalized = (value ?? '').toString().trim();
  return normalized ? normalized : undefined;
}

function requiredNumber(value, label) {
  const normalized = (value ?? '').toString().trim();
  if (!normalized) {
    throw new Error(`${label}不能为空`);
  }
  const parsed = Number(normalized);
  if (!Number.isFinite(parsed)) {
    throw new Error(`${label}必须为数字`);
  }
  return parsed;
}

function optionalNumber(value) {
  const normalized = (value ?? '').toString().trim();
  if (!normalized) {
    return undefined;
  }
  const parsed = Number(normalized);
  if (!Number.isFinite(parsed)) {
    throw new Error('请输入合法的数字');
  }
  return parsed;
}

function normalizeRole(value) {
  const text = optionalString(value);
  return text ? text.toUpperCase() : undefined;
}

function resetForm(key) {
  editing[key] = null;
  const defaults = {
    user: defaultUserForm,
    admin: defaultAdminForm,
    company: defaultCompanyForm,
    student: defaultStudentForm,
    job: defaultJobForm,
    resume: defaultResumeForm,
    application: defaultApplicationForm,
    message: defaultMessageForm,
    announcement: defaultAnnouncementForm,
    auditLog: defaultAuditLogForm
  };
  Object.assign(forms[key], defaults[key]());
}

function formatDate(value) {
  if (!value) {
    return '—';
  }
  return new Date(value).toLocaleString();
}

function syncAdminProfileForm() {
  if (adminProfile.value) {
    adminProfileForm.userId = adminProfile.value.userId;
    adminProfileForm.name = adminProfile.value.name;
    adminProfileForm.level = adminProfile.value.level ?? 1;
  } else if (user.value) {
    adminProfileForm.userId = user.value.userId;
  }
}

function resetAdminProfileForm() {
  adminProfile.value = JSON.parse(localStorage.getItem('tsukiAdminProfile') ?? 'null');
  syncAdminProfileForm();
}

async function loadData() {
  loading.value = true;
  error.value = '';
  try {
    const stored = localStorage.getItem('tsukiUser');
    if (!stored) {
      router.push({ name: 'login' });
      return;
    }
    const parsed = JSON.parse(stored);
    if (!parsed || parsed.role !== 'ADMIN') {
      throw new Error('请使用系统管理员账号登录');
    }
    user.value = parsed;
    const dashboard = await get(`/dashboard/admin/${parsed.userId}`);
    adminProfile.value = dashboard.profile ?? null;
    if (adminProfile.value) {
      localStorage.setItem('tsukiAdminProfile', JSON.stringify(adminProfile.value));
    }
    lists.users = dashboard.users ?? [];
    lists.admins = dashboard.admins ?? [];
    lists.companies = dashboard.companies ?? [];
    lists.students = dashboard.students ?? [];
    lists.jobs = dashboard.jobs ?? [];
    lists.resumes = dashboard.resumes ?? [];
    lists.applications = dashboard.applications ?? [];
    lists.announcements = dashboard.announcements ?? [];
    lists.auditLogs = dashboard.auditLogs ?? [];
    const allMessages = dashboard.allMessages ?? [];
    if (allMessages.length > 0) {
      lists.messages = allMessages;
    } else {
      const combined = [...(dashboard.inboxMessages ?? []), ...(dashboard.sentMessages ?? [])];
      const unique = new Map();
      combined.forEach((msg) => {
        if (msg && !unique.has(msg.id)) {
          unique.set(msg.id, msg);
        }
      });
      lists.messages = Array.from(unique.values());
    }
    syncAdminProfileForm();
  } catch (err) {
    console.error(err);
    error.value = err.message ?? '数据加载失败';
    if (err.message?.includes('登录')) {
      localStorage.removeItem('tsukiUser');
      router.push({ name: 'login' });
    }
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);

async function saveAdminProfile() {
  try {
    const payload = {
      userId: requiredNumber(adminProfileForm.userId, '用户ID'),
      name: requiredString(adminProfileForm.name, '姓名'),
      level: optionalNumber(adminProfileForm.level) ?? 1
    };
    if (adminProfile.value) {
      await put(`/admins/${adminProfile.value.id}`, {
        name: payload.name,
        level: payload.level
      });
      showMessage('管理员档案已更新');
    } else {
      const created = await post('/admins', payload);
      adminProfile.value = created;
      localStorage.setItem('tsukiAdminProfile', JSON.stringify(created));
      showMessage('管理员档案已创建');
    }
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '管理员档案更新失败', 'error');
  }
}

async function submitUser() {
  try {
    if (editing.user) {
      await put(`/users/${editing.user}`, {
        username: optionalString(forms.user.username),
        password: optionalString(forms.user.password),
        email: optionalString(forms.user.email),
        phone: optionalString(forms.user.phone),
        role: normalizeRole(forms.user.role),
        status: optionalNumber(forms.user.status)
      });
      showMessage('用户信息已更新');
    } else {
      await post('/users', {
        username: requiredString(forms.user.username, '用户名'),
        password: requiredString(forms.user.password, '密码'),
        email: requiredString(forms.user.email, '邮箱'),
        phone: optionalString(forms.user.phone),
        role: normalizeRole(forms.user.role) ?? 'ADMIN',
        status: optionalNumber(forms.user.status) ?? 1
      });
      showMessage('新增用户成功');
    }
    resetForm('user');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '用户操作失败', 'error');
  }
}

function startEditUser(item) {
  editing.user = item.id;
  Object.assign(forms.user, {
    username: item.username ?? '',
    password: '',
    email: item.email ?? '',
    phone: item.phone ?? '',
    role: item.role ?? 'ADMIN',
    status: item.status ?? 1
  });
}

async function removeUser(id) {
  if (!window.confirm('确定要删除该用户吗？')) {
    return;
  }
  try {
    await del(`/users/${id}`);
    showMessage('用户已删除');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '删除用户失败', 'error');
  }
}

async function submitAdmin() {
  try {
    if (editing.admin) {
      await put(`/admins/${editing.admin}`, {
        name: optionalString(forms.admin.name),
        level: optionalNumber(forms.admin.level)
      });
      showMessage('管理员信息已更新');
    } else {
      await post('/admins', {
        userId: requiredNumber(forms.admin.userId, '用户ID'),
        name: requiredString(forms.admin.name, '姓名'),
        level: optionalNumber(forms.admin.level) ?? 1
      });
      showMessage('新增管理员成功');
    }
    resetForm('admin');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '管理员操作失败', 'error');
  }
}

function startEditAdmin(item) {
  editing.admin = item.id;
  Object.assign(forms.admin, {
    userId: item.userId ?? '',
    name: item.name ?? '',
    level: item.level ?? 1
  });
}

async function removeAdmin(id) {
  if (!window.confirm('确定要删除该管理员吗？')) {
    return;
  }
  try {
    await del(`/admins/${id}`);
    showMessage('管理员已删除');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '删除管理员失败', 'error');
  }
}

async function submitCompany() {
  try {
    const payload = {
      companyName: optionalString(forms.company.companyName),
      licenseNumber: optionalString(forms.company.licenseNumber),
      industry: optionalString(forms.company.industry),
      address: optionalString(forms.company.address),
      website: optionalString(forms.company.website),
      description: optionalString(forms.company.description),
      logo: optionalString(forms.company.logo),
      auditStatus: optionalString(forms.company.auditStatus),
      auditReason: optionalString(forms.company.auditReason)
    };
    if (editing.company) {
      await put(`/companies/${editing.company}`, payload);
      showMessage('企业信息已更新');
    } else {
      await post('/companies', {
        userId: requiredNumber(forms.company.userId, '用户ID'),
        companyName: requiredString(forms.company.companyName, '企业名称'),
        licenseNumber: payload.licenseNumber,
        industry: payload.industry,
        address: payload.address,
        website: payload.website,
        description: payload.description,
        logo: payload.logo,
        auditStatus: payload.auditStatus ?? 'pending',
        auditReason: payload.auditReason
      });
      showMessage('新增企业成功');
    }
    resetForm('company');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '企业操作失败', 'error');
  }
}

function startEditCompany(item) {
  editing.company = item.id;
  Object.assign(forms.company, {
    userId: item.userId ?? '',
    companyName: item.companyName ?? '',
    licenseNumber: item.licenseNumber ?? '',
    industry: item.industry ?? '',
    address: item.address ?? '',
    website: item.website ?? '',
    description: item.description ?? '',
    logo: item.logo ?? '',
    auditStatus: item.auditStatus ?? 'pending',
    auditReason: item.auditReason ?? ''
  });
}

async function removeCompany(id) {
  if (!window.confirm('确定要删除该企业吗？')) {
    return;
  }
  try {
    await del(`/companies/${id}`);
    showMessage('企业已删除');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '删除企业失败', 'error');
  }
}

async function submitStudent() {
  try {
    if (editing.student) {
      await put(`/students/${editing.student}`, {
        name: optionalString(forms.student.name),
        gender: optionalString(forms.student.gender),
        school: optionalString(forms.student.school),
        major: optionalString(forms.student.major),
        grade: optionalString(forms.student.grade),
        education: optionalString(forms.student.education),
        avatar: optionalString(forms.student.avatar)
      });
      showMessage('学生信息已更新');
    } else {
      await post('/students', {
        userId: requiredNumber(forms.student.userId, '用户ID'),
        name: requiredString(forms.student.name, '姓名'),
        gender: optionalString(forms.student.gender),
        school: optionalString(forms.student.school),
        major: optionalString(forms.student.major),
        grade: optionalString(forms.student.grade),
        education: optionalString(forms.student.education),
        avatar: optionalString(forms.student.avatar)
      });
      showMessage('新增学生成功');
    }
    resetForm('student');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '学生操作失败', 'error');
  }
}

function startEditStudent(item) {
  editing.student = item.id;
  Object.assign(forms.student, {
    userId: item.userId ?? '',
    name: item.name ?? '',
    gender: item.gender ?? '',
    school: item.school ?? '',
    major: item.major ?? '',
    grade: item.grade ?? '',
    education: item.education ?? '',
    avatar: item.avatar ?? ''
  });
}

async function removeStudent(id) {
  if (!window.confirm('确定要删除该学生吗？')) {
    return;
  }
  try {
    await del(`/students/${id}`);
    showMessage('学生已删除');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '删除学生失败', 'error');
  }
}

async function submitJob() {
  try {
    if (editing.job) {
      await put(`/jobs/${editing.job}`, {
        jobTitle: optionalString(forms.job.jobTitle),
        jobType: optionalString(forms.job.jobType),
        salaryRange: optionalString(forms.job.salaryRange),
        location: optionalString(forms.job.location),
        requirement: optionalString(forms.job.requirement),
        description: optionalString(forms.job.description),
        status: optionalString(forms.job.status)
      });
      showMessage('职位信息已更新');
    } else {
      await post('/jobs', {
        companyId: requiredNumber(forms.job.companyId, '企业ID'),
        jobTitle: requiredString(forms.job.jobTitle, '职位名称'),
        jobType: optionalString(forms.job.jobType),
        salaryRange: optionalString(forms.job.salaryRange),
        location: optionalString(forms.job.location),
        requirement: optionalString(forms.job.requirement),
        description: optionalString(forms.job.description),
        status: optionalString(forms.job.status) ?? 'pending'
      });
      showMessage('新增职位成功');
    }
    resetForm('job');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '职位操作失败', 'error');
  }
}

function startEditJob(item) {
  editing.job = item.id;
  Object.assign(forms.job, {
    companyId: item.companyId ?? '',
    jobTitle: item.jobTitle ?? '',
    jobType: item.jobType ?? '',
    salaryRange: item.salaryRange ?? '',
    location: item.location ?? '',
    requirement: item.requirement ?? '',
    description: item.description ?? '',
    status: item.status ?? 'pending'
  });
}

async function removeJob(id) {
  if (!window.confirm('确定要删除该职位吗？')) {
    return;
  }
  try {
    await del(`/jobs/${id}`);
    showMessage('职位已删除');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '删除职位失败', 'error');
  }
}

async function submitResume() {
  try {
    if (editing.resume) {
      await put(`/resumes/${editing.resume}`, {
        title: optionalString(forms.resume.title),
        educationExperience: optionalString(forms.resume.educationExperience),
        workExperience: optionalString(forms.resume.workExperience),
        skills: optionalString(forms.resume.skills),
        selfEvaluation: optionalString(forms.resume.selfEvaluation),
        attachment: optionalString(forms.resume.attachment)
      });
      showMessage('简历信息已更新');
    } else {
      await post('/resumes', {
        studentId: requiredNumber(forms.resume.studentId, '学生ID'),
        title: requiredString(forms.resume.title, '简历标题'),
        educationExperience: optionalString(forms.resume.educationExperience),
        workExperience: optionalString(forms.resume.workExperience),
        skills: optionalString(forms.resume.skills),
        selfEvaluation: optionalString(forms.resume.selfEvaluation),
        attachment: optionalString(forms.resume.attachment)
      });
      showMessage('新增简历成功');
    }
    resetForm('resume');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '简历操作失败', 'error');
  }
}

function startEditResume(item) {
  editing.resume = item.id;
  Object.assign(forms.resume, {
    studentId: item.studentId ?? '',
    title: item.title ?? '',
    educationExperience: item.educationExperience ?? '',
    workExperience: item.workExperience ?? '',
    skills: item.skills ?? '',
    selfEvaluation: item.selfEvaluation ?? '',
    attachment: item.attachment ?? ''
  });
}

async function removeResume(id) {
  if (!window.confirm('确定要删除该简历吗？')) {
    return;
  }
  try {
    await del(`/resumes/${id}`);
    showMessage('简历已删除');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '删除简历失败', 'error');
  }
}

async function submitApplication() {
  try {
    if (editing.application) {
      await put(`/applications/${editing.application}`, {
        status: optionalString(forms.application.status)
      });
      showMessage('投递记录已更新');
    } else {
      await post('/applications', {
        studentId: requiredNumber(forms.application.studentId, '学生ID'),
        resumeId: requiredNumber(forms.application.resumeId, '简历ID'),
        jobId: requiredNumber(forms.application.jobId, '职位ID'),
        companyId: requiredNumber(forms.application.companyId, '企业ID'),
        status: optionalString(forms.application.status) ?? '待查看'
      });
      showMessage('新增投递记录成功');
    }
    resetForm('application');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '投递操作失败', 'error');
  }
}

function startEditApplication(item) {
  editing.application = item.id;
  Object.assign(forms.application, {
    studentId: item.studentId ?? '',
    resumeId: item.resumeId ?? '',
    jobId: item.jobId ?? '',
    companyId: item.companyId ?? '',
    status: item.status ?? '待查看'
  });
}

async function removeApplication(id) {
  if (!window.confirm('确定要删除该投递记录吗？')) {
    return;
  }
  try {
    await del(`/applications/${id}`);
    showMessage('投递记录已删除');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '删除投递记录失败', 'error');
  }
}

async function submitMessage() {
  try {
    if (editing.message) {
      await put(`/messages/${editing.message}`, {
        title: optionalString(forms.message.title),
        content: optionalString(forms.message.content),
        isRead: forms.message.isRead
      });
      showMessage('消息已更新');
    } else {
      await post('/messages', {
        senderId: optionalNumber(forms.message.senderId),
        receiverId: requiredNumber(forms.message.receiverId, '接收者ID'),
        title: requiredString(forms.message.title, '消息标题'),
        content: requiredString(forms.message.content, '消息内容'),
        isRead: forms.message.isRead
      });
      showMessage('消息发送成功');
    }
    resetForm('message');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '消息操作失败', 'error');
  }
}

function startEditMessage(item) {
  editing.message = item.id;
  Object.assign(forms.message, {
    senderId: item.senderId ?? '',
    receiverId: item.receiverId ?? '',
    title: item.title ?? '',
    content: item.content ?? '',
    isRead: !!item.isRead
  });
}

async function removeMessage(id) {
  if (!window.confirm('确定要删除该消息吗？')) {
    return;
  }
  try {
    await del(`/messages/${id}`);
    showMessage('消息已删除');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '删除消息失败', 'error');
  }
}

async function submitAnnouncement() {
  try {
    const adminId = adminProfile.value?.id ?? optionalNumber(forms.announcement.adminId);
    if (!adminId) {
      throw new Error('请先填写管理员ID或创建管理员档案');
    }
    if (editing.announcement) {
      await put(`/announcements/${editing.announcement}`, {
        title: optionalString(forms.announcement.title),
        content: optionalString(forms.announcement.content),
        target: optionalString(forms.announcement.target)
      });
      showMessage('公告已更新');
    } else {
      await post('/announcements', {
        adminId,
        title: requiredString(forms.announcement.title, '公告标题'),
        content: requiredString(forms.announcement.content, '公告内容'),
        target: optionalString(forms.announcement.target) ?? 'all'
      });
      showMessage('公告发布成功');
    }
    resetForm('announcement');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '公告操作失败', 'error');
  }
}

function startEditAnnouncement(item) {
  editing.announcement = item.id;
  Object.assign(forms.announcement, {
    adminId: item.adminId ?? '',
    title: item.title ?? '',
    content: item.content ?? '',
    target: item.target ?? 'all'
  });
}

async function removeAnnouncement(id) {
  if (!window.confirm('确定要删除该公告吗？')) {
    return;
  }
  try {
    await del(`/announcements/${id}`);
    showMessage('公告已删除');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '删除公告失败', 'error');
  }
}

async function submitAuditLog() {
  try {
    const adminId = adminProfile.value?.id ?? optionalNumber(forms.auditLog.adminId);
    if (!adminId) {
      throw new Error('请先填写管理员ID或创建管理员档案');
    }
    if (editing.auditLog) {
      await put(`/audit-logs/${editing.auditLog}`, {
        action: optionalString(forms.auditLog.action),
        targetType: optionalString(forms.auditLog.targetType),
        targetId: optionalNumber(forms.auditLog.targetId),
        result: optionalString(forms.auditLog.result)
      });
      showMessage('审核日志已更新');
    } else {
      await post('/audit-logs', {
        adminId,
        action: requiredString(forms.auditLog.action, '操作行为'),
        targetType: optionalString(forms.auditLog.targetType),
        targetId: optionalNumber(forms.auditLog.targetId),
        result: optionalString(forms.auditLog.result)
      });
      showMessage('审核日志已记录');
    }
    resetForm('auditLog');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '审核日志操作失败', 'error');
  }
}

function startEditAuditLog(item) {
  editing.auditLog = item.id;
  Object.assign(forms.auditLog, {
    adminId: item.adminId ?? '',
    action: item.action ?? '',
    targetType: item.targetType ?? '',
    targetId: item.targetId ?? '',
    result: item.result ?? ''
  });
}

async function removeAuditLog(id) {
  if (!window.confirm('确定要删除该日志吗？')) {
    return;
  }
  try {
    await del(`/audit-logs/${id}`);
    showMessage('审核日志已删除');
    await loadData();
  } catch (error) {
    showMessage(error.message ?? '删除审核日志失败', 'error');
  }
}

function logout() {
  localStorage.removeItem('tsukiUser');
  localStorage.removeItem('tsukiAdminProfile');
  router.push({ name: 'login' });
}
</script>

<style scoped>
.dashboard {
  padding: 32px 0 64px;
}

.loading-state {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #334155;
}

.container {
  width: min(1200px, 94vw);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 28px;
  padding: 28px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.2);
}

.hero h1 {
  margin: 0;
  font-size: 30px;
  color: #0f172a;
}

.hero p {
  margin: 8px 0 0;
  color: #475569;
}

.ghost {
  border: 1px solid rgba(59, 130, 246, 0.4);
  padding: 10px 20px;
  border-radius: 999px;
  background: transparent;
  cursor: pointer;
  color: #1d4ed8;
  font-weight: 600;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.error-banner {
  padding: 14px 18px;
  background: #fee2e2;
  color: #b91c1c;
  border-radius: 18px;
}

.toast {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: inset 0 0 0 1px rgba(30, 64, 175, 0.18);
}

.toast.success {
  background: #dcfce7;
  color: #15803d;
}

.toast.error {
  background: #fee2e2;
  color: #b91c1c;
}

.summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.summary article {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 20px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.18);
}

.summary h3 {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
}

.summary p {
  margin: 8px 0 0;
  font-size: 24px;
  color: #2563eb;
  font-weight: 700;
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

.panel {
  background: rgba(255, 255, 255, 0.96);
  border-radius: 24px;
  padding: 22px;
  box-shadow: 0 20px 44px rgba(15, 23, 42, 0.16);
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panel header h2 {
  margin: 0;
  font-size: 20px;
  color: #1f2937;
}

.panel header p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.form-grid label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}

.form-grid input,
.form-grid textarea,
.form-grid select {
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.55);
  font-family: inherit;
  font-size: 14px;
}

textarea {
  resize: vertical;
  min-height: 80px;
}

label.full {
  grid-column: 1 / -1;
}

label.checkbox {
  flex-direction: row;
  align-items: center;
  gap: 10px;
}

.form-actions {
  grid-column: 1 / -1;
  display: flex;
  gap: 12px;
}

button.primary {
  padding: 10px 18px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

button.danger {
  background: linear-gradient(135deg, #f87171, #ef4444);
  border: none;
  color: #fff;
  padding: 6px 12px;
  border-radius: 999px;
  cursor: pointer;
}

.item-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0;
  list-style: none;
}

.list li {
  background: rgba(241, 245, 249, 0.8);
  border-radius: 18px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list strong {
  color: #0f172a;
}

.list p {
  margin: 0;
  color: #475569;
  font-size: 13px;
}

.muted {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 768px) {
  .hero {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }
}
</style>
