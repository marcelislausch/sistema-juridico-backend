# Especificação Funcional e Técnica — Sistema de Gestão Jurídica

Este documento define os requisitos funcionais, fluxos de usuário, regras de negócio e os respectivos mapeamentos de endpoints da API (Swagger) para a construção da interface do sistema, mantendo foco total na usabilidade para a rotina do escritório.

## 1. Diretrizes Técnicas, Arquitetura e Segurança

*   **Stack Livre:** A escolha do framework front-end, bibliotecas de roteamento, gerenciamento de estado e estilização fica a critério do desenvolvedor, desde que garanta performance e um design system consistente.
*   **Identidade Visual:** A interface deve transmitir sobriedade e profissionalismo (tons neutros, tipografia legível, alta densidade de informação sem poluição visual).
*   **Segurança e Sessão:**
    *   **Login:** `POST /api/auth/login` para captura de credenciais.
    *   **Dados do Usuário/Advogado:** Após o login e captura do UUID no token, consumir `GET /api/usuarios/{id}` para resgatar o perfil, nome e OAB do usuário logado.
    *   O token JWT deve ser armazenado no `localStorage` e injetado automaticamente (`Authorization: Bearer <token>`) no interceptor de requisições.

## 2. Módulos do Sistema e Integração de Endpoints

### Dashboard (Visão Estratégica)
*   Painel consolidando métricas para acompanhamento diário (processos ativos, tarefas pendentes, audiências e financeiro).
*   **Endpoint:** `GET /api/dashboard/{usuarioId}` (Rota consolidada que entrega os contadores gerais).

### Gestão de Tarefas
*   Listagem e controle de prazos e diligências diárias.
*   **Endpoints:**
    *   Listar Tarefas do Usuário: `GET /api/tarefas/dashboard/{usuarioId}`
    *   Criar Tarefa: `POST /api/tarefas`
    *   Concluir Tarefa: `PATCH /api/tarefas/{id}/concluir`

### Gestão de Clientes e Documentos
*   Listagem paginada e formulário em modal/side-drawer para cadastro e edição.
*   **Endpoints Básicos:**
    *   Listar: `GET /api/clientes` (suporta paginação)
    *   Criar: `POST /api/clientes`
    *   Editar: `PUT /api/clientes/{id}`
*   **Geração de Documentos:** Na tela de detalhes do cliente, destacar a área de emissão documental.
    *   **Procuração:** Utilizar o endpoint nativo `GET /api/clientes/{id}/procuracao` para retornar o documento gerado em formato de bytes/arquivo.
    *   **Upload de Documentos Diversos:** `POST /api/documentos/upload` (permite vincular arquivos ao cliente ou processo).

### Gestão de Processos
*   Visão geral, filtragem, detalhes e exibição cronológica de histórico (Andamentos).
*   **Endpoints de Processo:**
    *   Listar Todos: `GET /api/processos`
    *   Listar por Cliente: `GET /api/processos/cliente/{clienteId}`
    *   Buscar Detalhes (UUID): `GET /api/processos/{id}`
    *   Criar Processo: `POST /api/processos`
    *   Arquivar Processo: `PATCH /api/processos/{id}/arquivar`
*   **Endpoints de Andamentos:**
    *   Listar Histórico: `GET /api/processos/{processoId}/andamentos`
    *   Adicionar Andamento: `POST /api/processos/{processoId}/andamentos`

### Agenda e Audiências
*   **Visualização em Calendário:** Tela principal com calendário expansivo. Os dias devem exibir blocos de cores distintos para **audiências** e **tarefas**, com filtros no topo para ocultar/exibir cada categoria conforme a necessidade.
*   **Endpoints:**
    *   Listar Agenda: `GET /api/audiencias/agenda?inicio={data}&fim={data}`
    *   Listar Audiências do Processo: `GET /api/audiencias/processo/{processoId}`
    *   Criar Audiência: `POST /api/audiencias`
    *   Alterar Status (Agendada/Realizada/Cancelada): `PATCH /api/audiencias/{id}/status`
*   **Resumo Estratégico com IA:** No clique do bloco da audiência, abrir modal para inserção da peça e consumo de `POST /api/audiencias/{id}/gerar-resumo-ia`, ou alternativamente `POST /api/ia/resumos/audiencia`.

### Financeiro e Faturamento
*   Controle unificado com cards totalizadores ("A Receber" vs. "A Pagar") e filtros ágeis por status.
*   **Endpoints:**
    *   Listar Todos: `GET /api/faturamentos`
    *   Listar por Processo: `GET /api/faturamentos/processo/{processoId}`
    *   Criar Fatura/Honorário: `POST /api/faturamentos`
    *   Registrar Pagamento/Baixa: `PATCH /api/faturamentos/{id}/pagar`

## 3. Requisitos de Usabilidade (Foco no Key User - Cristhian)

*   **Economia de Cliques:** A navegação deve evitar a mudança de telas desnecessária. O cadastro de clientes, leitura de andamentos e emissão de contratos devem ocorrer em modais ou *side-drawers* sobre a listagem principal, mantendo o contexto.
*   **Geração Documental Fluida:** O clique no botão "Gerar Procuração" deve devolver o arquivo processado pelo backend de forma invisível e já solicitar o download ou pré-visualização em PDF, otimizando o atendimento.
*   **Destaque Preparatório:** O botão de "Resumo IA" nas audiências é o principal diferencial da rotina pré-audiência. Ele deve ter destaque absoluto na interface e retornar o texto gerado com excelente legibilidade e formatação.
*   **Clareza Visual na Agenda e Financeiro:** O calendário gigante deve usar alto contraste nas datas e horários, permitindo leitura rápida (diferenciando visualmente tarefas de audiências). No módulo financeiro, utilizar cores semânticas estritas (verde para entradas, vermelho/alerta para saídas pendentes) e formatação monetária padrão BRL.