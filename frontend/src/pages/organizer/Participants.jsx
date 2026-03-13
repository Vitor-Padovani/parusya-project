import { useState, useEffect } from 'react'
import { Nav, PageHeader, Spinner, Alert } from '../../components/UI'
import { getRanking } from '../../api/participants'

const NAV_LINKS = [
  { to: '/organizer/dashboard',    label: 'Eventos'       },
  { to: '/organizer/participants', label: 'Participants'  },
  { to: '/organizer/staff',        label: 'Equipe'        },
  { to: '/organizer/group',        label: 'Grupo'         },
]

const PAGE_SIZE = 30

export default function Participants({ onSelectParticipant }) {
  const [search,          setSearch]          = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [absentOnly,      setAbsentOnly]      = useState(false)
  const [page,            setPage]            = useState(0)
  const [data,            setData]            = useState(null)   // ParticipantRankingResponse
  const [loading,         setLoading]         = useState(true)
  const [error,           setError]           = useState('')

  // Debounce de 300ms — mesmo padrão do EventDetail
  useEffect(() => {
    const t = setTimeout(() => setDebouncedSearch(search), 300)
    return () => clearTimeout(t)
  }, [search])

  // Reseta para página 0 sempre que filtro muda
  useEffect(() => {
    setPage(0)
  }, [debouncedSearch, absentOnly])

  // Busca sempre que página ou filtros mudam
  useEffect(() => {
    setLoading(true)
    setError('')
    getRanking({ page, size: PAGE_SIZE, name: debouncedSearch, absentOnly })
      .then(setData)
      .catch(() => setError('Erro ao carregar ranking.'))
      .finally(() => setLoading(false))
  }, [page, debouncedSearch, absentOnly])

  const totalPages = data ? Math.ceil(data.totalElements / PAGE_SIZE) : 0

  return (
    <div className="page">
      <Nav links={NAV_LINKS} />
      <div className="container" style={{ padding: '32px 24px' }}>
        <PageHeader
          title="Participants"
          sub="Ranking de presenças em todos os eventos do grupo"
        />

        {/* ── Filtros ──────────────────────────────────────────────────────── */}
        <div className="row" style={{ marginBottom: 20, gap: 16, flexWrap: 'wrap' }}>
          <div className="field" style={{ margin: 0, flex: '1 1 260px', maxWidth: 360 }}>
            <input
              placeholder="Buscar por nome..."
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
          </div>

          <label style={{
            display: 'flex', alignItems: 'center', gap: 8,
            cursor: 'pointer', fontSize: 14, color: 'var(--text)',
            userSelect: 'none',
          }}>
            <input
              type="checkbox"
              checked={absentOnly}
              onChange={e => setAbsentOnly(e.target.checked)}
              style={{ width: 15, height: 15, cursor: 'pointer' }}
            />
            Apenas ausentes do último evento
          </label>
        </div>

        {/* ── Conteúdo ─────────────────────────────────────────────────────── */}
        {error && <Alert>{error}</Alert>}

        {loading ? (
          <div className="empty"><Spinner /></div>
        ) : !data || data.content.length === 0 ? (
          <div className="empty">
            {absentOnly
              ? 'Todos os participants compareceram ao último evento.'
              : 'Nenhum participant encontrado.'}
          </div>
        ) : (
          <>
            <div className="table-wrap card-sm">
              <table>
                <thead>
                  <tr>
                    <th style={{ width: 56 }}>#</th>
                    <th>Nome</th>
                    <th style={{ width: 120, textAlign: 'right' }}>Presenças</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map(p => (
                    <tr
                      key={p.participantId}
                      style={{ cursor: 'pointer' }}
                      onClick={() => onSelectParticipant?.(p)}
                    >
                      <td className="mono" style={{ color: 'var(--subtle)', fontSize: 13 }}>
                        {p.rank}
                      </td>
                      <td style={{ fontWeight: 500 }}>{p.fullName}</td>
                      <td style={{ textAlign: 'right', fontFamily: 'var(--mono)', fontSize: 14 }}>
                        {p.totalCheckIns}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* ── Paginação ──────────────────────────────────────────────── */}
            {totalPages > 1 && (
              <div className="row" style={{ justifyContent: 'center', marginTop: 20, gap: 8 }}>
                <button
                  className="btn btn-ghost"
                  style={{ fontSize: 13, padding: '6px 14px' }}
                  disabled={page === 0}
                  onClick={() => setPage(p => p - 1)}
                >
                  ← Anterior
                </button>
                <span style={{ fontSize: 13, color: 'var(--subtle)', alignSelf: 'center' }}>
                  {page + 1} / {totalPages}
                </span>
                <button
                  className="btn btn-ghost"
                  style={{ fontSize: 13, padding: '6px 14px' }}
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage(p => p + 1)}
                >
                  Próximo →
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}