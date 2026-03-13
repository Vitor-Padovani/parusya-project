import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { Alert, Spinner } from '../components/UI'
import { EyeIcon } from '../pages/participant/Register'

export default function LoginPage({ role, title, redirectTo }) {
  const [email,    setEmail]    = useState('')
  const [password, setPassword] = useState('')
  const [loading,  setLoading]  = useState(false)
  const [error,    setError]    = useState('')
  const { login }  = useAuth()
  const navigate   = useNavigate()
  const [show, setShow] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password, role)
      navigate(redirectTo, { replace: true })
    } catch (err) {
      setError(err.response?.data?.message || 'Credenciais inválidas')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="centered">
      <div className="card" style={{ width: '100%', maxWidth: 400 }}>
        <div style={{ marginBottom: 28 }}>
          <div className="mono" style={{ fontSize: 20, fontWeight: 500, marginBottom: 4 }}>入</div>
          <h1 style={{ fontSize: 22, fontWeight: 600 }}>{title}</h1>
        </div>

        {error && <Alert>{error}</Alert>}

        <form className="form" style={{ marginTop: error ? 16 : 0 }} onSubmit={handleSubmit}>
          <div className="field">
            <label>E-mail</label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)}
              placeholder="seu@email.com" required autoFocus />
          </div>
          <div className="field" style={{ position: 'relative' }}>
            <label>Senha</label>
            <input type={show ? 'text' : 'password'} value={password} onChange={e => setPassword(e.target.value)}
              placeholder="Digite sua senha" required
              />
            <button
              type="button"
              onClick={() => setShow(v => !v)}
              aria-label={show ? 'Ocultar senha' : 'Mostrar senha'}
              style={{
                position: 'absolute',
                right: 12,
                top: '71%',
                transform: 'translateY(-50%)',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                color: 'var(--subtle)',
                padding: 0,
                display: 'flex',
                alignItems: 'center',
              }}
            >
              <EyeIcon open={show} />
            </button>
          </div>
          
          <button className="btn btn-primary btn-full" disabled={loading} style={{ marginTop: 8 }}>
            {loading ? <Spinner /> : 'Entrar'}
          </button>
        </form>

        {role === 'PARTICIPANT' && (
          <p style={{ textAlign: 'center', marginTop: 20, color: 'var(--subtle)', fontSize: 13 }}>
            Não tem conta? <Link to="/register">Cadastre-se</Link>
          </p>
        )}

        <p style={{ textAlign: 'center', marginTop: 20, color: 'var(--subtle)', fontSize: 13 }}>
          <Link to="/">← Voltar</Link>
        </p>
      </div>
    </div>
  )
}