import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { authApi } from '../services/api'

const AuthContext = createContext(null)
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [ready, setReady] = useState(false)
  useEffect(() => {
    if (localStorage.getItem('civicvote-token')) authApi.profile().then(({ data }) => setUser(data)).catch(() => localStorage.removeItem('civicvote-token')).finally(() => setReady(true))
    else setReady(true)
  }, [])
  const value = useMemo(() => ({
    user, ready,
    async login(credentials) { const { data } = await authApi.login(credentials); localStorage.setItem('civicvote-token', data.token); setUser(data.user); return data.user },
    async register(credentials) { return authApi.register(credentials) },
    logout() { localStorage.removeItem('civicvote-token'); setUser(null) },
  }), [user, ready])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
export const useAuth = () => useContext(AuthContext)
