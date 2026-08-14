<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api'

const router = useRouter()
const username = ref('')
const password = ref('')
const errorMsg = ref('')
const isLoading = ref(false)

const handleLogin = async() => {
    try {
        isLoading.value = true
        errorMsg.value = ''
        const data = await login(username.value, password.value)
        localStorage.setItem('jwt_token', data.token) // Save token
        router.push('/') // Navigate to Dashboard
    } catch(e: any) {
        errorMsg.value = e.message || 'Login failed'
    } finally {
        isLoading.value = false
    }
}
</script>
<template>
    <div class="flex items-center justify-center min-h-screen p-4">
        <div class="w-full max-w-md p-8 space-y-6 bg-slate-900/50 backdrop-blur-xl rounded-2xl border border-slate-800 shadow-2xl">
            <div class="text-center">
                <h1 class="text-3xl font-extrabold tracking-tight bg-gradient-to-r from-indigo-400 to-cyan-400 bg-clip-text text-transparent mb-2">
                    Welcome Back
                </h1>
                <p class="text-slate-400">Log in to access your AI Research Dashboard</p>
            </div>
            <form @submit.prevent="handleLogin" class="space-y-4">
                <div>
                    <input v-model="username" type="text" required placeholder="Username"
                        class="w-full px-4 py-3 bg-slate-800/50 border border-slate-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all placeholder:text-slate-500" />
                </div>
                <div>
                    <input v-model="password" type="password" required placeholder="Password"
                        class="w-full px-4 py-3 bg-slate-800/50 border border-slate-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all placeholder:text-slate-500" />
                </div>
                <p v-if="errorMsg" class="text-red-400 text-sm text-center">{{  errorMsg }}</p>

                <button type="submit" :disabled="isLoading"
                        class="w-full py-3 px-4 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl font-medium transition-all shadow-lg shadow-indigo-600/20 disabled:opacity-50">
                    {{  isLoading ? 'Authenticating...' : 'Sign In' }}    
                </button>
            </form>

            <p class="text-center text-slate-400 text-sm">
                Don't have an account?
                <router-link to="/register" class="text-cyan-400 hover:text-cyan-300 font-medium ml-1 transition-colors">Create one</router-link>
            </p>
        </div>
    </div>
</template>