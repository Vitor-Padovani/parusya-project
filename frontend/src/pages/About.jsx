import { useNavigate } from 'react-router-dom'

export default function About() {
  const navigate = useNavigate()

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: 24 }}>
      <div className="card" style={{ width: '100%', maxWidth: 560 }}>
        <button
          className="btn btn-ghost"
          style={{ fontSize: 13, padding: '4px 10px', marginBottom: 24 }}
          onClick={() => navigate('/')}
        >
          ← Voltar
        </button>

        <div className="mono" style={{ fontSize: 20, fontWeight: 500, marginBottom: 4 }}>入</div>
        <h1 style={{ fontSize: 22, fontWeight: 600, marginBottom: 16 }}>Sobre o Parusya</h1>

        <p style={{ color: 'var(--subtle)', fontSize: 14, lineHeight: 1.7, marginBottom: 12 }}>
          Parusya é um sistema de controle de presença por QR Code desenvolvido para facilitar
          a gestão de eventos e comunidades. Cada participante possui um QR Code único que é
          escaneado na entrada dos eventos, permitindo ao organizador acompanhar presença,
          frequência e histórico de forma simples.
        </p>

        <p style={{ color: 'var(--subtle)', fontSize: 14, lineHeight: 1.7, marginBottom: 12 }}>
          O projeto está atualmente em <strong style={{ color: 'var(--text)' }}>beta fechado</strong> —
          o cadastro de novos grupos está disponível apenas a convite.
          Se você tem interesse em usar o Parusya na sua comunidade ou evento,
          entre em contato.
        </p>

        <p style={{ color: 'var(--subtle)', fontSize: 14, lineHeight: 1.7 }}>
          Desenvolvido por{' '}
          
          <a
            href="https://vitorpadovani.com.br/"
            target="_blank"
            rel="noopener noreferrer"
          >
            Vitor Padovani
          </a>.
        </p>
      </div>
    </div>
  )
}