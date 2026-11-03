export default function StatCard({ icon: Icon, label, value }) {
  return <article className="stat-card"><span className="stat-icon"><Icon size={20} /></span><div><span>{label}</span><strong>{value}</strong></div></article>
}
