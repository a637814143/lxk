<template>
  <scroll-view class="page" scroll-y>
    <view class="hero">
      <view class="hero__info">
        <text class="hero__badge">STUDENT</text>
        <text class="hero__title">学生个性化学习主页</text>
        <text class="hero__subtitle">整合课表、学习进度与校园服务，为你打造高效的学习旅程。</text>
        <view class="hero__actions">
          <button class="hero__button hero__button--primary" @click="startLearning">继续学习</button>
          <button class="hero__button hero__button--ghost" @click="goHome">返回门户</button>
        </view>
      </view>
      <view class="hero__progress">
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

    <view class="section">
      <view class="section__title">今日课程安排</view>
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

    <view class="section">
      <view class="section__title">学习分析</view>
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

    <view class="section">
      <view class="section__title">快捷服务</view>
      <view class="service-grid">
        <view class="service-card" v-for="service in services" :key="service.title">
          <view class="service-card__icon">{{ service.icon }}</view>
          <text class="service-card__title">{{ service.title }}</text>
          <text class="service-card__desc">{{ service.desc }}</text>
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<script>
export default {
  data() {
    return {
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
      ]
    }
  },
  computed: {
    progressGradient() {
      const segments = this.progressDetail.reduce(
        (acc, item, index) => {
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
  methods: {
    startLearning() {
      uni.showToast({ title: '已进入学习中心', icon: 'success' })
    },
    goHome() {
      uni.navigateBack()
    }
  }
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f6f7ff;
}

.hero {
  display: flex;
  justify-content: space-between;
  padding: 80rpx 40rpx 64rpx;
  background: linear-gradient(135deg, #0984e3 0%, #74b9ff 100%);
  border-radius: 0 0 48rpx 48rpx;
  color: #ffffff;
  gap: 40rpx;
}

.hero__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.hero__badge {
  font-size: 24rpx;
  letter-spacing: 4rpx;
  opacity: 0.8;
}

.hero__title {
  font-size: 44rpx;
  font-weight: 700;
}

.hero__subtitle {
  font-size: 26rpx;
  line-height: 40rpx;
  opacity: 0.85;
}

.hero__actions {
  display: flex;
  gap: 20rpx;
  margin-top: 20rpx;
}

.hero__button {
  padding: 18rpx 36rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
}

.hero__button--primary {
  background: #ffffff;
  color: #0984e3;
}

.hero__button--ghost {
  background: transparent;
  color: #ffffff;
  border: 2rpx solid rgba(255, 255, 255, 0.45);
}

.hero__progress {
  width: 320rpx;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 28rpx;
  padding: 32rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  backdrop-filter: blur(8rpx);
}

.progress__title {
  font-size: 28rpx;
  font-weight: 600;
}

.progress__ring {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  align-self: center;
}

.progress__circle {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  box-shadow: inset 0 0 0 20rpx rgba(255, 255, 255, 0.75);
}

.progress__center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  color: #0984e3;
}

.progress__value {
  font-size: 34rpx;
  font-weight: 700;
}

.progress__label {
  font-size: 24rpx;
}

.progress__legend {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.legend-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
}

.legend-item__dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
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

.timeline {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.timeline__item {
  display: grid;
  grid-template-columns: 120rpx 1fr 120rpx;
  align-items: center;
  gap: 20rpx;
  background: #f6f8ff;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
}

.timeline__time {
  font-size: 28rpx;
  font-weight: 600;
  color: #0984e3;
}

.timeline__content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.timeline__name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.timeline__room {
  font-size: 24rpx;
  color: #7a8999;
}

.timeline__status {
  font-size: 24rpx;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  text-align: center;
  color: #ffffff;
}

.timeline__status.is-done {
  background: linear-gradient(135deg, #55efc4, #00b894);
}

.timeline__status.is-ongoing {
  background: linear-gradient(135deg, #fdcb6e, #e17055);
}

.timeline__status.is-upcoming {
  background: linear-gradient(135deg, #a29bfe, #6c5ce7);
}

.analysis {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 28rpx;
}

.analysis__chart {
  background: #f1f6ff;
  border-radius: 28rpx;
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.radar {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  background: radial-gradient(circle at center, rgba(116, 185, 255, 0.4) 0%, rgba(116, 185, 255, 0) 70%);
  position: relative;
  box-shadow: inset 0 0 0 18rpx rgba(9, 132, 227, 0.15);
}

.analysis__labels {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  width: 100%;
}

.analysis__label {
  font-size: 24rpx;
  color: #60718b;
  text-align: center;
}

.analysis__list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.analysis__item {
  background: #f8f9ff;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.analysis__item-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.analysis__item-desc {
  font-size: 24rpx;
  color: #7a8999;
  line-height: 36rpx;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
}

.service-card {
  background: #f7f9ff;
  border-radius: 28rpx;
  padding: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.service-card__icon {
  font-size: 36rpx;
}

.service-card__title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.service-card__desc {
  font-size: 24rpx;
  color: #7a8999;
  line-height: 36rpx;
}

@media screen and (max-width: 960rpx) {
  .hero {
    flex-direction: column;
  }

  .hero__progress {
    width: 100%;
  }

  .timeline__item {
    grid-template-columns: 1fr;
    gap: 12rpx;
  }

  .analysis {
    grid-template-columns: 1fr;
  }

  .service-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
