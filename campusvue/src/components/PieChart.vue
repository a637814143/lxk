<template>
  <div class="pie-chart">
    <div class="pie-chart__canvas" :style="chartStyle">
      <p v-if="!hasData" class="pie-chart__empty">暂无数据可展示</p>
    </div>

    <div v-if="hasData" class="pie-chart__details">
      <ul class="pie-chart__legend">
        <li v-for="segment in segments" :key="segment.label" class="pie-chart__legend-item">
          <span class="pie-chart__legend-color" :style="{ background: segment.color }"></span>
          <div class="pie-chart__legend-details">
            <span class="pie-chart__legend-label">{{ segment.label }}</span>
            <span class="pie-chart__legend-value">{{ segment.value }}（{{ segment.percentage.toFixed(1) }}%）</span>
          </div>
        </li>
      </ul>
      <p class="pie-chart__summary">总计 {{ totalValue }} 条记录</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

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

const palette = ['#2563eb', '#10b981', '#f97316', '#facc15', '#14b8a6', '#ec4899', '#8b5cf6', '#6366f1'];

const segments = computed(() => {
  const entries = Object.entries(props.data ?? {}).filter(([, value]) => Number(value) > 0);
  const total = entries.reduce((sum, [, value]) => sum + Number(value), 0);
  let current = 0;

  return entries.map(([label, value], index) => {
    const numericValue = Number(value);
    const percentage = total ? (numericValue / total) * 100 : 0;
    const start = current;
    current += percentage;

    return {
      label,
      value: numericValue,
      percentage,
      start,
      color: palette[index % palette.length]
    };
  });
});

const hasData = computed(() => segments.value.length > 0);

const chartStyle = computed(() => {
  if (!segments.value.length) {
    return {};
  }

  const gradients = segments.value.map((segment) => {
    const end = segment.start + segment.percentage;
    return `${segment.color} ${segment.start}% ${end}%`;
  });

  return {
    background: `conic-gradient(${gradients.join(', ')})`
  };
});

const totalValue = computed(() =>
  segments.value.reduce((sum, segment) => sum + segment.value, 0)
);
</script>

<style scoped>
.pie-chart {
  display: grid;
  gap: 1.5rem;
}

.pie-chart__canvas {
  position: relative;
  width: min(320px, 100%);
  aspect-ratio: 1 / 1;
  margin: 0 auto;
  border-radius: 50%;
  box-shadow: inset 0 0 0 12px rgba(255, 255, 255, 0.65);
  background: var(--surface-muted);
}

.pie-chart__empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.95rem;
}

.pie-chart__details {
  display: grid;
  gap: 1rem;
}

.pie-chart__legend {
  display: grid;
  gap: 0.75rem;
}

.pie-chart__legend-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.55);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.pie-chart__legend-color {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  flex-shrink: 0;
}

.pie-chart__legend-details {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  width: 100%;
  font-size: 0.95rem;
}

.pie-chart__legend-label {
  font-weight: 600;
  color: var(--text-primary);
}

.pie-chart__legend-value {
  color: var(--text-muted);
}

.pie-chart__summary {
  text-align: center;
  color: var(--text-muted);
  font-size: 0.9rem;
}

@media (min-width: 960px) {
  .pie-chart {
    grid-template-columns: min(320px, 40%) 1fr;
    align-items: center;
  }

  .pie-chart__canvas {
    margin: 0;
  }

  .pie-chart__details {
    gap: 1.25rem;
  }
}
</style>
