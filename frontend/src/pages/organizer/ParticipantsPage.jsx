import { useState, useCallback } from 'react'
import Participants from './Participants'
import ParticipantProfile from './ParticipantProfile'

export default function ParticipantsPage() {
  const [selected,     setSelected]     = useState(null)
  const [refreshToken, setRefreshToken] = useState(0)

  // Incrementar o token força o <Participants> a remontar e recarregar o ranking
  const handleDeleted = useCallback(() => {
    setSelected(null)
    setRefreshToken(t => t + 1)
  }, [])

  return (
    <>
      <Participants
        key={refreshToken}
        onSelectParticipant={setSelected}
      />
      {selected && (
        <ParticipantProfile
          participant={selected}
          onClose={() => setSelected(null)}
          onDeleted={handleDeleted}
        />
      )}
    </>
  )
}