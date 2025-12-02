<template>
  <view class="index-page">
    <view class="bg-orb orb-one"></view>
    <view class="bg-orb orb-two"></view>
    <view class="bg-pattern"></view>

    <view class="topbar">
      <view class="brand">
        <image class="brand-logo" src="/static/logo.png" mode="aspectFit"></image>
        <view>
          <text class="brand-name">Tsuki 校园</text>
          <text class="brand-desc">学生专属招聘 · 智能匹配</text>
        </view>
      </view>
      <view class="pill soft">每日更新岗位 · 保持领先</view>
    </view>

    <view class="hero-card">
      <view class="hero-grid">
        <view class="hero-copy">
          <view class="pill">AI 助力 · 校招加速</view>
          <text class="hero-title">{{ title }}</text>
          <text class="hero-subtitle">
            以同学的视角重新设计：更亮眼的视觉，更顺手的入口。发现岗位、打磨简历、预约宣讲会，一站式完成。
          </text>
          <view class="hero-actions">
            <button class="primary-btn">立即开始求职</button>
            <button class="ghost-btn">完善我的简历</button>
          </view>
          <view class="hero-stats">
            <view class="stat-card" v-for="stat in stats" :key="stat.label">
              <text class="stat-value">{{ stat.value }}</text>
              <text class="stat-label">{{ stat.label }}</text>
            </view>
          </view>
        </view>

        <view class="hero-visual">
          <view class="glass-panel">
            <view class="panel-header">
              <text class="panel-title">今日亮点</text>
              <view class="badge">精选</view>
            </view>
            <view class="timeline">
              <view class="timeline-item" v-for="item in highlights" :key="item.title">
                <view class="dot" :style="{ background: item.color }"></view>
                <view class="timeline-text">
                  <text class="timeline-title">{{ item.title }}</text>
                  <text class="timeline-desc">{{ item.desc }}</text>
                </view>
              </view>
            </view>
            <view class="panel-footer">
              <text>宣讲会 · 双选会一键订阅</text>
              <view class="badge ghost">同步提醒</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <view>
          <text class="section-title">即刻操作</text>
          <text class="section-subtitle">常用入口集中摆放，减少跳转成本</text>
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
          <view class="chip">{{ item.tip }}</view>
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
        <view class="pill ghost">智能记录进度</view>
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
          gradient: 'linear-gradient(135deg, #60a5fa, #1d4ed8)',
          tip: '30 秒完成'
        },
        {
          title: '简历快速美化',
          desc: '导入模板，10 分钟打造亮眼简历',
          icon: '✨',
          gradient: 'linear-gradient(135deg, #fbbf24, #f97316)',
          tip: 'AI 辅助'
        },
        {
          title: '宣讲会日历',
          desc: '不错过每一次校园宣讲与笔试',
          icon: '📅',
          gradient: 'linear-gradient(135deg, #34d399, #059669)',
          tip: '订阅提醒'
        },
        {
          title: '投递进度追踪',
          desc: '实时掌握面试进展与反馈',
          icon: '🛰️',
          gradient: 'linear-gradient(135deg, #a78bfa, #6d28d9)',
          tip: '秒级更新'
        }
      ],
      highlights: [
        { title: '热门岗位推荐', desc: '前端 / 数据 / 产品 3 个高匹配岗位', color: '#60a5fa' },
        { title: '宣讲会提醒', desc: '今晚 19:00 未来数据 · 秋招攻略', color: '#f59e0b' },
        { title: '简历优化建议', desc: '技能关键词覆盖率 92%，可再提升', color: '#22c55e' }
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
  padding: 56rpx 32rpx 100rpx;
  min-height: 100vh;
  background: linear-gradient(140deg, #f6f7ff 0%, #eef6ff 45%, #fdf3ff 100%);
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  width: 520rpx;
  height: 520rpx;
  border-radius: 50%;
  filter: blur(16rpx);
  opacity: 0.65;
  z-index: 1;
}

.orb-one {
  top: -160rpx;
  left: -120rpx;
  background: radial-gradient(circle at 30% 30%, rgba(99, 102, 241, 0.18), transparent 60%),
    radial-gradient(circle at 70% 70%, rgba(59, 130, 246, 0.18), transparent 60%);
}

.orb-two {
  bottom: -200rpx;
  right: -120rpx;
  background: radial-gradient(circle at 40% 40%, rgba(236, 72, 153, 0.18), transparent 60%),
    radial-gradient(circle at 80% 20%, rgba(14, 165, 233, 0.2), transparent 60%);
}

.bg-pattern {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle at 24rpx 24rpx, rgba(37, 99, 235, 0.06) 1rpx, transparent 0);
  background-size: 120rpx 120rpx;
  opacity: 0.5;
  z-index: 1;
}

.topbar {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.brand-logo {
  width: 60rpx;
  height: 60rpx;
  border-radius: 18rpx;
  background: #fff;
  box-shadow: 0 10rpx 28rpx rgba(37, 99, 235, 0.2);
}

.brand-name {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}

.brand-desc {
  display: block;
  font-size: 22rpx;
  color: #475569;
}

.pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
  font-size: 22rpx;
  font-weight: 700;
}

.pill.soft {
  background: rgba(15, 23, 42, 0.06);
  color: #0f172a;
}

.pill.ghost {
  background: rgba(255, 255, 255, 0.7);
  color: #0f172a;
  border: 1rpx solid rgba(148, 163, 184, 0.3);
}

.hero-card {
  position: relative;
  z-index: 2;
  margin-top: 12rpx;
  padding: 34rpx 30rpx 40rpx;
  background: rgba(255, 255, 255, 0.82);
  border-radius: 30rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 28rpx 60rpx rgba(15, 23, 42, 0.08), inset 0 0 0 1rpx rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(14rpx);
}

.hero-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24rpx;
  align-items: stretch;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.hero-title {
  font-size: 52rpx;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -1rpx;
}

.hero-subtitle {
  font-size: 26rpx;
  color: #334155;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 14rpx;
  margin-top: 6rpx;
}

.primary-btn {
  flex: 1;
  padding: 18rpx 24rpx;
  border-radius: 16rpx;
  border: none;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white;
  font-size: 26rpx;
  font-weight: 800;
  box-shadow: 0 22rpx 44rpx rgba(37, 99, 235, 0.32);
}

.ghost-btn {
  flex: 1;
  padding: 18rpx 24rpx;
  border-radius: 16rpx;
  border: 1rpx solid rgba(37, 99, 235, 0.2);
  background: #ffffff;
  color: #1d4ed8;
  font-size: 26rpx;
  font-weight: 800;
  box-shadow: 0 10rpx 24rpx rgba(37, 99, 235, 0.12);
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160rpx, 1fr));
  gap: 14rpx;
  margin-top: 18rpx;
}

.stat-card {
  padding: 18rpx 16rpx;
  border-radius: 18rpx;
  background: linear-gradient(165deg, rgba(59, 130, 246, 0.08), rgba(79, 70, 229, 0.06));
  border: 1rpx solid rgba(148, 163, 184, 0.24);
  box-shadow: 0 14rpx 26rpx rgba(15, 23, 42, 0.06);
}

.stat-value {
  display: block;
  font-size: 34rpx;
  font-weight: 900;
  color: #111827;
}

.stat-label {
  font-size: 22rpx;
  color: #475569;
}

.hero-visual {
  position: relative;
}

.glass-panel {
  height: 100%;
  border-radius: 22rpx;
  padding: 20rpx 18rpx;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0.88));
  border: 1rpx solid rgba(148, 163, 184, 0.25);
  box-shadow: 0 20rpx 46rpx rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.panel-header,
.panel-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10rpx;
}

.panel-title {
  font-size: 26rpx;
  font-weight: 800;
}

.badge {
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
  font-size: 20rpx;
  font-weight: 700;
}

.badge.ghost {
  background: rgba(15, 23, 42, 0.06);
  color: #0f172a;
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.timeline-item {
  display: grid;
  grid-template-columns: 22rpx 1fr;
  gap: 12rpx;
  align-items: flex-start;
  padding: 12rpx;
  border-radius: 14rpx;
  background: rgba(248, 250, 252, 0.9);
  border: 1rpx solid rgba(226, 232, 240, 0.8);
}

.dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  margin-top: 6rpx;
}

.timeline-title {
  font-size: 24rpx;
  font-weight: 800;
  color: #0f172a;
}

.timeline-desc {
  font-size: 22rpx;
  color: #475569;
}

.section {
  position: relative;
  z-index: 2;
  margin-top: 36rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.9);
  border: 1rpx solid rgba(148, 163, 184, 0.24);
  box-shadow: 0 18rpx 36rpx rgba(15, 23, 42, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  margin-bottom: 18rpx;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: 900;
  color: #0f172a;
}

.section-subtitle {
  display: block;
  font-size: 24rpx;
  color: #64748b;
  margin-top: 4rpx;
}

.section-link {
  color: #2563eb;
  font-size: 24rpx;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240rpx, 1fr));
  gap: 16rpx;
}

.action-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 18rpx;
  border-radius: 18rpx;
  border: 1rpx solid rgba(148, 163, 184, 0.25);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 16rpx 30rpx rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.chip {
  position: absolute;
  right: 14rpx;
  top: 14rpx;
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(15, 23, 42, 0.06);
  color: #0f172a;
  font-size: 20rpx;
}

.action-icon {
  width: 76rpx;
  height: 76rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36rpx;
  box-shadow: 0 12rpx 24rpx rgba(15, 23, 42, 0.12);
}

.action-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
}

.action-desc {
  font-size: 24rpx;
  color: #475569;
}

.opportunity-list {
  display: flex;
  gap: 16rpx;
  padding-bottom: 6rpx;
}

.opportunity-card {
  min-width: 280rpx;
  padding: 18rpx;
  border-radius: 20rpx;
  background: linear-gradient(160deg, rgba(59, 130, 246, 0.12), rgba(14, 165, 233, 0.08));
  border: 1rpx solid rgba(59, 130, 246, 0.2);
  box-shadow: 0 16rpx 28rpx rgba(15, 23, 42, 0.08);
}

.op-tag {
  display: inline-flex;
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.9);
  color: #1d4ed8;
  font-weight: 700;
  font-size: 22rpx;
}

.op-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  margin: 12rpx 0 6rpx;
  color: #0f172a;
}

.op-company {
  display: block;
  font-size: 24rpx;
  color: #1d4ed8;
  margin-bottom: 10rpx;
}

.op-meta {
  display: flex;
  justify-content: space-between;
  font-size: 22rpx;
  color: #475569;
}

.steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260rpx, 1fr));
  gap: 18rpx;
}

.step-card {
  position: relative;
  overflow: hidden;
  padding: 20rpx;
  border-radius: 20rpx;
  background: linear-gradient(165deg, rgba(99, 102, 241, 0.08), rgba(59, 130, 246, 0.06));
  border: 1rpx solid rgba(59, 130, 246, 0.22);
  box-shadow: 0 16rpx 32rpx rgba(15, 23, 42, 0.06);
}

.step-index {
  position: absolute;
  top: 12rpx;
  right: 14rpx;
  font-size: 44rpx;
  font-weight: 900;
  color: rgba(37, 99, 235, 0.08);
}

.step-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 6rpx;
}

.step-desc {
  display: block;
  font-size: 24rpx;
  color: #475569;
}

.step-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 12rpx;
}

.step-tag {
  padding: 8rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.88);
  color: #1d4ed8;
  font-size: 22rpx;
  border: 1rpx solid rgba(59, 130, 246, 0.2);
}

@media (max-width: 720px) {
  .index-page {
    padding: 38rpx 22rpx 80rpx;
  }

  .hero-grid {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    flex-direction: column;
  }

  .topbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 10rpx;
  }
}
</style>
