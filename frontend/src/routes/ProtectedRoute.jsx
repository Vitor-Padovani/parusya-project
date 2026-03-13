import { Navigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function ProtectedRoute({ children, role }) {
  const { auth } = useAuth()
  if (!auth) return <Navigate to="/" replace />
  if (role && auth.role !== role) return <Navigate to="/" replace />
  return children
}