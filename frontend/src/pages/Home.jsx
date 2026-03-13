import { useState, useEffect, useRef } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function Home() {
  const { auth } = useAuth()
  const navigate  = useNavigate()
  const [dropdownOpen, setDropdownOpen] = useState(false)
  const dropdownRef = useRef(null)

  useEffect(() => {
    if (!auth) return
    const dest = {
      PARTICIPANT: '/participant/qrcode',
      ORGANIZER:   '/organizer/dashboard',
      EVENT_STAFF: '/staff/scan',
    }[auth.role]
    if (dest) navigate(dest, { replace: true })
  }, [auth, navigate])

  // Fecha o dropdown ao clicar fora
  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '24px',
      position: 'relative',
    }}>

      {/* ── Conteúdo central ─────────────────────────────────────────────── */}
      <div style={{ textAlign: 'center', marginBottom: 32 }}>

        {/* ── Easter egg: dropdown escondido no caractere ───────────────────── */}
        <div ref={dropdownRef} style={{ position: 'relative', display: 'inline-block'}}>
          <span
            className="mono"
            title="入"
            onClick={() => setDropdownOpen(v => !v)}
            style={{
              fontSize: 22,
              fontWeight: 500,
              cursor: 'pointer',
              color: 'var(--subtle)',
              userSelect: 'none',
              transition: 'color .15s',
            }}
            onMouseEnter={e => e.currentTarget.style.color = 'var(--text)'}
            onMouseLeave={e => e.currentTarget.style.color = 'var(--subtle)'}
          >
            入
          </span>

          {dropdownOpen && (
            <div style={{
              position: 'absolute',
              top: 32,
              left: '50%',
              transform: 'translateX(-50%)',
              background: 'var(--surface)',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius)',
              padding: '4px',
              minWidth: 160,
              zIndex: 50,
              boxShadow: '0 8px 24px rgba(0,0,0,.4)',
            }}>
              {[
                { label: 'Organizer',  to: '/organizer/login' },
                { label: 'EventStaff', to: '/staff/login'     },
              ].map(({ label, to }) => (
                <button
                  key={to}
                  onClick={() => { setDropdownOpen(false); navigate(to) }}
                  style={{
                    display: 'block',
                    width: '100%',
                    textAlign: 'left',
                    background: 'none',
                    border: 'none',
                    borderRadius: 'calc(var(--radius) - 2px)',
                    color: 'var(--text)',
                    cursor: 'pointer',
                    fontFamily: 'var(--font)',
                    fontSize: 14,
                    padding: '8px 12px',
                    transition: 'background .1s',
                  }}
                  onMouseEnter={e => e.currentTarget.style.background = 'var(--border)'}
                  onMouseLeave={e => e.currentTarget.style.background = 'none'}
                >
                  {label}
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="mono" style={{ fontSize: 48, fontWeight: 500, letterSpacing: '-.03em' }}>
          Parusya
        </div>
        <p style={{ color: 'var(--subtle)', fontSize: 14, marginTop: 4 }}>
          Controle de presença por QR Code
        </p>
      </div>

      <div className="card" style={{ width: '100%', maxWidth: 320, textAlign: 'center' }}>
        <div style={{ marginBottom: 20 }}>
          <div style={{ fontWeight: 400, marginBottom: 4 }}>Acesse seu código de entrada</div>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          <button className="btn btn-primary btn-full" onClick={() => navigate('/participant/login')}>
            Entrar
          </button>
          <button className="btn btn-ghost btn-full" onClick={() => navigate('/register')}>
            Cadastrar
          </button>
        </div>
      </div>

      {/* ── Footer ───────────────────────────────────────────────────────── */}
      <footer style={{
        position: 'absolute',
        bottom: 24,
        left: 0,
        right: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 24,
        flexWrap: 'wrap',
        padding: '0 24px',
      }}>
        <span style={{ color: 'var(--subtle)', fontSize: 12 }}>© Parusya</span>

        <Link to="/about" style={{ color: 'var(--subtle)', fontSize: 12 }}>
          Sobre
        </Link>

        <Link to="/terms" style={{ color: 'var(--subtle)', fontSize: 12 }}>
          Termos e condições
        </Link>

        <a
          href="https://vitorpadovani.com.br/"
          target="_blank"
          rel="noopener noreferrer"
          style={{ color: 'var(--subtle)', fontSize: 12 }}
        >
          feito por Vitor Padovani
        </a>
      </footer>
    </div>
  )
}