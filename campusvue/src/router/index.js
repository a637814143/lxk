import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import AdminDashboard from '../views/AdminDashboard.vue';
import StudentDashboard from '../views/StudentDashboard.vue';
import CompanyDashboard from '../views/CompanyDashboard.vue';
import { getAuthInfo } from '../api/http';

const routes = [
  {
    path: '/',
    name: 'login',
    component: LoginView
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
    }
  },
  {
    path: '/admin',
    name: 'admin-dashboard',
    component: AdminDashboard,
    meta: {
      requiresRole: 'ADMIN'
    }
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
