# Parusya Project

Repositório não-oficial do Parusya para portifólio.

## Resumo do Projeto

*do grego: parousia ("presença","chegada","visita")*

### Problema e Solução

O objetivo é automatizar o controle de presença em eventos semanais, substituindo a chamada manual por uma leitura rápida de QR Code.

* **Problema:** Lentidão e erros humanos ao registrar a presença de 500 participantes manualmente.  
* **Solução:** Uma plataforma web onde participantes se cadastram e geram um QR Code único. Na entrada, recepcionistas validam o código via câmera.

## Visão geral

O Parusya automatiza o controle de presença em eventos, substituindo a chamada manual por leitura de QR Code. Participantes se cadastram na plataforma, recebem um QR Code único e, na entrada do evento, a equipe valida o código via câmera. O sistema suporta múltiplos eventos simultâneos, com check-ins independentes por evento.

* **Perfis de Usuário**  
  * **Participant:** pessoa que se cadastra na plataforma e pode utilizar seu QR Code em qualquer evento ativo, independentemente do Grupo organizador.  
  * **EventStaff**: membro da equipe responsável por escanear QR Codes na entrada. Vinculada a um Grupo  
  * **Organizer:** administrador que cria eventos, cadastra EventStaff e consulta relatórios. Pertence a exatamente um Grupo  
  * **Grupo**: unidade organizacional que agrupa um ou mais Organizers. Eventos, EventStaff, tags e estatísticas são compartilhados entre todos os Organizers do mesmo Grupo  
* **Histórias de Usuário Principais:**  
  * **Participant:** "Como participante, quero me cadastrar e ter acesso a um QR Code exclusivo para que minha entrada no evento seja rápida."  
  * **EventStaff:** "Como equipe do evento, quero escanear o código do participante para registrar sua presença instantaneamente no banco de dados."  
  * **Organizer:** "Como organizador, quero ver uma lista de quem compareceu para gerar relatórios de frequência."

## Requisitos

### Funcionais

#### Autenticação e acesso

1. O sistema deve permitir autenticação (login/logout) de Organizers  
2. O sistema deve permitir autenticação (login/logout) de EventStaff  
3. O sistema deve permitir autenticação (login/logout) de Participants

#### Gestão de grupos

4. O sistema deve suportar o conceito de Grupo como unidade organizacional central.  
5. Um Organizer pertence a exatamente um Grupo  
6. Todos os Organizers de um mesmo Grupo têm os mesmos poderes (não há hierarquia ou perfil de dono dentro do Grupo)  
7. Todos os recursos criados por qualquer Organizer do Grupo (eventos, EventStaff, tags) são visíveis e gerenciáveis por todos os demais Organizers do mesmo Grupo  
8. Grupos são isolados entre si: um Organizer não tem acesso a eventos, EventStaff ou dados de outro Grupo

#### Gestão de eventos

9. Qualquer Organizer do Grupo deve poder criar um novo evento com nome, descrição, data e hora.  
10. Qualquer Organizer do Grupo deve poder editar informações de qualquer evento do Grupo.  
11. Qualquer Organizer do Grupo deve poder ativar ou desativar qualquer evento do Grupo manualmente, independente da data/hora configurada.  
12. O sistema deve suportar múltiplos eventos simultâneos e ativos ao mesmo tempo, com check-ins completamente independentes por evento.

#### Gestão de EventStaff

13. Qualquer Organizer do Grupo deve poder cadastrar membros de EventStaff vinculados ao Grupo.  
14. A EventStaff de um Grupo tem acesso a todos os eventos ativos daquele Grupo, independente de qual Organizer os criou.  
15. Grupos diferentes têm EventStaffs completamente isoladas entre si.

#### Cadastro e QR Code de Participants

16. Um usuário deve poder se cadastrar na plataforma fornecendo: nome completo, sexo, celular, e-mail, data de nascimento e senha.  
17. Ao confirmar o cadastro, o Participant deve receber imediatamente seu QR Code.  
18. O Participant deve poder logar na plataforma e visualizar seu QR Code a qualquer momento.

#### Modelo de QR Code

19. O QR Code é único por Participant na versão atual, mas o modelo deve ser projetado para suportar evoluções futuras sem necessidade de refatoração estrutural. As seguintes decisões devem ser respeitadas na implementação:  
    1. O QR Code deve ser armazenado como entidade própria no banco de dados (tabela/coleção separada), com relacionamento ao Participant. Não deve ser um campo simples no cadastro do Participant.  
    2. Cada registro de QR Code deve conter: identificador único, dado codificado, data de criação e referência ao Participant dono.  
    3. O design deve permitir que, em versões futuras, o Organizer possa: (a) regerar todos os QR Codes relacionados aos seus eventos do banco de dados invalidando os anteriores; (b) emitir QR Codes específicos por evento, com validade restrita a um evento.  
    4. Para suportar QR Codes por evento no futuro, a entidade QR Code deve prever um campo opcional de referência a evento (nulo na versão atual \= QR Code global do Participant).

#### Check-in em Eventos

20. O EventStaff, estando logado, deve poder selecionar o evento ativo do Grupo para iniciar o escaneamento.  
21. O EventStaff deve poder escanear QR Codes com a própria câmera de seu dispositivo para registrar a entrada de um Participant no evento selecionado.  
22. O sistema deve validar o QR Code no momento do escaneamento e rejeitar: QR Codes inválidos ou não reconhecidos, e QR Codes de Participants já registrados naquele evento (entrada duplicada)  
23. O sistema deve registrar no log: identificador do Participant, identificador do EventStaff que realizou o check-in, evento, e timestamp do registro

#### Tags de Eventos

24. Qualquer Organizer do Grupo deve poder adicionar tags de texto livre a um evento no momento da criação ou edição.  
25. Um evento pode ter zero ou mais tags. Não há lista pré-definida:  o Organizer digita o valor livremente (ex.: "mensal", "turma-2025", "presencial").  
26. Qualquer Organizer do Grupo deve poder filtrar a consulta de estatísticas por uma ou mais tags, selecionando o conjunto de eventos do Grupo que possuem aquelas tags

#### Relatórios e estatísticas

27. Qualquer Organizer do Grupo deve poder consultar o log de qualquer evento do Grupo, visualizando: qual Participant foi registrado, por qual EventStaff e em que horário.  
28. Qualquer Organizer do Grupo deve poder consultar estatísticas de um evento específico ou de um conjunto de eventos do Grupo filtrado por período e/ou por tags.

### Não funcionais

29. Todas as telas devem ser responsivas e funcionar em desktop e mobile.  
30. O acesso às funcionalidades administrativas deve exigir autenticação.  
31. O sistema deve utilizar comunicação segura via HTTPS.  
32. O sistema deve proteger endpoints administrativos contra acesso não autorizado.  
33. Senhas devem ser armazenadas de forma criptografada com hash seguro (ex.: bcrypt).  
34. O backend deve seguir arquitetura modular com separação de camadas.  
35. O código deve seguir padrões REST para comunicação entre frontend e backend.  
36. O sistema deve suportar picos de leitura simultânea durante a entrada em eventos, especialmente em cenários com \~100 Participants chegando em curto intervalo.

# Casos de uso

### Participant

1. **Cadastrar-se na plataforma**  
   1. Atores: Participant  
   2. Pré-condições:   
      1. O usuário não possui cadastro na plataforma  
   3. Fluxo principal:  
      1. Usuário acessa a página de cadastro.  
      2. Preenche os dados: nome completo, sexo, celular, e-mail, data de nascimento e senha.  
      3. Sistema valida os dados (campos obrigatórios, formato de e-mail, força de senha).  
      4. Sistema verifica unicidade de e-mail e celular.  
      5. Sistema cria a conta e armazena a senha com hash seguro.  
      6. Sistema cria um registro de QR Code vinculado ao Participant, com data de criação.  
      7. Sistema confirma o cadastro e redireciona o Participant para a página de visualização do seu QR Code  
   4. Fluxos alternativos:  
      1. E-mail ou celular já cadastrado: sistema exibe mensagem de erro e solicita correção.  
      2. Dados inválidos (formato, campos em branco): sistema exibe mensagem de validação por campo.  
   5. Pós-condições:  
      1. Participant registrado no banco de dados.  
      2. QR Code criado e vinculado ao Participant.  
2. **Acessar QR Code**  
   1. Atores: Participant  
   2. Pré-condições:   
      1. Participant está autenticado na plataforma.  
   3. Fluxo principal:  
      1. Participant acessa a seção "Meu QR Code".  
      2. Sistema recupera o QR Code ativo vinculado ao Participant.  
      3. Sistema exibe o QR Code na tela.  
   4. Pós-condições:  
      1. Participant visualiza seu QR Code e pode apresentá-lo na entrada do evento.

### Organizer

3. **Criar evento**  
   1. Atores: Organizer  
   2. Pré-condições:   
      1. Organizer está autenticado e pertence a um Grupo.  
   3. Fluxo principal:  
      1. Organizer acessa a área de gestão de eventos do Grupo.  
      2. Seleciona "Criar novo evento".  
      3. Preenche os dados: nome, descrição, data e hora de início.  
      4. Opcionalmente, adiciona uma ou mais tags de texto livre ao evento (ex.: "mensal", "turma-2025").  
      5. Confirma a criação.  
      6. Sistema cria o evento associado ao Grupo, com status inativo por padrão.  
      7. Qualquer Organizer do Grupo pode ativar o evento manualmente quando desejar.  
   4. Fluxos alternativos:  
      1. Campos obrigatórios em branco: sistema exibe validação e impede criação.  
   5. Pós-condições:  
      1. Evento registrado com suas tags, visível para todos os Organizers do Grupo.  
4. **Ativar/desativar evento**  
   1. Atores: Organizer  
   2. Pré-condições:   
      1. Organizer está autenticado e pertence ao Grupo dono do evento.  
      2. Evento existe no sistema  
   3. Fluxo principal:  
      1. Organizer acessa a lista de eventos do Grupo.  
      2. Seleciona o evento desejado.  
      3. Alterna o status do evento entre ativo e inativo.  
      4. Sistema persiste a mudança de status imediatamente  
   4. Regras de negócio:  
      1. Um evento ativo aceita check-ins de qualquer EventStaff do Grupo.  
      2. Um evento inativo não aceita novos check-ins.  
      3. Múltiplos eventos do Grupo podem estar ativos simultaneamente.  
      4. Qualquer Organizer do Grupo pode alterar o status de qualquer evento do Grupo.  
   5. Pós-condições:  
      1. Status do evento atualizado no banco de dados.  
5. **Cadastrar EventStaff no Grupo**  
   1. Atores: Organizer  
   2. Pré-condições:   
      1. Organizer está autenticado e pertence a um Grupo.  
   3. Fluxo principal:  
      1. Organizer acessa a área de gestão de equipe do Grupo.  
      2. Seleciona "Adicionar membro da equipe".  
      3. Preenche os dados do novo membro: nome, e-mail e senha temporária (ou convite por e-mail).  
      4. Sistema cria a conta de EventStaff vinculada ao Grupo (não a um Organizer específico).  
      5. O novo membro recebe acesso a todos os eventos ativos do Grupo  
   4. Regras de negócio:  
      1. A EventStaff é vinculada ao Grupo, não a um Organizer individualmente. Todos os Organizers do Grupo a enxergam e podem gerenciá-la.  
      2. EventStaff de Grupos diferentes são completamente isoladas  
   5. Fluxos alternativos:  
      1. E-mail já cadastrado: sistema exibe erro e impede duplicidade  
   6. Pós-condições:  
      1. Conta de EventStaff criada e vinculada ao Grupo.

### EventStaff

6. **Realizar Check-in por QR Code**  
   1. Atores: EventStaff e Participant  
   2. Pré-condições:   
      1. EventStaff está autenticada e vinculada a um Grupo.  
      2. Existe ao menos um evento ativo no Grupo.  
   3. Fluxo principal:  
      1. EventStaff acessa a área de check-in.  
      2. Sistema lista os eventos ativos do Grupo.  
      3. EventStaff seleciona o evento em que está trabalhando.  
      4. EventStaff aciona o leitor de QR Code (câmera do dispositivo).  
      5. Participant apresenta seu QR Code.  
      6. Sistema lê e decodifica o QR Code.  
      7. Sistema valida o QR Code: verifica se o código existe, se o Participant está cadastrado e se não houve check-in anterior naquele evento.  
      8. Sistema registra o check-in com: ID do Participant, ID do EventStaff, ID do evento e timestamp.  
      9. Sistema exibe confirmação visual de sucesso (nome do Participant).  
   4. Fluxos alternativos:  
      1. QR Code inválido ou não reconhecido: sistema exibe mensagem de erro e permite nova leitura.  
      2. Participant já fez check-in neste evento: sistema alerta sobre entrada duplicada e não registra novamente.  
      3. Nenhum evento ativo disponível no Grupo: sistema informa que não há eventos ativos no momento.  
   5. Pós-condições:  
      1. Check-in registrado no banco de dados com todos os metadados.  
      2. Log do evento atualizado

### Organizer

7. **Consultar Log de um Evento**  
   1. Atores: Organizer  
   2. Pré-condições:   
      1. Organizer está autenticado e pertence ao Grupo dono do evento.  
      2. Existe ao menos um evento do Grupo com registros de check-in  
   3. Fluxo principal:  
      1. Organizer acessa a área de relatórios.  
      2. Seleciona um evento do Grupo.  
      3. Sistema exibe o log de check-ins com: nome do Participant, nome do EventStaff responsável e timestamp da entrada.  
      4. Organizer pode filtrar por período, EventStaff ou Participant.  
      5. Organizer pode exportar o log (CSV ou similar).  
   4. Pós-condições:  
      1. Organizer visualiza ou exporta o registro de presença do evento  
8. **Consultar estatísticas de Eventos**  
   1. Atores: Organizer  
   2. Pré-condições:   
      1. Organizer está autenticado e pertence ao Grupo.  
      2. Existem eventos do Grupo com registros de check-in.  
   3. Fluxo principal:  
      1. Organizer acessa a área de estatísticas.  
      2. Seleciona um evento específico do Grupo ou define filtros: período (data início / data fim) e/ou uma ou mais tags.  
      3. Sistema retorna o conjunto de eventos do Grupo que satisfazem os filtros aplicados.  
      4. Sistema calcula e exibe: total de Participants registrados, total de check-ins realizados, distribuição por horário de entrada e breakdown por EventStaff (quantos check-ins cada membro realizou).  
   4. Regras de negócio:  
      1. Filtro por tags é inclusivo: selecionar as tags "mensal" e "presencial" retorna eventos que possuam ao menos uma dessas tags.  
      2. Filtros de período e tag são combináveis (AND entre si).  
      3. O escopo é sempre restrito ao Grupo do Organizer autenticado  
   5. Pós-condições:  
      1. Organizer obtém visão agregada da frequência nos eventos selecionados do Grupo  
9. **Adicionar Organizer ao Grupo**  
   1. Atores: Organizer  
   2. Pré-condições:   
      1. Organizer está autenticado e pertence a um Grupo.  
      2. O novo Organizer ainda não possui conta na plataforma, ou possui conta sem Grupo.  
   3. Fluxo principal:  
      1. Organizer acessa a área de configurações do Grupo.  
      2. Seleciona "Convidar Organizer".  
      3. Informa o e-mail do novo membro.  
      4. Sistema envia convite ou cria a conta vinculada ao Grupo.  
      5. Novo Organizer passa a enxergar todos os eventos, EventStaff e dados do Grupo  
   4. Regras de negócio:  
      1. Um Organizer pertence a exatamente um Grupo. Não é possível pertencer a dois Grupos simultaneamente.  
      2. Todos os membros do Grupo têm os mesmos poderes. Não há papel de administrador do Grupo  
   5. Fluxos alternativos:  
      1. E-mail já pertence a um Organizer de outro Grupo: sistema exibe erro informando que o usuário já está associado a um Grupo  
   6. Pós-condições:  
      1. Novo Organizer vinculado ao Grupo com acesso completo aos recursos compartilhados

# Decisões de arquitetura registradas

Esta seção consolida decisões tomadas durante o levantamento de requisitos para guiar a implementação.

### Grupo como unidade central de isolamento

O Grupo substitui o Organizer individual como unidade de ownership dos recursos. Eventos, EventStaff e tags pertencem ao Grupo, não a um Organizer específico. Isso significa que a chave de isolamento em todas as queries administrativas é o group\_id, não o organizer\_id. Qualquer Organizer autenticado carrega o group\_id no seu token/sessão, e o backend deve usar esse valor para delimitar o escopo de acesso.

### Participant como entidade global da plataforma

O Participant é uma entidade global da plataforma e não pertence a nenhum Grupo específico. Seu cadastro é único e pode ser utilizado em qualquer evento que utilize o sistema, independentemente do Grupo organizador. O isolamento entre Grupos ocorre exclusivamente por meio da entidade Evento e do contexto de autenticação do Organizer/EventStaff. O QR Code do Participant é global e pode ser apresentado em qualquer evento ativo. A validação de acesso considera o evento selecionado e o Grupo ao qual ele pertence, e não uma vinculação prévia do Participant ao Grupo.

### QR Code como entidade independente

O QR Code não deve ser modelado como um simples campo do Participant. Deve existir como entidade própria com: id, valor codificado, data de criação, participant\_id (FK) e event\_id opcional (nulo \= QR Code global). Isso permite que versões futuras adicionem QR Codes por evento ou mecanismo de regeração em massa sem alterar a estrutura do Participant.

### EventStaff vinculada ao Grupo, não ao Organizer

A EventStaff é vinculada diretamente ao Grupo no momento do cadastro. O organizer\_id de quem a cadastrou pode ser registrado para fins de auditoria, mas não deve ser usado como critério de acesso. O critério de acesso é sempre o group\_id. Isso garante que, se um Organizer sair do Grupo, a EventStaff continue operacional.

### Suporte a eventos simultâneos

O sistema deve tratar múltiplos eventos ativos do mesmo Grupo de forma completamente independente. Um check-in está sempre associado a um evento específico. Um Participant pode ter presença registrada em mais de um evento simultâneo (casos de presença em diferentes salas/tracks no mesmo dia, por exemplo).

### Validação de entrada duplicada

O sistema deve garantir, a nível de banco de dados, que a combinação (participant\_id, event\_id) seja única na tabela de check-ins. A validação deve ocorrer tanto na camada de aplicação (resposta imediata ao EventStaff) quanto na camada de dados (constraint de unicidade).

### Tags como entidade relacionada ao Grupo

As tags devem ser modeladas como uma relação many-to-many entre Evento e Tag, onde Tag contém um valor de texto normalizado (ex.: lowercase, sem espaços extras) e uma referência ao Grupo. Isso permite consultas eficientes por tag dentro do escopo do Grupo e evita duplicidade de valores. Tags de Grupos diferentes são isoladas entre si.

### Tecnologias

* **Spring Boot 3.5.11** e **Java 21**.  
* **Spring Data JPA** com **PostgreSQL** (Docker).  
* **Spring Security \+ JWT:** Para garantir que apenas recepcionistas autorizados possam bater o ponto no endpoint de /scan.  
* **ZXing Library:** Para gerar o QR Code no Backend.  
* **Lombok:** Para reduzir o código boilerplate (Getters/Setters).  
* **Html5-qrcode**: Biblioteca JavaScript que permite que o navegador do celular abra a câmera e leia o código diretamente, enviando o ID via API para o Spring Boot

# Modelo de domínio

### Mermaid

classDiagram  
    direction TB

    %% Entidades Principais %%  
    class Group {  
        id  
        name  
        createdAt  
    }

    class Organizer {  
        id  
        name  
        email  
        password  
    }

    class EventStaff {  
        id  
        name  
        email  
        password  
    }

    class Event {  
        id  
        name  
        description  
        startDateTime  
        isActive  
    }

    class Tag {  
        id  
        name  
    }

    class Participant {  
        id  
        fullName  
        gender  
        phone  
        email  
        birthDate  
        password  
    }

    class QrCode {  
        id  
        encodedData  
        createdAt  
    }

    class CheckIn {  
        id  
        timestamp  
    }

    %% Relacionamentos do Grupo %%  
    Group "1" \--\> "\*" Organizer : has  
    Group "1" \--\> "\*" EventStaff : has  
    Group "1" \--\> "\*" Event : owns  
    Group "1" \--\> "\*" Tag : owns

    %% Relacionamentos do Evento %%  
    Event "\*" \--\> "\*" Tag : has

    %% Relacionamentos do Participante %%  
    Participant "1" \--\> "1..\*" QrCode : owns  
    QrCode "\*" \--\> "0..1" Event : references (optional)

    %% Relacionamentos do Check-In (Ação/Log) %%  
    CheckIn "\*" \--\> "1" Participant : registers  
    CheckIn "\*" \--\> "1" Event : at  
    CheckIn "\*" \--\> "1" EventStaff : scanned by
