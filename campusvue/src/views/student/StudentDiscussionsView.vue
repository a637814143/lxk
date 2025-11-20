<template>
  <section class="section">
    <header class="section__header">
      <div>
        <h2>企业讨论区</h2>
        <p class="muted">
          查看企业的公开讨论记录，可通过职位列表的「查看企业讨论」快速跳转。
        </p>
      </div>
      <div class="actions">
        <input v-model="manualCompanyId" placeholder="输入企业 ID" />
        <button class="outline" type="button" @click="loadByManualId" :disabled="loading">
          加载讨论
        </button>
        <input v-model="searchKeyword" placeholder="按企业名称搜索" />
        <button class="outline" type="button" @click="searchCompanies" :disabled="loading">
          搜索企业
        </button>
        <button class="outline" type="button" @click="goBackToJobs">返回职位</button>
      </div>
      <ul v-if="companySearchResults.length" class="companies">
        <li v-for="c in companySearchResults" :key="c.id">
          <button class="outline" type="button" @click="selectCompany(c)">
            {{ c.companyName }} · #{{ c.id }}
          </button>
        </li>
      </ul>
    </header>

    <div v-if="currentCompany.id" class="current-company">
      <h3>{{ currentCompany.name }}</h3>
      <p class="muted">企业 ID：{{ currentCompany.id }}</p>
    </div>

    <section v-if="currentCompany.id" class="compose card">
      <h3>发布讨论</h3>
      <form class="form" @submit.prevent="submitDiscussion">
        <input v-model="form.title" placeholder="讨论标题" required />
        <textarea v-model="form.content" placeholder="讨论内容" required></textarea>
        <div class="actions">
          <button class="primary" type="submit">发布</button>
          <button class="outline" type="button" @click="resetForm">重置</button>
        </div>
      </form>
    </section>

    <p class="muted" v-if="loading">正在加载企业讨论，请稍候…</p>
    <template v-else>
      <p class="muted" v-if="!currentCompany.id">
        尚未选择企业。可在职位列表中点击「查看企业讨论」或在上方输入企业 ID。
      </p>
      <div v-else>
        <p v-if="feedback.message" :class="['feedback', feedback.type]">
          {{ feedback.message }}
        </p>

        <ul class="list" v-if="discussions.length">
          <li v-for="post in discussions" :key="post.id" class="list__item">
            <div>
              <h3>{{ post.title }}</h3>
              <p class="muted">发布时间：{{ formatDate(post.createdAt) }}</p>
              <p>{{ post.sanitizedContent || post.content }}</p>
              <p v-if="post.reviewComment" class="muted">
                审核备注：{{ post.reviewComment }}
              </p>
            </div>

            <div class="comments">
              <button class="outline" type="button" @click="toggleComments(post)">
                {{ post._showComments ? '收起评论' : '展开评论' }}
              </button>

              <div v-if="post._showComments" class="comments__body">
                <p v-if="post._loadingComments" class="muted">正在加载评论...</p>

                <template v-else>
                  <ul v-if="post._commentTree.length" class="comments__list">
                    <li
                      v-for="comment in post._commentTree"
                      :key="comment.id"
                      class="comments__item"
                    >
                      <CommentNode
                        :comment="comment"
                        :level="0"
                        :format-date="formatDate"
                        @reply="startReply(post, $event)"
                      />
                    </li>
                  </ul>
                  <p v-else class="muted">暂无评论，来说两句吧～</p>

                  <div class="comments__editor">
                    <input
                      v-model="post._newComment"
                      :placeholder="post._replyTo ? `回复 @${post._replyTo.authorUsername}` : '发表你的看法...'"
                    />
                    <button class="primary" type="button" @click="submitComment(post)">
                      发送
                    </button>
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

        <p v-else class="muted">暂无公开讨论，欢迎稍后再来查看。</p>
      </div>
    </template>
  </section>
</template>

<script setup>
import CommentNode from './StudentDiscussionsViewNode.vue';
import { reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { get, post as httpPost } from '../../api/http';

const route = useRoute();
const router = useRouter();

const currentCompany = reactive({ id: null, name: '' });
const discussions = ref([]);
const loading = ref(false);
const feedback = reactive({ message: '', type: 'info' });
const manualCompanyId = ref('');
const searchKeyword = ref('');
const companySearchResults = ref([]);
const form = reactive({ title: '', content: '' });

async function loadDiscussions(companyId, companyName = '') {
  if (!companyId) {
    currentCompany.id = null;
    currentCompany.name = '';
    discussions.value = [];
    feedback.message = '';
    feedback.type = 'info';
    manualCompanyId.value = '';
    return;
  }

  currentCompany.id = companyId;
  currentCompany.name = companyName || `企业 #${companyId}`;
  manualCompanyId.value = String(companyId);
  loading.value = true;
  feedback.message = '';
  try {
    const data = await get(`/public/discussions/company/${companyId}`);
    discussions.value = data;
    discussions.value.forEach(post => {
      post._showComments = false;
      post._loadingComments = false;
      post._comments = [];
      post._commentTree = [];
      post._newComment = '';
      post._feedback = '';
      post._feedbackType = 'info';
      post._replyTo = null;
    });
    if (!data.length) {
      feedback.message = '暂无公开讨论记录';
      feedback.type = 'info';
    }
  } catch (error) {
    discussions.value = [];
    feedback.message = error.message ?? '加载讨论失败';
    feedback.type = 'error';
  } finally {
    loading.value = false;
  }
}

function loadByManualId() {
  const parsed = Number(manualCompanyId.value);
  if (!parsed) {
    feedback.message = '请输入有效的企业 ID';
    feedback.type = 'error';
    return;
  }
  router.replace({
    name: 'student-discussions',
    query: { companyId: parsed }
  });
  loadDiscussions(parsed, '');
}

function goBackToJobs() {
  router.push({ name: 'student-jobs' });
}

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

async function searchCompanies() {
  try {
    const q = (searchKeyword.value || '').trim();
    companySearchResults.value = await get(
      `/public/companies${q ? `?q=${encodeURIComponent(q)}` : ''}`
    );
  } catch {
    companySearchResults.value = [];
  }
}

function selectCompany(c) {
  companySearchResults.value = [];
  router.replace({
    name: 'student-discussions',
    query: { companyId: c.id, companyName: c.companyName }
  });
  loadDiscussions(c.id, c.companyName);
}

async function submitDiscussion() {
  if (!currentCompany.id) return;
  if (!form.title || !form.content) return;
  try {
    await httpPost('/portal/student/discussions', {
      companyId: currentCompany.id,
      title: form.title,
      content: form.content
    });
    form.title = '';
    form.content = '';
    await loadDiscussions(currentCompany.id, currentCompany.name);
    feedback.message = '提交成功，待管理员审核通过后对外展示';
    feedback.type = 'success';
  } catch (error) {
    feedback.message = error.message ?? '提交失败';
    feedback.type = 'error';
  }
}

function resetForm() {
  form.title = '';
  form.content = '';
}

async function toggleComments(post) {
  post._showComments = !post._showComments;
  if (!post._showComments) return;
  post._feedback = '';
  post._feedbackType = 'info';
  post._replyTo = null;
  if (post._comments && post._comments.length) return;
  try {
    post._loadingComments = true;
    const flat = await get(`/public/discussions/${post.id}/comments`);
    post._comments = flat;
    post._commentTree = buildCommentTree(flat);
  } catch {
    post._comments = [];
    post._commentTree = [];
  } finally {
    post._loadingComments = false;
  }
}

async function submitComment(post) {
  if (!post._newComment || !post.id) return;
  try {
    const base = post._newComment.trim();
    if (!base) return;

    const content =
      post._replyTo && post._replyTo.authorUsername
        ? `回复 @${post._replyTo.authorUsername}: ${base}`
        : base;

    await httpPost('/portal/student/discussions/' + post.id + '/comments', {
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
    } catch {}
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

watch(
  () => route.query,
  query => {
    const companyId = query.companyId ? Number(query.companyId) : null;
    const companyName = query.companyName ?? '';
    loadDiscussions(companyId, companyName);
  },
  { immediate: true, deep: true }
);
</script>

<style scoped>
.section__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.actions input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 8px 10px;
  min-width: 160px;
}

.companies {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.companies li {
  display: inline-flex;
}

.current-company {
  background: var(--color-primary-soft);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.compose {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: var(--shadow-soft);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form input,
.form textarea {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px;
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

.comment-meta {
  margin: 0 0 4px;
  font-size: 12px;
  color: #6b7280;
}

.comment-reply {
  margin-top: 4px;
  padding: 0;
  border: none;
  background: transparent;
  color: #2563eb;
  font-size: 12px;
  cursor: pointer;
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
