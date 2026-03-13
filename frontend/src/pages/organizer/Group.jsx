import { useEffect, useState } from 'react'
import api from '../../api/axios'
import { Nav, PageHeader, Alert, Spinner, Modal } from '../../components/UI'

const NAV_LINKS = [
  { to: '/organizer/dashboard', label: 'Eventos' },
  { to: '/organizer/participants', label: 'Participants' },
  { to: '/organizer/staff',     label: 'Equipe'  },
  { to: '/organizer/group',     label: 'Grupo'   },
]

export default function Group() {
  const [group,      setGroup]      = useState(null)
  const [organizers, setOrganizers] = useState([])
  const [loading,    setLoading]    = useState(true)
  const [error,      setError]      = useState('')
  const [showInvite, setShowInvite] = useState(false)

  function load() {
    setLoading(true)
    Promise.all([api.get('/groups/me'), api.get('/groups/me/organizers')])
      .then(([gRes, oRes]) => {
        setGroup(gRes.data)
        setOrganizers(oRes.data)
      })
      .catch(() => setError('Erro ao carregar dados do grupo.'))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  return (
    <div className="page">
      <Nav links={NAV_LINKS} />
      <div className="container" style={{ padding: '32px 24px' }}>
        <PageHeader title="Grupo" sub="Configurações e membros organizadores" />

        {error && <Alert>{error}</Alert>}
        {loading && <div className="empty"><Spinner /></div>}

        {group && (
          <>
            <div className="card-sm" style={{ marginBottom: 28 }}>
              <div className="section-title">Informações</div>
              <div style={{ fontWeight: 600, fontSize: 18 }}>{group.name}</div>
              <div style={{ color: 'var(--subtle)', fontSize: 13, marginTop: 4 }}>
                Criado em {new Date(group.createdAt).toLocaleDateString('pt-BR')}
              </div>
            </div>

            <div className="row-between" style={{ marginBottom: 12 }}>
              <div className="section-title" style={{ marginBottom: 0 }}>Organizers</div>
              <button className="btn btn-ghost" style={{ fontSize: 13 }}
                onClick={() => setShowInvite(true)}>
                + Convidar organizer
              </button>
            </div>

            <div className="table-wrap card-sm">
              <table>
                <thead>
                  <tr>
                    <th>Nome</th>
                    <th>E-mail</th>
                    <th>Membro desde</th>
                  </tr>
                </thead>
                <tbody>
                  {organizers.map(o => (
                    <tr key={o.id}>
                      <td style={{ fontWeight: 500 }}>{o.name}</td>
                      <td style={{ color: 'var(--subtle)' }}>{o.email}</td>
                      <td style={{ color: 'var(--subtle)', fontSize: 13 }}>
                        {new Date(o.createdAt).toLocaleDateString('pt-BR')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>

      {showInvite && <InviteModal onClose={() => setShowInvite(false)} onCreated={load} />}
    </div>
  )
}

function InviteModal({ onClose, onCreated }) {
  const [form,    setForm]    = useState({ name: '', email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error,   setError]   = useState('')

  function set(field) {
    return e => setForm(f => ({ ...f, [field]: e.target.value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post('/groups/me/organizers/invite', form)
      onCreated()
      onClose()
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao convidar organizer.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal title="Convidar Organizer" onClose={onClose}>
      {error && <Alert>{error}</Alert>}
      <form className="form" style={{ marginTop: error ? 16 : 0 }} onSubmit={handleSubmit}>
        <div className="field">
          <label>Nome *</label>
          <input value={form.name} onChange={set('name')} required />
        </div>
        <div className="field">
          <label>E-mail *</label>
          <input type="email" value={form.email} onChange={set('email')} required />
        </div>
        <div className="field">
          <label>Senha inicial *</label>
          <input type="password" value={form.password} onChange={set('password')} required />
        </div>
        <div className="row" style={{ justifyContent: 'flex-end', marginTop: 8 }}>
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" disabled={loading}>
            {loading ? <Spinner /> : 'Convidar'}
          </button>
        </div>
      </form>
    </Modal>
  )
}