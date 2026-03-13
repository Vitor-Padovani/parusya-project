import api from './axios'

export function getRanking({ page = 0, size = 30, name, absentOnly = false }) {
  return api.get('/participants/ranking', {
    params: {
      page,
      size,
      name: name || undefined,   // não envia o param se vazio
      absentOnly,
    },
  }).then(r => r.data)
}

export function getProfile(participantId) {
  return api.get(`/participants/ranking/${participantId}`).then(r => r.data)
}

export function deleteParticipant(participantId) {
  return api.delete(`/participants/ranking/${participantId}`)
}
