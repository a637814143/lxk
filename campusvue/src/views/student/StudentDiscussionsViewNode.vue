<template>
  <div class="comment-node" :style="{ marginLeft: `${level * 16}px` }">
    <p class="comment-meta">
      {{ comment.authorUsername || '用户' }} · {{ formatDate(comment.createdAt) }}
    </p>
    <p>{{ comment.sanitizedContent || comment.content }}</p>
    <div class="comment-actions">
      <button class="comment-action" type="button" @click="$emit('reply', comment)">
        回复
      </button>
      <button
        v-if="isOwnComment"
        class="comment-action"
        type="button"
        @click="$emit('edit', comment)"
      >
        编辑
      </button>
      <button
        v-if="isOwnComment"
        class="comment-action comment-action--danger"
        type="button"
        @click="$emit('delete', comment)"
      >
        删除
      </button>
    </div>
    <ul v-if="comment.replies && comment.replies.length" class="comments__list">
      <li v-for="child in comment.replies" :key="child.id" class="comments__item">
        <CommentNode
          :comment="child"
          :level="level + 1"
          :format-date="formatDate"
          @reply="$emit('reply', $event)"
          @edit="$emit('edit', $event)"
          @delete="$emit('delete', $event)"
        />
      </li>
    </ul>
  </div>
</template>

<script setup>
import { computed, inject } from 'vue';
import CommentNode from './StudentDiscussionsViewNode.vue';

const props = defineProps({
  comment: {
    type: Object,
    required: true
  },
  level: {
    type: Number,
    default: 0
  },
  formatDate: {
    type: Function,
    required: true
  }
});

const authInfo = inject('authInfo', null);
const isOwnComment = computed(() => {
  if (!authInfo || !props.comment || !props.comment.authorUserId) return false;
  return authInfo.userId === props.comment.authorUserId;
});
</script>

<style scoped>
.comment-node {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-meta {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
}

.comment-actions {
  margin-top: 4px;
  display: flex;
  gap: 8px;
}

.comment-action {
  margin-top: 4px;
  padding: 0;
  border: none;
  background: transparent;
  color: #2563eb;
  font-size: 12px;
  cursor: pointer;
  align-self: flex-start;
}

.comment-action--danger {
  color: #dc2626;
}

.comments__list {
  list-style: none;
  margin: 6px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.comments__item {
  padding: 6px 8px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: #fff;
}
</style>
