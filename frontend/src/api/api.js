import axios from 'axios'

/**
 * Use relative URL so Vite proxy works in dev and nginx works in Docker/prod.
 * Override with VITE_API_URL if needed (e.g. absolute URL in some deployments).
 */
const API = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '',
  headers: {
    'Content-Type': 'application/json',
  },
})

API.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.detail ||
      error.response?.data?.message ||
      error.message ||
      'Une erreur est survenue'

    // Keep console for debugging
    console.error('API Error:', error.response?.data || error.message)

    // Attach a friendly message so callers can display it easily
    error.friendlyMessage = typeof message === 'string' ? message : JSON.stringify(message)
    return Promise.reject(error)
  }
)

export default API
