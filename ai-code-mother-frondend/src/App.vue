<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useUserStore } from '@/stores/user'
import HeaderNav from '@/components/common/HeaderNav.vue'

const userStore = useUserStore()

onMounted(() => {
    userStore.initUserInfo()
})
</script>

<template>
    <div id="app">
        <HeaderNav />
        <main class="main-content">
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
</style>
