// frontend/src/pages/participant/Register.jsx
import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../../api/axios'
import { Alert, Spinner } from '../../components/UI'
import PhoneInput from '../../components/PhoneInput'
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

const GENDER_OPTIONS = [
  { value: 'MALE',   label: 'Masculino' },
  { value: 'FEMALE', label: 'Feminino'  },
]

  const userFormSchema = z.object({
    fullName: z.string().min(1, {error: "Preencha o campo de nome"}).max(50, {error: "Nome não pode ter mais de 50 caracteres"}),
    gender: z.enum(["MALE", "FEMALE", "OTHER"], {
      errorMap: () => ({ message: "Gênero inválido" })
    }),
    birthDate: z.string().min(1, { error: "Preencha a data corretamente" }),
    password: z.string().min(8, { error: "A senha deve ter 8 caracteres" }),
    passwordConfirm: z.string().min(8, { error: "A senha deve ter 8 caracteres" }),
    phone: z.string().min(9, { error: "Preencha o campo de telefone"}),
    email: z.email("Email inválido").max(100, { error: "Email não pode ter mais de 100 caracteres"})
  })
  .refine((data) => data.password === data.passwordConfirm, {
    message: "As senhas não coincidem",
    path: ["passwordConfirm"]
  })

// ─── Ícone de olho ────────────────────────────────────────────────────────────
export function EyeIcon({ open }) {
  return open ? (
    // olho aberto
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
      <circle cx="12" cy="12" r="3"/>
    </svg>
  ) : (
    // olho fechado
    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
      <line x1="1" y1="1" x2="23" y2="23"/>
    </svg>
  )
}

// ─── Campo de senha com botão mostrar/ocultar ─────────────────────────────────
function PasswordField({ label, placeholder, id, error, register, zodErrors, zodField}) {
  const [show, setShow] = useState(false)

  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <div>
        <div style={{ position: 'relative' }}>
          <input
          {...register(zodField)}
            id={id}
            type={show ? 'text' : 'password'}
            placeholder={placeholder}
            style={{
              width: '100%',
              boxSizing: 'border-box',
              paddingRight: 44,
              borderColor: error ? 'var(--red)' : undefined,
            }}
          
          />
          <button
            type="button"
            onClick={() => setShow(v => !v)}
            aria-label={show ? 'Ocultar senha' : 'Mostrar senha'}
            style={{
              position: 'absolute',
              right: 12,
              top: '50%',
              transform: 'translateY(-50%)',
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              color: 'var(--subtle)',
              padding: 0,
              display: 'flex',
              alignItems: 'center',
            }}
          >
            <EyeIcon open={show} />
          </button>
        </div>
        {zodErrors && <p style={{ margin: '4px 0 0', fontSize: 12, color: 'var(--red)' }}>{zodErrors.message}</p>}
      </div>
      {error && (
        <p style={{ margin: '4px 0 0', fontSize: 12, color: 'var(--red)' }}>{error}</p>
      )}
    </div>
  )
}

export default function Register() {


  const navigate = useNavigate()
  const [form, setForm] = useState({
    fullName: '', gender: 'MALE', phone: '', email: '', birthDate: '', password: ''
  })
  const [confirmPassword, setConfirmPassword] = useState('')
  const [passwordError,   setPasswordError]   = useState('')
  const [loading, setLoading] = useState(false)
  const [error,   setError]   = useState('')
  const { register, handleSubmit, formState: { errors }} = useForm({
    resolver: zodResolver(userFormSchema)
  })

  function set(field) {
    return e => setForm(f => ({ ...f, [field]: e.target.value }))
  }

  // Usado pelo PhoneInput — recebe o valor E.164 diretamente
  function setPhone(e164) {
    setForm(f => ({ ...f, phone: e164 }))
  }

  // Valida confirmação em tempo real
  function handleConfirmChange(e) {
    const val = e.target.value
    setConfirmPassword(val)
    if (val && val !== form.password) {
      setPasswordError('As senhas não coincidem')
    } else {
      setPasswordError('')
    }
  }

  // Revalida confirmação se a senha principal mudar
  function handlePasswordChange(e) {
    set('password')(e)
    if (confirmPassword && e.target.value !== confirmPassword) {
      setPasswordError('As senhas não coincidem')
    } else {
      setPasswordError('')
    }
  }

  async function onSubmit(data) {
    const {passwordConfirm, ...dataToPost} = data;
    dataToPost.phone = form.phone;

    if (form.password !== confirmPassword) {
      setPasswordError('As senhas não coincidem')
      return
    }
    setError('')
    setLoading(true)
    try {
      await api.post('/participants/register', dataToPost)
      navigate('/participant/login')
    } catch (err) {
      const data = err.response?.data
      setError(data?.message || 'Erro ao cadastrar. Verifique os dados.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="centered">
      <div className="card" style={{ width: '100%', maxWidth: 440 }}>
        <div style={{ marginBottom: 24 }}>
          <div className="mono" style={{ fontSize: 20, fontWeight: 500, marginBottom: 4 }}>入</div>
          <h1 style={{ fontSize: 22, fontWeight: 600 }}>Criar conta</h1>
          <p style={{ color: 'var(--subtle)', fontSize: 13, marginTop: 4 }}>
            Cadastre-se para receber seu QR Code de acesso
          </p>
        </div>

        {error && <Alert>{error}</Alert>}

        <form className="form" style={{ marginTop: error ? 16 : 0 }} onSubmit={handleSubmit(onSubmit)}>
          <div className="field">
            <label >Nome completo</label>
            <input {...register("fullName")}
              placeholder="João Batista" maxLength={50}/>
              {errors.fullName && <p style={{ margin: '4px 0 0', fontSize: 12, color: 'var(--red)' }}>{errors.fullName.message}</p>}
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="field">
              <label >Sexo</label>
              <select {...register("gender")}>
                {GENDER_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
              </select>
            </div>
            
            <div className="field">
              <label>Data de nascimento</label>
              <input {...register("birthDate")} type="date"/>
              {errors.birthDate && <p style={{ margin: '4px 0 0', fontSize: 12, color: 'var(--red)' }}>{errors.birthDate.message}</p>}
            </div>
          </div>

          {/* ── Campo de telefone aprimorado ────────────────────────────── */}
          <PhoneInput
            value={form.phone}
            onChange={setPhone}
            register={register}
            zodError={errors}
          />

          <div className="field">
            <label>E-mail</label>
            <input {...register("email")} type="email"
              placeholder="exemplo@gmail.com" maxLength={100}/>
              {errors.email && <p style={{ margin: '4px 0 0', fontSize: 12, color: 'var(--red)' }}>{errors.email.message}</p>}
          </div>

          {/* ── Senha com mostrar/ocultar ────────────────────────────────── */}
          <PasswordField
            id="password"
            label="Senha"
            value={form.password}
            onChange={handlePasswordChange}
            placeholder="Mínimo 8 caracteres"
            register={register}
            zodErrors={errors.password}
            zodField={"password"}
          />

          {/* ── Confirmar senha ──────────────────────────────────────────── */}
          <PasswordField
            id="confirmPassword"
            label="Confirmar senha"
            value={confirmPassword}
            onChange={handleConfirmChange}
            placeholder="Repita a senha"
            error={passwordError}
            register={register}
            zodErrors={errors.passwordConfirm}
            zodField={"passwordConfirm"}
          />

          <button
            className="btn btn-primary btn-full"
            disabled={loading || !!passwordError}
            style={{ marginTop: 8 }}
          >
            {loading ? <Spinner /> : 'Criar conta'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 20, color: 'var(--subtle)', fontSize: 13 }}>
          Já tem conta? <Link to="/participant/login">Entrar</Link>
        </p>
      </div>
    </div>
  )
}