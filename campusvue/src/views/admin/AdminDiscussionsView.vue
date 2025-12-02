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

    <!-- 帖子审核 + 全部帖子 -->
    <section v-if="activeTab==='posts'">
      <h3 class="sub-title">待审核帖子</h3>
      <ul v-if="pendingDiscussions.length" class="list">
        <li v-for="item in pendingDiscussions" :key="item.id" class="list__item">
          <div>
            <h3>{{ item.title }} <small class="muted">企业 #{{ item.companyId }}</small></h3>
            <p class="muted">
              提交时间：{{ formatDate(item.createdAt) }}
              <span v-if="item.status" class="pill pill--ghost">状态：{{ item.status }}</span>
            </p>
            <p>{{ item.sanitizedContent || item.content }}</p>
            <p v-if="item.reviewComment" class="violation">提示：{{ item.reviewComment }}</p>
          </div>
          <div class="list__actions">
            <button class="primary" @click="review(item.id, 'approved')">通过</button>
            <button class="outline" @click="openRejectDialog(item)">驳回</button>
            <button class="outline" type="button" @click="deleteDiscussion(item.id)">删除</button>
          </div>
        </li>
      </ul>
      <p v-else class="muted">暂无待审核的讨论内容</p>

      <h3 class="sub-title sub-title--spaced">全部帖子</h3>
      <ul v-if="allDiscussions.length" class="list">
        <li
          v-for="item in allDiscussions"
          :key="`all-post-${item.id}`"
          class="list__item list__item--compact"
        >
          <div>
            <h3>{{ item.title }} <small class="muted">企业 #{{ item.companyId }}</small></h3>
            <p class="muted">
              作者：{{ item.authorName || '-' }} · 时间：{{ formatDate(item.createdAt) }}
              · 状态：{{ item.status || '-' }}
            </p>
            <p>{{ item.sanitizedContent || item.content }}</p>
            <p v-if="item.reviewComment" class="violation">提示：{{ item.reviewComment }}</p>
            <div class="list__actions">
              <button class="outline" type="button" @click="deleteDiscussion(item.id)">删除</button>
            </div>
          </div>
        </li>
      </ul>
      <p v-else class="muted">暂无帖子记录</p>
    </section>

    <!-- 评论审核 + 全部评论 -->
    <section v-else-if="activeTab==='comments'">
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

const pendingDiscussions = ref([]);
const allDiscussions = ref([]);
const activeTab = ref('posts');
const pendingComments = ref([]);
const allComments = ref([]);
const toast = useToast();

const rejectDialog = reactive({
  visible: false,
  discussionId: null,
  reason: ''
});

// 评论驳回对话框
const rejectCommentDialog = reactive({ visible: false, commentId: null, reason: '' });

onMounted(async () => {
  await loadPendingDiscussions();
  await loadAllDiscussions();
  await loadPendingComments();
  await loadAllComments();
});

async function loadPendingDiscussions() {
  try {
    pendingDiscussions.value = await get('/portal/admin/discussions/pending');
  } catch (error) {
    toast.error(error.message ?? '加载待审核讨论失败');
  }
}

async function loadAllDiscussions() {
  try {
    allDiscussions.value = await get('/portal/admin/discussions?status=all');
  } catch {
    allDiscussions.value = [];
  }
}

async function loadPendingComments() {
  try {
    pendingComments.value = await get('/portal/admin/discussions/comments/pending');
  } catch {
    pendingComments.value = [];
  }
}

async function loadAllComments() {
  try {
    allComments.value = await get('/portal/admin/discussions/comments?status=all');
  } catch {
    allComments.value = [];
  }
}

async function refresh() {
  await Promise.all([
    loadPendingDiscussions(),
    loadAllDiscussions(),
    loadPendingComments(),
    loadAllComments()
  ]);
}

async function review(discussionId, status, reason = '') {
  try {
    const payload = { status, reviewComment: reason };
    await post(`/portal/admin/discussions/${discussionId}/review`, payload);
    toast.success('讨论审核已提交');
    await Promise.all([loadPendingDiscussions(), loadAllDiscussions()]);
  } catch (error) {
    toast.error(error.message ?? '提交审核失败');
  }
}

async function deleteDiscussion(discussionId) {
  if (!discussionId) return;
  if (!confirm('确定要删除该帖子吗？')) return;
  try {
    await httpDelete(`/portal/admin/discussions/${discussionId}`);
    await refresh();
  } catch (error) {
    toast.error(error.message ?? '删除帖子失败');
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
    await Promise.all([loadPendingComments(), loadAllComments()]);
  } catch {
    // 错误统一由上层提示
  }
}

async function deleteComment(commentId) {
  if (!commentId) return;
  if (!confirm('确定要删除该评论吗？此操作不可撤销。')) return;
  try {
    await httpDelete(`/portal/admin/discussions/comments/${commentId}`);
    await refresh();
    toast.success('评论已删除');
  } catch (error) {
    toast.error(error.message ?? '删除评论失败');
  }
}
</script>

<style scoped>
.list__actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.tabs {
  display: flex;
  gap: 8px;
  align-items: center;
}

.tab {
  border: 1px solid #d1d5db;
  background: #fff;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.tab.active {
  background: #2563eb;
  color: #fff;
  border-color: #2563eb;
}

.sub-title {
  margin: 8px 0;
  font-size: 15px;
}

.sub-title--spaced {
  margin-top: 20px;
}

.list__item--compact {
  padding: 12px;
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid #e5e7eb;
  margin-left: 6px;
}

.pill--ghost {
  background: #f8fafc;
  color: #475569;
}

.violation {
  margin: 6px 0 0;
  color: #b91c1c;
  background: #fef2f2;
  border: 1px solid #fecdd3;
  padding: 6px 8px;
  border-radius: 8px;
  font-size: 13px;
}
</style>
