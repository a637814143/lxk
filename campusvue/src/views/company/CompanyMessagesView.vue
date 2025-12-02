<template>
  <section class="section">
    <header class="section__header">
      <div>
        <h2>消息中心</h2>
        <p class="muted">查看并处理来自管理员或系统的通知</p>
      </div>
      <button class="outline" type="button" @click="loadMessages" :disabled="loading">
        {{ loading ? '刷新中…' : '刷新' }}
      </button>
    </header>

    <ul class="list" v-if="messages.length">
      <li v-for="message in messages" :key="message.id" class="list__item clickable" @click="openDetail(message)">
        <div>
          <h3>{{ message.title }}</h3>
          <p class="muted">{{ formatDate(message.sendTime) }}</p>
          <p class="muted preview">{{ message.content }}</p>
        </div>
        <span class="pill" :class="{ unread: !message.isRead }">{{ message.isRead ? '已读' : '未读' }}</span>
      </li>
    </ul>
    <p v-else class="muted">暂无消息</p>

    <div v-if="detail.visible" class="modal-backdrop" @click.self="closeDetail">
      <div class="modal">
        <header class="modal__header">
          <div>
            <h3>{{ detail.message?.title }}</h3>
            <p class="muted">{{ formatDate(detail.message?.sendTime) }}</p>
          </div>
          <button class="outline" @click="closeDetail">关闭</button>
        </header>
        <div class="modal__body">
          <p class="pre">{{ detail.message?.content }}</p>
        </div>
        <footer class="modal__footer">
          <button class="outline" @click="gotoTarget" :disabled="!detail.targetRoute">跳转相关功能</button>
          <button class="primary" @click="markRead(detail.message)" :disabled="detail.message?.isRead">标记已读</button>
        </footer>
      </div>
    </div>
  </section>
</template>

<script setup>
import { inject, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { get, put } from '../../api/http';
import { useToast } from '../../ui/toast';

const authInfo = inject('authInfo', null);
const router = useRouter();
const toast = useToast();

const messages = ref([]);
const loading = ref(false);

const detail = reactive({
  visible: false,
  message: null,
  targetRoute: null
});

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

function resolveTargetRoute(message) {
  const text = (message?.title || '') + ' ' + (message?.content || '');
  if (/审核|认证|资料/.test(text)) return 'company-profile';
  if (/充值|钱包|订阅|扣款/.test(text)) return 'company-finance';
  if (/职位|岗位|发布/.test(text)) return 'company-jobs';
  if (/投递|简历|申请/.test(text)) return 'company-applications';
  if (/公告/.test(text)) return 'company-announcements';
  return null;
}

async function loadMessages() {
  try {
    loading.value = true;
    const list = await get(`/messages/receiver/${authInfo?.userId}`);
    messages.value = list;
  } catch (error) {
    toast.error(error.message ?? '加载消息失败');
  } finally {
    loading.value = false;
  }
}

async function markRead(message) {
  if (!message) return;
  try {
    const updated = await put(`/messages/${message.id}`, { isRead: true });
    messages.value = messages.value.map(item => (item.id === message.id ? updated : item));
    detail.message = updated;
  } catch (error) {
    toast.error(error.message ?? '标记已读失败');
  }
}

function openDetail(message) {
  detail.message = message;
  detail.targetRoute = resolveTargetRoute(message);
  detail.visible = true;
  if (!message.isRead) {
    markRead(message);
  }
}

function closeDetail() {
  detail.visible = false;
  detail.message = null;
  detail.targetRoute = null;
}

function gotoTarget() {
  if (!detail.targetRoute) return;
  router.push({ name: detail.targetRoute });
  closeDetail();
}

onMounted(loadMessages);
</script>

<style scoped>
.clickable {
  cursor: pointer;
  transition: transform 0.08s ease, box-shadow 0.08s ease;
}

.clickable:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.08);
}

.preview {
  max-width: 540px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pill.unread {
  background: #fef3c7;
  color: #b45309;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 16px;
  width: min(780px, 92vw);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.16);
  overflow: hidden;
}

.modal__header,
.modal__body,
.modal__footer {
  padding: 16px 20px;
}

.modal__header {
  border-bottom: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.modal__body {
  white-space: pre-wrap;
}

.modal__footer {
  border-top: 1px solid var(--border-subtle);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
