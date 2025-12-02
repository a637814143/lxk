<template>
  <section class="overview">
    <header class="overview-head">
      <div>
        <p class="eyebrow">数据分析</p>
        <h2>平台概览</h2>
        <p class="muted">实时掌握学生、企业、职位、投递与内容审核的核心态势。</p>
      </div>
      <button class="outline" @click="loadSummary">刷新</button>
    </header>

    <div v-if="summary" class="summary-grid">
      <div v-for="item in kpiCards" :key="item.label" class="summary-card">
        <p class="label">{{ item.label }}</p>
        <strong>{{ item.value }}</strong>
        <small v-if="item.note">{{ item.note }}</small>
      </div>
    </div>

    <section v-if="summary" class="chart-grid">
      <div class="chart-card">
        <div class="card__title">
          <h3>投递状态分布</h3>
          <span class="muted">查看当前投递审核与处理情况</span>
        </div>
        <div class="bar-chart">
          <div
            v-for="item in statusBars"
            :key="item.key"
            class="bar"
            :style="{ height: item.height + '%'}"
          >
            <span class="bar-value">{{ item.value }}</span>
            <span class="bar-label">{{ item.key }}</span>
          </div>
          <p v-if="!statusBars.length" class="muted">暂无投递数据</p>
        </div>
      </div>

      <div class="chart-card">
        <div class="card__title">
          <h3>平台核心指标</h3>
          <span class="muted">学生 / 企业 / 职位 / 投递实时对比</span>
        </div>
        <div class="spark-grid">
          <div v-for="item in sparkBars" :key="item.label" class="spark-row">
            <div class="spark-label">
              <span class="dot" :style="{ background: item.color }"></span>
              <span>{{ item.label }}</span>
            </div>
            <div class="spark-bar">
              <div class="spark-bar__fill" :style="{ width: item.percent + '%' , background: item.color}"></div>
            </div>
            <span class="spark-value">{{ item.value }}</span>
          </div>
        </div>
      </div>
    </section>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { get } from '../../api/http';

const summary = ref(null);
const feedback = reactive({ message: '', type: 'info' });

onMounted(() => {
  loadSummary();
});

async function loadSummary() {
  try {
    summary.value = await get('/portal/admin/summary');
    feedback.message = '';
  } catch (error) {
    showFeedback(error.message ?? '加载概览失败', 'error');
  }
}

const kpiCards = computed(() => {
  if (!summary.value) return [];
  return [
    { label: '学生人数', value: summary.value.totalStudents },
    { label: '企业数量', value: summary.value.totalCompanies, note: `待审 ${summary.value.pendingCompanies}` },
    { label: '职位总数', value: summary.value.totalJobs, note: `已发布 ${summary.value.approvedJobs}` },
    { label: '投递总量', value: summary.value.totalApplications },
    { label: '待审讨论', value: summary.value.pendingDiscussions },
    { label: '待审评论', value: summary.value.pendingComments },
    { label: '未读消息', value: summary.value.unreadMessages }
  ];
});

const statusBars = computed(() => {
  if (!summary.value || !summary.value.statusBreakdown) return [];
  const entries = Object.entries(summary.value.statusBreakdown);
  const max = Math.max(...entries.map(([, v]) => Number(v) || 0), 1);
  return entries.map(([key, value], idx) => ({
    key,
    value,
    height: Math.round(((Number(value) || 0) / max) * 100),
    color: barColors[idx % barColors.length]
  }));
});

const sparkBars = computed(() => {
  if (!summary.value) return [];
  const metrics = [
    { label: '学生', value: Number(summary.value.totalStudents) || 0 },
    { label: '企业', value: Number(summary.value.totalCompanies) || 0 },
    { label: '职位', value: Number(summary.value.totalJobs) || 0 },
    { label: '投递', value: Number(summary.value.totalApplications) || 0 }
  ];
  const max = Math.max(...metrics.map(m => m.value), 1);
  return metrics.map((m, idx) => ({
    ...m,
    color: barColors[idx % barColors.length],
    percent: Math.min(100, Math.round((m.value / max) * 100))
  }));
});

const barColors = ['#2563eb', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6', '#0ea5e9'];

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}
</script>

<style scoped>
.overview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overview-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.eyebrow {
  margin: 0 0 4px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #2563eb;
  font-weight: 700;
}

.summary-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.summary-card {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid var(--border-subtle);
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: var(--shadow-soft);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-card .label {
  margin: 0;
  font-size: 14px;
  color: #475569;
}

.summary-card strong {
  font-size: 22px;
  color: var(--color-text-main);
}

.summary-card small {
  color: var(--color-text-muted);
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.chart-card {
  background: rgba(255, 255, 255, 0.96);
  border-radius: 16px;
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-soft);
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bar-chart {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(60px, 1fr));
  align-items: end;
  gap: 12px;
  min-height: 180px;
}

.bar {
  position: relative;
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.8), rgba(37, 99, 235, 0.4));
  border-radius: 10px 10px 4px 4px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 6px;
  transition: transform 0.15s ease;
}

.bar:hover {
  transform: translateY(-4px);
}

.bar-value {
  position: absolute;
  top: -22px;
  font-size: 12px;
  color: #0f172a;
}

.bar-label {
  position: absolute;
  bottom: -20px;
  font-size: 12px;
  color: #475569;
  text-align: center;
  width: 100%;
}

.spark-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.spark-row {
  display: grid;
  grid-template-columns: 1fr 3fr auto;
  align-items: center;
  gap: 10px;
}

.spark-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #0f172a;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  display: inline-block;
}

.spark-bar {
  background: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
  height: 10px;
}

.spark-bar__fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.2s ease;
}

.spark-value {
  font-variant-numeric: tabular-nums;
  color: #0f172a;
  font-weight: 600;
}

.feedback {
  padding: 10px 14px;
  border-radius: 12px;
  text-align: center;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}

@media (max-width: 768px) {
  .overview-head {
    flex-direction: column;
  }
}
</style>
