<template>
  <div class="pie-chart">
    <canvas ref="canvasRef"></canvas>
    <p v-if="!hasData" class="pie-chart__empty">暂无数据可展示</p>
  </div>
</template>

<script setup>
import { Chart, ArcElement, Legend, Tooltip } from 'chart.js';
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';

Chart.register(ArcElement, Tooltip, Legend);

const props = defineProps({
  data: {
    type: Object,
    default: () => ({})
  },
  title: {
    type: String,
    default: ''
  }
});

const canvasRef = ref(null);
const chartInstance = ref(null);

const hasData = computed(() => Object.values(props.data).some((value) => Number(value) > 0));

const palette = ['#2563eb', '#10b981', '#f97316', '#facc15', '#14b8a6', '#ec4899', '#8b5cf6', '#6366f1'];

function renderChart() {
  if (!canvasRef.value) {
    return;
  }

  const labels = Object.keys(props.data ?? {});
  const values = Object.values(props.data ?? {});

  if (!labels.length || !hasData.value) {
    if (chartInstance.value) {
      chartInstance.value.destroy();
      chartInstance.value = null;
    }
    return;
  }

  const datasetColors = labels.map((_, index) => palette[index % palette.length]);

  const config = {
    type: 'pie',
    data: {
      labels,
      datasets: [
        {
          label: props.title || '数据占比',
          data: values,
          backgroundColor: datasetColors,
          borderWidth: 1,
          borderColor: '#ffffff'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom'
        },
        title: props.title
          ? {
              display: true,
              text: props.title
            }
          : undefined,
        tooltip: {
          callbacks: {
            label: (context) => {
              const total = values.reduce((sum, value) => sum + value, 0);
              const percentage = total ? ((context.parsed / total) * 100).toFixed(1) : 0;
              return `${context.label}: ${context.parsed} (${percentage}%)`;
            }
          }
        }
      }
    }
  };

  if (chartInstance.value) {
    chartInstance.value.destroy();
  }

  chartInstance.value = new Chart(canvasRef.value, config);
}

onMounted(() => {
  renderChart();
});

onBeforeUnmount(() => {
  if (chartInstance.value) {
    chartInstance.value.destroy();
    chartInstance.value = null;
  }
});

watch(
  () => ({ ...props.data }),
  () => {
    renderChart();
  },
  { deep: true }
);
</script>

<style scoped>
.pie-chart {
  position: relative;
  min-height: 260px;
}

.pie-chart canvas {
  width: 100% !important;
  height: 100% !important;
}

.pie-chart__empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
}
</style>
