import LoginPage from '../../components/LoginPage'

export default function StaffLogin() {
  return <LoginPage role="EVENT_STAFF" title="Entrar como EventStaff" redirectTo="/staff/scan" />
}