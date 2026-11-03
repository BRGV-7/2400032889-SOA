import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import Loading from '../components/Loading'
import Notice from '../components/Notice'
import { useAuth } from '../context/AuthContext'
import { useRequest } from '../hooks/useRequest'
import { electionApi, resultApi } from '../services/api'

const NOTA_CANDIDATE_ID = -1

export default function ResultsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const { user } = useAuth(); const { data: elections, loading, error } = useRequest(electionApi.list, []); const [id, setId] = useState(searchParams.get('electionId') || '')
  if (loading) return <Loading />
  const selectedElection = (elections || []).find((election) => String(election.id) === id)
  const administrativeView = user?.role === 'ADMIN'
  return <><div className="page-heading"><div><span className="eyebrow">{administrativeView ? 'ADMINISTRATIVE REVIEW' : 'TRANSPARENCY'}</span><h1>{administrativeView ? 'Monitor election results' : 'Election results'}</h1><p>Results are calculated from persisted anonymous ballots and disclosed according to election rules.</p></div></div><Notice>{error}</Notice><label className="select-election">Choose an election<select value={id} onChange={(e) => { setId(e.target.value); if (e.target.value) setSearchParams({ electionId: e.target.value }); else setSearchParams({}) }}><option value="">Select election…</option>{(elections || []).map((e) => <option key={e.id} value={e.id}>{e.title} · {e.status}</option>)}</select></label>{selectedElection && <ResultChart id={id} election={selectedElection} />}</>
}
function ResultChart({ id, election }) {
  const { user } = useAuth(); const { data, loading, error, reload } = useRequest(() => resultApi.get(id), [id]); const [publishing, setPublishing] = useState(false); const [publishError, setPublishError] = useState(''); const [publishedMessage, setPublishedMessage] = useState('')
  if (loading) return <Loading label="Loading published results…" />
  if (error) return <section className="result-panel"><div><span className="eyebrow">RESULTS STATUS</span><h2>{election.title}</h2></div>{user?.role === 'ADMIN' && election.status === 'CLOSED' ? <><p className="muted">This election is closed. Publish its persisted ballot tally to make results available.</p><Notice>{publishError}</Notice><button className="button primary" disabled={publishing} onClick={async () => { setPublishing(true); setPublishError(''); try { await resultApi.publish(id); setPublishedMessage('Results published successfully.'); await reload() } catch (e) { setPublishError(e.response?.data?.detail || e.response?.data?.message || 'Unable to publish results.') } finally { setPublishing(false) } }}>{publishing ? 'Publishing…' : 'Publish results'}</button><Notice type="success">{publishedMessage}</Notice></> : <p className="muted">{user?.role === 'ADMIN' ? `Results stay hidden while voting is ${election.status?.toLowerCase()}.` : 'Results are not available yet. They will appear here after publication.'}</p>}</section>
  const rows = data?.candidates || []
  const winnerRows = rows.filter((row) => data.winners?.includes(row.candidateId))
  const winnerNames = winnerRows.map((row) => row.candidateName)
  const winnerLabel = data.tied
    ? `Tie: ${winnerNames.join(', ')}`
    : winnerRows[0]?.candidateId === NOTA_CANDIDATE_ID
      ? 'NOTA received the most votes; no listed candidate was selected.'
      : winnerNames.length ? `Winner: ${winnerNames.join(', ')}` : ''
  return <section className="result-panel"><div><span className="eyebrow">OFFICIAL TALLY</span><h2>{data.electionTitle || 'Election results'}</h2><p className="muted">{data.totalVotes} ballots counted</p>{winnerLabel && <p className="result-winner">{winnerLabel}</p>}</div>{rows.length ? <div className="chart-wrap"><ResponsiveContainer width="100%" height={320}><BarChart data={rows}><CartesianGrid strokeDasharray="3 3" vertical={false} /><XAxis dataKey="candidateName" /><YAxis allowDecimals={false} /><Tooltip /><Bar dataKey="votes" fill="#138a83" radius={[6, 6, 0, 0]} /></BarChart></ResponsiveContainer></div> : <div className="empty-state">No votes have been cast.</div>}<div className="result-list">{rows.map((row) => <div key={row.candidateId}><span>{row.candidateName}</span><strong>{row.votes} · {Number(row.percentage).toFixed(1)}%</strong></div>)}</div></section>
}
