<template>
  <view class="index-page">
    <view class="hero-blob"></view>
    <view class="hero-blob secondary"></view>

    <view class="hero-card">
      <view class="hero-top">
        <view class="hero-text">
          <view class="pill">学生专属 · 校园求职</view>
          <text class="hero-title">{{ title }}</text>
          <text class="hero-subtitle">
            智能匹配实习与校招岗位，沉浸式体验为你量身打造。查看企业热招、打磨简历、收集宣讲会资讯，一站解决。
          </text>
          <view class="hero-actions">
            <button class="primary-btn">立即开始求职</button>
            <button class="ghost-btn">完善我的简历</button>
          </view>
          <view class="hero-stats">
            <view class="stat-item" v-for="stat in stats" :key="stat.label">
              <text class="stat-value">{{ stat.value }}</text>
              <text class="stat-label">{{ stat.label }}</text>
            </view>
          </view>
        </view>
        <view class="hero-illustration">
          <image class="hero-logo" src="/static/logo.png" mode="aspectFit"></image>
          <view class="bubble bubble-one"></view>
          <view class="bubble bubble-two"></view>
          <view class="bubble bubble-three"></view>
        </view>
      </view>

      <view class="quick-actions">
        <view class="action-card" v-for="item in actions" :key="item.title">
          <view class="action-icon" :style="{ background: item.gradient }">
            <text class="icon-text">{{ item.icon }}</text>
          </view>
          <view class="action-info">
            <text class="action-title">{{ item.title }}</text>
            <text class="action-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <view>
            <text class="section-title">为你精选</text>
            <text class="section-subtitle">热门岗位与校招活动一目了然</text>
          </view>
          <text class="section-link">查看更多</text>
        </view>
        <scroll-view class="opportunity-list" scroll-x>
          <view class="opportunity-card" v-for="item in opportunities" :key="item.title">
            <view class="op-tag">{{ item.type }}</view>
            <text class="op-title">{{ item.title }}</text>
            <text class="op-company">{{ item.company }}</text>
            <view class="op-meta">
              <text>{{ item.location }}</text>
              <text>{{ item.time }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="section">
        <view class="section-header">
          <view>
            <text class="section-title">成长路径</text>
            <text class="section-subtitle">跟随步骤，轻松完成校园求职</text>
          </view>
        </view>
        <view class="steps">
          <view class="step-card" v-for="(item, index) in steps" :key="item.title">
            <view class="step-index">0{{ index + 1 }}</view>
            <view class="step-content">
              <text class="step-title">{{ item.title }}</text>
              <text class="step-desc">{{ item.desc }}</text>
              <view class="step-tags">
                <text class="step-tag" v-for="tag in item.tags" :key="tag">{{ tag }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      title: '校园招聘平台',
      stats: [
        { label: '岗位更新', value: '120+' },
        { label: '合作企业', value: '60家' },
        { label: '平均匹配度', value: '93%' }
      ],
      actions: [
        {
          title: '智能职位匹配',
          desc: '输入兴趣与专业，获取专属推荐',
          icon: '🎯',
          gradient: 'linear-gradient(135deg, #60a5fa, #1d4ed8)'
        },
        {
          title: '简历快速美化',
          desc: '导入模板，10 分钟打造亮眼简历',
          icon: '✨',
          gradient: 'linear-gradient(135deg, #fbbf24, #f97316)'
        },
        {
          title: '宣讲会日历',
          desc: '不错过每一次校园宣讲与笔试',
          icon: '📅',
          gradient: 'linear-gradient(135deg, #34d399, #059669)'
        },
        {
          title: '投递进度追踪',
          desc: '实时掌握面试进展与反馈',
          icon: '🛰️',
          gradient: 'linear-gradient(135deg, #a78bfa, #6d28d9)'
        }
      ],
      opportunities: [
        {
          type: '实习',
          title: '前端开发实习生',
          company: '星辰科技',
          location: '上海 · 混合办公',
          time: '每周 3 天'
        },
        {
          type: '校招',
          title: '数据分析管培生',
          company: '未来数据',
          location: '北京 · 全职',
          time: '24 届'
        },
        {
          type: '实习',
          title: '产品助理',
          company: '创想工作室',
          location: '深圳 · 远程灵活',
          time: '即时入职'
        },
        {
          type: '校招',
          title: '算法培训生',
          company: '启航智能',
          location: '杭州 · 全职',
          time: '双休 · 五险一金'
        }
      ],
      steps: [
        {
          title: '完善职业画像',
          desc: '选择专业、兴趣与期望城市，系统生成求职画像。',
          tags: ['AI 画像', '城市偏好']
        },
        {
          title: '获取岗位推荐',
          desc: '根据画像与招聘趋势，为你推送匹配度最高的职位。',
          tags: ['实时推荐', '行业榜单']
        },
        {
          title: '一键投递 & 追踪',
          desc: '提交简历后自动同步投递状态，面试提醒不错过。',
          tags: ['智能提醒', '进度更新']
        }
      ]
    };
  }
};
</script>

<style>
.index-page {
  position: relative;
  flex: 1;
  padding: 60rpx 32rpx 120rpx;
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f5ff 0%, #e0f7ff 35%, #f5f3ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-blob {
  position: absolute;
  width: 520rpx;
  height: 520rpx;
  background: radial-gradient(circle at 30% 30%, rgba(99, 102, 241, 0.14), transparent 55%),
    radial-gradient(circle at 80% 80%, rgba(59, 130, 246, 0.18), transparent 55%);
  filter: blur(10rpx);
  top: -120rpx;
  left: -80rpx;
  z-index: 1;
}

.hero-blob.secondary {
  right: -120rpx;
  bottom: -80rpx;
  left: auto;
  top: auto;
  background: radial-gradient(circle at 50% 50%, rgba(16, 185, 129, 0.14), transparent 55%),
    radial-gradient(circle at 90% 10%, rgba(96, 165, 250, 0.16), transparent 55%);
}

.hero-card {
  position: relative;
  width: 100%;
  max-width: 900rpx;
  padding: 44rpx 36rpx;
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 22rpx 60rpx rgba(15, 23, 42, 0.12);
  backdrop-filter: blur(18rpx);
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  box-sizing: border-box;
  z-index: 2;
}

.hero-top {
  display: flex;
  gap: 28rpx;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
}

.hero-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.pill {
  align-self: flex-start;
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.14), rgba(16, 185, 129, 0.12));
  color: #1d4ed8;
  font-size: 22rpx;
  font-weight: 600;
}

.hero-title {
  font-size: 44rpx;
  font-weight: 700;
  color: #0f172a;
}

.hero-subtitle {
  font-size: 26rpx;
  line-height: 1.6;
  color: #4b5563;
}

.hero-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 8rpx;
}

.primary-btn,
.ghost-btn {
  padding: 16rpx 28rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
  font-weight: 600;
  border: none;
}

.primary-btn {
  color: #ffffff;
  background: linear-gradient(135deg, #2563eb, #1e3a8a);
  box-shadow: 0 12rpx 28rpx rgba(37, 99, 235, 0.26);
}

.ghost-btn {
  color: #1d4ed8;
  background: #eff6ff;
  border: 2rpx solid rgba(37, 99, 235, 0.16);
}

.hero-stats {
  display: flex;
  gap: 36rpx;
  margin-top: 18rpx;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.stat-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #111827;
}

.stat-label {
  font-size: 22rpx;
  color: #6b7280;
}

.hero-illustration {
  width: 240rpx;
  height: 240rpx;
  position: relative;
  background: radial-gradient(circle at 50% 50%, rgba(59, 130, 246, 0.12), rgba(16, 185, 129, 0.12));
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-logo {
  width: 160rpx;
  height: 160rpx;
  z-index: 2;
}

.bubble {
  position: absolute;
  border-radius: 50%;
  filter: blur(4rpx);
}

.bubble-one {
  width: 120rpx;
  height: 120rpx;
  background: rgba(59, 130, 246, 0.16);
  top: -40rpx;
  left: 10rpx;
}

.bubble-two {
  width: 160rpx;
  height: 160rpx;
  background: rgba(16, 185, 129, 0.18);
  bottom: -50rpx;
  right: -20rpx;
}

.bubble-three {
  width: 90rpx;
  height: 90rpx;
  background: rgba(59, 130, 246, 0.2);
  bottom: 20rpx;
  left: -30rpx;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320rpx, 1fr));
  gap: 18rpx;
}

.action-card {
  display: flex;
  gap: 14rpx;
  padding: 22rpx;
  border-radius: 24rpx;
  background: #f8fafc;
  border: 2rpx solid #e5e7eb;
  box-shadow: 0 12rpx 30rpx rgba(148, 163, 184, 0.18);
}

.action-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 32rpx;
  box-shadow: 0 12rpx 30rpx rgba(0, 0, 0, 0.08);
}

.icon-text {
  transform: translateY(-4rpx);
}

.action-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.action-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.action-desc {
  font-size: 24rpx;
  color: #4b5563;
  line-height: 1.5;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #0f172a;
}

.section-subtitle {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #6b7280;
}

.section-link {
  font-size: 24rpx;
  color: #2563eb;
}

.opportunity-list {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  padding: 8rpx 2rpx 2rpx;
  white-space: nowrap;
}

.opportunity-card {
  width: 280rpx;
  display: inline-flex;
  flex-direction: column;
  gap: 10rpx;
  padding: 20rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 16rpx 30rpx rgba(15, 23, 42, 0.12);
  border: 2rpx solid #e5e7eb;
}

.op-tag {
  align-self: flex-start;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: #e0f2fe;
  color: #0369a1;
  font-size: 22rpx;
  font-weight: 600;
}

.op-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.op-company {
  font-size: 24rpx;
  color: #475569;
}

.op-meta {
  display: flex;
  justify-content: space-between;
  font-size: 22rpx;
  color: #64748b;
}

.steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280rpx, 1fr));
  gap: 16rpx;
}

.step-card {
  display: flex;
  gap: 14rpx;
  padding: 22rpx;
  border-radius: 22rpx;
  background: #ffffff;
  border: 2rpx solid #e2e8f0;
  box-shadow: 0 14rpx 36rpx rgba(148, 163, 184, 0.2);
}

.step-index {
  font-size: 32rpx;
  font-weight: 800;
  color: #1d4ed8;
  opacity: 0.85;
}

.step-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.step-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #0f172a;
}

.step-desc {
  font-size: 22rpx;
  color: #4b5563;
  line-height: 1.6;
}

.step-tags {
  display: flex;
  gap: 8rpx;
  flex-wrap: wrap;
}

.step-tag {
  padding: 6rpx 14rpx;
  border-radius: 12rpx;
  background: #eef2ff;
  color: #4338ca;
  font-size: 20rpx;
}

@media (max-width: 720px) {
  .index-page {
    padding: 48rpx 28rpx 80rpx;
  }

  .hero-card {
    padding: 36rpx 28rpx;
    gap: 24rpx;
  }

  .hero-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-illustration {
    align-self: center;
  }

  .hero-actions {
    flex-wrap: wrap;
  }
}
</style>
