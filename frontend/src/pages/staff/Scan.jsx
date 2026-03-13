import { useEffect, useRef, useState } from 'react'
import { Html5Qrcode } from 'html5-qrcode'
import api from '../../api/axios'
import { Nav, Alert, Spinner } from '../../components/UI'

export default function StaffScan() {
  const [events,        setEvents]        = useState([])
  const [selectedEvent, setSelectedEvent] = useState(null)
  const [scanning,      setScanning]      = useState(false)
  const [feedback,      setFeedback]      = useState(null) // { type, message }
  const [loadingEvents, setLoadingEvents] = useState(true)
  const scannerRef = useRef(null)
  const html5Ref   = useRef(null)

  useEffect(() => {
    api.get('/events/active')
      .then(r => setEvents(r.data))
      .catch(() => setEvents([]))
      .finally(() => setLoadingEvents(false))

    return () => stopScanner()
  }, [])

async function startScanner() {
  if (!selectedEvent) return
  setFeedback(null)
  setScanning(true)

  const scanner = new Html5Qrcode('qr-reader')
  html5Ref.current = scanner

  // tenta câmera traseira, cai para frontal se não tiver
  const cameras = [
    { facingMode: 'environment' },
    { facingMode: 'user' },
  ]

  for (const camera of cameras) {
    try {
      await scanner.start(
        camera,
        { fps: 10, qrbox: { width: 250, height: 250 } },
        onScanSuccess,
        () => {}
      )
      return // deu certo, sai do loop
    } catch (err) {
      console.warn('Falhou com', camera, err)
    }
  }

  // nenhuma câmera funcionou
  setFeedback({ type: 'error', message: 'Não foi possível acessar a câmera.' })
  setScanning(false)
}

  async function stopScanner() {
    if (html5Ref.current) {
      try { await html5Ref.current.stop() } catch {}
      html5Ref.current = null
    }
    setScanning(false)
  }

  async function onScanSuccess(encodedData) {
    // Pausa o scanner durante o processamento
    await stopScanner()

    try {
      const { data } = await api.post('/checkins/scan', {
        eventId: selectedEvent.id,
        encodedData,
      })
      setFeedback({
        type: 'success',
        message: `✓ Check-in registrado: ${data.participant.fullName}`,
      })
    } catch (err) {
      const code = err.response?.data?.error
      const msg  = err.response?.data?.message || 'Erro ao registrar check-in.'

      setFeedback({
        type: code === 'DUPLICATE_CHECKIN' ? 'warning' : 'error',
        message: code === 'DUPLICATE_CHECKIN'
          ? '⚠ Participante já realizou check-in neste evento.'
          : `✕ ${msg}`,
      })
    }
  }

  function resetScan() {
    setFeedback(null)
    startScanner()
  }

  return (
    <div className="page">
      <Nav />
      <div style={{ maxWidth: 480, margin: '0 auto', padding: '32px 24px' }}>
        <h1 className="page-title">Scan de QR Code</h1>
        <p className="page-sub">Selecione o evento e inicie a leitura</p>

        {/* Event selector */}
        {loadingEvents ? (
          <div className="empty"><Spinner /></div>
        ) : events.length === 0 ? (
          <Alert type="info">Nenhum evento ativo no momento.</Alert>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 24 }}>
            <div className="section-title">Evento</div>
            {events.map(ev => (
              <button key={ev.id}
                className={`btn ${selectedEvent?.id === ev.id ? 'btn-primary' : 'btn-ghost'}`}
                style={{ justifyContent: 'flex-start', padding: '12px 16px' }}
                onClick={() => { setSelectedEvent(ev); setFeedback(null) }}>
                <span style={{ fontWeight: 600 }}>{ev.name}</span>
                <span style={{ color: 'inherit', opacity: .6, fontSize: 12, marginLeft: 'auto' }}>
                  {new Date(ev.startDateTime).toLocaleString('pt-BR')}
                </span>
              </button>
            ))}
          </div>
        )}

        {/* Scanner area */}
        {selectedEvent && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div id="qr-reader" ref={scannerRef}
              style={{
                width: '100%', borderRadius: 12,
                overflow: 'hidden',
                border: scanning ? '2px solid var(--accent)' : '2px solid var(--border)',
                minHeight: scanning ? 'auto' : 0,
                display: scanning ? 'block' : 'none',
              }}
            />

            {/* Feedback */}
            {feedback && (
              <div className={`alert alert-${feedback.type === 'warning' ? 'info' : feedback.type}`}
                style={{ fontSize: 15, textAlign: 'center' }}>
                {feedback.message}
              </div>
            )}

            {/* Action buttons */}
            {!scanning && !feedback && (
              <button className="btn btn-primary btn-full" style={{ padding: '14px' }}
                onClick={startScanner}>
                📷 Iniciar leitura
              </button>
            )}

            {!scanning && feedback && (
              <button className="btn btn-primary btn-full" style={{ padding: '14px' }}
                onClick={resetScan}>
                📷 Próximo scan
              </button>
            )}

            {scanning && (
              <button className="btn btn-ghost btn-full" onClick={stopScanner}>
                Parar câmera
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  )
}