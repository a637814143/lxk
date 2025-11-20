<template>
  <section class="card">
    <div class="card__title">
      <h2>企业讨论区</h2>
      <button class="outline" @click="loadDiscussions">刷新</button>
    </div>

    <form class="form-grid" @submit.prevent="createDiscussion">
      <label class="full">
        讨论主题
        <input v-model="discussionForm.title" required maxlength="100" />
      </label>
      <label class="full">
        讨论内容
        <textarea v-model="discussionForm.content" required maxlength="1000"></textarea>
      </label>
      <div class="full actions">
        <button class="primary" type="submit">提交审核</button>
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
                  />
                </li>
              </ul>
              <p v-else class="muted">暂无评论，欢迎与学生交流～</p>

              <div class="comments__editor">
                <input
                  v-model="post._newComment"
                  :placeholder="post._replyTo ? `回复 @${post._replyTo.authorUsername}` : '发表你的看法...'"
                />
                <button class="primary" type="button" @click="submitComment(post)">发送</button>
                <button
                  v-if="post._replyTo"
                  class="outline"
                  type="button"
                  @click="cancelReply(post)"
                >
                  取消回复
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
import { onMounted, reactive, ref } from 'vue';
import { get, post as httpPost } from '../../api/http';
import { useToast } from '../../ui/toast';
import CommentNode from '../student/StudentDiscussionsViewNode.vue';

const discussions = ref([]);
const discussionForm = reactive({ title: '', content: '' });
const toast = useToast();

onMounted(loadDiscussions);

async function loadDiscussions() {
  try {
    const data = await get('/portal/company/discussions');
    discussions.value = data.map(post => ({
      ...post,
      _showComments: false,
      _loadingComments: false,
      _comments: [],
      _commentTree: [],
      _newComment: '',
      _feedback: '',
      _feedbackType: 'info',
      _replyTo: null
    }));
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
    const created = await httpPost('/portal/company/discussions', discussionForm);
    discussions.value.unshift({
      ...created,
      _showComments: false,
      _loadingComments: false,
      _comments: [],
      _commentTree: [],
      _newComment: '',
      _feedback: '',
      _feedbackType: 'info',
      _replyTo: null
    });
    toast.success('讨论已提交审核');
    resetDiscussionForm();
  } catch (error) {
    toast.error(error.message ?? '提交讨论失败');
  }
}

function resetDiscussionForm() {
  discussionForm.title = '';
  discussionForm.content = '';
}

function translateStatus(status) {
  switch ((status || '').toLowerCase()) {
    case 'approved':
      return '已发布';
    case 'rejected':
      return '已驳回';
    case 'pending':
      return '待审核';
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
  const base = post._newComment.trim();
  if (!base) return;

  const content =
    post._replyTo && post._replyTo.authorUsername
      ? `回复 @${post._replyTo.authorUsername}: ${base}`
      : base;

  try {
    await httpPost(`/portal/company/discussions/${post.id}/comments`, {
      postId: post.id,
      content,
      parentCommentId: post._replyTo ? post._replyTo.id : null
    });
    post._newComment = '';
    post._replyTo = null;
    post._feedback = '已提交评论，待审核通过后可见';
    post._feedbackType = 'success';
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
  post._newComment = '';
  post._feedback = '';
}

function cancelReply(post) {
  post._replyTo = null;
  post._newComment = '';
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
