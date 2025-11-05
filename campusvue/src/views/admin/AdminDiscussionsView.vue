<template>
  <section class="card">
    <div class="card__title">
      <h2>讨论审核</h2>
      <button class="outline" @click="loadPendingDiscussions">刷新</button>
    </div>

    <ul class="list" v-if="pendingDiscussions.length">
      <li v-for="item in pendingDiscussions" :key="item.id" class="list__item">
        <div>
          <h3>{{ item.title }} <small class="muted">企业 #{{ item.companyId }}</small></h3>
          <p class="muted">提交时间：{{ formatDate(item.createdAt) }}</p>
          <p>{{ item.sanitizedContent || item.content }}</p>
          <p v-if="item.reviewComment" class="muted">备注：{{ item.reviewComment }}</p>
        </div>
        <div class="list__actions">
          <button class="primary" @click="review(item.id, 'approved')">通过</button>
          <button class="outline" @click="openRejectDialog(item)">驳回</button>
        </div>
      </li>
    </ul>
    <p v-else class="muted">暂无待审核的讨论内容</p>

    <section v-if="rejectDialog.visible" class="card">
      <h3>讨论驳回备注</h3>
      <form class="form-grid" @submit.prevent="submitReject">
        <label class="full">备注<textarea v-model="rejectDialog.reason" maxlength="255" placeholder="请输入驳回原因"></textarea></label>
        <div class="full actions">
          <button class="primary" type="submit">确认驳回</button>
          <button class="outline" type="button" @click="closeRejectDialog">取消</button>
        </div>
      </form>
    </section>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, post } from '../../api/http';
import { useToast } from '../../ui/toast';

const pendingDiscussions = ref([]);
const toast = useToast();

const rejectDialog = reactive({
  visible: false,
  discussionId: null,
  reason: ''
});

onMounted(() => {
  loadPendingDiscussions();
});

async function loadPendingDiscussions() {
  try {
    pendingDiscussions.value = await get('/portal/admin/discussions/pending');
  } catch (error) {
    toast.error(error.message ?? '加载待审核讨论失败');
  }
}

async function review(discussionId, status, reason = '') {
  try {
    const payload = { status, reviewComment: reason };
    await post(`/portal/admin/discussions/${discussionId}/review`, payload);
    toast.success('讨论审核已提交');
    await loadPendingDiscussions();
  } catch (error) {
    toast.error(error.message ?? '提交审核失败');
  }
}

function openRejectDialog(item) {
  rejectDialog.visible = true;
  rejectDialog.discussionId = item.id;
  rejectDialog.reason = '';
}

function closeRejectDialog() {
  rejectDialog.visible = false;
  rejectDialog.discussionId = null;
  rejectDialog.reason = '';
}

async function submitReject() {
  if (!rejectDialog.discussionId) return;
  await review(rejectDialog.discussionId, 'rejected', rejectDialog.reason);
  closeRejectDialog();
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString();
}
</script>

<style scoped>
.list__actions { display: flex; gap: 12px; margin-top: 12px; }
</style>

