<template>
  <view class="index-page">
    <view class="hero-card">
      <image class="hero-logo" src="/static/logo.png" mode="aspectFit"></image>
      <view class="hero-text">
        <text class="hero-title">{{ title }}</text>
        <text class="hero-subtitle">连接学生、企业与管理员的校园招聘平台</text>
      </view>
      <view class="hero-tags">
        <text class="tag">学生求职</text>
        <text class="tag">企业招聘</text>
        <text class="tag">实习管理</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      title: '校园招聘平台'
    };
  }
};
</script>

<style>
.index-page {
  flex: 1;
  padding: 80rpx 40rpx;
  background-image: linear-gradient(135deg, #eff6ff, #e0f2fe);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-card {
  width: 100%;
  max-width: 700rpx;
  padding: 40rpx 32rpx;
  border-radius: 32rpx;
  background-color: #ffffff;
  box-shadow: 0 18rpx 60rpx rgba(15, 23, 42, 0.12);
  display: flex;
  flex-direction: column;
  align-items: center;
  box-sizing: border-box;
}

.hero-logo {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 24rpx;
}

.hero-text {
  align-items: center;
  text-align: center;
  margin-bottom: 24rpx;
}

.hero-title {
  font-size: 40rpx;
  font-weight: 600;
  color: #111827;
}

.hero-subtitle {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #6b7280;
}

.hero-tags {
  flex-direction: row;
  justify-content: center;
  flex-wrap: wrap;
}

.hero-tags,
.hero-text {
  display: flex;
}

.tag {
  margin: 6rpx 10rpx 0;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  background-color: #eff6ff;
  color: #1d4ed8;
  font-size: 22rpx;
}
</style>
