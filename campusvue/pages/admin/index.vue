<template>
  <scroll-view class="page" scroll-y>
    <view class="header">
      <view class="header__content">
        <text class="header__badge">ADMIN</text>
        <text class="header__title">管理员运行驾驶舱</text>
        <text class="header__subtitle">
          实时掌握校园系统运行情况，快速处理审批、告警与服务请求。
        </text>
        <view class="header__actions">
          <button class="ghost-button" @click="goHome">返回门户</button>
          <button class="primary-button">发布公告</button>
        </view>
      </view>
      <view class="header__stat">
        <text class="header__stat-value">98%</text>
        <text class="header__stat-label">服务准时率</text>
      </view>
    </view>

    <view class="section">
      <view class="section__title">关键指标</view>
      <view class="stat-grid">
        <view class="stat-card" v-for="stat in stats" :key="stat.label">
          <view class="stat-card__header">
            <text class="stat-card__label">{{ stat.label }}</text>
            <text class="stat-card__trend" :class="{ 'is-up': stat.trend > 0, 'is-down': stat.trend < 0 }">
              {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%
            </text>
          </view>
          <text class="stat-card__value">{{ stat.value }}</text>
          <view class="progress">
            <view class="progress__inner" :style="{ width: stat.progress + '%' }"></view>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__title">工单与告警</view>
      <view class="split">
        <view class="panel">
          <view class="panel__header">
            <text class="panel__title">处理进度</text>
            <text class="panel__caption">近7日处理完成率</text>
          </view>
          <view class="panel__chart">
            <view class="line-chart">
              <view
                class="line-chart__point"
                v-for="point in ticketTrend"
                :key="point.day"
                :style="getLinePointStyle(point)"
              ></view>
              <view class="line-chart__baseline"></view>
            </view>
            <view class="chart__axis">
              <text class="axis-label" v-for="point in ticketTrend" :key="point.day">{{ point.day }}</text>
            </view>
          </view>
        </view>
        <view class="panel">
          <view class="panel__header">
            <text class="panel__title">告警概览</text>
            <text class="panel__caption">系统按严重程度排序</text>
          </view>
          <view class="alert-list">
            <view class="alert-item" v-for="alert in alerts" :key="alert.name">
              <view class="alert-item__level" :class="'is-' + alert.level"></view>
              <view class="alert-item__content">
                <text class="alert-item__name">{{ alert.name }}</text>
                <text class="alert-item__desc">{{ alert.desc }}</text>
              </view>
              <text class="alert-item__count">{{ alert.count }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__title">快速操作</view>
      <view class="action-grid">
        <view class="action-card" v-for="action in actions" :key="action.title">
          <view class="action-card__icon">{{ action.icon }}</view>
          <text class="action-card__title">{{ action.title }}</text>
          <text class="action-card__desc">{{ action.desc }}</text>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<script>
export default {
  data() {
    return {
      stats: [
        { label: '审批待处理', value: 12, progress: 45, trend: -6 },
        { label: '今日工单', value: 48, progress: 78, trend: 12 },
        { label: '安全巡检', value: 6, progress: 62, trend: 4 }
      ],
      ticketTrend: [
        { day: '周一', value: 72 },
        { day: '周二', value: 68 },
        { day: '周三', value: 74 },
        { day: '周四', value: 81 },
        { day: '周五', value: 88 },
        { day: '周六', value: 83 },
        { day: '周日', value: 90 }
      ],
      alerts: [
        { name: '身份认证平台', desc: '登录失败率升高', level: 'high', count: 5 },
        { name: '课程排课系统', desc: '资源占用接近上限', level: 'medium', count: 3 },
        { name: '数据分析引擎', desc: '夜间批处理稍有延迟', level: 'low', count: 2 }
      ],
      actions: [
        { icon: '📢', title: '公告中心', desc: '发布最新通知与资讯' },
        { icon: '✅', title: '审批审核', desc: '查看并批量处理流程' },
        { icon: '🛡️', title: '安全巡检', desc: '巡检安全策略与日志' },
        { icon: '📊', title: '报表导出', desc: '导出运行报表与统计' }
      ]
    }
  },
  methods: {
    goHome() {
      uni.navigateBack()
    },
    getLinePointStyle(point) {
      return {
        left: `calc(${point.position}% - 12rpx)`,
        bottom: `${point.value}%`
      }
    }
  },
  created() {
    const step = 100 / (this.ticketTrend.length - 1)
    this.ticketTrend = this.ticketTrend.map((item, index) => ({
      ...item,
      position: index * step
    }))
  }
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f5f7fb;
}

.header {
  background: linear-gradient(135deg, #4b6bfb 0%, #6d83ff 100%);
  padding: 80rpx 48rpx 72rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-radius: 0 0 48rpx 48rpx;
  color: #ffffff;
  box-shadow: 0 32rpx 60rpx rgba(75, 107, 251, 0.35);
}

.header__content {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  max-width: 540rpx;
}

.header__badge {
  font-size: 24rpx;
  letter-spacing: 6rpx;
  opacity: 0.8;
}

.header__title {
  font-size: 46rpx;
  font-weight: 700;
  line-height: 60rpx;
}

.header__subtitle {
  font-size: 26rpx;
  opacity: 0.85;
  line-height: 40rpx;
}

.header__actions {
  display: flex;
  flex-direction: row;
  gap: 20rpx;
}

.header__stat {
  text-align: right;
}

.header__stat-value {
  font-size: 80rpx;
  font-weight: 700;
}

.header__stat-label {
  font-size: 26rpx;
  opacity: 0.85;
}

.primary-button {
  background: #ffffff;
  color: #4b6bfb;
  padding: 18rpx 36rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  font-weight: 600;
  border: none;
}

.ghost-button {
  background: transparent;
  color: #ffffff;
  padding: 18rpx 36rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.45);
}

.section {
  margin: 36rpx;
  background: #ffffff;
  border-radius: 32rpx;
  padding: 36rpx;
  box-shadow: 0 16rpx 34rpx rgba(31, 45, 61, 0.08);
}

.section__title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 28rpx;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24rpx;
}

.stat-card {
  background: #f6f8ff;
  border-radius: 28rpx;
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.stat-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 26rpx;
  color: #5c6c7a;
}

.stat-card__value {
  font-size: 42rpx;
  font-weight: 700;
  color: #1f2d3d;
}

.stat-card__trend {
  font-size: 24rpx;
}

.stat-card__trend.is-up {
  color: #27ae60;
}

.stat-card__trend.is-down {
  color: #e17055;
}

.progress {
  height: 16rpx;
  background: rgba(75, 107, 251, 0.15);
  border-radius: 999rpx;
  overflow: hidden;
}

.progress__inner {
  height: 100%;
  border-radius: 999rpx;
  background: linear-gradient(90deg, #4b6bfb 0%, #9face6 100%);
}

.split {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.panel {
  background: #f8f9ff;
  border-radius: 28rpx;
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.panel__header {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.panel__title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.panel__caption {
  font-size: 24rpx;
  color: #7a8999;
}

.panel__chart {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.line-chart {
  position: relative;
  height: 240rpx;
  background: linear-gradient(180deg, rgba(75, 107, 251, 0.12) 0%, rgba(255, 255, 255, 0) 100%);
  border-radius: 24rpx;
  overflow: hidden;
}

.line-chart__point {
  position: absolute;
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #ffffff;
  border: 6rpx solid rgba(75, 107, 251, 0.95);
  box-shadow: 0 8rpx 18rpx rgba(75, 107, 251, 0.25);
}

.line-chart__baseline {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 6rpx;
  background: rgba(75, 107, 251, 0.16);
}

.chart__axis {
  display: flex;
  justify-content: space-between;
  font-size: 22rpx;
  color: #7a8999;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.alert-item__level {
  width: 16rpx;
  height: 80rpx;
  border-radius: 999rpx;
  background: #f1f3ff;
}

.alert-item__level.is-high {
  background: linear-gradient(180deg, #ff7675 0%, #d63031 100%);
}

.alert-item__level.is-medium {
  background: linear-gradient(180deg, #fdcb6e 0%, #e17055 100%);
}

.alert-item__level.is-low {
  background: linear-gradient(180deg, #55efc4 0%, #00b894 100%);
}

.alert-item__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.alert-item__name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.alert-item__desc {
  font-size: 24rpx;
  color: #7a8999;
}

.alert-item__count {
  font-size: 28rpx;
  font-weight: 600;
  color: #4b6bfb;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
}

.action-card {
  background: #f6f8ff;
  border-radius: 28rpx;
  padding: 32rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  align-items: flex-start;
}

.action-card__icon {
  font-size: 36rpx;
}

.action-card__title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.action-card__desc {
  font-size: 24rpx;
  color: #7a8999;
}

@media screen and (max-width: 960rpx) {
  .stat-grid {
    grid-template-columns: 1fr;
  }

  .split {
    grid-template-columns: 1fr;
  }

  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
