import { useNavigate } from 'react-router-dom'

export default function Terms() {
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
        <h1 style={{ fontSize: 22, fontWeight: 600, marginBottom: 16 }}>Termos e condições</h1>

        <Section title="Dados coletados">
          Coletamos nome, e-mail, telefone, data de nascimento e sexo no momento
          do cadastro. Esses dados são utilizados exclusivamente para identificação dentro
          da plataforma e geração do QR Code de acesso.
        </Section>

        <Section title="Uso dos dados">
          Os dados coletados não são compartilhados com terceiros nem utilizados para fins
          comerciais. Apenas o organizador tem acesso aos dados de quem foi em seus eventos.
        </Section>

        <Section title="LGPD">
          Em conformidade com a Lei Geral de Proteção de Dados (Lei nº 13.709/2018),
          você tem direito a solicitar a correção ou exclusão dos seus dados a qualquer momento
          entrando em contato com o responsável pelo grupo ao qual está vinculado.
        </Section>

        <Section title="Beta fechado">
          O Parusya está em fase de testes. Funcionalidades podem mudar sem aviso prévio.
          Não nos responsabilizamos por indisponibilidades durante este período.
        </Section>

        <p style={{ color: 'var(--subtle)', fontSize: 12, marginTop: 24 }}>
          Última atualização: março de 2026
        </p>
      </div>
    </div>
  )
}

function Section({ title, children }) {
  return (
    <div style={{ marginBottom: 20 }}>
      <h2 style={{ fontSize: 15, fontWeight: 600, marginBottom: 6 }}>{title}</h2>
      <p style={{ color: 'var(--subtle)', fontSize: 14, lineHeight: 1.7 }}>{children}</p>
    </div>
  )
}