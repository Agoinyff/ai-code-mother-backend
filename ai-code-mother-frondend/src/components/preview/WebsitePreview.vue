<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  url: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const iframeRef = ref<HTMLIFrameElement>()
const isLoading = ref(true)

function handleLoad() {
  isLoading.value = false
}

function refresh() {
  isLoading.value = true
  if (iframeRef.value) {
    iframeRef.value.src = props.url + (props.url.includes('?') ? '&' : '?') + 't=' + Date.now()
  }
}

function openInNewTab() {
  window.open(props.url, '_blank')
}

defineExpose({ refresh })
</script>

<template>
  <div class="website-preview">
    <div class="preview-header">
      <div class="browser-dots">
        <span class="dot red"></span>
        <span class="dot yellow"></span>
        <span class="dot green"></span>
      </div>
      <div class="url-bar">
        <span class="url-text">{{ url }}</span>
      </div>
      <div class="preview-actions">
        <button class="action-btn" title="刷新" @click="refresh">🔄</button>
        <button class="action-btn" title="新标签页打开" @click="openInNewTab">↗</button>
      </div>
    </div>
    
    <div class="preview-content">
      <div v-if="isLoading || loading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <p>正在加载预览...</p>
      </div>
      <iframe
        v-if="url"
        ref="iframeRef"
        :src="url"
        class="preview-iframe"
        @load="handleLoad"
      ></iframe>
      <div v-else class="empty-preview">
        <div class="empty-icon">🌐</div>
        <p>等待生成网站...</p>
        <p class="empty-hint">AI 生成完成后，预览将自动显示</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.website-preview {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.preview-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-tertiary);
  border-bottom: 1px solid var(--border-color);
}

.browser-dots {
  display: flex;
  gap: 6px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dot.red { background: #ff5f57; }
.dot.yellow { background: #febc2e; }
.dot.green { background: #28c840; }

.url-bar {
  flex: 1;
  background: var(--bg-glass);
  border-radius: var(--radius-sm);
  padding: 6px 12px;
  overflow: hidden;
}

.url-text {
  font-size: 0.75rem;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
  font-family: var(--font-mono);
}

.preview-actions {
  display: flex;
  gap: var(--spacing-xs);
}

.action-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: var(--bg-glass);
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  transition: all var(--transition-fast);
}

.action-btn:hover {
  background: var(--primary-color);
}

.preview-content {
  flex: 1;
  position: relative;
  background: white;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.loading-overlay {
  position: absolute;
  inset: 0;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  color: var(--text-secondary);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border-color);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-preview {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  color: var(--text-secondary);
  background: var(--bg-secondary);
}

.empty-icon {
  font-size: 4rem;
  opacity: 0.5;
}

.empty-hint {
  font-size: 0.875rem;
  color: var(--text-muted);
}
</style>
