<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  role: 'user' | 'assistant'
  content: string
  isTyping?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isTyping: false,
})

const isUser = computed(() => props.role === 'user')
</script>

<template>
  <div class="message-bubble" :class="{ 'is-user': isUser, 'is-assistant': !isUser }">
    <div class="avatar">
      <span v-if="isUser">👤</span>
      <span v-else>🤖</span>
    </div>
    <div class="message-content">
      <div class="message-header">
        <span class="sender">{{ isUser ? '你' : 'AI 助手' }}</span>
      </div>
      <div class="message-text">
        <template v-if="content">{{ content }}</template>
        <span v-if="isTyping" class="typing-cursor">▊</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.message-bubble {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  border-radius: var(--radius-lg);
  animation: fadeIn 0.3s ease;
}

.is-user {
  background: var(--bg-card);
}

.is-assistant {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.05));
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.is-assistant .avatar {
  background: var(--gradient-primary);
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  margin-bottom: var(--spacing-xs);
}

.sender {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.message-text {
  font-size: 0.9375rem;
  line-height: 1.7;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
}

.typing-cursor {
  display: inline-block;
  animation: blink 1s infinite;
  color: var(--primary-color);
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
