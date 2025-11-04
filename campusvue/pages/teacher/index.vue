<template>
  <view class="dashboard-page teacher-page">
    <view class="dashboard-layout">
      <view class="dashboard-sidebar">
        <view class="sidebar__header">
          <text class="sidebar__title">教师教学驾驶舱</text>
          <text class="sidebar__subtitle">Teaching Workspace</text>
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
          <view class="teacher-hero">
            <view class="teacher-hero__content">
              <text class="teacher-hero__badge">TEACHER</text>
              <text class="teacher-hero__title">教师教学驾驶舱</text>
              <text class="teacher-hero__subtitle">
                查看课程安排、学习反馈与教学数据分析，轻松掌握课堂节奏。
              </text>
              <view class="teacher-hero__actions">
                <button class="teacher-hero__button teacher-hero__button--primary" @click="createLesson">
                  新建课程
                </button>
                <button class="teacher-hero__button" @click="openCalendar">教学日程</button>
              </view>
            </view>
            <view class="teacher-hero__schedule">
              <text class="schedule__title">今日课程</text>
              <view class="schedule__items">
                <view class="schedule__item" v-for="lesson in todayLessons" :key="lesson.name">
                  <text class="schedule__time">{{ lesson.time }}</text>
                  <view class="schedule__info">
                    <text class="schedule__name">{{ lesson.name }}</text>
                    <text class="schedule__room">{{ lesson.room }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="section-block" id="section-performance">
          <view class="section__header">
            <text class="section__title">课堂表现</text>
            <text class="section__subtitle">近四周课堂参与度走势</text>
          </view>
          <view class="chart-card">
            <view class="chart-card__header">
              <text class="chart-card__title">课堂参与度</text>
              <text class="chart-card__caption">签到与互动反馈统计</text>
            </view>
            <view class="chart-bar">
              <view class="chart-bar__item" v-for="item in engagement" :key="item.week">
                <view class="chart-bar__fill" :style="{ height: item.value + '%' }"></view>
                <text class="chart-bar__label">{{ item.week }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="section-block" id="section-assignments">
          <view class="section__header">
            <text class="section__title">作业批改</text>
            <text class="section__subtitle">批改进度与反馈统计</text>
          </view>
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

        <view class="section-block" id="section-students">
          <view class="section__header">
            <text class="section__title">学生关注</text>
            <text class="section__subtitle">重点学生与教学提醒</text>
          </view>
          <view class="focus">
            <view class="focus__list">
              <view class="focus__item" v-for="student in students" :key="student.name">
                <view class="focus__info">
                  <text class="focus__name">{{ student.name }}</text>
                  <text class="focus__tag" :class="'is-' + student.tagType">{{ student.tag }}</text>
                </view>
                <text class="focus__note">{{ student.note }}</text>
              </view>
            </view>
            <view class="focus__tips">
              <text class="focus__tips-title">教学提醒</text>
              <text class="focus__tips-desc">
                结合课堂互动数据，及时与学生沟通，提升学习成效。
              </text>
              <view class="tips-chart">
                <view
                  class="tips-chart__slice"
                  v-for="slice in tipsChart"
                  :key="slice.label"
                  :style="sliceStyle(slice)"
                >
                  <text class="tips-chart__label">{{ slice.label }}</text>
                </view>
              </view>
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
        { label: '教学概览', target: 'section-overview' },
        { label: '课堂表现', target: 'section-performance' },
        { label: '作业批改', target: 'section-assignments' },
        { label: '学生关注', target: 'section-students' }
      ],
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
    createLesson() {
      uni.showToast({ title: '已创建课程模版', icon: 'success' })
    },
    openCalendar() {
      uni.showToast({ title: '打开教学日程', icon: 'none' })
    },
    goHome() {
      uni.navigateBack()
    },
    sliceStyle(slice) {
      return {
        backgroundColor: slice.color,
        flex: slice.size,
        borderRadius: '24rpx'
      }
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
  box-shadow: 0 20rpx 60rpx rgba(73, 168, 120, 0.08);
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
  color: #10423c;
}

.sidebar__subtitle {
  font-size: 22rpx;
  color: #6b8b7c;
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
  color: #5f7a6e;
  transition: background 0.2s ease;
}

.sidebar__item.is-active {
  background: rgba(0, 184, 148, 0.12);
  color: #00a892;
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
  background: linear-gradient(135deg, #00b894 0%, #55efc4 100%);
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
  box-shadow: 0 24rpx 60rpx rgba(16, 66, 60, 0.06);
}

.section-block--hero {
  padding: 0;
  background: none;
  box-shadow: none;
}

.teacher-hero {
  display: flex;
  align-items: stretch;
  gap: 32rpx;
  padding: 56rpx 48rpx;
  background: linear-gradient(135deg, #00b894 0%, #55efc4 100%);
  border-radius: 36rpx;
  color: #ffffff;
}

.teacher-hero__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.teacher-hero__badge {
  font-size: 24rpx;
  letter-spacing: 4rpx;
  opacity: 0.8;
}

.teacher-hero__title {
  font-size: 44rpx;
  font-weight: 700;
}

.teacher-hero__subtitle {
  font-size: 26rpx;
  line-height: 1.6;
  opacity: 0.85;
}

.teacher-hero__actions {
  display: flex;
  gap: 24rpx;
}

.teacher-hero__button {
  padding: 20rpx 32rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
  font-size: 26rpx;
}

.teacher-hero__button--primary {
  background: #ffffff;
  color: #009d82;
}

.teacher-hero__schedule {
  width: 280rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  background: rgba(255, 255, 255, 0.14);
  border-radius: 28rpx;
  padding: 32rpx 24rpx;
}

.schedule__title {
  font-size: 30rpx;
  font-weight: 600;
}

.schedule__items {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.schedule__item {
  display: flex;
  gap: 16rpx;
  align-items: flex-start;
}

.schedule__time {
  font-size: 24rpx;
  opacity: 0.9;
  width: 90rpx;
}

.schedule__info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.schedule__name {
  font-size: 28rpx;
  font-weight: 600;
}

.schedule__room {
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
  color: #10423c;
}

.section__subtitle {
  font-size: 26rpx;
  color: #6b8b7c;
}

.chart-card {
  padding: 32rpx;
  background: #f1fffa;
  border-radius: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.chart-card__header {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  color: #10423c;
}

.chart-card__title {
  font-size: 32rpx;
  font-weight: 600;
}

.chart-card__caption {
  font-size: 24rpx;
  color: #6b8b7c;
}

.chart-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24rpx;
}

.chart-bar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.chart-bar__fill {
  width: 64rpx;
  border-radius: 32rpx;
  background: linear-gradient(180deg, #00b894 0%, #55efc4 100%);
}

.chart-bar__label {
  font-size: 24rpx;
  color: #6b8b7c;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260rpx, 1fr));
  gap: 24rpx;
}

.card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, rgba(0, 184, 148, 0.12) 0%, rgba(0, 184, 148, 0.04) 100%);
  color: #10423c;
}

.card__icon {
  font-size: 40rpx;
}

.card__title {
  font-size: 30rpx;
  font-weight: 600;
}

.card__desc {
  font-size: 24rpx;
  color: #6b8b7c;
}

.card__count {
  margin-left: auto;
  font-size: 30rpx;
  font-weight: 600;
  color: #00a892;
}

.focus {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 28rpx;
}

.focus__list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.focus__item {
  padding: 24rpx;
  border-radius: 24rpx;
  background: #f1fffa;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.focus__info {
  display: flex;
  gap: 16rpx;
  align-items: center;
}

.focus__name {
  font-size: 30rpx;
  font-weight: 600;
  color: #10423c;
}

.focus__tag {
  font-size: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
}

.focus__tag.is-warning {
  background: rgba(255, 159, 67, 0.2);
  color: #ff9f43;
}

.focus__tag.is-success {
  background: rgba(85, 239, 196, 0.2);
  color: #00a892;
}

.focus__tag.is-info {
  background: rgba(9, 132, 227, 0.2);
  color: #0984e3;
}

.focus__note {
  font-size: 24rpx;
  color: #6b8b7c;
  line-height: 1.6;
}

.focus__tips {
  background: linear-gradient(180deg, rgba(0, 184, 148, 0.12) 0%, rgba(0, 184, 148, 0.04) 100%);
  border-radius: 28rpx;
  padding: 32rpx 28rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  color: #10423c;
}

.focus__tips-title {
  font-size: 32rpx;
  font-weight: 600;
}

.focus__tips-desc {
  font-size: 24rpx;
  color: #6b8b7c;
}

.tips-chart {
  display: flex;
  gap: 16rpx;
}

.tips-chart__slice {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24rpx 0;
  color: #ffffff;
  font-size: 24rpx;
}

.tips-chart__label {
  transform: rotate(-90deg);
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

  .teacher-hero {
    flex-direction: column;
  }

  .teacher-hero__schedule {
    width: 100%;
  }

  .focus {
    grid-template-columns: 1fr;
  }
}
</style>
