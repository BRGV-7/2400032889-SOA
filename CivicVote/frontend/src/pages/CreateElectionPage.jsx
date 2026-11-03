import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Notice from '../components/Notice'
import { electionApi } from '../services/api'
import { errorMessage } from '../utils/format'

function toLocalDateTime(value) {
  const localDate = new Date(value.getTime() - value.getTimezoneOffset() * 60000)
  return localDate.toISOString().slice(0, 16)
}

function initialForm() {
  const startsAt = new Date(Date.now() + 5 * 60 * 1000)
  startsAt.setSeconds(0, 0)
  const endsAt = new Date(startsAt.getTime() + 60 * 60 * 1000)
  return { title: '', description: '', startsAt: toLocalDateTime(startsAt), endsAt: toLocalDateTime(endsAt) }
}

export default function CreateElectionPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const minStart = toLocalDateTime(new Date())

  async function submit(e) {
    e.preventDefault()
    const startsAt = new Date(form.startsAt)
    const endsAt = new Date(form.endsAt)
    if (startsAt <= new Date()) {
      setError('Choose a start time in the future.')
      return
    }
    if (endsAt <= startsAt) {
      setError('Choose an end time later than the start time.')
      return
    }

    setBusy(true)
    setError('')
    try {
      const { data } = await electionApi.create({
        ...form,
        startsAt: startsAt.toISOString(),
        endsAt: endsAt.toISOString(),
      })
      navigate(`/elections/${data.id}`)
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  function updateStart(value) {
    const start = new Date(value)
    const currentEnd = new Date(form.endsAt)
    const nextEnd = currentEnd > start ? form.endsAt : toLocalDateTime(new Date(start.getTime() + 60 * 60 * 1000))
    setForm({ ...form, startsAt: value, endsAt: nextEnd })
  }

  return <><div className="page-heading"><div><span className="eyebrow">ADMINISTRATION</span><h1>Create an election</h1><p>Set up a transparent election for your community.</p></div></div><form className="form-card" onSubmit={submit}><Notice>{error}</Notice><label>Election title<input required maxLength="160" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></label><label>Description<textarea rows="4" maxLength="2000" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></label><div className="form-row"><label>Starts at<input required type="datetime-local" min={minStart} value={form.startsAt} onChange={(e) => updateStart(e.target.value)} /></label><label>Ends at<input required type="datetime-local" min={form.startsAt || minStart} value={form.endsAt} onChange={(e) => setForm({ ...form, endsAt: e.target.value })} /></label></div><button className="button primary" disabled={busy}>{busy ? 'Creating…' : 'Create election'}</button></form></>
}
