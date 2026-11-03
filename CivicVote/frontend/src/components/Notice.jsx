import { AlertCircle, CheckCircle2 } from 'lucide-react'
export default function Notice({ children, type = 'error' }) {
  return children ? <div className={`notice ${type}`} role={type === 'error' ? 'alert' : 'status'}>{type === 'error' ? <AlertCircle size={18} /> : <CheckCircle2 size={18} />}{children}</div> : null
}
