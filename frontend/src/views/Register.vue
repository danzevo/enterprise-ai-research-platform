<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register, login } from '../api'

const router = useRouter()
const username = ref('')
const password = ref('')
const errorMsg = ref('')
const isLoading = ref(false)

const handleRegister = async() => {
    try {
        isLoading.value = true
        errorMsg.value = ''

        // 1. Register the user
        await register(username.value, password.value)

        // 2. Automatically log them in for smooth UX
        const data = await login(username.value, password.value)
        localStorage.setItem('jwt_token', data.token)

        // 3. Redirect to dashboard
        router.push('/')
    } catch (e: any) {
        errorMsg.value = e.message || 'Registration failed'
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
                    Create Account
                </h1>
                <p class="text-slate-400">Join the AI Research Platform</p>
            </div>
            <form @submit.prevent="handleRegister" class="space-y-4">
                <div>
                    <input v-model="username" type="text" required placeholder="Choose a Username"
                        class="w-full px-4 py-3 bg-slate-800/50 border border-slate-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all placeholder:text-slate-500" />
                </div>
                <div>
                    <input v-model="password" type="password" required placeholder="Choose a Password"
                        class="w-full px-4 py-3 bg-slate-800/50 border border-slate-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all placeholder:text-slate-500" />
                </div>
                <p v-if="errorMsg" class="text-red-400 text-sm text-center">{{ errorMsg }}</p>
                <button type="submit" :disabled="isLoading"
                    class="w-full py-3 px-4 bg-cyan-600 hover:bg-cyan-500 text-white rounded-xl font-medium transition-all shadow-lg shadow-cyan-600/20 disabled:opacity-50">
                    {{ isLoading ? 'Creating Account...' : 'Sign Up & Login' }}
                </button>
            </form>
            <p class="text-center text-slate-400 text-sm">
                Already have an account?
                <router-link to="/login" class="text-indigo-400 hover:text-indigo-300 font-medium ml-1 transition-colors">
                    Sign in
                </router-link>
            </p>
        </div>
    </div>
</template>