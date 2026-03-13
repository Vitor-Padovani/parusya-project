const DAYS   = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S']
const MONTHS = ['Jan','Fev','Mar','Abr','Mai','Jun','Jul','Ago','Set','Out','Nov','Dez']

const CELL   = 13   // tamanho do quadrado em px
const GAP    =  3   // espaço entre quadrados em px

export default function AttendanceGrid({ checkInDates = [] }) {
  const dateSet = new Set(checkInDates)   // lookup O(1)

  // Âncora: domingo da semana atual
  const today     = new Date()
  const dayOfWeek = today.getDay()                    // 0=dom … 6=sáb
  const anchor    = new Date(today)
  anchor.setDate(today.getDate() - dayOfWeek)         // recua ao domingo
  anchor.setHours(0, 0, 0, 0)

  // Gera 53 semanas (371 dias) para trás — cobre ~12 meses
  const WEEKS = 53
  const start = new Date(anchor)
  start.setDate(anchor.getDate() - (WEEKS - 1) * 7)  // domingo de 53 semanas atrás

  // Monta array de semanas: cada semana é array de 7 datas (dom → sáb)
  const weeks = []
  for (let w = 0; w < WEEKS; w++) {
    const week = []
    for (let d = 0; d < 7; d++) {
      const date = new Date(start)
      date.setDate(start.getDate() + w * 7 + d)
      week.push(date)
    }
    weeks.push(week)
  }

  // Decide onde mostrar o label do mês: apenas na semana em que aparece o dia 1
  function monthLabel(week) {
    for (const date of week) {
      if (date.getDate() === 1) return MONTHS[date.getMonth()]
    }
    return null
  }

  function toIso(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }

  function isFuture(date) {
    return date > today
  }

  return (
    <div style={{ overflowX: 'auto', paddingBottom: 4 }}>
      <div style={{ display: 'inline-flex', flexDirection: 'column', gap: 0 }}>

        {/* ── Labels de mês ─────────────────────────────────────────────── */}
        <div style={{ display: 'flex', marginBottom: 4, marginLeft: CELL + GAP + 4 }}>
          {weeks.map((week, wi) => (
            <div
              key={wi}
              style={{
                width: CELL,
                marginRight: wi < weeks.length - 1 ? GAP : 0,
                fontSize: 10,
                color: 'var(--subtle)',
                fontFamily: 'var(--mono)',
                textAlign: 'left',
                overflow: 'visible',
                whiteSpace: 'nowrap',
              }}
            >
              {monthLabel(week) ?? ''}
            </div>
          ))}
        </div>

        {/* ── Grid: labels de dia + colunas de semanas ──────────────────── */}
        <div style={{ display: 'flex', gap: 0 }}>

          {/* Labels dom → sáb */}
          <div style={{
            display: 'flex', flexDirection: 'column',
            gap: GAP, marginRight: 4,
          }}>
            {DAYS.map((label, di) => (
              <div
                key={di}
                style={{
                  width: CELL, height: CELL,
                  fontSize: 9,
                  color: di % 2 === 1 ? 'var(--subtle)' : 'transparent',  // alterna visibilidade
                  fontFamily: 'var(--mono)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  userSelect: 'none',
                }}
              >
                {label}
              </div>
            ))}
          </div>

          {/* Colunas de semanas */}
          {weeks.map((week, wi) => (
            <div
              key={wi}
              style={{
                display: 'flex', flexDirection: 'column',
                gap: GAP,
                marginRight: wi < weeks.length - 1 ? GAP : 0,
              }}
            >
              {week.map((date, di) => {
                const iso     = toIso(date)
                const present = dateSet.has(iso)
                const future  = isFuture(date)

                let bg
                if (future)        bg = 'transparent'
                else if (present)  bg = 'var(--accent)'
                else               bg = 'var(--border)'

                let border = 'none'
                if (future) border = `1px solid var(--border)`

                return (
                  <div
                    key={di}
                    title={future ? '' : `${iso}${present ? ' · presença' : ''}`}
                    style={{
                      width:  CELL,
                      height: CELL,
                      borderRadius: 2,
                      background: bg,
                      border,
                      flexShrink: 0,
                      opacity: future ? 0.25 : 1,
                    }}
                  />
                )
              })}
            </div>
          ))}
        </div>

        {/* ── Legenda ───────────────────────────────────────────────────── */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 6,
          marginTop: 8, marginLeft: CELL + GAP + 4,
        }}>
          <div style={{ width: CELL, height: CELL, borderRadius: 2, background: 'var(--border)' }} />
          <span style={{ fontSize: 11, color: 'var(--subtle)' }}>Ausente</span>
          <div style={{ width: CELL, height: CELL, borderRadius: 2, background: 'var(--accent)', marginLeft: 8 }} />
          <span style={{ fontSize: 11, color: 'var(--subtle)' }}>Presente</span>
        </div>

      </div>
    </div>
  )
}