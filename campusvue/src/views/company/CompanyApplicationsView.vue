<template>
  <section class="card">
    <div class="card__title">
      <h2>简历投递</h2>
      <button class="outline" @click="reload">刷新</button>
    </div>

    <table v-if="applications.length" class="table">
      <thead>
        <tr><th>职位</th><th>学生ID</th><th>简历</th><th>状态</th><th>备注</th><th>更新时间</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="app in applications" :key="app.id">
          <td>{{ resolveJobTitle(app.jobId) }}</td>
          <td>{{ app.studentId }}</td>
          <td>
            <button class="outline" type="button" @click="viewResume(app)">查看简历</button>
          </td>
          <td>{{ app.status }}</td>
          <td>
            <input v-model="app.decisionNote" maxlength="500" placeholder="备注（可选）" @change="updateStatus(app)" />
          </td>
          <td>{{ formatDate(app.updateTime) }}</td>
          <td class="actions">
            <select v-model="app.status" @change="updateStatus(app)">
              <option value="待查阅">待查阅</option>
              <option value="已查阅">已查阅</option>
              <option value="面试中">面试中</option>
              <option value="录用">录用</option>
              <option value="拒绝">拒绝</option>
            </select>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无投递</p>

    <div v-if="showResume" class="modal-backdrop" @click.self="closeResume">
      <div class="modal">
        <header class="modal__header">
          <div>
            <h3>{{ activeResume?.title || '简历详情' }}</h3>
            <p v-if="activeApplication" class="muted">当前投递状态：{{ activeApplication.status }}</p>
          </div>
          <button class="outline" @click="closeResume">关闭</button>
        </header>
        <template v-if="!loadingResume && activeResume">
          <div class="modal__body">
            <p><strong>教育经历：</strong></p>
            <p class="pre">{{ activeResume.educationExperience || '—' }}</p>
            <p><strong>实习/工作经历：</strong></p>
            <p class="pre">{{ activeResume.workExperience || '—' }}</p>
            <p><strong>技能特长：</strong></p>
            <p class="pre">{{ activeResume.skills || '—' }}</p>
            <p><strong>自我评价：</strong></p>
            <p class="pre">{{ activeResume.selfEvaluation || '—' }}</p>
            <p v-if="activeResume.attachment"><strong>附件：</strong><a :href="activeResume.attachment" target="_blank" rel="noopener">点击查看附件</a></p>
          </div>
          <div class="modal__body reply-card" v-if="activeApplication">
            <div class="reply-card__header">
              <div>
                <h4>回复投递者</h4>
                <p class="muted">发送后将显示在学生消息中心，当前状态和备注会一并保存。</p>
              </div>
              <div class="pill">
                <span>状态</span>
                <strong>{{ activeApplication.status }}</strong>
              </div>
            </div>
            <label class="form-label" for="reply-title">消息标题</label>
            <input
              id="reply-title"
              v-model="replyTitle"
              :disabled="sendingReply"
              maxlength="100"
              placeholder="例如：面试邀请 / 简历反馈"
            />
            <label class="form-label" for="reply-content">消息内容</label>
            <textarea
              id="reply-content"
              v-model="replyContent"
              :disabled="sendingReply"
              rows="4"
              placeholder="请输入要发送给投递者的回复"
            ></textarea>
            <div class="reply-actions">
              <p class="muted">建议告知投递进度或提供面试安排。</p>
              <button class="outline" type="button" @click="sendReply" :disabled="sendingReply">
                {{ sendingReply ? '正在发送...' : '发送消息' }}
              </button>
            </div>
          </div>
        </template>
        <div class="modal__body" v-else>正在加载简历...</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { get, patch } from '../../api/http';
import { useToast } from '../../ui/toast';

const applications = ref([]);
const jobs = ref([]);
const toast = useToast();

const activeResume = ref(null);
const activeApplication = ref(null);
const showResume = ref(false);
const loadingResume = ref(false);
const replyTitle = ref('');
const replyContent = ref('');
const sendingReply = ref(false);

const jobMap = computed(() => {
  const map = new Map();
  jobs.value.forEach(job => { map.set(job.id, job.jobTitle); });
  return map;
});

onMounted(reload);

async function reload() { await Promise.all([loadJobs(), loadApplications()]); }

async function loadJobs() {
  try { jobs.value = await get('/portal/company/jobs'); } catch {}
}

async function loadApplications() {
  try {
    const items = await get('/portal/company/applications');
    applications.value = items.map(normalizeApplication);
  } catch (error) {
    toast.error(error.message ?? '加载投递记录失败');
  }
}

function normalizeApplication(item) {
  return { id: item.id, jobId: item.jobId, studentId: item.studentId, resumeId: item.resumeId, status: item.status, decisionNote: item.decisionNote || '', updateTime: item.updateTime };
}

async function updateStatus(app) {
  try {
    const updated = await patch(`/portal/company/applications/${app.id}`, { status: app.status, decisionNote: app.decisionNote });
    Object.assign(app, normalizeApplication(updated));
    toast.success('投递状态已更新');
  } catch (error) {
    toast.error(error.message ?? '更新状态失败');
  }
}

function resolveJobTitle(jobId) { return jobMap.value.get(jobId) || `职位 #${jobId}`; }
function formatDate(value) { if (!value) return '-'; return new Date(value).toLocaleString(); }

async function viewResume(app) {
  if (!app?.resumeId) { toast.error('该投递未关联简历'); return; }
  activeApplication.value = app;
  replyTitle.value = `关于${resolveJobTitle(app.jobId)}的反馈`;
  replyContent.value = '';
  try {
    loadingResume.value = true;
    activeResume.value = await get(`/resumes/${app.resumeId}`);
    showResume.value = true;
  } catch (error) {
    activeResume.value = null;
    toast.error(error.message ?? '加载简历失败');
  } finally {
    loadingResume.value = false;
  }
}

async function sendReply() {
  if (!activeApplication.value) { toast.error('请先打开要回复的投递'); return; }
  const title = replyTitle.value.trim();
  const content = replyContent.value.trim();
  if (!title || !content) { toast.error('请输入完整的消息标题和内容'); return; }
  if (!activeApplication.value.status) { toast.error('请先选择投递状态'); return; }

  try {
    sendingReply.value = true;
    const updated = await patch(`/portal/company/applications/${activeApplication.value.id}`, {
      status: activeApplication.value.status,
      decisionNote: activeApplication.value.decisionNote,
      messageTitle: title,
      messageContent: content
    });
    const normalized = normalizeApplication(updated);
    const target = applications.value.find(item => item.id === normalized.id);
    if (target) {
      Object.assign(target, normalized);
    }
    Object.assign(activeApplication.value, normalized);
    toast.success('已发送回复并同步最新状态');
    replyTitle.value = '';
    replyContent.value = '';
  } catch (error) {
    toast.error(error.message ?? '发送消息失败');
  } finally {
    sendingReply.value = false;
  }
}

function closeResume() {
  showResume.value = false;
  activeResume.value = null;
  activeApplication.value = null;
  replyTitle.value = '';
  replyContent.value = '';
}
</script>

<style scoped>
.table input { min-width: 160px; }
.actions { display: flex; gap: 12px; }

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 12px;
  width: min(800px, 92vw);
  max-height: 80vh;
  overflow: auto;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.2);
}

.modal__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.modal__body {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pre {
  white-space: pre-wrap;
}

.reply-card {
  border-top: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 999px;
  background: #eef2ff;
  color: #1d4ed8;
  font-weight: 600;
}

.form-label {
  font-weight: 600;
}

.reply-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.reply-actions button {
  min-width: 140px;
}

.modal textarea {
  resize: vertical;
}
</style>
