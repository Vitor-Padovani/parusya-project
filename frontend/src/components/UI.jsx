// ─── Spinner ──────────────────────────────────────────────────────────────────
export function Spinner() {
  return <span className="spin" aria-label="carregando">⟳</span>
}

// ─── Alert ────────────────────────────────────────────────────────────────────
export function Alert({ type = 'error', children }) {
  return <div className={`alert alert-${type}`}>{children}</div>
}

// ─── Nav ──────────────────────────────────────────────────────────────────────
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export function Nav({ links = [] }) {
  const { logout } = useAuth()
  const navigate   = useNavigate()

  function handleLogout() {
    logout()
    navigate('/')
  }

  return (
    <nav className="nav">
      <span className="nav-brand mono">入</span>
      {links.map(({ to, label }) => (
        <Link key={to} to={to} className="nav-link">{label}</Link>
      ))}
      <button className="btn btn-ghost" style={{ fontSize: 13, padding: '6px 12px' }}
        onClick={handleLogout}>
        Sair
      </button>
    </nav>
  )
}

// ─── PageHeader ───────────────────────────────────────────────────────────────
export function PageHeader({ title, sub, action }) {
  return (
    <div className="row-between" style={{ marginBottom: 28 }}>
      <div>
        <h1 className="page-title">{title}</h1>
        {sub && <p className="page-sub">{sub}</p>}
      </div>
      {action}
    </div>
  )
}

// ─── Modal ────────────────────────────────────────────────────────────────────
export function Modal({ title, onClose, children }) {
  return (
    <div style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,.7)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      zIndex: 100, padding: 24,
    }}>
      <div className="card" style={{ width: '100%', maxWidth: 480, position: 'relative' }}>
        <div className="row-between" style={{ marginBottom: 24 }}>
          <h2 style={{ fontSize: 18, fontWeight: 600 }}>{title}</h2>
          <button className="btn btn-ghost" style={{ padding: '4px 10px' }}
            onClick={onClose}>✕</button>
        </div>
        {children}
      </div>
    </div>
  )
}