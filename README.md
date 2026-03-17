# Parusya
*do grego: parousia, "presença", "chegada", "visita"*

Sistema web de controle de presença por QR Code, desenvolvido para substituir chamadas manuais em eventos recorrentes com grande volume de participantes.

---

## Motivação

Coordeno o ministério de acolhimento de um grupo de ação social que cresceu de 20–50 para mais de 300 participantes por semana. O controle de presença feito em planilhas Google (que já havia substituído o papel) chegou ao seu limite: filas na entrada, planilhas com mais de 1000 linhas travando, erros de digitação de nomes e desgaste da equipe voluntária.

O Parusya resolve isso: cada participante tem um QR Code único, que é escaneado em segundos na entrada. O registro acontece instantaneamente, e as estatísticas são geradas em tempo real.

---

## Funcionalidades

- **Cadastro de participantes** com nome completo, sexo, data de nascimento, telefone e e-mail
- **QR Code pessoal** gerado automaticamente após o cadastro (válido em qualquer evento ativo)
- **Scan via câmera do celular** sem app, direto no navegador
- **Múltiplos eventos simultâneos** com check-ins independentes por evento
- **Dashboard do organizador** com estatísticas em tempo real: total de presentes, gráfico de frequência por intervalo de 5 minutos e breakdown por membro da equipe
- **Ranking de participantes** por grupo, com filtro de ausentes no último evento
- **Perfil de participante** com histórico de presenças em grid estilo GitHub
- **Exportação de relatórios** por evento
- **Gestão de equipe** (EventStaff) vinculada ao grupo do organizador
- **Tags** para categorização de participantes
- Conformidade com a **LGPD**

---

## Perfis de Usuário

| Perfil | Acesso | Responsabilidade |
|---|---|---|
| **Participant** | `/participant/*` | Cadastra-se, visualiza e exibe seu QR Code |
| **EventStaff** | `/staff/*` | Faz login e escaneia QR Codes na entrada |
| **Organizer** | `/organizer/*` | Cria eventos, gerencia equipe e consulta relatórios |

Organizadores e EventStaff pertencem a um **Grupo**, a unidade organizacional que compartilha eventos, estatísticas e tags entre múltiplos organizadores.

---

## Arquitetura

```
┌─────────────────────┐        HTTPS         ┌──────────────────────────┐
│   React (Vercel)    │ ──────────────────── │  Spring Boot (Railway)   │
│   Vite + React 18   │    REST / JSON JWT   │  Java 21 + PostgreSQL    │
└─────────────────────┘                      └──────────────────────────┘
         │                                              │
   Cloudflare DNS                               Railway (DB + API)
```

---

## Tech Stack

### Backend
- **Java 21** + **Spring Boot 3.5**
- **Spring Data JPA** + **PostgreSQL** (Docker local)
- **Spring Security** + **JWT RS256** (OAuth2 Resource Server com par de chaves RSA)
- **ZXing** — geração de QR Code em PNG no servidor
- **Lombok** — redução de boilerplate
- **Railway** — deploy e banco de dados em produção

### Frontend
- **React 18** + **Vite**
- **React Router v6** — roteamento com rotas protegidas por role
- **Axios** — cliente HTTP com interceptors para JWT e redirect em 401
- **html5-qrcode** — acesso à câmera do navegador para leitura de QR Code
- **qrcode.react** — renderização do QR Code do participante
- **React Hook Form** + **Zod** — validação de formulários
- **Recharts** — gráficos de frequência
- **Vercel** — deploy do frontend

---

## Segurança

A autenticação é stateless via **JWT assinado com RS256**. Cada token carrega as claims `role`, `user_id` e `group_id`, usadas pelos services para autorização sem consultas extras ao banco.

```
POST /v1/auth/login  →  { token: "eyJ..." }
```

Mapeamento de permissões:

| Endpoint | Role exigida |
|---|---|
| `POST /v1/participants/register` | público |
| `POST /v1/checkins/scan` | `ROLE_EVENT_STAFF` |
| `GET /v1/events/active` | `ROLE_EVENT_STAFF` |
| `GET /v1/checkins/**`, `/v1/stats/**`, `/v1/export/**` | `ROLE_ORGANIZER` |
| `GET /v1/participants/me/**` | `ROLE_PARTICIPANT` |

Integridade dos dados: a constraint `uk_checkin_participant_event` no banco impede check-in duplicado mesmo sob condição de corrida.

---

## Estrutura de Rotas (Frontend)

```
/                          → Home
/register                  → Cadastro de participante
/participant/login         → Login do participante
/participant/qrcode        → QR Code pessoal          [PARTICIPANT]

/organizer/login           → Login do organizador
/organizer/dashboard       → Dashboard + eventos      [ORGANIZER]
/organizer/events/:id      → Detalhe do evento        [ORGANIZER]
/organizer/staff           → Gestão de equipe         [ORGANIZER]
/organizer/group           → Configurações do grupo   [ORGANIZER]
/organizer/participants    → Ranking de participantes  [ORGANIZER]

/staff/login               → Login do EventStaff
/staff/scan                → Scanner de QR Code       [EVENT_STAFF]
```

---

## Rodando Localmente

### Pré-requisitos
- Java 21+
- Node.js 18+
- Docker (para o PostgreSQL)

### Backend

```bash
# Suba o banco
docker run -d \
  --name parusya-db \
  -e POSTGRES_DB=parusya \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16

# Configure as variáveis de ambiente (application.properties lê via env)
export DB_URL=jdbc:postgresql://localhost:5432/parusya
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----"
export JWT_PUBLIC_KEY="-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----"
export JWT_EXPIRATION_SECONDS=86400
export PORT=8080

# Rode
./mvnw spring-boot:run
```

> Para gerar o par de chaves RSA: `openssl genrsa -out private.pem 2048` e `openssl rsa -in private.pem -pubout -out public.pem`

### Frontend

```bash
cd frontend
npm install

# Crie o arquivo de variáveis
echo "VITE_API_URL=http://localhost:8080/v1" > .env.local

npm run dev
# Acesse: http://localhost:3000
```

O Vite já está configurado com proxy para `/v1 → http://localhost:8080`, então `VITE_API_URL` pode ser omitido em desenvolvimento local.

---

## Variáveis de Ambiente

### Backend (Railway)

| Variável | Descrição |
|---|---|
| `DB_URL` | JDBC URL do PostgreSQL |
| `DB_USERNAME` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_PRIVATE_KEY` | Chave privada RSA (PEM) |
| `JWT_PUBLIC_KEY` | Chave pública RSA (PEM) |
| `JWT_EXPIRATION_SECONDS` | Validade do token em segundos |
| `PORT` | Porta da aplicação |

### Frontend (Vercel)

| Variável | Descrição |
|---|---|
| `VITE_API_URL` | URL base da API (ex: `https://sua-api.railway.app/v1`) |

---

## Deploy

O projeto está configurado para deploy contínuo:

- **Frontend** → Vercel (build: `npm run build`, output: `dist/`)
- **Backend** → Railway (detecta Maven automaticamente)
- **DNS** → Cloudflare

---

## Sobre o Projeto

Desenvolvido por um estudante de Ciência da Computação que coordena voluntariamente um ministério de acolhimento. O Parusya nasceu de uma necessidade real — e é usado toda semana por centenas de pessoas.

O nome vem do grego *parousia*: presença, chegada, visita. Exatamente o que o sistema registra.
