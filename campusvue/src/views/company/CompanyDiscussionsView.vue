<template>
  <section class="card">
    <div class="card__title">
      <h2>企业讨论区</h2>
      <button class="outline" @click="loadDiscussions">刷新</button>
    </div>

    <form class="form-grid" @submit.prevent="submitDiscussion">
      <label class="full">
        讨论主题
        <input v-model="discussionForm.title" required maxlength="100" />
      </label>
      <label class="full">
        讨论内容
        <textarea v-model="discussionForm.content" required maxlength="1000"></textarea>
      </label>
      <div class="full actions">
        <button class="primary" type="submit">
          {{ editingPostId ? '保存修改' : '提交审核' }}
        </button>
        <button class="outline" type="button" @click="resetDiscussionForm">清空</button>
      </div>
    </form>

    <ul class="list" v-if="discussions.length">
      <li v-for="post in discussions" :key="post.id" class="list__item">
        <div>
          <h3>{{ post.title }}</h3>
          <p class="muted">
            状态：{{ translateStatus(post.status) }} · 提交时间：{{ formatDate(post.createdAt) }}
          </p>
          <p>{{ post.sanitizedContent || post.content }}</p>
          <p v-if="post.reviewComment" class="muted">审核备注：{{ post.reviewComment }}</p>
          <div v-if="isOwnPost(post)" class="post-actions">
            <button class="outline" type="button" @click="startEditPost(post)">编辑帖子</button>
            <button class="outline" type="button" @click="deletePost(post)">删除帖子</button>
          </div>
        </div>

        <div class="comments">
          <button class="outline" type="button" @click="toggleComments(post)">
            {{ post._showComments ? '收起评论' : '展开评论' }}
          </button>

          <div v-if="post._showComments" class="comments__body">
            <p v-if="post._loadingComments" class="muted">正在加载评论...</p>

            <template v-else>
              <ul v-if="post._commentTree.length" class="comments__list">
                <li v-for="comment in post._commentTree" :key="comment.id" class="comments__item">
                  <CommentNode
                    :comment="comment"
                    :level="0"
                    :format-date="formatDate"
                    @reply="startReply(post, $event)"
                    @edit="startEditComment(post, $event)"
                    @delete="deleteComment(post, $event)"
                  />
                </li>
              </ul>
              <p v-else class="muted">暂无评论，欢迎与学生交流。</p>

              <div class="comments__editor">
                <input
                  v-model="post._newComment"
                  :placeholder="
                    post._replyTo
                      ? `回复 @${post._replyTo.authorUsername}`
                      : '发表你的看法...'
                  "
                />
                <button class="primary" type="button" @click="submitComment(post)">发表</button>
                <button
                  v-if="post._replyTo || post._editTarget"
                  class="outline"
                  type="button"
                  @click="cancelReply(post)"
                >
                  取消
                </button>
              </div>

              <p v-if="post._feedback" :class="['feedback', post._feedbackType]">
                {{ post._feedback }}
              </p>
            </template>
          </div>
        </div>
      </li>
    </ul>
    <p v-else class="muted">暂无讨论内容，欢迎提交与学生交流的话题。</p>
  </section>
</template>

<script setup>
import { inject, onMounted, reactive, ref } from 'vue';
import {
  get,
  post as httpPost,
  patch as httpPatch,
  del as httpDelete,
  put as httpPut
} from '../../api/http';
import { useToast } from '../../ui/toast';
import CommentNode from '../student/StudentDiscussionsViewNode.vue';

const discussions = ref([]);
const discussionForm = reactive({ title: '', content: '' });
const toast = useToast();
const authInfo = inject('authInfo', null);
const editingPostId = ref(null);

onMounted(loadDiscussions);

async function loadDiscussions() {
  try {
    const data = await get('/portal/company/discussions');
    discussions.value = data
      .filter(post => (post.status || '').toLowerCase() !== 'deleted')
      .map(post => ({
        ...post,
        _showComments: false,
        _loadingComments: false,
        _comments: [],
        _commentTree: [],
        _newComment: '',
        _feedback: '',
        _feedbackType: 'info',
        _replyTo: null,
        _editTarget: null
      }));
  } catch (error) {
    toast.error(error.message ?? '加载讨论内容失败');
  }
}

async function submitDiscussion() {
  if (!discussionForm.title || !discussionForm.content) {
    toast.error('请填写讨论主题和内容');
    return;
  }
  try {
    if (editingPostId.value) {
      await httpPut(`/portal/company/discussions/${editingPostId.value}`, {
        title: discussionForm.title,
        content: discussionForm.content,
        companyId: null
      });
      toast.success('帖子已更新');
    } else {
      const created = await httpPost('/portal/company/discussions', discussionForm);
      if (
        created?.status?.toLowerCase?.() === 'pending' &&
        typeof created?.reviewComment === 'string' &&
        created.reviewComment.includes('敏感词')
      ) {
        toast.error(
          '发布内容包含敏感词，系统已自动将敏感词替换为“*”，并标记为违规，待管理员审核。'
        );
      } else {
        toast.success('讨论已提交审核');
      }
    }
    resetDiscussionForm();
    editingPostId.value = null;
    await loadDiscussions();
  } catch (error) {
    toast.error(error.message ?? '提交讨论失败');
  }
}

function resetDiscussionForm() {
  discussionForm.title = '';
  discussionForm.content = '';
  editingPostId.value = null;
}

function translateStatus(status) {
  switch ((status || '').toLowerCase()) {
    case 'approved':
      return '已发布';
    case 'rejected':
      return '已驳回';
    case 'pending':
      return '待审核';
    case 'deleted':
      return '已删除';
    default:
      return status || '-';
  }
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString();
}

async function toggleComments(post) {
  post._showComments = !post._showComments;
  if (!post._showComments) {
    return;
  }
  post._feedback = '';
  post._feedbackType = 'info';
  post._replyTo = null;
  post._editTarget = null;
  if (post._comments && post._comments.length) {
    return;
  }
  try {
    post._loadingComments = true;
    const flat = await get(`/public/discussions/${post.id}/comments`);
    post._comments = flat;
    post._commentTree = buildCommentTree(flat);
  } catch (error) {
    post._comments = [];
    post._commentTree = [];
  } finally {
    post._loadingComments = false;
  }
}

async function submitComment(post) {
  if (!post._newComment || !post.id) return;

  // 编辑已有评论
  if (post._editTarget) {
    const baseEdit = post._newComment.trim();
    if (!baseEdit) return;
    try {
      await httpPatch(`/portal/company/discussions/${post.id}/comments/${post._editTarget.id}`, {
        postId: post.id,
        content: baseEdit,
        parentCommentId: post._editTarget.parentCommentId ?? null
      });
      post._newComment = '';
      post._replyTo = null;
      post._editTarget = null;
      post._feedback =
        '评论已更新，如包含敏感词系统会自动替换为“*”，并标记为违规等待管理员审核。';
      post._feedbackType = 'success';
      try {
        const flatAfterEdit = await get(`/public/discussions/${post.id}/comments`);
        post._comments = flatAfterEdit;
        post._commentTree = buildCommentTree(flatAfterEdit);
      } catch {
        // ignore
      }
    } catch (error) {
      post._feedback = error.message ?? '更新评论失败';
      post._feedbackType = 'error';
    }
    return;
  }

  const base = post._newComment.trim();
  if (!base) return;

  const content =
    post._replyTo && post._replyTo.authorUsername
      ? `回复 @${post._replyTo.authorUsername}: ${base}`
      : base;

  try {
    const created = await httpPost(`/portal/company/discussions/${post.id}/comments`, {
      postId: post.id,
      content,
      parentCommentId: post._replyTo ? post._replyTo.id : null
    });
    post._newComment = '';
    post._replyTo = null;
    post._feedback = '已提交评论，待审核通过后可见。';
    post._feedbackType = 'success';
    if (
      created?.status?.toLowerCase?.() === 'pending' &&
      typeof created?.reviewComment === 'string' &&
      created.reviewComment.includes('敏感词')
    ) {
      post._feedback =
        '评论包含敏感词，系统已自动将敏感词替换为“*”，并标记为违规，待管理员审核。';
      post._feedbackType = 'error';
    }
    try {
      const flat = await get(`/public/discussions/${post.id}/comments`);
      post._comments = flat;
      post._commentTree = buildCommentTree(flat);
    } catch {
      // ignore refresh error
    }
  } catch (error) {
    post._feedback = error.message ?? '发表评论失败';
    post._feedbackType = 'error';
  }
}

function startReply(post, comment) {
  post._replyTo = comment;
  post._editTarget = null;
  post._newComment = '';
  post._feedback = '';
}

function cancelReply(post) {
  post._replyTo = null;
  post._newComment = '';
  post._editTarget = null;
}

function startEditComment(post, comment) {
  post._editTarget = comment;
  post._replyTo = null;
  post._newComment = comment.content || '';
  post._feedback = '';
}

async function deleteComment(post, comment) {
  if (!comment || !comment.id) return;
  if (!confirm('确定要删除该评论吗？')) return;
  try {
    await httpDelete(`/portal/company/discussions/${post.id}/comments/${comment.id}`);
    post._feedback = '评论已删除。';
    post._feedbackType = 'success';
    try {
      const flat = await get(`/public/discussions/${post.id}/comments`);
      post._comments = flat;
      post._commentTree = buildCommentTree(flat);
    } catch {
      // ignore refresh error
    }
  } catch (error) {
    post._feedback = error.message ?? '删除评论失败';
    post._feedbackType = 'error';
  }
}

function buildCommentTree(list) {
  const map = new Map();
  const roots = [];
  list.forEach(c => {
    c.replies = [];
    map.set(c.id, c);
  });
  list.forEach(c => {
    const parentId = c.parentCommentId || c.parent_comment_id || null;
    if (parentId) {
      const parent = map.get(parentId);
      if (parent) {
        parent.replies.push(c);
      } else {
        roots.push(c);
      }
    } else {
      roots.push(c);
    }
  });
  return roots;
}

function isOwnPost(post) {
  return authInfo && post && post.authorId && authInfo.userId === post.authorId;
}

function startEditPost(post) {
  if (!isOwnPost(post)) return;
  editingPostId.value = post.id;
  discussionForm.title = post.title || '';
  discussionForm.content = post.content || '';
}

async function deletePost(post) {
  if (!isOwnPost(post)) return;
  if (!confirm('确定要删除该帖子吗？')) return;
  try {
    await httpDelete(`/portal/company/discussions/${post.id}`);
    toast.success('帖子已删除');
    if (editingPostId.value === post.id) {
      resetDiscussionForm();
    }
    await loadDiscussions();
  } catch (error) {
    toast.error(error.message ?? '删除帖子失败');
  }
}
</script>

<style scoped>
.actions {
  display: flex;
  gap: 12px;
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
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
}

.post-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.comments {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comments__body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comments__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comments__item {
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
}

.comments__editor {
  display: flex;
  gap: 8px;
  align-items: center;
}

.comments__editor input {
  flex: 1;
}
</style>

