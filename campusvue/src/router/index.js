import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import AdminDashboard from '../views/AdminDashboard.vue';

const routes = [
  {
    path: '/',
    name: 'login',
    component: LoginView
  },
  {
    path: '/admin',
    name: 'admin-dashboard',
    component: AdminDashboard,
    meta: {
      requiresAdmin: true
    }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  if (!to.meta.requiresAdmin) {
    next();
    return;
  }
  try {
    const raw = localStorage.getItem('tsukiUser');
    if (!raw) {
      next({ name: 'login' });
      return;
    }
    const user = JSON.parse(raw);
    if (user && user.role === 'ADMIN') {
      next();
      return;
    }
  } catch (error) {
    console.warn('无法解析登录信息', error);
  }
  next({ name: 'login' });
});

export default router;
