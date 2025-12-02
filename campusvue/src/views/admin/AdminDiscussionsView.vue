<template>
  <section class="card">
    <div class="card__title">
      <h2>评论审核</h2>
      <button class="outline" @click="refresh">刷新</button>
    </div>

    <section>
      <h3 class="sub-title">待审核评论</h3>
      <ul v-if="pendingComments.length" class="list">
        <li v-for="c in pendingComments" :key="c.id" class="list__item">
          <div>
            <h3>评论 · 帖子 #{{ c.postId }}</h3>
            <p class="muted">
              作者：{{ c.authorUsername }} · 时间：{{ formatDate(c.createdAt) }}
              <span v-if="c.status" class="pill pill--ghost">状态：{{ c.status }}</span>
            </p>
            <p>{{ c.sanitizedContent || c.content }}</p>
            <p v-if="c.reviewComment" class="violation">提示：{{ c.reviewComment }}</p>
          </div>
          <div class="list__actions">
            <button class="primary" @click="reviewComment(c.id, 'approved')">通过</button>
            <button class="outline" @click="openRejectCommentDialog(c)">驳回</button>
            <button class="outline" type="button" @click="deleteComment(c.id)">删除</button>
          </div>
        </li>
      </ul>
      <p v-else class="muted">暂无待审核的评论</p>

      <h3 class="sub-title sub-title--spaced">全部评论</h3>
      <ul v-if="allComments.length" class="list">
        <li v-for="c in allComments" :key="`all-${c.id}`" class="list__item list__item--compact">
          <div>
            <h3>帖子 #{{ c.postId }} · 评论 #{{ c.id }}</h3>
            <p class="muted">
              作者：{{ c.authorUsername || '-' }} · 时间：{{ formatDate(c.createdAt) }} · 状态：{{ c.status || '-' }}
            </p>
            <p>{{ c.sanitizedContent || c.content }}</p>
            <p v-if="c.reviewComment" class="violation">提示：{{ c.reviewComment }}</p>
            <div class="list__actions">
              <button class="outline" type="button" @click="deleteComment(c.id)">删除</button>
            </div>
          </div>
        </li>
      </ul>
      <p v-else class="muted">暂无评论记录</p>
    </section>

    <!-- 帖子驳回对话框 -->
    <section v-if="rejectDialog.visible" class="card">
      <h3>讨论驳回备注</h3>
      <form class="form-grid" @submit.prevent="submitReject">
        <label class="full">
          备注
          <textarea
            v-model="rejectDialog.reason"
            maxlength="255"
            placeholder="请输入驳回原因"
          ></textarea>
        </label>
        <div class="full actions">
          <button class="primary" type="submit">确认驳回</button>
          <button class="outline" type="button" @click="closeRejectDialog">取消</button>
        </div>
      </form>
    </section>
  </section>

  <!-- 评论驳回对话框 -->
  <section v-if="rejectCommentDialog.visible" class="card">
    <h3>评论驳回备注</h3>
    <form class="form-grid" @submit.prevent="submitRejectComment">
      <label class="full">
        备注
        <textarea
          v-model="rejectCommentDialog.reason"
          maxlength="255"
          placeholder="请输入驳回原因"
        ></textarea>
      </label>
      <div class="full actions">
        <button class="primary" type="submit">确认驳回</button>
        <button class="outline" type="button" @click="closeRejectCommentDialog">取消</button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, post, del as httpDelete } from '../../api/http';
import { useToast } from '../../ui/toast';

const pendingComments = ref([]);
const allComments = ref([]);
const toast = useToast();

const rejectCommentDialog = reactive({ visible: false, commentId: null, reason: '' });

onMounted(async () => {
  await refresh();
});

async function loadPendingComments() {
  try {
    pendingComments.value = await get('/portal/admin/discussions/comments/pending');
  } catch {
    pendingComments.value = [];
  }
}

async function loadAllComments() {
  try {
    allComments.value = await get('/portal/admin/discussions/comments');
  } catch {
    allComments.value = [];
  }
}

async function refresh() {
  await Promise.all([
    loadPendingComments(),
    loadAllComments()
  ]);
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString('zh-CN', { hour12: false });
}

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
  try {
    await post(`/portal/admin/discussions/comments/${rejectCommentDialog.commentId}/review`, {
      status: 'rejected',
      reviewComment: rejectCommentDialog.reason
    });
    toast.success('评论已驳回');
    await Promise.all([loadPendingComments(), loadAllComments()]);
  } catch (error) {
    toast.error(error.message ?? '驳回评论失败');
  } finally {
    closeRejectCommentDialog();
  }
}

async function reviewComment(commentId, status, reason = '') {
  try {
    await post(`/portal/admin/discussions/comments/${commentId}/review`, {
      status,
      reviewComment: reason
    });
    toast.success('评论审核已提交');
    await Promise.all([loadPendingComments(), loadAllComments()]);
  } catch (error) {
    toast.error(error.message ?? '提交审核失败');
  }
}

async function deleteComment(commentId) {
  if (!commentId) return;
  if (!confirm('确定要删除该评论吗？')) return;
  try {
    await httpDelete(`/portal/admin/discussions/comments/${commentId}`);
    await refresh();
  } catch (error) {
    toast.error(error.message ?? '删除评论失败');
  }
}
</script>

<style scoped>
.card__title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.tabs {
  display: flex;
  gap: 8px;
}

.tab {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid var(--border-subtle);
  background: #fff;
}

.tab.active {
  background: #2563eb;
  color: #fff;
  border-color: #2563eb;
}

.sub-title {
  margin: 12px 0 8px;
  font-size: 16px;
  font-weight: 700;
}

.sub-title--spaced {
  margin-top: 18px;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list__item {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.list__item--compact {
  padding: 10px;
}

.list__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid #cbd5e1;
}

.pill--ghost {
  background: #f8fafc;
  color: #475569;
}

.violation {
  color: #b91c1c;
  font-weight: 600;
}

.muted {
  color: #64748b;
}
</style>
