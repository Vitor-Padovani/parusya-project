// frontend/src/components/PhoneInput.jsx
// Componente de telefone com seletor de país e máscara automática.
// O valor exposto para o formulário é sempre no formato E.164 (ex: +5511999999999).

import { useState, useEffect } from 'react'

// ─── Lista de países mais comuns ──────────────────────────────────────────────
const COUNTRIES = [
  { code: 'BR', dial: '+55',  flag: '🇧🇷', name: 'Brasil',          digits: 11 },
  { code: 'US', dial: '+1',   flag: '🇺🇸', name: 'EUA / Canadá',    digits: 10 },
  { code: 'PT', dial: '+351', flag: '🇵🇹', name: 'Portugal',         digits: 9  },
  { code: 'AR', dial: '+54',  flag: '🇦🇷', name: 'Argentina',        digits: 10 },
  { code: 'MX', dial: '+52',  flag: '🇲🇽', name: 'México',           digits: 10 },
  { code: 'CO', dial: '+57',  flag: '🇨🇴', name: 'Colômbia',         digits: 10 },
  { code: 'CL', dial: '+56',  flag: '🇨🇱', name: 'Chile',            digits: 9  },
  { code: 'ES', dial: '+34',  flag: '🇪🇸', name: 'Espanha',          digits: 9  },
  { code: 'DE', dial: '+49',  flag: '🇩🇪', name: 'Alemanha',         digits: 10 },
  { code: 'FR', dial: '+33',  flag: '🇫🇷', name: 'França',           digits: 9  },
  { code: 'GB', dial: '+44',  flag: '🇬🇧', name: 'Reino Unido',      digits: 10 },
  { code: 'IT', dial: '+39',  flag: '🇮🇹', name: 'Itália',           digits: 10 },
  { code: 'JP', dial: '+81',  flag: '🇯🇵', name: 'Japão',            digits: 10 },
  { code: 'CN', dial: '+86',  flag: '🇨🇳', name: 'China',            digits: 11 },
  { code: 'IN', dial: '+91',  flag: '🇮🇳', name: 'Índia',            digits: 10 },
  { code: 'AU', dial: '+61',  flag: '🇦🇺', name: 'Austrália',        digits: 9  },
  { code: 'ZA', dial: '+27',  flag: '🇿🇦', name: 'África do Sul',    digits: 9  },
  { code: 'NG', dial: '+234', flag: '🇳🇬', name: 'Nigéria',          digits: 10 },
  { code: 'AO', dial: '+244', flag: '🇦🇴', name: 'Angola',           digits: 9  },
  { code: 'MZ', dial: '+258', flag: '🇲🇿', name: 'Moçambique',       digits: 9  },
]

// ─── Formata dígitos locais visualmente (ex: (11) 99999-9999 para BR) ─────────
function formatLocal(digits, countryCode) {
  const d = digits.replace(/\D/g, '')

  if (countryCode === 'BR') {
    // (DDD) + número
    if (d.length === 0) return ''
    if (d.length <= 2) return `(${d}`
    if (d.length <= 7) return `(${d.slice(0,2)}) ${d.slice(2)}`
    if (d.length <= 11) return `(${d.slice(0,2)}) ${d.slice(2, d.length - 4)}-${d.slice(-4)}`
    return `(${d.slice(0,2)}) ${d.slice(2, 7)}-${d.slice(7, 11)}`
  }

  if (countryCode === 'US') {
    // (DDD) NNN-NNNN
    if (d.length === 0) return ''
    if (d.length <= 3) return `(${d}`
    if (d.length <= 6) return `(${d.slice(0,3)}) ${d.slice(3)}`
    return `(${d.slice(0,3)}) ${d.slice(3,6)}-${d.slice(6,10)}`
  }

  // Formato genérico: agrupa em blocos de 3-4
  if (d.length === 0) return ''
  const chunks = d.match(/.{1,4}/g) || []
  return chunks.join(' ')
}

// ─── Componente principal ─────────────────────────────────────────────────────
export default function PhoneInput({ onChange, required, register, zodError }) {
  const [country,    setCountry]    = useState(COUNTRIES[0]) // Brasil
  const [localRaw,   setLocalRaw]   = useState('')           // apenas dígitos locais
  const [display,    setDisplay]    = useState('')           // texto exibido no input

  // Sincroniza para fora: sempre E.164
  useEffect(() => {
    const digits = localRaw.replace(/\D/g, '')
    const e164   = digits.length > 0 ? `${country.dial}${digits}` : ''
    onChange(e164)
  }, [localRaw, country])

  function handleCountryChange(e) {
    const selected = COUNTRIES.find(c => c.code === e.target.value)
    if (selected) {
      setCountry(selected)
      setLocalRaw('')
      setDisplay('')
    }
  }

  function handlePhoneChange(e) {
    const raw    = e.target.value
    const digits = raw.replace(/\D/g, '').slice(0, country.digits)
    setLocalRaw(digits)
    setDisplay(formatLocal(digits, country.code))
  }

  const isComplete = localRaw.replace(/\D/g, '').length >= country.digits
  const hint       = `${country.dial} + ${country.digits} dígitos`

  return (
    <div className="field">
      <label>Celular</label>
      <div style={{ display: 'flex', gap: 8, alignItems: 'stretch' }}>

        {/* ── Seletor de país ─────────────────────────────────────────────── */}
        <select
          value={country.code}
          onChange={handleCountryChange}
          title="Código do país"
          style={{
            flexShrink: 0,
            width: 'auto',
            minWidth: 90,
            padding: '0 8px',
            borderRadius: 'var(--radius, 8px)',
            border: '1px solid var(--border)',
            background: 'var(--surface)',
            color: 'var(--text)',
            cursor: 'pointer',
            fontSize: 14,
          }}
        >
          {COUNTRIES.map(c => (
            <option key={c.code} value={c.code}>
              {c.flag} {c.dial}
            </option>
          ))}
        </select>

        {/* ── Campo de número ──────────────────────────────────────────────── */}
        <div style={{ flex: 1, position: 'relative' }}>
          <input
            {...register("phone")}
            type="tel"
            inputMode="numeric"
            value={display}
            onChange={handlePhoneChange}
            placeholder={country.code === 'BR' ? '(44) 99912-3456' : hint}
            required={required}
            style={{
              width: '100%',
              boxSizing: 'border-box',
              paddingRight: isComplete ? 32 : undefined,
              borderColor: localRaw.length > 0 && !isComplete
                ? 'var(--warning, #f59e0b)'
                : undefined,
            }}
          />
          {zodError.phone && <p style={{ margin: '4px 0 0', fontSize: 12, color: 'var(--red)' }}>{zodError.phone.message}</p>}
          {/* Ícone de confirmação quando número está completo */}
          {isComplete && (
            <span
              style={{
                position: 'absolute', right: 10, top: '50%',
                transform: 'translateY(-50%)',
                color: 'var(--success, #22c55e)',
                fontSize: 16, pointerEvents: 'none',
              }}
            >
              ✓
            </span>
          )}
        </div>
      </div>

      {/* ── Hint com o valor E.164 que será enviado ──────────────────────── */}
      {localRaw.length > 0 && (
        <p style={{
          margin: '4px 0 0',
          fontSize: 11,
          color: isComplete ? 'var(--subtle)' : 'var(--warning, #f59e0b)',
        }}>
          {isComplete
            ? `Será enviado: ${country.dial}${localRaw}`
            : `Faltam ${country.digits - localRaw.replace(/\D/g,'').length} dígito(s)`}
        </p>
      )}
    </div>
  )
}