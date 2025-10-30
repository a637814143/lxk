import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import AdminDashboard from '../views/AdminDashboard.vue';
import AdminOverview from '../views/admin/AdminOverview.vue';
import AdminCompanyReview from '../views/admin/AdminCompanyReview.vue';
import AdminJobReview from '../views/admin/AdminJobReview.vue';
import AdminDiscussionReview from '../views/admin/AdminDiscussionReview.vue';
import AdminUsers from '../views/admin/AdminUsers.vue';
import AdminFinance from '../views/admin/AdminFinance.vue';
import AdminAnnouncements from '../views/admin/AdminAnnouncements.vue';
import AdminBackups from '../views/admin/AdminBackups.vue';
import StudentDashboard from '../views/StudentDashboard.vue';
import StudentProfile from '../views/student/StudentProfile.vue';
import StudentResumes from '../views/student/StudentResumes.vue';
import StudentJobs from '../views/student/StudentJobs.vue';
import StudentApplications from '../views/student/StudentApplications.vue';
import StudentMessages from '../views/student/StudentMessages.vue';
import StudentAnnouncements from '../views/student/StudentAnnouncements.vue';
import StudentDiscussions from '../views/student/StudentDiscussions.vue';
import CompanyDashboard from '../views/CompanyDashboard.vue';
import CompanyProfile from '../views/company/CompanyProfile.vue';
import CompanyJobs from '../views/company/CompanyJobs.vue';
import CompanyFinance from '../views/company/CompanyFinance.vue';
import CompanyApplications from '../views/company/CompanyApplications.vue';
import CompanyDiscussions from '../views/company/CompanyDiscussions.vue';
import CompanyAnnouncements from '../views/company/CompanyAnnouncements.vue';
import { getAuthInfo } from '../api/http';

const routes = [
  {
    path: '/',
    name: 'login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView
  },
  {
    path: '/student',
    component: StudentDashboard,
    meta: {
      requiresRole: 'STUDENT'
    },
    children: [
      { path: '', redirect: '/student/profile' },
      { path: 'profile', name: 'student-profile', component: StudentProfile },
      { path: 'resumes', name: 'student-resumes', component: StudentResumes },
      { path: 'jobs', name: 'student-jobs', component: StudentJobs },
      { path: 'applications', name: 'student-applications', component: StudentApplications },
      { path: 'messages', name: 'student-messages', component: StudentMessages },
      { path: 'announcements', name: 'student-announcements', component: StudentAnnouncements },
      { path: 'discussions', name: 'student-discussions', component: StudentDiscussions }
    ]
  },
  {
    path: '/company',
    component: CompanyDashboard,
    meta: {
      requiresRole: 'COMPANY'
    },
    children: [
      { path: '', redirect: '/company/profile' },
      { path: 'profile', name: 'company-profile', component: CompanyProfile },
      { path: 'jobs', name: 'company-jobs', component: CompanyJobs },
      { path: 'finance', name: 'company-finance', component: CompanyFinance },
      { path: 'applications', name: 'company-applications', component: CompanyApplications },
      { path: 'discussions', name: 'company-discussions', component: CompanyDiscussions },
      { path: 'announcements', name: 'company-announcements', component: CompanyAnnouncements }
    ]
  },
  {
    path: '/admin',
    component: AdminDashboard,
    meta: {
      requiresRole: 'ADMIN'
    },
    children: [
      { path: '', redirect: '/admin/overview' },
      { path: 'overview', name: 'admin-overview', component: AdminOverview },
      { path: 'company-review', name: 'admin-company-review', component: AdminCompanyReview },
      { path: 'job-review', name: 'admin-job-review', component: AdminJobReview },
      { path: 'discussion-review', name: 'admin-discussion-review', component: AdminDiscussionReview },
      { path: 'users', name: 'admin-users', component: AdminUsers },
      { path: 'finance', name: 'admin-finance', component: AdminFinance },
      { path: 'announcements', name: 'admin-announcements', component: AdminAnnouncements },
      { path: 'backups', name: 'admin-backups', component: AdminBackups }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const requiredRole = to.matched
    .slice()
    .reverse()
    .find(record => record.meta && record.meta.requiresRole)?.meta.requiresRole;
  if (!requiredRole) {
    next();
    return;
  }
  const auth = getAuthInfo();
  if (auth && auth.role === requiredRole) {
    next();
    return;
  }
  next({ name: 'login', query: { redirect: to.fullPath } });
});

export default router;
