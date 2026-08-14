<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import TaskForm from '../components/TaskForm.vue'
import TaskList from '../components/TaskList.vue'
import { fetchAllTasks } from '../api'
import { wsService } from '../websocket'

const router = useRouter()
const tasks = ref<any[]>([])

const fetchTasks = async() => {
    try {
        tasks.value = await fetchAllTasks()
    } catch (error) {
        console.error("Error fetching tasks:", error)
    }
}

const handleLogout = () => {
    localStorage.removeItem('jwt_token')
    wsService.disconnect()
    router.push('/login')
}

// Fetch tasks and connect to WebSockets when dashboard loads
onMounted(() => {
    fetchTasks()
    wsService.connect((completedTask) => {
        // 🌟 REAL-TIME MAGIC 🌟
        // When the backend broadcasts a finished task, we find it in our local state and update it instantly!
        const index = tasks.value.findIndex(t => t.id === completedTask.id)
        if (index !== -1) {
            tasks.value[index] = completedTask
        } else {
            // If we don't have it (maybe submitted on another tab), add it to the top!
            tasks.value.unshift(completedTask)
        }
    })
})

// Clean up WebSocket connection when navigating away
onUnmounted(() => {
    wsService.disconnect()
})
</script>
<template>
    <div class="p-8">
        <header class="max-w-5xl mx-auto mb-12 flex justify-between items-center">
            <div class="space-y-2">
                <h1 class="text-5xl font-extrabold tracking-tight bg-gradient-to-r from-indigo-400 to-cyan-400 bg-clip-text text-transparent">
                    Enterprise AI Researcher
                </h1>
                <p class="text-slate-400 text-lg">
                    Secured, real-time, event-driven architecture.
                </p>
            </div>

            <!-- Logout Button -->
            <button @click="handleLogout" class="px-5 py-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-300 rounded-xl transition-colors font-medium text-sm">
                Sign Out
            </button>
        </header>
        <main class="max-w-3xl mx-auto">
            <TaskForm @task-submitted="fetchTasks"/>
            <TaskList :tasks="tasks" />
        </main>
    </div>
</template>