import { createContext, useContext, useState, useCallback } from 'react'
import api from '../api/axios'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem('token')
    const role  = localStorage.getItem('role')
    const user  = JSON.parse(localStorage.getItem('user') || 'null')
    return token ? { token, role, user } : null
  })

  const login = useCallback(async (email, password, role) => {
    const endpoint = {
      ORGANIZER:   '/auth/login/organizer',
      EVENT_STAFF: '/auth/login/staff',
      PARTICIPANT: '/auth/login/participant',
    }[role]

    const { data } = await api.post(endpoint, { email, password })
    localStorage.setItem('token', data.token)
    localStorage.setItem('role',  data.user.role)
    localStorage.setItem('user',  JSON.stringify(data.user))
    setAuth({ token: data.token, role: data.user.role, user: data.user })
    return data.user.role
  }, [])

  const logout = useCallback(() => {
    localStorage.clear()
    setAuth(null)
  }, [])

  return (
    <AuthContext.Provider value={{ auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)