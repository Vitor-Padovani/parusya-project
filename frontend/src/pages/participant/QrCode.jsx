import { useEffect, useState } from 'react'
import { useAuth } from '../../contexts/AuthContext'
import { Nav, Alert, Spinner } from '../../components/UI'
import api from '../../api/axios'

export default function ParticipantQrCode() {
  const { auth }  = useAuth()
  const [qr,      setQr]      = useState(null)
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState('')

  useEffect(() => {
    api.get('/participants/me/qrcode')
      .then(r => setQr(r.data))
      .catch(() => setError('Não foi possível carregar o QR Code.'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="page">
      <Nav />
      <div className="centered" style={{ minHeight: 'calc(100vh - 56px)' }}>
        <div className="card" style={{ width: '100%', maxWidth: 400, textAlign: 'center' }}>
          <p style={{ color: 'var(--subtle)', fontSize: 13, marginBottom: 4 }}>Bem-vindo,</p>
          <h1 style={{ fontSize: 22, fontWeight: 600, marginBottom: 28 }}>
            {auth?.user?.name}
          </h1>

          {loading && <div style={{ padding: 40 }}><Spinner /></div>}
          {error && <Alert>{error}</Alert>}

          {qr && (
            <>
              <div style={{
                background: '#fff',
                borderRadius: 12,
                display: 'inline-block',
                padding: 16,
                marginBottom: 20,
              }}>
                <img src={qr.imageBase64} alt="QR Code" width={240} height={240} />
              </div>

              <p className="mono" style={{
                color: 'var(--subtle)', fontSize: 12,
                wordBreak: 'break-all', marginBottom: 8,
              }}>
                {qr.encodedData}
              </p>

              <p style={{ color: 'var(--subtle)', fontSize: 12 }}>
                Apresente este código na entrada do evento
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  )
}