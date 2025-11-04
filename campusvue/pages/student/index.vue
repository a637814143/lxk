<template>
  <view class="dashboard-page student-page">
    <view class="dashboard-layout">
      <view class="dashboard-sidebar">
        <view class="sidebar__header">
          <text class="sidebar__title">学生学习主页</text>
          <text class="sidebar__subtitle">Learning Portal</text>
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
          <view class="student-hero">
            <view class="student-hero__info">
              <text class="student-hero__badge">STUDENT</text>
              <text class="student-hero__title">学生个性化学习主页</text>
              <text class="student-hero__subtitle">
                整合课表、学习进度与校园服务，为你打造高效的学习旅程。
              </text>
              <view class="student-hero__actions">
                <button class="student-hero__button student-hero__button--primary" @click="startLearning">
                  继续学习
                </button>
                <button class="student-hero__button" @click="openPlanner">学习计划</button>
              </view>
            </view>
            <view class="student-hero__progress">
              <text class="progress__title">学习进度</text>
              <view class="progress__ring">
                <view class="progress__circle" :style="{ background: progressGradient }"></view>
                <view class="progress__center">
                  <text class="progress__value">{{ progress }}%</text>
                  <text class="progress__label">阶段完成</text>
                </view>
              </view>
              <view class="progress__legend">
                <view class="legend-item" v-for="item in progressDetail" :key="item.label">
                  <view class="legend-item__dot" :style="{ backgroundColor: item.color }"></view>
                  <text class="legend-item__label">{{ item.label }}</text>
                  <text class="legend-item__value">{{ item.value }}%</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="section-block" id="section-schedule">
          <view class="section__header">
            <text class="section__title">今日课程安排</text>
            <text class="section__subtitle">根据时间轴查看课程状态</text>
          </view>
          <view class="timeline">
            <view class="timeline__item" v-for="course in courses" :key="course.name">
              <view class="timeline__time">{{ course.time }}</view>
              <view class="timeline__content">
                <text class="timeline__name">{{ course.name }}</text>
                <text class="timeline__room">{{ course.room }}</text>
              </view>
              <view class="timeline__status" :class="'is-' + course.status">{{ course.statusText }}</view>
            </view>
          </view>
        </view>

        <view class="section-block" id="section-analysis">
          <view class="section__header">
            <text class="section__title">学习分析</text>
            <text class="section__subtitle">多维数据雷达分析</text>
          </view>
          <view class="analysis">
            <view class="analysis__chart">
              <view class="radar" :style="{ background: radarGradient }"></view>
              <view class="analysis__labels">
                <text v-for="item in radarMetrics" :key="item" class="analysis__label">{{ item }}</text>
              </view>
            </view>
            <view class="analysis__list">
              <view class="analysis__item" v-for="insight in insights" :key="insight.title">
                <text class="analysis__item-title">{{ insight.title }}</text>
                <text class="analysis__item-desc">{{ insight.desc }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="section-block" id="section-services">
          <view class="section__header">
            <text class="section__title">快捷服务</text>
            <text class="section__subtitle">常用功能快速直达</text>
          </view>
          <view class="service-grid">
            <view class="service-card" v-for="service in services" :key="service.title">
              <view class="service-card__icon">{{ service.icon }}</view>
              <text class="service-card__title">{{ service.title }}</text>
              <text class="service-card__desc">{{ service.desc }}</text>
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
        { label: '学习概览', target: 'section-overview' },
        { label: '今日课表', target: 'section-schedule' },
        { label: '学习分析', target: 'section-analysis' },
        { label: '快捷服务', target: 'section-services' }
      ],
      progress: 76,
      progressDetail: [
        { label: '课程学习', value: 52, color: '#0984e3' },
        { label: '作业完成', value: 18, color: '#6c5ce7' },
        { label: '实验实践', value: 30, color: '#74b9ff' }
      ],
      courses: [
        { time: '08:00', name: '大学英语', room: '文科楼 203', status: 'done', statusText: '已结束' },
        { time: '10:15', name: '编程基础', room: '信息楼 506', status: 'ongoing', statusText: '进行中' },
        { time: '14:00', name: '创新实践', room: '创新中心 201', status: 'upcoming', statusText: '即将开始' }
      ],
      radarMetrics: ['知识掌握', '课堂参与', '作业完成', '实验能力', '自学能力'],
      insights: [
        { title: '课堂活跃度提升', desc: '与上周相比，互动次数提升了 15%，保持积极交流有助于理解知识点。' },
        { title: '实验能力优秀', desc: '实验课程成绩稳定在 90 分以上，可继续挑战高阶项目。' },
        { title: '作业时间规划', desc: '作业完成集中于截止日前一日，建议提前安排时间，减轻压力。' }
      ],
      services: [
        { icon: '📚', title: '课程资源', desc: '下载课件与课堂笔记' },
        { icon: '🧠', title: '学习计划', desc: '定制个性化复习计划' },
        { icon: '💬', title: '导师答疑', desc: '预约导师在线辅导' },
        { icon: '🏃', title: '校园活动', desc: '报名社团与实践活动' }
      ],
      scrollTarget: '',
      activeNav: 'section-overview',
      sectionOffsets: [],
      contentTop: 0,
      scrollTimer: null
    }
  },
  computed: {
    progressGradient() {
      const segments = this.progressDetail.reduce(
        (acc, item) => {
          const previous = acc.total
          const current = previous + item.value
          acc.stops.push(`${item.color} ${previous}% ${current}%`)
          acc.total = current
          return acc
        },
        { stops: [], total: 0 }
      )
      return `conic-gradient(${segments.stops.join(', ')})`
    },
    radarGradient() {
      return 'radial-gradient(circle at center, rgba(9, 132, 227, 0.35) 0%, rgba(9, 132, 227, 0) 70%)'
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
    startLearning() {
      uni.showToast({ title: '已进入学习中心', icon: 'success' })
    },
    openPlanner() {
      uni.showToast({ title: '打开学习计划', icon: 'none' })
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
    }
  }
}
</script>

<style>
.dashboard-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f4f7ff 0%, #e9f1ff 100%);
}

.dashboard-layout {
  display: flex;
  min-height: 100vh;
}

.dashboard-sidebar {
  width: 240rpx;
  padding: 48rpx 32rpx 64rpx;
  background: #ffffff;
  box-shadow: 0 20rpx 60rpx rgba(9, 132, 227, 0.08);
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
  color: #0b2a66;
}

.sidebar__subtitle {
  font-size: 22rpx;
  color: #5b6f99;
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
  color: #5b6f99;
  transition: background 0.2s ease;
}

.sidebar__item.is-active {
  background: rgba(9, 132, 227, 0.12);
  color: #246bff;
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
  background: linear-gradient(135deg, #0984e3 0%, #74b9ff 100%);
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
  box-shadow: 0 24rpx 60rpx rgba(11, 42, 102, 0.06);
}

.section-block--hero {
  padding: 0;
  background: none;
  box-shadow: none;
}

.student-hero {
  display: flex;
  gap: 32rpx;
  padding: 56rpx 48rpx;
  background: linear-gradient(135deg, #0984e3 0%, #74b9ff 100%);
  border-radius: 36rpx;
  color: #ffffff;
}

.student-hero__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.student-hero__badge {
  font-size: 24rpx;
  letter-spacing: 4rpx;
  opacity: 0.8;
}

.student-hero__title {
  font-size: 44rpx;
  font-weight: 700;
}

.student-hero__subtitle {
  font-size: 26rpx;
  line-height: 1.6;
  opacity: 0.85;
}

.student-hero__actions {
  display: flex;
  gap: 24rpx;
}

.student-hero__button {
  padding: 20rpx 32rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
  font-size: 26rpx;
}

.student-hero__button--primary {
  background: #ffffff;
  color: #246bff;
}

.student-hero__progress {
  width: 280rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  background: rgba(255, 255, 255, 0.14);
  border-radius: 28rpx;
  padding: 32rpx 24rpx;
}

.progress__title {
  font-size: 30rpx;
  font-weight: 600;
}

.progress__ring {
  position: relative;
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  align-self: center;
}

.progress__circle {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.progress__center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #ffffff;
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  color: #0b2a66;
}

.progress__value {
  font-size: 36rpx;
  font-weight: 700;
}

.progress__label {
  font-size: 22rpx;
}

.progress__legend {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 24rpx;
}

.legend-item__dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
}

.legend-item__value {
  margin-left: auto;
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
  color: #0b2a66;
}

.section__subtitle {
  font-size: 26rpx;
  color: #5b6f99;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.timeline__item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx 28rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: inset 0 0 0 2rpx rgba(36, 107, 255, 0.08);
}

.timeline__time {
  font-size: 28rpx;
  color: #246bff;
  width: 120rpx;
}

.timeline__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.timeline__name {
  font-size: 30rpx;
  font-weight: 600;
  color: #0b2a66;
}

.timeline__room {
  font-size: 24rpx;
  color: #5b6f99;
}

.timeline__status {
  font-size: 24rpx;
  padding: 12rpx 20rpx;
  border-radius: 22rpx;
}

.timeline__status.is-done {
  background: rgba(116, 185, 255, 0.15);
  color: #0984e3;
}

.timeline__status.is-ongoing {
  background: rgba(108, 92, 231, 0.15);
  color: #6c5ce7;
}

.timeline__status.is-upcoming {
  background: rgba(9, 132, 227, 0.1);
  color: #246bff;
}

.analysis {
  display: grid;
  grid-template-columns: minmax(260rpx, 320rpx) 1fr;
  gap: 28rpx;
}

.analysis__chart {
  position: relative;
  background: #f0f6ff;
  border-radius: 28rpx;
  padding: 40rpx 24rpx;
  display: flex;
  justify-content: center;
  align-items: center;
}

.radar {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
}

.analysis__labels {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  pointer-events: none;
}

.analysis__label {
  position: absolute;
  font-size: 22rpx;
  color: #0b2a66;
}

.analysis__label:nth-child(1) { top: 12rpx; }
.analysis__label:nth-child(2) { right: 32rpx; top: 70rpx; }
.analysis__label:nth-child(3) { right: 32rpx; bottom: 70rpx; }
.analysis__label:nth-child(4) { left: 32rpx; bottom: 70rpx; }
.analysis__label:nth-child(5) { left: 32rpx; top: 70rpx; }

.analysis__list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.analysis__item {
  padding: 24rpx 28rpx;
  border-radius: 24rpx;
  background: #f7f9ff;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.analysis__item-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #0b2a66;
}

.analysis__item-desc {
  font-size: 24rpx;
  color: #5b6f99;
  line-height: 1.6;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220rpx, 1fr));
  gap: 24rpx;
}

.service-card {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 28rpx;
  border-radius: 28rpx;
  background: linear-gradient(180deg, rgba(9, 132, 227, 0.12) 0%, rgba(9, 132, 227, 0.05) 100%);
  color: #0b2a66;
}

.service-card__icon {
  font-size: 40rpx;
}

.service-card__title {
  font-size: 30rpx;
  font-weight: 600;
}

.service-card__desc {
  font-size: 24rpx;
  color: #5b6f99;
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

  .sidebar__home {
    margin-top: 0;
  }

  .dashboard-content {
    height: auto;
    padding: 32rpx;
  }

  .student-hero {
    flex-direction: column;
  }

  .student-hero__progress {
    width: 100%;
  }

  .analysis {
    grid-template-columns: 1fr;
  }
}
</style>
