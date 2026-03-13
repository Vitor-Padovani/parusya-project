import axios from 'axios'

// ─── Configura a baseURL da API ─────────────────────────────────────────────
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/v1', // backend local padrão
  headers: {
    'Content-Type': 'application/json',
    'ngrok-skip-browser-warning': 'true',
  },
})

// ─── Interceptor de requisição: adiciona JWT se existir ─────────────────────
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token') // ⚠️ garante que o token está salvo com a chave 'token'
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ─── Interceptor de resposta: trata 401 e outros erros ─────────────────────
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      const isLoginRoute = error.config?.url?.includes('/auth/login')
      if (error.response.status === 401 && !isLoginRoute) {
        // Token inválido ou expirado → limpa storage e redireciona
        localStorage.clear()
        window.location.href = '/'
      }
    } else {
      console.error('Erro de rede ou CORS:', error)
    }
    return Promise.reject(error)
  }
)

export default api