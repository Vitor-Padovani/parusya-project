import { useState, useEffect } from 'react'
import { Spinner, Alert } from '../../components/UI'
import { getProfile, deleteParticipant } from '../../api/participants'
import AttendanceGrid from '../../components/AttendanceGrid'

export default function ParticipantProfile({ participant, onClose, onDeleted }) {
  const [profile,           setProfile]           = useState(null)
  const [loading,           setLoading]           = useState(true)
  const [error,             setError]             = useState('')
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const [deleting,          setDeleting]          = useState(false)
  const [deleteError,       setDeleteError]       = useState('')

  useEffect(() => {
    setLoading(true)
    setError('')
    getProfile(participant.participantId)
      .then(setProfile)
      .catch(() => setError('Erro ao carregar perfil.'))
      .finally(() => setLoading(false))
  }, [participant.participantId])

  // Fecha ao clicar no backdrop
  function handleBackdropClick(e) {
    if (e.target === e.currentTarget) onClose()
  }

  async function handleDelete() {
    setDeleting(true)
    setDeleteError('')
    try {
      await deleteParticipant(participant.participantId)
      onClose()
      onDeleted?.()
    } catch (err) {
      setDeleteError(err.response?.data?.message || 'Erro ao deletar participant.')
      setDeleting(false)
    }
  }

  return (
    <div
      onClick={handleBackdropClick}
      style={{
        position: 'fixed', inset: 0,
        background: 'rgba(0,0,0,.75)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        zIndex: 100, padding: 24,
      }}
    >
      <div
        className="card"
        style={{ width: '100%', maxWidth: 760, maxHeight: '90vh', overflowY: 'auto' }}
      >
        {/* ── Cabeçalho ──────────────────────────────────────────────────── */}
        <div className="row-between" style={{ marginBottom: 24 }}>
          <div>
            <h2 style={{ fontSize: 20, fontWeight: 600 }}>{participant.fullName}</h2>
            <p style={{ color: 'var(--subtle)', fontSize: 13, marginTop: 2 }}>
              Perfil de frequência
            </p>
          </div>
          <div className="row" style={{ gap: 8 }}>
            <button
              className="btn btn-danger"
              style={{ fontSize: 13, padding: '4px 12px', flexShrink: 0 }}
              onClick={() => setShowDeleteConfirm(true)}
            >
              Deletar
            </button>
            <button
              className="btn btn-ghost"
              style={{ padding: '4px 10px', flexShrink: 0 }}
              onClick={onClose}
            >
              ✕
            </button>
          </div>
        </div>

        {/* ── Conteúdo ───────────────────────────────────────────────────── */}
        {loading && (
          <div className="empty"><Spinner /></div>
        )}

        {error && <Alert>{error}</Alert>}

        {!loading && !error && profile && (
          <>
            {/* Dados pessoais */}
            <div className="card-sm" style={{ marginBottom: 20 }}>
              <div className="section-title" style={{ marginBottom: 12 }}>Dados pessoais</div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '8px 24px' }}>
                <InfoRow label="E-mail"     value={profile.email} />
                <InfoRow label="Telefone"   value={profile.phone} />
                <InfoRow label="Nascimento" value={new Date(profile.birthDate + 'T12:00:00').toLocaleDateString('pt-BR')} />
                <InfoRow label="Sexo"       value={{ MALE: 'Masculino', FEMALE: 'Feminino', OTHER: 'Outro' }[profile.gender] ?? profile.gender} />
              </div>
            </div>

            {/* Cards de estatísticas */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
              gap: 12,
              marginBottom: 28,
            }}>
              <StatCard label="Total de presenças" value={profile.totalCheckIns} />
              <StatCard
                label="Frequentador desde"
                value={
                  profile.firstCheckIn
                    ? new Date(profile.firstCheckIn).toLocaleDateString('pt-BR')
                    : '—'
                }
              />
            </div>

            {/* Painel de frequência */}
            {profile.checkInDates.length > 0 ? (
              <>
                <div className="section-title" style={{ marginBottom: 12 }}>
                  Histórico de presenças
                </div>
                <AttendanceGrid checkInDates={profile.checkInDates} />
              </>
            ) : (
              <div className="empty" style={{ padding: '24px 0' }}>
                Nenhuma presença registrada neste grupo.
              </div>
            )}
          </>
        )}
      </div>

      {/* ── Modal de confirmação de deleção ─────────────────────────────── */}
      {showDeleteConfirm && (
        <div
          style={{
            position: 'fixed', inset: 0,
            background: 'rgba(0,0,0,.6)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            zIndex: 200, padding: 24,
          }}
        >
          <div className="card" style={{ width: '100%', maxWidth: 400 }}>
            <h3 style={{ fontSize: 18, fontWeight: 600, marginBottom: 8 }}>
              Deletar participant
            </h3>
            <p style={{ color: 'var(--subtle)', fontSize: 14, lineHeight: 1.6, marginBottom: 20 }}>
              Tem certeza que deseja deletar <strong style={{ color: 'var(--text)' }}>{participant.fullName}</strong>?
              Todos os check-ins serão removidos permanentemente. Esta ação não pode ser desfeita.
            </p>

            {deleteError && (
              <Alert style={{ marginBottom: 16 }}>{deleteError}</Alert>
            )}

            <div className="row" style={{ justifyContent: 'flex-end', gap: 8 }}>
              <button
                className="btn btn-ghost"
                onClick={() => { setShowDeleteConfirm(false); setDeleteError('') }}
                disabled={deleting}
              >
                Cancelar
              </button>
              <button
                className="btn btn-danger"
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? <Spinner /> : 'Confirmar deleção'}
              </button>
            </div>
          </div>
        </div>
      )}
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

function InfoRow({ label, value }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 2, padding: '4px 0' }}>
      <span style={{ fontSize: 11, color: 'var(--subtle)', textTransform: 'uppercase', letterSpacing: '.05em', fontWeight: 600 }}>
        {label}
      </span>
      <span style={{ fontSize: 14, color: 'var(--text)' }}>{value}</span>
    </div>
  )
}