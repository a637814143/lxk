import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import StudentDashboard from '../views/StudentDashboard.vue';
import CompanyDashboard from '../views/company/CompanyDashboard.vue';
import CompanyProfileView from '../views/company/CompanyProfileView.vue';
import CompanyJobsView from '../views/company/CompanyJobsView.vue';
import CompanyFinanceView from '../views/company/CompanyFinanceView.vue';
import CompanyApplicationsView from '../views/company/CompanyApplicationsView.vue';
import CompanyDiscussionsView from '../views/company/CompanyDiscussionsView.vue';
import CompanyAnnouncementsView from '../views/company/CompanyAnnouncementsView.vue';
import AdminDashboard from '../views/admin/AdminDashboard.vue';
import AdminOverviewView from '../views/admin/AdminOverviewView.vue';
import AdminCompaniesView from '../views/admin/AdminCompaniesView.vue';
import AdminJobsView from '../views/admin/AdminJobsView.vue';
import AdminDiscussionsView from '../views/admin/AdminDiscussionsView.vue';
import AdminUsersView from '../views/admin/AdminUsersView.vue';
import AdminFinanceView from '../views/admin/AdminFinanceView.vue';
import AdminAnnouncementsView from '../views/admin/AdminAnnouncementsView.vue';
import AdminInvitesView from '../views/admin/AdminInvitesView.vue';
import AdminBackupsView from '../views/admin/AdminBackupsView.vue';
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
    name: 'student-dashboard',
    component: StudentDashboard,
    meta: {
      requiresRole: 'STUDENT'
    }
  },
  {
    path: '/company',
    name: 'company-dashboard',
    component: CompanyDashboard,
    meta: {
      requiresRole: 'COMPANY'
    },
    redirect: { name: 'company-profile' },
    children: [
      {
        path: 'profile',
        name: 'company-profile',
        component: CompanyProfileView,
        meta: { requiresRole: 'COMPANY' }
      },
      {
        path: 'jobs',
        name: 'company-jobs',
        component: CompanyJobsView,
        meta: { requiresRole: 'COMPANY' }
      },
      {
        path: 'finance',
        name: 'company-finance',
        component: CompanyFinanceView,
        meta: { requiresRole: 'COMPANY' }
      },
      {
        path: 'applications',
        name: 'company-applications',
        component: CompanyApplicationsView,
        meta: { requiresRole: 'COMPANY' }
      },
      {
        path: 'discussions',
        name: 'company-discussions',
        component: CompanyDiscussionsView,
        meta: { requiresRole: 'COMPANY' }
      },
      {
        path: 'announcements',
        name: 'company-announcements',
        component: CompanyAnnouncementsView,
        meta: { requiresRole: 'COMPANY' }
      }
    ]
  },
  {
    path: '/admin',
    name: 'admin-dashboard',
    component: AdminDashboard,
    meta: {
      requiresRole: 'ADMIN'
    },
    redirect: { name: 'admin-overview' },
    children: [
      {
        path: 'overview',
        name: 'admin-overview',
        component: AdminOverviewView,
        meta: { requiresRole: 'ADMIN' }
      },
      {
        path: 'companies',
        name: 'admin-companies',
        component: AdminCompaniesView,
        meta: { requiresRole: 'ADMIN' }
      },
      {
        path: 'jobs',
        name: 'admin-jobs',
        component: AdminJobsView,
        meta: { requiresRole: 'ADMIN' }
      },
      {
        path: 'discussions',
        name: 'admin-discussions',
        component: AdminDiscussionsView,
        meta: { requiresRole: 'ADMIN' }
      },
      {
        path: 'users',
        name: 'admin-users',
        component: AdminUsersView,
        meta: { requiresRole: 'ADMIN' }
      },
      {
        path: 'finance',
        name: 'admin-finance',
        component: AdminFinanceView,
        meta: { requiresRole: 'ADMIN' }
      },
      {
        path: 'announcements',
        name: 'admin-announcements',
        component: AdminAnnouncementsView,
        meta: { requiresRole: 'ADMIN' }
      },
      {
        path: 'invites',
        name: 'admin-invites',
        component: AdminInvitesView,
        meta: { requiresRole: 'ADMIN' }
      },
      {
        path: 'backups',
        name: 'admin-backups',
        component: AdminBackupsView,
        meta: { requiresRole: 'ADMIN' }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const requiredRole = to.meta.requiresRole;
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
