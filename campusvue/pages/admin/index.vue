<template>
  <view class="dashboard-page">
    <view class="dashboard-layout">
      <view class="dashboard-sidebar">
        <view class="sidebar__header">
          <text class="sidebar__title">管理员驾驶舱</text>
          <text class="sidebar__subtitle">Campus Operations</text>
        </view>
        <view class="sidebar__nav">
          <view
            v-for="item in navItems"
            :key="item.target"
            class="sidebar__item"
            :class="{ 'is-active': activeNav === item.target }"
            @click="goSection(item.target)"
          >
            <view class="sidebar__dot"></view>
            <text class="sidebar__label">{{ item.label }}</text>
          </view>
        </view>
        <button class="sidebar__home" @click="goHome">返回门户</button>
      </view>

      <scroll-view
        class="dashboard-content"
        scroll-y
        :scroll-into-view="scrollTarget"
        @scroll="onScroll"
      >
        <view class="section-block section-block--hero" id="section-overview">
          <view class="hero">
            <view class="hero__content">
              <text class="hero__badge">ADMIN</text>
              <text class="hero__title">管理员运行驾驶舱</text>
              <text class="hero__subtitle">
                实时掌握校园系统运行情况，快速处理审批、告警与服务请求。
              </text>
              <view class="hero__actions">
                <button class="hero__button hero__button--primary" @click="publishNotice">
                  发布公告
                </button>
                <button class="hero__button" @click="openProcessCenter">流程中心
                </button>
              </view>
            </view>
            <view class="hero__stat">
              <text class="hero__stat-value">98%</text>
              <text class="hero__stat-label">服务准时率</text>
            </view>
          </view>
        </view>

        <view class="section-block" id="section-metrics">
          <view class="section__header">
            <text class="section__title">关键指标</text>
            <text class="section__subtitle">审批、工单与巡检的实时进展</text>
          </view>
          <view class="stat-grid">
            <view class="stat-card" v-for="stat in stats" :key="stat.label">
              <view class="stat-card__header">
                <text class="stat-card__label">{{ stat.label }}</text>
                <text
                  class="stat-card__trend"
                  :class="{ 'is-up': stat.trend > 0, 'is-down': stat.trend < 0 }"
                >
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

        <view class="section-block" id="section-tickets">
          <view class="section__header">
            <text class="section__title">工单与告警</text>
            <text class="section__subtitle">查看处理趋势与系统告警详情</text>
          </view>
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

        <view class="section-block" id="section-actions">
          <view class="section__header">
            <text class="section__title">快速操作</text>
            <text class="section__subtitle">一键触达常用任务</text>
          </view>
          <view class="action-grid">
            <view class="action-card" v-for="action in actions" :key="action.title">
              <view class="action-card__icon">{{ action.icon }}</view>
              <text class="action-card__title">{{ action.title }}</text>
              <text class="action-card__desc">{{ action.desc }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      navItems: [
        { label: '运行概览', target: 'section-overview' },
        { label: '关键指标', target: 'section-metrics' },
        { label: '工单与告警', target: 'section-tickets' },
        { label: '快速操作', target: 'section-actions' }
      ],
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
        { icon: '📊', title: '运行分析', desc: '查看实时监控与统计' }
      ],
      scrollTarget: '',
      activeNav: 'section-overview',
      sectionOffsets: [],
      contentTop: 0,
      scrollTimer: null
    }
  },
  onReady() {
    this.$nextTick(() => {
      setTimeout(() => {
        this.calculateSectionOffsets()
      }, 120)
    })
  },
  onUnload() {
    if (this.scrollTimer) {
      clearTimeout(this.scrollTimer)
      this.scrollTimer = null
    }
  },
  methods: {
    publishNotice() {
      uni.showToast({ title: '公告已发布', icon: 'success' })
    },
    openProcessCenter() {
      uni.showToast({ title: '跳转至审批流程', icon: 'none' })
    },
    goHome() {
      uni.navigateBack()
    },
    goSection(target) {
      this.scrollTarget = target
      this.activeNav = target
      this.resetScrollTarget()
    },
    onScroll(event) {
      const scrollTop = event.detail.scrollTop
      let active = this.navItems[0].target
      for (const anchor of this.sectionOffsets) {
        if (scrollTop >= anchor.top - 80) {
          active = anchor.id
        }
      }
      this.activeNav = active
    },
    calculateSectionOffsets() {
      const query = uni.createSelectorQuery().in(this)
      query
        .select('.dashboard-content')
        .boundingClientRect(rect => {
          this.contentTop = rect ? rect.top : 0
        })
      query
        .selectAll('.section-block')
        .boundingClientRect(rects => {
          if (!rects) {
            this.sectionOffsets = []
            return
          }
          this.sectionOffsets = rects.map(rect => ({
            id: rect.id,
            top: rect.top - this.contentTop
          }))
        })
      query.exec()
    },
    resetScrollTarget() {
      if (this.scrollTimer) {
        clearTimeout(this.scrollTimer)
      }
      this.scrollTimer = setTimeout(() => {
        this.scrollTarget = ''
        this.scrollTimer = null
      }, 240)
    },
    getLinePointStyle(point) {
      return {
        left: `${this.ticketTrend.indexOf(point) * 100 / (this.ticketTrend.length - 1)}%`,
        bottom: `${point.value}%`
      }
    }
  }
}
</script>

<style>
.dashboard-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f6ff 0%, #eef1ff 100%);
}

.dashboard-layout {
  display: flex;
  min-height: 100vh;
}

.dashboard-sidebar {
  width: 240rpx;
  padding: 48rpx 32rpx 64rpx;
  background: #ffffff;
  box-shadow: 0 20rpx 60rpx rgba(99, 110, 255, 0.08);
  display: flex;
  flex-direction: column;
  gap: 40rpx;
  position: sticky;
  top: 0;
  height: 100vh;
  box-sizing: border-box;
}

.sidebar__header {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.sidebar__title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2b66;
}

.sidebar__subtitle {
  font-size: 22rpx;
  color: #7f8aa3;
}

.sidebar__nav {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.sidebar__item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 12rpx;
  border-radius: 20rpx;
  color: #5b6690;
  transition: background 0.2s ease;
}

.sidebar__item.is-active {
  background: rgba(99, 110, 255, 0.1);
  color: #3a4af5;
}

.sidebar__item:active {
  background: rgba(99, 110, 255, 0.08);
}

.sidebar__dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: currentColor;
}

.sidebar__label {
  font-size: 26rpx;
}

.sidebar__home {
  margin-top: auto;
  padding: 20rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #3a4af5 0%, #7a5cfa 100%);
  color: #ffffff;
  font-size: 26rpx;
}

.dashboard-content {
  flex: 1;
  height: 100vh;
  padding: 0 48rpx 80rpx;
  box-sizing: border-box;
}

.section-block {
  margin-bottom: 56rpx;
  padding: 48rpx;
  background: #ffffff;
  border-radius: 32rpx;
  box-shadow: 0 24rpx 60rpx rgba(31, 43, 102, 0.06);
}

.section-block--hero {
  padding: 0;
  background: none;
  box-shadow: none;
}

.hero {
  display: flex;
  align-items: stretch;
  gap: 32rpx;
  padding: 56rpx 48rpx;
  background: linear-gradient(135deg, #3a4af5 0%, #5c6cff 100%);
  border-radius: 36rpx;
  color: #ffffff;
}

.hero__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.hero__badge {
  font-size: 24rpx;
  letter-spacing: 4rpx;
  opacity: 0.75;
}

.hero__title {
  font-size: 44rpx;
  font-weight: 700;
}

.hero__subtitle {
  font-size: 26rpx;
  line-height: 1.6;
  opacity: 0.85;
}

.hero__actions {
  display: flex;
  gap: 24rpx;
}

.hero__button {
  padding: 20rpx 32rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
  font-size: 26rpx;
}

.hero__button--primary {
  background: #ffffff;
  color: #3a4af5;
}

.hero__stat {
  width: 220rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 32rpx;
  padding: 32rpx 16rpx;
  text-align: center;
}

.hero__stat-value {
  font-size: 64rpx;
  font-weight: 700;
}

.hero__stat-label {
  font-size: 24rpx;
  opacity: 0.8;
}

.section__header {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 32rpx;
}

.section__title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1f2b66;
}

.section__subtitle {
  font-size: 26rpx;
  color: #7f8aa3;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200rpx, 1fr));
  gap: 28rpx;
}

.stat-card {
  padding: 32rpx;
  border-radius: 28rpx;
  background: linear-gradient(180deg, rgba(86, 102, 255, 0.1) 0%, rgba(86, 102, 255, 0.04) 100%);
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.stat-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 26rpx;
  color: #5b6690;
}

.stat-card__value {
  font-size: 44rpx;
  font-weight: 700;
  color: #1f2b66;
}

.stat-card__trend {
  font-size: 24rpx;
}

.stat-card__trend.is-up {
  color: #21bf73;
}

.stat-card__trend.is-down {
  color: #ff7675;
}

.progress {
  height: 12rpx;
  border-radius: 12rpx;
  background: rgba(26, 42, 108, 0.1);
}

.progress__inner {
  height: 100%;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #3a4af5 0%, #7a5cfa 100%);
}

.split {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320rpx, 1fr));
  gap: 28rpx;
}

.panel {
  padding: 32rpx;
  background: #f7f8ff;
  border-radius: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.panel__header {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  color: #1f2b66;
}

.panel__title {
  font-size: 30rpx;
  font-weight: 600;
}

.panel__caption {
  font-size: 24rpx;
  color: #7f8aa3;
}

.panel__chart {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.line-chart {
  position: relative;
  height: 200rpx;
  background: rgba(61, 75, 245, 0.12);
  border-radius: 24rpx;
}

.line-chart__point {
  position: absolute;
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #3a4af5;
  transform: translate(-50%, 50%);
}

.line-chart__baseline {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4rpx;
  background: rgba(61, 75, 245, 0.3);
}

.chart__axis {
  display: flex;
  justify-content: space-between;
  font-size: 22rpx;
  color: #7f8aa3;
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
  padding: 20rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.9);
}

.alert-item__level {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
}

.alert-item__level.is-high {
  background: #ff7675;
}

.alert-item__level.is-medium {
  background: #fdcb6e;
}

.alert-item__level.is-low {
  background: #55efc4;
}

.alert-item__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.alert-item__name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2b66;
}

.alert-item__desc {
  font-size: 24rpx;
  color: #7f8aa3;
}

.alert-item__count {
  font-size: 28rpx;
  font-weight: 600;
  color: #3a4af5;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240rpx, 1fr));
  gap: 24rpx;
}

.action-card {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 28rpx;
  border-radius: 28rpx;
  background: linear-gradient(180deg, rgba(122, 92, 250, 0.12) 0%, rgba(122, 92, 250, 0.05) 100%);
  color: #1f2b66;
}

.action-card__icon {
  font-size: 40rpx;
}

.action-card__title {
  font-size: 30rpx;
  font-weight: 600;
}

.action-card__desc {
  font-size: 24rpx;
  color: #7f8aa3;
}

@media screen and (max-width: 750px) {
  .dashboard-layout {
    flex-direction: column;
  }

  .dashboard-sidebar {
    width: 100%;
    height: auto;
    position: relative;
    box-shadow: none;
    flex-direction: row;
    align-items: center;
    gap: 24rpx;
    overflow-x: auto;
  }

  .sidebar__nav {
    flex-direction: row;
    gap: 16rpx;
  }

  .sidebar__item {
    padding: 16rpx 24rpx;
  }

  .sidebar__home {
    margin-top: 0;
  }

  .dashboard-content {
    height: auto;
    padding: 32rpx;
  }

  .hero {
    flex-direction: column;
    text-align: left;
  }

  .hero__stat {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
  }
}
</style>
