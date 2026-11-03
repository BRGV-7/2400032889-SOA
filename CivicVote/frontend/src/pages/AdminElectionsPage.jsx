import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Search } from 'lucide-react'
import Loading from '../components/Loading'
import Notice from '../components/Notice'
import { useRequest } from '../hooks/useRequest'
import { electionApi } from '../services/api'
import { formatDate } from '../utils/format'

export default function AdminElectionsPage() {
  const { data, loading, error } = useRequest(electionApi.list, [])
  const [search, setSearch] = useState('')
  const elections = useMemo(() => (data || []).filter((election) => `${election.title} ${election.status}`.toLowerCase().includes(search.toLowerCase())), [data, search])
  if (loading) return <Loading label="Loading elections…" />
  return <>
    <div className="page-heading"><div><span className="eyebrow">ADMINISTRATION</span><h1>Manage elections</h1><p>Manage scheduled elections and candidates, and follow each election through publication.</p></div><Link className="button primary" to="/admin/elections/new">Create election</Link></div>
    <Notice>{error}</Notice>
    <div className="toolbar"><label className="search-box"><Search size={18} /><input placeholder="Search by title or status" value={search} onChange={(event) => setSearch(event.target.value)} /></label><span className="muted">{elections.length} elections</span></div>
    <div className="table-wrap"><table><thead><tr><th>Election</th><th>Status</th><th>Candidates &amp; options</th><th>Voting closes</th><th>Actions</th></tr></thead><tbody>{elections.map((election) => <tr key={election.id}><td><strong>{election.title}</strong><small>{election.description || 'Community election'}</small></td><td><span className={`status-badge ${election.status?.toLowerCase()}`}>{election.status}</span></td><td>{election.candidates?.length || 0}</td><td>{formatDate(election.endsAt)}</td><td><div className="table-actions"><Link className="text-link" to={`/elections/${election.id}`}>{election.status === 'SCHEDULED' ? 'Manage' : 'Details'}</Link>{election.status === 'CLOSED' && <Link className="text-link" to={`/results?electionId=${election.id}`}>Results</Link>}</div></td></tr>)}</tbody></table>{!elections.length && <div className="empty-state">No elections match your search.</div>}</div>
  </>
}
