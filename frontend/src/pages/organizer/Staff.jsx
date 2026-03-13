import { useEffect, useState } from 'react'
import api from '../../api/axios'
import { Nav, PageHeader, Alert, Spinner, Modal } from '../../components/UI'

const NAV_LINKS = [
  { to: '/organizer/dashboard', label: 'Eventos' },
  { to: '/organizer/participants', label: 'Participants' },
  { to: '/organizer/staff',     label: 'Equipe'  },
  { to: '/organizer/group',     label: 'Grupo'   },
]

export default function Staff() {
  const [list,    setList]    = useState([])
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState('')
  const [showNew, setShowNew] = useState(false)

  function load() {
    setLoading(true)
    api.get('/staff')
      .then(r => setList(r.data))
      .catch(() => setError('Erro ao carregar equipe.'))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  async function handleDelete(id) {
    if (!confirm('Remover este membro da equipe?')) return
    await api.delete(`/staff/${id}`)
    load()
  }

  return (
    <div className="page">
      <Nav links={NAV_LINKS} />
      <div className="container" style={{ padding: '32px 24px' }}>
        <PageHeader
          title="Equipe"
          sub="EventStaff vinculados ao seu grupo"
          action={
            <button className="btn btn-primary" onClick={() => setShowNew(true)}>
              + Adicionar membro
            </button>
          }
        />

        {error && <Alert>{error}</Alert>}

        {loading ? (
          <div className="empty"><Spinner /></div>
        ) : list.length === 0 ? (
          <div className="empty">Nenhum membro cadastrado ainda.</div>
        ) : (
          <div className="table-wrap card-sm">
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>E-mail</th>
                  <th>Cadastrado em</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {list.map(s => (
                  <tr key={s.id}>
                    <td style={{ fontWeight: 500 }}>{s.name}</td>
                    <td style={{ color: 'var(--subtle)' }}>{s.email}</td>
                    <td style={{ color: 'var(--subtle)', fontSize: 13 }}>
                      {new Date(s.createdAt).toLocaleDateString('pt-BR')}
                    </td>
                    <td>
                      <button className="btn btn-danger"
                        style={{ fontSize: 13, padding: '4px 10px' }}
                        onClick={() => handleDelete(s.id)}>
                        Remover
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {showNew && <NewStaffModal onClose={() => setShowNew(false)} onCreated={load} />}
    </div>
  )
}

function NewStaffModal({ onClose, onCreated }) {
  const [form, setForm] = useState({ name: '', email: '', password: '' })
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
      await api.post('/staff', form)
      onCreated()
      onClose()
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao cadastrar membro.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal title="Adicionar membro" onClose={onClose}>
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
            {loading ? <Spinner /> : 'Adicionar'}
          </button>
        </div>
      </form>
    </Modal>
  )
}