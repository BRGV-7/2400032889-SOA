import { useState } from 'react'
import { votingApi } from '../services/api'
import { errorMessage } from '../utils/format'
import Notice from '../components/Notice'
export default function StatusPage() {
  const [id, setId] = useState(''); const [status, setStatus] = useState(null); const [error, setError] = useState('')
  async function check(e) { e.preventDefault(); setError(''); setStatus(null); try { const { data } = await votingApi.status(id); setStatus(data) } catch (err) { setError(errorMessage(err)) } }
  return <><div className="page-heading"><div><span className="eyebrow">PRIVATE RECORD</span><h1>Voting status</h1><p>Check participation without revealing your anonymous selection.</p></div></div><form className="form-card compact" onSubmit={check}><label>Election ID<input type="number" min="1" required value={id} onChange={(e) => setId(e.target.value)} /></label><Notice>{error}</Notice><button className="button primary">Check status</button>{status && <Notice type="success">{status.hasVoted ? 'A ballot was recorded for this election.' : 'No participation has been recorded for this election.'}</Notice>}</form></>
}
