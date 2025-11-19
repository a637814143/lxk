<template>
  <section class="card">
    <div class="card__title">
      <h2>讨论审核</h2>
      <div class="tabs">
        <button :class="['tab', activeTab==='posts' ? 'active' : '']" @click="activeTab='posts'">帖子</button>
        <button :class="['tab', activeTab==='comments' ? 'active' : '']" @click="activeTab='comments'">评论</button>
      </div>
      <button class="outline" @click="refresh">刷新</button>
    </div>

    <ul v-if="activeTab==='posts' && pendingDiscussions.length" class="list">
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
    <p v-else-if="activeTab==='posts'" class="muted">暂无待审核的讨论内容</p>

    <ul v-if="activeTab==='comments' && pendingComments.length" class="list">
      <li v-for="c in pendingComments" :key="c.id" class="list__item">
        <div>
          <h3>评论 · 帖子 #{{ c.postId }}</h3>
          <p class="muted">作者：{{ c.authorUsername }} · 时间：{{ formatDate(c.createdAt) }}</p>
          <p>{{ c.sanitizedContent || c.content }}</p>
          <p v-if="c.reviewComment" class="muted">备注：{{ c.reviewComment }}</p>
        </div>
        <div class="list__actions">
          <button class="primary" @click="reviewComment(c.id, 'approved')">通过</button>
          <button class="outline" @click="openRejectCommentDialog(c)">驳回</button>
        </div>
      </li>
    </ul>
    <p v-else-if="activeTab==='comments'" class="muted">暂无待审核的评论</p>

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

  <section v-if="rejectCommentDialog.visible" class="card">
    <h3>评论驳回备注</h3>
    <form class="form-grid" @submit.prevent="submitRejectComment">
      <label class="full">备注<textarea v-model="rejectCommentDialog.reason" maxlength="255" placeholder="请输入驳回原因"></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">确认驳回</button>
        <button class="outline" type="button" @click="closeRejectCommentDialog">取消</button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, post } from '../../api/http';
import { useToast } from '../../ui/toast';

const pendingDiscussions = ref([]);
const activeTab = ref('posts');
const pendingComments = ref([]);
const toast = useToast();

const rejectDialog = reactive({
  visible: false,
  discussionId: null,
  reason: ''
});

onMounted(async () => {
  await loadPendingDiscussions();
  await loadPendingComments();
});

async function loadPendingDiscussions() {
  try {
    pendingDiscussions.value = await get('/portal/admin/discussions/pending');
  } catch (error) {
    toast.error(error.message ?? '加载待审核讨论失败');
  }
}

async function loadPendingComments() {
  try {
    pendingComments.value = await get('/portal/admin/discussions/comments/pending');
  } catch (error) {
    // ignore
  }
}

async function refresh() {
  await Promise.all([loadPendingDiscussions(), loadPendingComments()]);
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

// 评论审核对话框及方法
const rejectCommentDialog = reactive({ visible: false, commentId: null, reason: '' });

function openRejectCommentDialog(c) {
  rejectCommentDialog.visible = true;
  rejectCommentDialog.commentId = c.id;
  rejectCommentDialog.reason = '';
}

function closeRejectCommentDialog() {
  rejectCommentDialog.visible = false;
  rejectCommentDialog.commentId = null;
  rejectCommentDialog.reason = '';
}

async function submitRejectComment() {
  if (!rejectCommentDialog.commentId) return;
  await reviewComment(rejectCommentDialog.commentId, 'rejected', rejectCommentDialog.reason);
  closeRejectCommentDialog();
}

async function reviewComment(commentId, status, reason = '') {
  try {
    const payload = { status, reviewComment: reason };
    await post(`/portal/admin/discussions/comments/${commentId}/review`, payload);
    await loadPendingComments();
  } catch (error) {
    // ignore; 统一由上层提示
  }
}
</script>

<style scoped>
.list__actions { display: flex; gap: 12px; margin-top: 12px; }
.tabs { display: flex; gap: 8px; align-items: center; }
.tab { border: 1px solid #d1d5db; background: #fff; padding: 6px 12px; border-radius: 8px; cursor: pointer; }
.tab.active { background: #2563eb; color: #fff; border-color: #2563eb; }
</style>
