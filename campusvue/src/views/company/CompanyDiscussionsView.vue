<template>
  <section class="card">
    <div class="card__title">
      <h2>企业讨论区</h2>
      <button class="outline" @click="loadDiscussions">刷新</button>
    </div>

    <form class="form-grid" @submit.prevent="createDiscussion">
      <label class="full">讨论主题<input v-model="discussionForm.title" required maxlength="100" /></label>
      <label class="full">讨论内容<textarea v-model="discussionForm.content" required maxlength="1000"></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">提交审核</button>
        <button class="outline" type="button" @click="resetDiscussionForm">清空</button>
      </div>
    </form>

    <ul class="list" v-if="discussions.length">
      <li v-for="item in discussions" :key="item.id" class="list__item">
        <div>
          <h3>{{ item.title }}</h3>
          <p class="muted">状态：{{ translateStatus(item.status) }} · 提交时间：{{ formatDate(item.createdAt) }}</p>
          <p>{{ item.sanitizedContent || item.content }}</p>
          <p v-if="item.reviewComment" class="muted">审核备注：{{ item.reviewComment }}</p>
        </div>
      </li>
    </ul>
    <p v-else class="muted">暂无讨论内容，欢迎提交与学生交流的话题。</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, post } from '../../api/http';
import { useToast } from '../../ui/toast';

const discussions = ref([]);
const discussionForm = reactive({ title: '', content: '' });
const toast = useToast();

onMounted(loadDiscussions);

async function loadDiscussions() {
  try {
    discussions.value = await get('/portal/company/discussions');
  } catch (error) {
    toast.error(error.message ?? '加载讨论内容失败');
  }
}

async function createDiscussion() {
  if (!discussionForm.title || !discussionForm.content) {
    toast.error('请填写讨论主题和内容');
    return;
  }
  try {
    const created = await post('/portal/company/discussions', discussionForm);
    discussions.value.unshift(created);
    toast.success('讨论已提交审核');
    resetDiscussionForm();
  } catch (error) {
    toast.error(error.message ?? '提交讨论失败');
  }
}

function resetDiscussionForm() { discussionForm.title = ''; discussionForm.content = ''; }

function translateStatus(status) {
  switch ((status || '').toLowerCase()) {
    case 'approved': return '已发布';
    case 'rejected': return '已驳回';
    case 'pending': return '待审核';
    default: return status || '-';
  }
}

function formatDate(value) { if (!value) return '-'; return new Date(value).toLocaleString(); }
</script>

<style scoped>
.actions { display: flex; gap: 12px; }
.list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 12px; }
.list__item { padding: 16px; border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; }
</style>

