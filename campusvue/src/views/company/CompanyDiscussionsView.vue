<template>
  <section class="card">
    <div class="card__title">
      <h2>企业讨论区</h2>
      <button class="outline" @click="loadDiscussions">刷新</button>
    </div>

    <form class="discussion-form" @submit.prevent="createDiscussion">
      <label class="full">讨论主题（可选）<input v-model="discussionForm.title" maxlength="100"
          placeholder="为评论添加一个主题" /></label>
      <label class="full">评论内容<textarea v-model="discussionForm.content" required maxlength="1000"
          placeholder="欢迎向学生介绍企业或回复同学的疑问"></textarea></label>
      <div class="discussion-actions">
        <span v-if="replyTarget" class="reply-hint">
          正在回复：{{ replyTarget.title || replyTarget.sanitizedContent || replyTarget.content }}
        </span>
        <span class="spacer"></span>
        <button v-if="replyTarget" class="outline" type="button" @click="cancelReply">取消回复</button>
        <button class="primary" type="submit">{{ replyTarget ? '发布回复' : '发布评论' }}</button>
      </div>
    </form>

    <ul v-if="discussionThreads.length" class="discussion-list">
      <li v-for="thread in discussionThreads" :key="thread.id" class="discussion-thread">
        <article class="discussion-post">
          <header>
            <h3>{{ thread.title }}</h3>
            <p class="muted">{{ resolveAuthorLabel(thread) }} · {{ formatDate(thread.createdAt) }}</p>
          </header>
          <p>{{ thread.sanitizedContent || thread.content }}</p>
          <footer>
            <button class="text" type="button" @click="setReplyTarget(thread)">回复</button>
          </footer>
        </article>
        <ul v-if="thread.replies.length" class="reply-list">
          <li v-for="reply in thread.replies" :key="reply.id" class="discussion-reply">
            <div class="reply-header">
              <strong>{{ resolveAuthorLabel(reply) }}</strong>
              <span class="muted">{{ formatDate(reply.createdAt) }}</span>
            </div>
            <p>{{ reply.sanitizedContent || reply.content }}</p>
            <button class="text" type="button" @click="setReplyTarget(reply)">回复</button>
          </li>
        </ul>
      </li>
    </ul>
    <p v-else class="muted">暂无讨论内容，欢迎提交与学生交流的话题。</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { get, post } from '../../api/http';

const discussions = ref([]);
const discussionThreads = computed(() => buildDiscussionThreads(discussions.value));
const discussionForm = reactive({
  title: '',
  content: ''
});
const replyTarget = ref(null);
const feedback = reactive({ message: '', type: 'info' });

onMounted(() => {
  loadDiscussions();
});

async function loadDiscussions() {
  try {
    discussions.value = await get('/portal/company/discussions');
    replyTarget.value = null;
    showFeedback('', 'info');
  } catch (error) {
    showFeedback(error.message ?? '加载讨论内容失败', 'error');
  }
}

async function createDiscussion() {
  if (!discussionForm.content.trim()) {
    showFeedback('请填写评论内容', 'error');
    return;
  }
  try {
    const payload = {
      title: discussionForm.title?.trim() || undefined,
      content: discussionForm.content.trim(),
      parentId: replyTarget.value?.id ?? null
    };
    const created = await post('/portal/company/discussions', payload);
    discussions.value = [...discussions.value, created];
    showFeedback('评论已发布', 'success');
    resetDiscussionForm();
  } catch (error) {
    showFeedback(error.message ?? '提交讨论失败', 'error');
  }
}

function resetDiscussionForm() {
  discussionForm.title = '';
  discussionForm.content = '';
  replyTarget.value = null;
}

function formatDate(value) {
  if (!value) {
    return '-';
  }
  return new Date(value).toLocaleString();
}

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}

function setReplyTarget(post) {
  replyTarget.value = post;
  showFeedback(`正在回复 ${post.authorName ?? '该评论'}`, 'info');
}

function cancelReply() {
  replyTarget.value = null;
  showFeedback('', 'info');
}

function buildDiscussionThreads(items) {
  if (!Array.isArray(items)) {
    return [];
  }
  const nodes = items.map(item => ({ ...item, replies: [] }));
  const map = new Map(nodes.map(node => [node.id, node]));
  const roots = [];
  nodes.forEach(node => {
    if (node.parentId && map.has(node.parentId)) {
      map.get(node.parentId).replies.push(node);
    } else {
      roots.push(node);
    }
  });
  return roots;
}

function resolveAuthorLabel(post) {
  return post.authorRole ?? '用户';
}
</script>

<style scoped>
.discussion-form {
  display: grid;
  gap: 12px;
}

.discussion-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.discussion-actions .spacer {
  flex: 1;
}

.reply-hint {
  color: #475569;
  font-size: 14px;
}

.discussion-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.discussion-thread {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.discussion-post header {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.discussion-post footer {
  margin-top: 8px;
}

.reply-list {
  list-style: none;
  margin: 0;
  padding-left: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.discussion-reply {
  border-left: 3px solid #bfdbfe;
  padding-left: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #475569;
}

button.text {
  background: none;
  border: none;
  color: #2563eb;
  cursor: pointer;
  padding: 0;
  font-size: 14px;
}

button.text:hover {
  text-decoration: underline;
}

.feedback {
  padding: 10px 14px;
  border-radius: 12px;
  text-align: center;
}

.feedback.success {
  background: #dcfce7;
  color: #15803d;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}
</style>
