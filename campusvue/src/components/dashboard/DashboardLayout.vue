<template>
  <div class="dashboard-layout">
    <header class="dashboard-layout__header">
      <div>
        <h1>{{ title }}</h1>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>
      <button class="outline" type="button" @click="$emit('logout')">退出登录</button>
    </header>
    <div class="dashboard-layout__body">
      <nav class="dashboard-layout__sidebar">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="sidebar-link"
          active-class="is-active"
        >
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
      <main class="dashboard-layout__content">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router';

defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  navItems: {
    type: Array,
    default: () => []
  }
});

defineEmits(['logout']);
</script>

<style scoped>
.dashboard-layout {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px;
  max-width: 1280px;
  margin: 0 auto;
}

.dashboard-layout__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dashboard-layout__header h1 {
  margin: 0;
  font-size: 28px;
  color: #0f172a;
}

.dashboard-layout__header p {
  margin: 6px 0 0;
  color: #475569;
}

.dashboard-layout__body {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 24px;
  align-items: start;
}

.dashboard-layout__sidebar {
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #0f172a;
  border-radius: 16px;
  padding: 20px 16px;
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.15);
}

.sidebar-link {
  display: block;
  color: #e2e8f0;
  text-decoration: none;
  padding: 12px 16px;
  border-radius: 12px;
  font-weight: 600;
  transition: background 0.2s ease, color 0.2s ease;
}

.sidebar-link:hover {
  background: rgba(226, 232, 240, 0.12);
}

.sidebar-link.is-active {
  background: #2563eb;
  color: #fff;
}

.dashboard-layout__content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.outline {
  background: transparent;
  border: 1px solid #2563eb;
  color: #2563eb;
  padding: 8px 16px;
  border-radius: 10px;
  cursor: pointer;
}

:deep(.card) {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

:deep(.card__title) {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

:deep(.billing-summary) {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 14px;
  color: #0f172a;
}

:deep(.billing-summary span) {
  background: #f8fafc;
  border-radius: 999px;
  padding: 6px 12px;
}

:deep(.summary-grid) {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 16px;
}

:deep(.summary-item) {
  background: #eff6ff;
  padding: 16px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #1e3a8a;
}

:deep(.summary-item strong) {
  font-size: 20px;
}

:deep(.table) {
  width: 100%;
  border-collapse: collapse;
}

:deep(.table th),
:deep(.table td) {
  text-align: left;
  padding: 8px 12px;
  border-bottom: 1px solid #e5e7eb;
}

:deep(.form-grid) {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

:deep(.form-grid label) {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
}

:deep(.form-grid input),
:deep(.form-grid textarea),
:deep(.form-grid select),
:deep(.table select) {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px;
  font-size: 14px;
}

:deep(.form-grid textarea) {
  min-height: 100px;
  resize: vertical;
}

:deep(.form-grid .full) {
  grid-column: 1 / -1;
}

:deep(.actions) {
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.list) {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

:deep(.list__item) {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

:deep(.list__actions) {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

:deep(.muted) {
  color: #6b7280;
}

:deep(.primary) {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border: none;
  color: #fff;
  padding: 10px 18px;
  border-radius: 10px;
  cursor: pointer;
}

:deep(.danger) {
  background: transparent;
  border: 1px solid #dc2626;
  color: #dc2626;
  padding: 8px 16px;
  border-radius: 10px;
  cursor: pointer;
}

:deep(.outline) {
  background: transparent;
  border: 1px solid #2563eb;
  color: #2563eb;
  padding: 8px 16px;
  border-radius: 10px;
  cursor: pointer;
}

:deep(.feedback) {
  text-align: center;
  padding: 12px;
  border-radius: 12px;
}

:deep(.feedback.success) {
  background: #dcfce7;
  color: #15803d;
}

:deep(.feedback.error) {
  background: #fee2e2;
  color: #b91c1c;
}

@media (max-width: 1024px) {
  .dashboard-layout__body {
    grid-template-columns: 1fr;
  }

  .dashboard-layout__sidebar {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 8px;
  }

  .sidebar-link {
    flex: 1 1 calc(50% - 8px);
    text-align: center;
  }
}
</style>
