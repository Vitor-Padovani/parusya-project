import LoginPage from '../../components/LoginPage'

export default function ParticipantLogin() {
  return <LoginPage role="PARTICIPANT" title="Acessar seu QR Code" redirectTo="/participant/qrcode" />
}