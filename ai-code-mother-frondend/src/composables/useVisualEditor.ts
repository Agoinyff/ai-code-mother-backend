import { ref } from 'vue'

/**
 * 选中元素信息
 */
export interface SelectedElement {
    tagName: string       // 标签名，如 'DIV'
    className: string     // 类名，如 'hero-section'
    id: string            // 元素 id
    textPreview: string   // 文本内容摘要（前 50 字符）
    cssSelector: string   // 唯一 CSS 选择器路径
    outerHTML: string     // outerHTML 截取（前 200 字符）
}

// 注入到 iframe 中的样式 ID
const INJECTED_STYLE_ID = '__visual_editor_style__'
// 悬浮高亮属性名
const HOVER_ATTR = 'data-ve-hover'
// 选中高亮属性名
const SELECTED_ATTR = 'data-ve-selected'
// 忽略的标签
const IGNORED_TAGS = ['HTML', 'HEAD', 'BODY', 'SCRIPT', 'STYLE', 'META', 'LINK', 'TITLE']

/**
 * 可视化编辑 composable
 * 封装 iframe 内元素的高亮交互、选中管理和提示词构建逻辑
 *
 * 核心原理：主网站和 iframe 同域（开发环境通过 Vite proxy，生产环境同域部署），
 * 因此可以直接通过 iframe.contentDocument 操作 iframe 内的 DOM，
 * 不需要注入 <script> 标签，而是从父窗口直接注册事件监听器到 iframe document 上。
 */
export function useVisualEditor() {
    // ===== 状态 =====
    const isEditMode = ref(false)
    const selectedElements = ref<SelectedElement[]>([])

    // 当前绑定的 iframe 引用
    let currentIframe: HTMLIFrameElement | null = null
    // 已注册的事件监听器（用于清理）
    let registeredListeners: { type: string; handler: EventListener }[] = []
    // 上一个悬浮的元素
    let lastHovered: Element | null = null

    // ===== 工具函数 =====

    function shouldIgnore(el: any): boolean {
        return !el || !el.tagName || IGNORED_TAGS.indexOf(el.tagName) !== -1
    }

    /** 生成唯一 CSS 选择器路径 */
    function getCssSelector(el: Element): string {
        if (el.id) return '#' + el.id
        const path: string[] = []
        let current: Element | null = el
        while (current && current.nodeType === 1) {
            let selector = current.tagName.toLowerCase()
            if (current.id) {
                path.unshift('#' + current.id)
                break
            }
            const parent = current.parentElement
            if (parent) {
                const siblings = Array.from(parent.children).filter(c => c.tagName === current!.tagName)
                if (siblings.length > 1) {
                    const idx = siblings.indexOf(current) + 1
                    selector += `:nth-of-type(${idx})`
                }
            }
            path.unshift(selector)
            current = parent
            if (current && current.tagName === 'BODY') {
                path.unshift('body')
                break
            }
        }
        return path.join(' > ')
    }

    /** 获取文本摘要 */
    function getTextPreview(el: Element): string {
        const text = (el.textContent || '').trim().replace(/\s+/g, ' ')
        return text.length > 50 ? text.substring(0, 50) + '...' : text
    }

    // ===== 进入编辑模式 =====
    function enterEditMode(iframe: HTMLIFrameElement) {
        if (isEditMode.value) return

        currentIframe = iframe
        isEditMode.value = true

        try {
            const iframeDoc = iframe.contentDocument || iframe.contentWindow?.document
            if (!iframeDoc) {
                throw new Error('无法访问 iframe document，可能存在跨域限制')
            }

            // 1. 注入样式
            injectEditorStyle(iframeDoc)

            // 2. 直接在 iframe document 上注册事件监听器
            registerIframeListeners(iframeDoc)

        } catch (e) {
            console.error('[VisualEditor] 进入编辑模式失败:', e)
            isEditMode.value = false
            currentIframe = null
        }
    }

    // ===== 退出编辑模式 =====
    function exitEditMode() {
        if (!isEditMode.value) return

        isEditMode.value = false
        selectedElements.value = []

        // 清理 iframe 内容
        if (currentIframe) {
            try {
                const iframeDoc = currentIframe.contentDocument || currentIframe.contentWindow?.document
                if (iframeDoc) {
                    // 移除注入的样式
                    const style = iframeDoc.getElementById(INJECTED_STYLE_ID)
                    if (style) style.remove()

                    // 移除事件监听器
                    registeredListeners.forEach(({ type, handler }) => {
                        iframeDoc.removeEventListener(type, handler, true)
                    })

                    // 清除所有悬浮/选中标记
                    iframeDoc.querySelectorAll(`[${HOVER_ATTR}]`).forEach(el => {
                        el.removeAttribute(HOVER_ATTR)
                    })
                    iframeDoc.querySelectorAll(`[${SELECTED_ATTR}]`).forEach(el => {
                        el.removeAttribute(SELECTED_ATTR)
                    })
                }
            } catch (e) {
                console.error('[VisualEditor] 退出编辑模式清理失败:', e)
            }
        }

        registeredListeners = []
        lastHovered = null
        currentIframe = null
    }

    // ===== 切换编辑模式 =====
    function toggleEditMode(iframe: HTMLIFrameElement) {
        if (isEditMode.value) {
            exitEditMode()
        } else {
            enterEditMode(iframe)
        }
    }

    // ===== 移除指定选中元素 =====
    function removeSelectedElement(index: number) {
        const removed = selectedElements.value[index]
        selectedElements.value.splice(index, 1)

        // 同步清除 iframe 中的选中样式
        if (currentIframe && removed) {
            try {
                const iframeDoc = currentIframe.contentDocument || currentIframe.contentWindow?.document
                if (iframeDoc) {
                    const el = iframeDoc.querySelector(removed.cssSelector)
                    if (el) {
                        el.removeAttribute(SELECTED_ATTR)
                    }
                }
            } catch (e) {
                // 忽略
            }
        }
    }

    // ===== 清空所有选中 =====
    function clearSelections() {
        if (currentIframe) {
            try {
                const iframeDoc = currentIframe.contentDocument || currentIframe.contentWindow?.document
                if (iframeDoc) {
                    iframeDoc.querySelectorAll(`[${SELECTED_ATTR}]`).forEach(el => {
                        el.removeAttribute(SELECTED_ATTR)
                    })
                }
            } catch (e) {
                // 忽略
            }
        }
        selectedElements.value = []
    }

    // ===== 构建带元素信息的提示词 =====
    function buildPromptWithElements(userMessage: string): string {
        if (selectedElements.value.length === 0) {
            return userMessage
        }

        const elementsInfo = selectedElements.value.map((el, i) => {
            const parts = [`元素 ${i + 1}: <${el.tagName.toLowerCase()}>`]
            if (el.id) parts.push(`id="${el.id}"`)
            if (el.className) parts.push(`class="${el.className}"`)
            if (el.textPreview) parts.push(`文本: "${el.textPreview}"`)
            parts.push(`选择器: ${el.cssSelector}`)
            if (el.outerHTML) parts.push(`HTML片段: ${el.outerHTML}`)
            return parts.join(', ')
        }).join('\n')

        return `[用户选中了以下页面元素，请针对这些元素进行修改]\n${elementsInfo}\n\n[用户的修改需求]\n${userMessage}`
    }

    // ===== 内部方法：注入样式 =====
    function injectEditorStyle(doc: Document) {
        if (doc.getElementById(INJECTED_STYLE_ID)) return

        const style = doc.createElement('style')
        style.id = INJECTED_STYLE_ID
        style.textContent = `
            [${HOVER_ATTR}] {
                outline: 2px dashed #667eea !important;
                outline-offset: 2px;
                cursor: pointer !important;
            }
            [${SELECTED_ATTR}] {
                outline: 2px solid #e74c3c !important;
                outline-offset: 2px;
            }
            [${SELECTED_ATTR}][${HOVER_ATTR}] {
                outline-color: #c0392b !important;
                outline-style: solid !important;
            }
        `
        doc.head.appendChild(style)
    }

    // ===== 内部方法：注册 iframe 事件监听器 =====
    function registerIframeListeners(doc: Document) {
        // 清理旧的监听器
        registeredListeners.forEach(({ type, handler }) => {
            doc.removeEventListener(type, handler, true)
        })
        registeredListeners = []

        // 悬浮高亮
        const mouseoverHandler: EventListener = (e: Event) => {
            const target = e.target as Element
            if (shouldIgnore(target)) return
            if (lastHovered && lastHovered !== target) {
                lastHovered.removeAttribute(HOVER_ATTR)
            }
            target.setAttribute(HOVER_ATTR, '')
            lastHovered = target
        }

        const mouseoutHandler: EventListener = (e: Event) => {
            const target = e.target as Element
            if (target && target.removeAttribute) {
                target.removeAttribute(HOVER_ATTR)
            }
        }

        // 点击选中
        const clickHandler: EventListener = (e: Event) => {
            e.preventDefault()
            e.stopPropagation()
            const target = e.target as Element
            if (shouldIgnore(target)) return

            // 切换选中状态
            const wasSelected = target.hasAttribute(SELECTED_ATTR)
            if (wasSelected) {
                target.removeAttribute(SELECTED_ATTR)
            } else {
                target.setAttribute(SELECTED_ATTR, '')
            }

            // 构建元素信息
            let outerHTML = target.outerHTML || ''
            if (outerHTML.length > 200) outerHTML = outerHTML.substring(0, 200) + '...'

            const elementInfo: SelectedElement = {
                tagName: target.tagName,
                className: (typeof target.className === 'string' ? target.className : '') || '',
                id: target.id || '',
                textPreview: getTextPreview(target),
                cssSelector: getCssSelector(target),
                outerHTML: outerHTML,
            }

            // 直接更新状态（同域，不需要 postMessage）
            if (!wasSelected) {
                // 新选中：去重添加
                const exists = selectedElements.value.some(e => e.cssSelector === elementInfo.cssSelector)
                if (!exists) {
                    selectedElements.value.push(elementInfo)
                }
            } else {
                // 取消选中：移除
                const idx = selectedElements.value.findIndex(e => e.cssSelector === elementInfo.cssSelector)
                if (idx !== -1) {
                    selectedElements.value.splice(idx, 1)
                }
            }
        }

        // 注册监听器（使用 capture 模式确保优先处理）
        doc.addEventListener('mouseover', mouseoverHandler, true)
        doc.addEventListener('mouseout', mouseoutHandler, true)
        doc.addEventListener('click', clickHandler, true)

        // 记录以便清理
        registeredListeners = [
            { type: 'mouseover', handler: mouseoverHandler },
            { type: 'mouseout', handler: mouseoutHandler },
            { type: 'click', handler: clickHandler },
        ]
    }

    return {
        isEditMode,
        selectedElements,
        enterEditMode,
        exitEditMode,
        toggleEditMode,
        removeSelectedElement,
        clearSelections,
        buildPromptWithElements,
    }
}
