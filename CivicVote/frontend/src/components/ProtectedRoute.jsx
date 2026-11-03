import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Loading from './Loading'
export default function ProtectedRoute({ role }) {
  const { user, ready } = useAuth()
  if (!ready) return <Loading label="Checking your session…" />
  if (!user) return <Navigate to="/login" replace />
  if (role && user.role !== role) return <Navigate to="/denied" replace />
  return <Outlet />
}
