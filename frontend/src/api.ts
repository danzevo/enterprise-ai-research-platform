// Vite magically injects the .env variable using import.meta.env
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

// Helper to get the JWT token
const getToken = () => localStorage.getItem('jwt_token')

// Helper to construct headers with the token
const getHeaders = () => {
    const token = getToken()
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    }
}

// --- Auth Endpoints ---
export const login = async (username: string, password: string) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    })
    if (!response.ok) throw new Error('Invalid credentials')
    return await response.json()
}

export const register = async (username: string, password: string) => {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    })
    if (!response.ok) throw new Error('Registration failed')
    return await response.json()
}

// --- Research Endpoints ---
export const fetchAllTasks = async () => {
    const response = await fetch(`${API_BASE_URL}/research`, {
        headers: getHeaders()
    })
    if (response.status === 401) {
        localStorage.removeItem('jwt_token') // Clear expired token
        window.location.href = '/login' // Force re-login
    }
    return await response.json()
}

// Centralized function to submit tasks
export const submitResearchTask = async (topic: string) => {
    return await fetch(`${API_BASE_URL}/research`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify({ topic })
    })
}