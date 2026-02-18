<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import HeaderNav from '@/components/common/HeaderNav.vue'

const userStore = useUserStore()
const route = useRoute()

const hideNav = computed(() => !!route.meta.hideNav)

onMounted(() => {
    userStore.initUserInfo()
})
</script>

<template>
    <div id="app">
        <HeaderNav v-if="!hideNav" />
        <main class="main-content" :class="{ 'with-nav': !hideNav }">
            <RouterView />
        </main>
    </div>
</template>

<style>
@import '@/assets/styles/global.css';

#app {
    min-height: 100vh;
}

.main-content {
    min-height: 100vh;
}

.main-content.with-nav {
    padding-top: 60px;
}
</style>
