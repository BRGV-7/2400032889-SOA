import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Search } from 'lucide-react'
import Loading from '../components/Loading'
import Notice from '../components/Notice'
import { useAuth } from '../context/AuthContext'
import { useRequest } from '../hooks/useRequest'
import { electionApi } from '../services/api'
import { formatDate } from '../utils/format'
export default function ElectionsPage() {
  const { user } = useAuth(); const { data, loading, error } = useRequest(electionApi.list, []); const [search, setSearch] = useState('')
  if (loading) return <Loading />
  const elections = (data || []).filter((e) => `${e.title} ${e.status}`.toLowerCase().includes(search.toLowerCase()))
  return <><div className="page-heading"><div><span className="eyebrow">COMMUNITY</span><h1>Elections</h1><p>Explore upcoming and active elections in your community.</p></div>{user?.role === 'ADMIN' && <Link className="button primary" to="/admin/elections/new">Create election</Link>}</div><Notice>{error}</Notice><div className="toolbar"><label className="search-box"><Search size={18} /><input placeholder="Search elections" value={search} onChange={(e) => setSearch(e.target.value)} /></label><span className="muted">{elections.length} elections</span></div><div className="table-wrap"><table><thead><tr><th>Election</th><th>Status</th><th>Voting period</th><th></th></tr></thead><tbody>{elections.map((e) => <tr key={e.id}><td><strong>{e.title}</strong><small>{e.description}</small></td><td><span className={`status-badge ${e.status?.toLowerCase()}`}>{e.status}</span></td><td>{formatDate(e.startsAt)} – {formatDate(e.endsAt)}</td><td><Link className="text-link" to={`/elections/${e.id}`}>Details →</Link></td></tr>)}</tbody></table>{!elections.length && <div className="empty-state">No matching elections found.</div>}</div></>
}
