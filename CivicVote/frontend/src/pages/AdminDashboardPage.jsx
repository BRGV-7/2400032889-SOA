import { useMemo } from 'react'
import { Link } from 'react-router-dom'
import { Activity, BarChart3, CalendarDays, CirclePlus, ClipboardList, ShieldCheck } from 'lucide-react'
import Loading from '../components/Loading'
import Notice from '../components/Notice'
import StatCard from '../components/StatCard'
import { useAuth } from '../context/AuthContext'
import { useRequest } from '../hooks/useRequest'
import { electionApi } from '../services/api'
import { formatDate } from '../utils/format'

export default function AdminDashboardPage() {
  const { user } = useAuth()
  const { data, loading, error, reload } = useRequest(electionApi.list, [])
  const elections = useMemo(() => data || [], [data])
  if (loading) return <Loading label="Loading election management…" />

  const open = elections.filter((election) => election.status === 'OPEN').length
  const scheduled = elections.filter((election) => election.status === 'SCHEDULED').length
  const closed = elections.filter((election) => election.status === 'CLOSED').length

  return <>
    <div className="page-heading">
      <div><span className="eyebrow">ADMINISTRATION</span><h1>Admin dashboard</h1><p>Welcome, {user?.username}. Create elections, manage candidates, and review published results.</p></div>
      <Link className="button primary" to="/admin/elections/new"><CirclePlus size={17} />Create election</Link>
    </div>
    <Notice>{error}</Notice>
    <div className="admin-shortcuts">
      <Link className="admin-shortcut" to="/admin/elections"><ClipboardList size={19} /><span><strong>Manage elections</strong><small>Edit scheduled elections and manage their candidates</small></span><span className="shortcut-arrow">→</span></Link>
      <Link className="admin-shortcut" to="/results"><BarChart3 size={19} /><span><strong>Monitor results</strong><small>Review results and publish tallies for closed elections</small></span><span className="shortcut-arrow">→</span></Link>
    </div>
    <div className="stats-grid">
      <StatCard icon={CalendarDays} label="All elections" value={elections.length} />
      <StatCard icon={Activity} label="Voting open" value={open} />
      <StatCard icon={ShieldCheck} label="Scheduled / closed" value={`${scheduled} / ${closed}`} />
    </div>
    <div className="section-heading"><div><span className="eyebrow">ELECTION OVERSIGHT</span><h2>Recent elections</h2></div><button className="text-button" onClick={() => reload().catch(() => {})}>Refresh</button></div>
    <div className="admin-election-list">
      {elections.slice(0, 6).map((election) => <article className="admin-election-row" key={election.id}>
        <div className="admin-election-info"><span className={`status-badge ${election.status?.toLowerCase()}`}>{election.status}</span><h3>{election.title}</h3><p>{election.candidates?.length || 0} candidates and options · Closes {formatDate(election.endsAt)}</p></div>
        <div className="admin-election-actions"><Link className="button secondary" to={`/elections/${election.id}`}>{election.status === 'SCHEDULED' ? 'Manage election' : 'View election'}</Link>{election.status === 'CLOSED' && <Link className="text-link" to={`/results?electionId=${election.id}`}>Review results →</Link>}</div>
      </article>)}
      {!elections.length && <div className="empty-state">No elections yet. Create your first election to get started.</div>}
    </div>
    <p className="admin-integrity-note">Role restrictions and election rules are enforced by the backend. Ballots are recorded separately from voter participation.</p>
  </>
}
