import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, ReferenceLine,
} from 'recharts'
import api from '../../api/axios'
import { Nav, PageHeader, Spinner, Alert, Modal } from '../../components/UI'

const NAV_LINKS = [
  { to: '/organizer/dashboard',    label: 'Eventos'      },
  { to: '/organizer/participants', label: 'Participants' },
  { to: '/organizer/staff',        label: 'Equipe'       },
  { to: '/organizer/group',        label: 'Grupo'        },
]

export default function OrganizerDashboard() {
  const navigate = useNavigate()

  const [events,  setEvents]  = useState([])
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState('')
  const [showNew, setShowNew] = useState(false)

  const [chartData,    setChartData]    = useState([])
  const [chartLoading, setChartLoading] = useState(true)

  const [exporting, setExporting] = useState(false)

  function load() {
    setLoading(true)
    api.get('/events')
      .then(r => setEvents(r.data.content))
      .catch(() => setError('Erro ao carregar eventos.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()

    api.get('/stats/events', { params: { limit: 50 } })
      .then(r => {
        setChartData(r.data.events.map(ev => ({
          name: ev.name,
          checkIns: ev.checkIns,
        })))
      })
      .catch(() => {})   // gráfico é melhor esforço — não bloqueia a página
      .finally(() => setChartLoading(false))
  }, [])

  async function toggleStatus(event) {
    await api.patch(`/events/${event.id}/status`, { isActive: !event.isActive })
    load()
  }

  // Média para a linha de referência
  const avg = chartData.length
    ? Math.round(chartData.reduce((s, d) => s + d.checkIns, 0) / chartData.length)
    : null

  async function handleExport() {
    setExporting(true)
    try {
      const response = await api.get('/export/xlsx', { responseType: 'blob' })
      const url  = URL.createObjectURL(response.data)
      const link = document.createElement('a')
      link.href  = url
      link.download = `parusya-export-${new Date().toISOString().slice(0, 10)}.xlsx`
      link.click()
      URL.revokeObjectURL(url)
    } catch {
      // falha silenciosa — o usuário pode tentar novamente
    } finally {
      setExporting(false)
    }
  }

  return (
    <div className="page">
      <Nav links={NAV_LINKS} />
      <div className="container" style={{ padding: '32px 24px' }}>
        <PageHeader
          title="Eventos"
          sub="Gerencie os eventos do seu grupo"
          action={
            <button className="btn btn-primary" onClick={() => setShowNew(true)}>
              + Novo evento
            </button>
          }
        />

        {/* ── Gráfico de presenças ─────────────────────────────────────────── */}
        {chartLoading ? (
          <div className="card-sm" style={{ marginBottom: 28, height: 180, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Spinner />
          </div>
        ) : chartData.length > 1 && (
          <div className="card-sm" style={{ marginBottom: 28 }}>
            <div className="section-title" style={{ marginBottom: 16 }}>
              Presenças por evento — últimos {chartData.length}
            </div>
            <ResponsiveContainer width="100%" height={200}>
              <AreaChart data={chartData} margin={{ top: 8, right: 8, left: 0, bottom: 8 }}>
                <defs>
                  <linearGradient id="dashGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%"  stopColor="var(--accent)" stopOpacity={0.25} />
                    <stop offset="95%" stopColor="var(--accent)" stopOpacity={0}    />
                  </linearGradient>
                </defs>
                <CartesianGrid
                  strokeDasharray="3 3"
                  stroke="var(--border)"
                  vertical={false}
                />
                <XAxis
                  dataKey="name"
                  tick={{ fill: 'var(--subtle)', fontSize: 11, fontFamily: 'var(--mono)' }}
                  tickLine={false}
                  axisLine={{ stroke: 'var(--border)' }}
                  interval="preserveStartEnd"
                  tickFormatter={name => name.length > 12 ? name.slice(0, 12) + '…' : name}
                />
                <YAxis
                  allowDecimals={false}
                  tick={{ fill: 'var(--subtle)', fontSize: 12 }}
                  tickLine={false}
                  axisLine={false}
                  width={28}
                />
                <Tooltip
                  cursor={{ stroke: 'var(--border)' }}
                  contentStyle={{
                    background: 'var(--surface)',
                    border: '1px solid var(--border)',
                    borderRadius: 'var(--radius)',
                    fontSize: 13,
                  }}
                  labelStyle={{ color: 'var(--subtle)', marginBottom: 4 }}
                  itemStyle={{ color: 'var(--accent)' }}
                  formatter={value => [value, 'check-ins']}
                />
                {avg !== null && (
                  <ReferenceLine
                    y={avg}
                    stroke="var(--subtle)"
                    strokeDasharray="4 4"
                    label={{
                      value: `média ${avg}`,
                      position: 'insideTopRight',
                      fill: 'var(--subtle)',
                      fontSize: 11,
                      fontFamily: 'var(--mono)',
                    }}
                  />
                )}
                <Area
                  type="monotone"
                  dataKey="checkIns"
                  stroke="var(--accent)"
                  strokeWidth={2}
                  fill="url(#dashGradient)"
                  dot={false}
                  activeDot={{ r: 4, fill: 'var(--accent)' }}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        )}

        {/* ── Tabela de eventos ────────────────────────────────────────────── */}
        {error && <Alert>{error}</Alert>}

        {loading ? (
          <div className="empty"><Spinner /></div>
        ) : events.length === 0 ? (
          <div className="empty">Nenhum evento criado ainda.</div>
        ) : (
          <div className="table-wrap card-sm">
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>Data</th>
                  <th>Tags</th>
                  <th>Status</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {events.map(ev => (
                  <tr key={ev.id}>
                    <td style={{ fontWeight: 500 }}>{ev.name}</td>
                    <td style={{ color: 'var(--subtle)', fontSize: 13 }}>
                      {new Date(ev.startDateTime).toLocaleString('pt-BR')}
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                        {ev.tags?.map(t => <span key={t} className="tag-pill">{t}</span>)}
                      </div>
                    </td>
                    <td>
                      <span className={`badge ${ev.isActive ? 'badge-green' : 'badge-muted'}`}>
                        {ev.isActive ? 'Ativo' : 'Inativo'}
                      </span>
                    </td>
                    <td>
                      <div className="row">
                        <button className="btn btn-ghost"
                          style={{ fontSize: 13, padding: '4px 10px' }}
                          onClick={() => navigate(`/organizer/events/${ev.id}`)}>
                          Detalhes
                        </button>
                        <button
                          className={`btn ${ev.isActive ? 'btn-danger' : 'btn-success'}`}
                          style={{ fontSize: 13, padding: '4px 10px' }}
                          onClick={() => toggleStatus(ev)}>
                          {ev.isActive ? 'Desativar' : 'Ativar'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

      {/* ── Exportar ────────────────────────────────────────────────────── */}
      {!loading && events.length > 0 && (
        <div style={{ marginTop: 24, display: 'flex', justifyContent: 'flex-end' }}>
          <button
            className="btn btn-ghost"
            onClick={handleExport}
            disabled={exporting}
            style={{ fontSize: 13 }}
          >
            {exporting ? <Spinner /> : '↓ Exportar dados'}
          </button>
        </div>
      )}

      </div>

      {showNew && <NewEventModal onClose={() => setShowNew(false)} onCreated={load} />}
    </div>
  )
}

function NewEventModal({ onClose, onCreated }) {
  const [form, setForm] = useState({ name: '', description: '', startDateTime: '', tags: '' })
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
      const tags = form.tags ? form.tags.split(',').map(t => t.trim()).filter(Boolean) : []
      await api.post('/events', { ...form, tags })
      onCreated()
      onClose()
    } catch (err) {
      setError(err.response?.data?.message || 'Erro ao criar evento.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal title="Novo evento" onClose={onClose}>
      {error && <Alert>{error}</Alert>}
      <form className="form" style={{ marginTop: error ? 16 : 0 }} onSubmit={handleSubmit}>
        <div className="field">
          <label>Nome *</label>
          <input value={form.name} onChange={set('name')} required />
        </div>
        <div className="field">
          <label>Descrição</label>
          <textarea value={form.description} onChange={set('description')} />
        </div>
        <div className="field">
          <label>Data e hora de início *</label>
          <input type="datetime-local" value={form.startDateTime} onChange={set('startDateTime')} required />
        </div>
        <div className="field">
          <label>Tags (separadas por vírgula)</label>
          <input value={form.tags} onChange={set('tags')} placeholder="mensal, turma-2025" />
        </div>
        <div className="row" style={{ justifyContent: 'flex-end', marginTop: 8 }}>
          <button type="button" className="btn btn-ghost" onClick={onClose}>Cancelar</button>
          <button className="btn btn-primary" disabled={loading}>
            {loading ? <Spinner /> : 'Criar evento'}
          </button>
        </div>
      </form>
    </Modal>
  )
}