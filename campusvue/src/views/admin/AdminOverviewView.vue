<template>
  <section class="card">
    <div class="card__title">
      <h2>平台概览</h2>
      <button class="outline" @click="loadSummary">刷新</button>
    </div>

    <div v-if="summary" class="summary-grid">
      <div class="summary-item"><span>学生人数</span><strong>{{ summary.totalStudents }}</strong></div>
      <div class="summary-item"><span>企业数量</span><strong>{{ summary.totalCompanies }}</strong></div>
      <div class="summary-item"><span>待审企业</span><strong>{{ summary.pendingCompanies }}</strong></div>
      <div class="summary-item"><span>职位总数</span><strong>{{ summary.totalJobs }}</strong></div>
      <div class="summary-item"><span>已发布职位</span><strong>{{ summary.approvedJobs }}</strong></div>
      <div class="summary-item"><span>待审核职位</span><strong>{{ summary.pendingJobs }}</strong></div>
      <div class="summary-item"><span>投递总量</span><strong>{{ summary.totalApplications }}</strong></div>
      <div class="summary-item"><span>管理员未读消息</span><strong>{{ summary.unreadMessages }}</strong></div>
    </div>

    <div v-if="summary" class="status-section">
      <h3>投递状态分析</h3>
      <div class="status-charts">
        <div class="chart-card">
          <h4>状态占比（饼图）</h4>
          <div ref="pieChartRef" class="chart-container"></div>
        </div>
        <div class="chart-card">
          <h4>状态分布（折线图）</h4>
          <div ref="lineChartRef" class="chart-container"></div>
        </div>
      </div>
    </div>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { get } from '../../api/http';

const summary = ref(null);
const feedback = reactive({ message: '', type: 'info' });

const pieChartRef = ref(null);
const lineChartRef = ref(null);
let pieChartInstance = null;
let lineChartInstance = null;

onMounted(() => {
  loadSummary();
});

onBeforeUnmount(() => {
  if (pieChartInstance) {
    pieChartInstance.dispose();
    pieChartInstance = null;
  }
  if (lineChartInstance) {
    lineChartInstance.dispose();
    lineChartInstance = null;
  }
  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', handleResize);
  }
});

watch(
  () => summary.value && summary.value.statusBreakdown,
  async (val) => {
    if (!val) {
      return;
    }
    await nextTick();
    renderCharts();
  }
);

async function loadSummary() {
  try {
    summary.value = await get('/portal/admin/summary');
    feedback.message = '';
  } catch (error) {
    showFeedback(error.message ?? '加载概览失败', 'error');
  }
}

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}

function buildChartData() {
  const breakdown = summary.value?.statusBreakdown ?? {};
  return Object.entries(breakdown).map(([name, value]) => ({
    name,
    value: Number(value ?? 0)
  }));
}

function renderCharts() {
  const data = buildChartData();
  if (!data.length) {
    return;
  }

  if (pieChartRef.value && !pieChartInstance) {
    pieChartInstance = echarts.init(pieChartRef.value);
  }
  if (lineChartRef.value && !lineChartInstance) {
    lineChartInstance = echarts.init(lineChartRef.value);
  }

  if (!pieChartInstance || !lineChartInstance) {
    return;
  }

  const names = data.map((item) => item.name);
  const values = data.map((item) => item.value);

  const pieOption = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      bottom: 0
    },
    series: [
      {
        name: '投递状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          formatter: '{b}: {d}%'
        },
        data
      }
    ]
  };

  const lineOption = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '8%',
      right: '4%',
      top: 30,
      bottom: 30
    },
    xAxis: {
      type: 'category',
      data: names,
      axisTick: { alignWithLabel: true }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '投递数量',
        type: 'line',
        smooth: true,
        areaStyle: {
          opacity: 0.15
        },
        lineStyle: {
          width: 3
        },
        data: values
      }
    ]
  };

  pieChartInstance.setOption(pieOption);
  lineChartInstance.setOption(lineOption);

  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', handleResize);
    window.addEventListener('resize', handleResize);
  }
}

function handleResize() {
  if (pieChartInstance) {
    pieChartInstance.resize();
  }
  if (lineChartInstance) {
    lineChartInstance.resize();
  }
}
</script>

<style scoped>
.summary-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.summary-item {
  background: #eff6ff;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #1e3a8a;
}

.summary-item span {
  font-size: 14px;
  color: #475569;
}

.summary-item strong {
  font-size: 22px;
}

.status-section {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-charts {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.chart-card {
  background: #f9fafb;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
}

.chart-card h4 {
  margin: 0 0 10px;
  font-size: 14px;
  color: #111827;
}

.chart-container {
  width: 100%;
  height: 260px;
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
</style>
