import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../../api/axios'
import { Nav, Alert, Spinner, PageHeader } from '../../components/UI'
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

const NAV_LINKS = [
  { to: '/organizer/dashboard', label: 'Eventos' },
  { to: '/organizer/participants', label: 'Participants' },
  { to: '/organizer/staff',     label: 'Equipe'  },
  { to: '/organizer/group',     label: 'Grupo'   },
]

export default function EventDetail() {
  const { id }      = useParams()
  const navigate    = useNavigate()
  const [event,     setEvent]   = useState(null)
  const [log,       setLog]     = useState([])
  const [stats,     setStats]   = useState(null)
  const [tab,       setTab]     = useState('log')
  const [loading,   setLoading] = useState(true)
  const [error,     setError]   = useState('')
  const [search, setSearch] = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')

  // Debounce para não disparar a cada tecla
  useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(search), 300)
    return () => clearTimeout(t)
  }, [search])

  // Recarrega o log quando a busca muda
  useEffect(() => {
    if (!id) return
    api.get(`/checkins/event/${id}`, {
      params: { name: debouncedSearch || undefined }
    }).then(r => setLog(r.data.content))
  }, [debouncedSearch, id])

  useEffect(() => {
    Promise.all([
      api.get(`/events/${id}`),
      api.get(`/checkins/event/${id}`),
      api.get(`/stats/events/${id}`),
    ])
      .then(([evRes, logRes, statsRes]) => {
        setEvent(evRes.data)
        setLog(logRes.data.content)
        setStats(statsRes.data)
      })
      .catch(() => setError('Erro ao carregar dados do evento.'))
      .finally(() => setLoading(false))
  }, [id])

  async function toggleStatus() {
    await api.patch(`/events/${id}/status`, { isActive: !event.isActive })
    setEvent(e => ({ ...e, isActive: !e.isActive }))
  }

  if (loading) return (
    <div className="page">
      <Nav links={NAV_LINKS} />
      <div className="empty" style={{ marginTop: 80 }}><Spinner /></div>
    </div>
  )

  if (error) return (
    <div className="page">
      <Nav links={NAV_LINKS} />
      <div className="container" style={{ padding: '32px 24px' }}>
        <Alert>{error}</Alert>
      </div>
    </div>
  )

  return (
    <div className="page">
      <Nav links={NAV_LINKS} />
      <div className="container" style={{ padding: '32px 24px' }}>
        <PageHeader
          title={event.name}
          sub={new Date(event.startDateTime).toLocaleString('pt-BR')}
          action={
            <div className="row">
              <button className="btn btn-ghost" onClick={() => navigate('/organizer/dashboard')}>
                ← Voltar
              </button>
              <button
                className={`btn ${event.isActive ? 'btn-danger' : 'btn-success'}`}
                onClick={toggleStatus}>
                {event.isActive ? 'Desativar' : 'Ativar'}
              </button>
            </div>
          }
        />

        {/* Tags */}
        {event.tags?.length > 0 && (
          <div className="row" style={{ marginBottom: 24, flexWrap: 'wrap' }}>
            {event.tags.map(t => <span key={t} className="tag-pill">{t}</span>)}
          </div>
        )}

        {/* Stats summary */}
        {stats && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 12, marginBottom: 28 }}>
            <StatCard label="Total check-ins" value={stats.totalCheckIns} />
            {stats.staffBreakdown?.map(s => (
              <StatCard key={s.staffId} label={s.staffName} value={`${s.checkIns} scans`} />
            ))}
          </div>
        )}

        {/* Tabs */}
        <div className="row" style={{ marginBottom: 16, gap: 4 }}>
          {['log', 'horarios'].map(t => (
            <button key={t}
              className={`btn ${tab === t ? 'btn-primary' : 'btn-ghost'}`}
              style={{ fontSize: 13, padding: '6px 14px' }}
              onClick={() => setTab(t)}>
              {t === 'log' ? 'Log de presença' : 'Chegadas a cada 5 min'}
            </button>
          ))}
        </div>

        {tab === 'log' && (
          <>
            <div className="field" style={{ marginBottom: 16, maxWidth: 320 }}>
              <input
                placeholder="Buscar por nome..."
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
            <div className="table-wrap card-sm">
              {log.length === 0 ? (
                <div className="empty">Nenhum check-in registrado.</div>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Participant</th>
                      <th>EventStaff</th>
                      <th>Horário</th>
                    </tr>
                  </thead>
                  <tbody>
                    {log.map(entry => (
                      <tr key={entry.checkInId}>
                        <td style={{ fontWeight: 500 }}>{entry.participant?.fullName}</td>
                        <td style={{ color: 'var(--subtle)' }}>{entry.staff?.name ?? '—'}</td>
                        <td style={{ color: 'var(--subtle)', fontSize: 13, fontFamily: 'var(--mono)' }}>
                          {new Date(entry.timestamp).toLocaleString('pt-BR')}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </>
        )}

        {tab === 'horarios' && (
          <div className="card-sm">
            {!stats?.hourlyDistribution?.length ? (
              <div className="empty">Sem dados de distribuição.</div>
            ) : (
              <ResponsiveContainer width="100%" height={280}>
                <AreaChart
                  data={stats.hourlyDistribution}
                  margin={{ top: 8, right: 16, left: 0, bottom: 8 }}
                >
                  <defs>
                    <linearGradient id="areaGradient" x1="0" y1="0" x2="0" y2="1">
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
                    dataKey="hour"
                    tick={{ fill: 'var(--subtle)', fontSize: 12, fontFamily: 'var(--mono)' }}
                    tickLine={false}
                    axisLine={{ stroke: 'var(--border)' }}
                    interval="preserveStartEnd"
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
                    formatter={(value) => [value, 'check-ins']}
                  />
                  <Area
                    type="monotone"
                    dataKey="count"
                    stroke="var(--accent)"
                    strokeWidth={2}
                    fill="url(#areaGradient)"
                    dot={false}
                    activeDot={{ r: 4, fill: 'var(--accent)' }}
                  />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

function StatCard({ label, value }) {
  return (
    <div className="card-sm">
      <div style={{ color: 'var(--subtle)', fontSize: 12, marginBottom: 4 }}>{label}</div>
      <div style={{ fontSize: 24, fontWeight: 600, fontFamily: 'var(--mono)' }}>{value}</div>
    </div>
  )
}