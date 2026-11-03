import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { BarChart3, ClipboardList, LayoutDashboard, LogOut, ShieldCheck, Vote } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
const voterLinks = [
  { to: '/', label: 'Overview', icon: LayoutDashboard },
  { to: '/elections', label: 'Elections', icon: ClipboardList },
  { to: '/vote', label: 'Cast your vote', icon: Vote, voter: true },
  { to: '/status', label: 'Voting status', icon: ShieldCheck, voter: true },
  { to: '/results', label: 'Results', icon: BarChart3 },
]
const adminLinks = [
  { to: '/admin', label: 'Admin dashboard', icon: LayoutDashboard },
  { to: '/admin/elections', label: 'Manage elections', icon: ClipboardList },
  { to: '/admin/elections/new', label: 'Create election', icon: Vote },
  { to: '/results', label: 'Monitor results', icon: BarChart3 },
]
export default function AppLayout() {
  const { user, logout } = useAuth(); const navigate = useNavigate()
  const links = user?.role === 'ADMIN' ? adminLinks : voterLinks
  return <div className="app-shell"><aside className="sidebar"><a className="brand" href={user?.role === 'ADMIN' ? '/admin' : '/'}><span className="brand-mark"><ShieldCheck /></span><span>Civic<span>Vote</span><small>TECHNOLOGIES</small></span></a><nav>{links.map(({ to, label, icon: Icon }) => <NavLink key={to} to={to} end={to === '/' || to === '/admin'}><Icon size={18} />{label}</NavLink>)}</nav><div className="sidebar-bottom"><div className="profile"><div className="avatar">{user?.username?.slice(0, 1).toUpperCase()}</div><div><strong>{user?.username}</strong><span>{user?.role}</span></div></div><button className="logout" onClick={() => { logout(); navigate('/login') }}><LogOut size={17} />Sign out</button></div></aside><main className="main-area"><header className="topbar"><span>{user?.role === 'ADMIN' ? 'CivicVote administration' : 'Trusted civic participation'}</span><span className="secure-label"><span className="pulse" />Secure session</span></header><div className="page-content"><Outlet /></div></main></div>
}
