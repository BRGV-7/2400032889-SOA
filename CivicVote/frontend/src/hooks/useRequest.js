import { useCallback, useEffect, useState } from 'react'
export function useRequest(request, dependencies = []) {
  const [state, setState] = useState({ data: null, loading: true, error: '' })
  const run = useCallback(async () => {
    setState((current) => ({ ...current, loading: true, error: '' }))
    try { const { data } = await request(); setState({ data, loading: false, error: '' }); return data }
    catch (error) { const message = error.response?.data?.message || error.response?.data?.detail || 'Unable to load data. Please try again.'; setState({ data: null, loading: false, error: message }); throw error }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, dependencies)
  useEffect(() => { run().catch(() => {}) }, [run])
  return { ...state, reload: run }
}
