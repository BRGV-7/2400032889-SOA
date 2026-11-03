import { useMemo } from 'react'
import { Activity, CalendarDays, CheckCircle2, Vote } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { electionApi } from '../services/api'
import { useRequest } from '../hooks/useRequest'
import { formatDate } from '../utils/format'
import Loading from '../components/Loading'
import Notice from '../components/Notice'
import StatCard from '../components/StatCard'
export default function DashboardPage() {
  const { user } = useAuth(); const { data, loading, error } = useRequest(electionApi.list, [])
  const elections = useMemo(() => data || [], [data])
  if (loading) return <Loading />
  return <><div className="page-heading"><div><span className="eyebrow">YOUR CIVIC SPACE</span><h1>Good day, {user?.username}</h1><p>Stay informed and take part in the decisions that shape your community.</p></div></div><Notice>{error}</Notice><div className="stats-grid"><StatCard icon={CalendarDays} label="Total elections" value={elections.length} /><StatCard icon={Activity} label="Open now" value={elections.filter((e) => e.status === 'OPEN').length} /><StatCard icon={CheckCircle2} label="Your role" value={user?.role} /></div><div className="section-heading"><div><span className="eyebrow">UPDATES</span><h2>Current elections</h2></div><a className="text-link" href="/elections">View all →</a></div><div className="election-grid">{elections.slice(0, 4).map((e) => <article className="election-card" key={e.id}><div className="card-top"><span className={`status-badge ${e.status?.toLowerCase()}`}>{e.status}</span><Vote size={19} /></div><h3>{e.title}</h3><p>{e.description || 'Participate in this community election.'}</p><div className="card-meta"><span>Starts {formatDate(e.startsAt)}</span><span>Ends {formatDate(e.endsAt)}</span></div><a className="button secondary" href={`/elections/${e.id}`}>View election</a></article>)}</div>{!elections.length && <div className="empty-state">No elections have been created yet.</div>}</>
}
