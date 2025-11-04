<template>
  <scroll-view class="page" scroll-y>
    <view class="banner">
      <view class="banner__content">
        <text class="banner__badge">TEACHER</text>
        <text class="banner__title">教师教学驾驶舱</text>
        <text class="banner__subtitle">查看课程安排、学习反馈与教学数据分析，轻松掌握课堂节奏。</text>
        <view class="banner__actions">
          <button class="primary" @click="createLesson">新建课程</button>
          <button class="secondary" @click="goHome">返回门户</button>
        </view>
      </view>
      <view class="banner__schedule">
        <text class="schedule__title">今日课程</text>
        <view class="schedule__items">
          <view class="schedule__item" v-for="lesson in todayLessons" :key="lesson.name">
            <text class="schedule__time">{{ lesson.time }}</text>
            <text class="schedule__name">{{ lesson.name }}</text>
            <text class="schedule__room">{{ lesson.room }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__title">课堂表现</view>
      <view class="chart-card">
        <view class="chart-card__header">
          <text class="chart-card__title">近四周课堂参与度</text>
          <text class="chart-card__caption">通过签到与互动反馈统计</text>
        </view>
        <view class="chart-bar">
          <view class="chart-bar__item" v-for="item in engagement" :key="item.week">
            <view class="chart-bar__fill" :style="{ height: item.value + '%' }"></view>
            <text class="chart-bar__label">{{ item.week }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__title">作业批改</view>
      <view class="cards">
        <view class="card" v-for="task in assignments" :key="task.title">
          <view class="card__icon">{{ task.icon }}</view>
          <view class="card__content">
            <text class="card__title">{{ task.title }}</text>
            <text class="card__desc">{{ task.desc }}</text>
          </view>
          <text class="card__count">{{ task.count }}</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__title">学生关注</view>
      <view class="focus">
        <view class="focus__list">
          <view class="focus__item" v-for="student in students" :key="student.name">
            <text class="focus__name">{{ student.name }}</text>
            <text class="focus__tag" :class="'is-' + student.tagType">{{ student.tag }}</text>
            <text class="focus__note">{{ student.note }}</text>
          </view>
        </view>
        <view class="focus__tips">
          <text class="focus__tips-title">教学提醒</text>
          <text class="focus__tips-desc">结合课堂互动数据，及时与学生沟通，提升学习成效。</text>
          <view class="tips-chart">
            <view class="tips-chart__slice" v-for="slice in tipsChart" :key="slice.label" :style="sliceStyle(slice)">
              <text class="tips-chart__label">{{ slice.label }}</text>
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
      todayLessons: [
        { time: '08:30', name: '高等数学', room: '第一教学楼 302' },
        { time: '10:20', name: '线性代数', room: '第一教学楼 407' },
        { time: '14:00', name: '数据分析实践', room: '实验楼 B203' }
      ],
      engagement: [
        { week: '第1周', value: 68 },
        { week: '第2周', value: 74 },
        { week: '第3周', value: 86 },
        { week: '第4周', value: 92 }
      ],
      assignments: [
        { icon: '📝', title: '待批改作业', desc: '上周提交：数据结构', count: 12 },
        { icon: '💡', title: '课堂反馈', desc: '学生互动与提问', count: 28 },
        { icon: '📈', title: '成绩分析', desc: '阶段成绩更新', count: 3 }
      ],
      students: [
        { name: '李晓', tag: '需跟进', tagType: 'warning', note: '最近两次缺勤需关注出勤情况。' },
        { name: '王亮', tag: '优秀', tagType: 'success', note: '课堂互动积极，可鼓励分享经验。' },
        { name: '陈蕾', tag: '待辅导', tagType: 'info', note: '对新知识点反馈困难，需安排答疑。' }
      ],
      tipsChart: [
        { label: '出勤', color: '#74b9ff', size: 40 },
        { label: '互动', color: '#00b894', size: 32 },
        { label: '作业', color: '#fdcb6e', size: 28 }
      ]
    }
  },
  methods: {
    createLesson() {
      uni.showToast({ title: '已创建课程模版', icon: 'success' })
    },
    goHome() {
      uni.navigateBack()
    },
    sliceStyle(slice) {
      return {
        backgroundColor: slice.color,
        height: slice.size + '%'
      }
    }
  }
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f5f9ff;
}

.banner {
  display: flex;
  justify-content: space-between;
  padding: 72rpx 40rpx 64rpx;
  background: linear-gradient(135deg, #00b894 0%, #55efc4 100%);
  border-radius: 0 0 48rpx 48rpx;
  color: #ffffff;
  gap: 32rpx;
}

.banner__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.banner__badge {
  font-size: 24rpx;
  letter-spacing: 4rpx;
  opacity: 0.8;
}

.banner__title {
  font-size: 44rpx;
  font-weight: 700;
}

.banner__subtitle {
  font-size: 26rpx;
  line-height: 40rpx;
  opacity: 0.85;
  max-width: 480rpx;
}

.banner__actions {
  display: flex;
  gap: 20rpx;
}

.banner__schedule {
  width: 300rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 28rpx;
  padding: 28rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  backdrop-filter: blur(8rpx);
}

.schedule__title {
  font-size: 28rpx;
  font-weight: 600;
}

.schedule__items {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.schedule__item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  font-size: 24rpx;
}

.schedule__time {
  font-weight: 600;
}

.schedule__room {
  opacity: 0.8;
}

.primary,
.secondary {
  padding: 18rpx 32rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
}

.primary {
  background: #ffffff;
  color: #00b894;
}

.secondary {
  background: transparent;
  color: #ffffff;
  border: 2rpx solid rgba(255, 255, 255, 0.45);
}

.section {
  margin: 36rpx;
  background: #ffffff;
  border-radius: 32rpx;
  padding: 36rpx;
  box-shadow: 0 14rpx 32rpx rgba(31, 45, 61, 0.1);
}

.section__title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 28rpx;
}

.chart-card {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.chart-card__header {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.chart-card__title {
  font-size: 30rpx;
  font-weight: 600;
}

.chart-card__caption {
  font-size: 24rpx;
  color: #7a8999;
}

.chart-bar {
  display: flex;
  align-items: flex-end;
  gap: 32rpx;
  height: 240rpx;
}

.chart-bar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.chart-bar__fill {
  width: 60rpx;
  background: linear-gradient(180deg, #55efc4 0%, #00b894 100%);
  border-radius: 28rpx 28rpx 14rpx 14rpx;
}

.chart-bar__label {
  font-size: 24rpx;
  color: #7a8999;
}

.cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24rpx;
}

.card {
  background: #f6fff9;
  border-radius: 28rpx;
  padding: 28rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.card__icon {
  font-size: 36rpx;
}

.card__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.card__title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.card__desc {
  font-size: 24rpx;
  color: #7a8999;
}

.card__count {
  font-size: 30rpx;
  font-weight: 600;
  color: #00b894;
}

.focus {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24rpx;
}

.focus__list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.focus__item {
  background: #f8fbff;
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.focus__name {
  font-size: 28rpx;
  font-weight: 600;
}

.focus__tag {
  width: fit-content;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  color: #ffffff;
}

.focus__tag.is-warning {
  background: linear-gradient(135deg, #fdcb6e, #e17055);
}

.focus__tag.is-success {
  background: linear-gradient(135deg, #55efc4, #00b894);
}

.focus__tag.is-info {
  background: linear-gradient(135deg, #74b9ff, #0984e3);
}

.focus__note {
  font-size: 24rpx;
  color: #7a8999;
  line-height: 36rpx;
}

.focus__tips {
  background: #f0fff6;
  border-radius: 28rpx;
  padding: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.focus__tips-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.focus__tips-desc {
  font-size: 24rpx;
  color: #7a8999;
  line-height: 36rpx;
}

.tips-chart {
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
  height: 180rpx;
}

.tips-chart__slice {
  flex: 1;
  border-radius: 24rpx 24rpx 12rpx 12rpx;
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.tips-chart__label {
  position: absolute;
  bottom: -36rpx;
  font-size: 22rpx;
  color: #7a8999;
}

@media screen and (max-width: 960rpx) {
  .banner {
    flex-direction: column;
  }

  .banner__schedule {
    width: 100%;
    flex-direction: row;
  }

  .cards {
    grid-template-columns: 1fr;
  }

  .focus {
    grid-template-columns: 1fr;
  }
}
</style>
