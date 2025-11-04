<template>
  <scroll-view class="page" scroll-y>
    <view class="hero">
      <view class="hero__content">
        <text class="hero__badge">智慧校园运营中心</text>
        <text class="hero__title">构建多角色协同的校园门户</text>
        <text class="hero__subtitle">
          为管理员、教师与学生提供量身打造的工作台，实时掌握数据动态并快速完成日常操作。
        </text>
        <view class="hero__stats">
          <view class="stat">
            <text class="stat__value">96%</text>
            <text class="stat__label">流程线上化</text>
          </view>
          <view class="stat">
            <text class="stat__value">12</text>
            <text class="stat__label">运行中的系统</text>
          </view>
          <view class="stat">
            <text class="stat__value">3</text>
            <text class="stat__label">核心角色入口</text>
          </view>
        </view>
      </view>
      <view class="hero__visual">
        <view class="visual__card">
          <text class="visual__title">今日数据</text>
          <view class="visual__list">
            <view class="visual__item" v-for="item in todaySummary" :key="item.label">
              <text class="visual__item-label">{{ item.label }}</text>
              <text class="visual__item-value">{{ item.value }}</text>
            </view>
          </view>
        </view>
        <view class="visual__sparkline">
          <view
            class="sparkline__bar"
            v-for="point in trend"
            :key="point.day"
            :style="{ height: point.value + '%'}"
          ></view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__header">
        <text class="section__title">角色工作台</text>
        <text class="section__subtitle">选择角色进入专属页面，查看任务与分析结果。</text>
      </view>
      <view class="role-grid">
        <view class="role-card" v-for="role in roles" :key="role.id" @click="goToRole(role.path)">
          <view class="role-card__icon" :class="'role-card__icon--' + role.id">
            <text>{{ role.icon }}</text>
          </view>
          <text class="role-card__title">{{ role.title }}</text>
          <text class="role-card__desc">{{ role.description }}</text>
          <view class="role-card__actions">
            <text class="role-card__cta">立即进入</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__header">
        <text class="section__title">运行数据概览</text>
        <text class="section__subtitle">关键指标对比与趋势一目了然，辅助决策。</text>
      </view>
      <view class="analytics">
        <view class="chart-card">
          <text class="chart-card__title">系统活跃度（月）</text>
          <view class="chart chart--bar">
            <view class="chart__bars">
              <view
                class="chart__bar"
                v-for="item in monthlyActive"
                :key="item.month"
              >
                <view class="chart__bar-fill" :style="{ height: item.value + '%' }"></view>
                <text class="chart__bar-label">{{ item.month }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="chart-card">
          <text class="chart-card__title">服务响应占比</text>
          <view class="chart chart--donut">
            <view class="donut" :style="{ background: donutGradient }">
              <view class="donut__center">
                <text class="donut__center-value">{{ serviceShare[0].value }}%</text>
                <text class="donut__center-label">主流程</text>
              </view>
            </view>
            <view class="chart__legend">
              <view class="legend-item" v-for="segment in serviceShare" :key="segment.label">
                <view class="legend-item__dot" :style="{ backgroundColor: segment.color }"></view>
                <text class="legend-item__label">{{ segment.label }}</text>
                <text class="legend-item__value">{{ segment.value }}%</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<script>
export default {
  data() {
    return {
      roles: [
        {
          id: 'admin',
          title: '管理员中心',
          description: '发布公告、审核流程、监控运行状态。',
          icon: '管',
          path: '/pages/admin/index'
        },
        {
          id: 'teacher',
          title: '教师工作台',
          description: '课务安排、课堂反馈与成绩统计管理。',
          icon: '师',
          path: '/pages/teacher/index'
        },
        {
          id: 'student',
          title: '学生主页',
          description: '查看课表、学习进度与校园服务。',
          icon: '生',
          path: '/pages/student/index'
        }
      ],
      todaySummary: [
        { label: '待办事项', value: '18' },
        { label: '实时告警', value: '2' },
        { label: '处理完成', value: '96%' }
      ],
      trend: [
        { day: 'Mon', value: 40 },
        { day: 'Tue', value: 56 },
        { day: 'Wed', value: 64 },
        { day: 'Thu', value: 72 },
        { day: 'Fri', value: 84 },
        { day: 'Sat', value: 68 },
        { day: 'Sun', value: 52 }
      ],
      monthlyActive: [
        { month: '3月', value: 68 },
        { month: '4月', value: 72 },
        { month: '5月', value: 80 },
        { month: '6月', value: 86 },
        { month: '7月', value: 92 }
      ],
      serviceShare: [
        { label: '主流程', value: 45, color: '#6c5ce7' },
        { label: '移动端', value: 30, color: '#00b894' },
        { label: '门户服务', value: 15, color: '#0984e3' },
        { label: '统计分析', value: 10, color: '#fdcb6e' }
      ]
    }
  },
  methods: {
    goToRole(path) {
      uni.navigateTo({ url: path })
    }
  },
  computed: {
    donutGradient() {
      let cursor = 0
      const segments = this.serviceShare.map((segment) => {
        const start = cursor
        const end = cursor + (segment.value / 100) * 360
        cursor = end
        return `${segment.color} ${start}deg ${end}deg`
      })
      return `conic-gradient(${segments.join(', ')})`
    }
  }
}
</script>

<style>
.page {
  height: 100vh;
  background: linear-gradient(180deg, #f5f7ff 0%, #ffffff 40%);
}

.hero {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  padding: 96rpx 48rpx 64rpx;
  gap: 48rpx;
}

.hero__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.hero__badge {
  font-size: 26rpx;
  letter-spacing: 4rpx;
  color: #6c5ce7;
  background: rgba(108, 92, 231, 0.1);
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  width: fit-content;
}

.hero__title {
  font-size: 48rpx;
  line-height: 64rpx;
  font-weight: 700;
  color: #1f2d3d;
}

.hero__subtitle {
  font-size: 28rpx;
  line-height: 44rpx;
  color: #5c7080;
  max-width: 560rpx;
}

.hero__stats {
  display: flex;
  flex-direction: row;
  gap: 32rpx;
  margin-top: 24rpx;
}

.stat {
  background: #ffffff;
  padding: 24rpx 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 16rpx 32rpx rgba(56, 83, 179, 0.08);
}

.stat__value {
  font-size: 40rpx;
  font-weight: 700;
  color: #2d3436;
}

.stat__label {
  font-size: 24rpx;
  color: #7f8c8d;
  margin-top: 12rpx;
}

.hero__visual {
  width: 320rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.visual__card {
  background: linear-gradient(160deg, #6c5ce7 0%, #a29bfe 100%);
  border-radius: 32rpx;
  padding: 32rpx 24rpx;
  color: #ffffff;
  box-shadow: 0 20rpx 40rpx rgba(108, 92, 231, 0.3);
}

.visual__title {
  font-size: 30rpx;
  font-weight: 600;
}

.visual__list {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.visual__item {
  display: flex;
  justify-content: space-between;
  font-size: 26rpx;
}

.visual__item-label {
  color: rgba(255, 255, 255, 0.8);
}

.visual__item-value {
  font-weight: 600;
}

.visual__sparkline {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 180rpx;
  box-shadow: inset 0 0 0 2rpx rgba(108, 92, 231, 0.05);
}

.sparkline__bar {
  width: 24rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #81ecec 0%, #0984e3 100%);
}

.section {
  background: #ffffff;
  margin: 0 32rpx 48rpx;
  border-radius: 32rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 20rpx 36rpx rgba(31, 45, 61, 0.08);
}

.section__header {
  margin-bottom: 32rpx;
}

.section__title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.section__subtitle {
  font-size: 26rpx;
  color: #8291a4;
  margin-top: 12rpx;
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24rpx;
}

.role-card {
  background: #f8f9ff;
  border-radius: 28rpx;
  padding: 32rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 10rpx 24rpx rgba(108, 92, 231, 0.06);
}

.role-card:active {
  transform: translateY(6rpx);
  box-shadow: 0 6rpx 16rpx rgba(108, 92, 231, 0.12);
}

.role-card__icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: 600;
  color: #ffffff;
}

.role-card__icon--admin {
  background: linear-gradient(135deg, #6c5ce7 0%, #a29bfe 100%);
}

.role-card__icon--teacher {
  background: linear-gradient(135deg, #00b894 0%, #55efc4 100%);
}

.role-card__icon--student {
  background: linear-gradient(135deg, #0984e3 0%, #74b9ff 100%);
}

.role-card__title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.role-card__desc {
  font-size: 26rpx;
  color: #60718b;
  line-height: 40rpx;
}

.role-card__actions {
  display: flex;
  justify-content: flex-start;
}

.role-card__cta {
  font-size: 26rpx;
  color: #6c5ce7;
  font-weight: 600;
}

.analytics {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.chart-card {
  background: #f8f9ff;
  border-radius: 28rpx;
  padding: 32rpx;
  box-shadow: 0 16rpx 28rpx rgba(31, 45, 61, 0.08);
}

.chart-card__title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 24rpx;
}

.chart__bars {
  display: flex;
  align-items: flex-end;
  gap: 24rpx;
  height: 260rpx;
}

.chart__bar {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 16rpx;
}

.chart__bar-fill {
  width: 64rpx;
  background: linear-gradient(180deg, #74b9ff 0%, #0984e3 100%);
  border-radius: 24rpx 24rpx 12rpx 12rpx;
  transition: height 0.4s ease;
}

.chart__bar-label {
  font-size: 24rpx;
  color: #60718b;
}

.chart--donut {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.donut {
  width: 260rpx;
  height: 260rpx;
  border-radius: 50%;
  position: relative;
  align-self: center;
  box-shadow: inset 0 0 0 22rpx #ffffff;
}

.donut__center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.donut__center-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2d3d;
}

.donut__center-label {
  font-size: 24rpx;
  color: #8291a4;
}

.chart__legend {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.legend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  font-size: 24rpx;
  color: #60718b;
}

.legend-item__dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
}

@media screen and (max-width: 960rpx) {
  .hero {
    flex-direction: column;
  }

  .hero__visual {
    width: 100%;
    flex-direction: row;
    align-items: stretch;
  }

  .visual__card {
    flex: 2;
  }

  .visual__sparkline {
    flex: 1;
  }

  .role-grid {
    grid-template-columns: 1fr;
  }

  .analytics {
    grid-template-columns: 1fr;
  }
}
</style>
