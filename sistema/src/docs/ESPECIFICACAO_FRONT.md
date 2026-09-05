# Especificação Funcional e de Integração de API — Front-end (React)

**Projeto:** Sistema de Gestão Jurídica Inteligente  
**Perfil:** Guia de Integração e Contratos de API para a Equipe de Front-end  
**Alinhamento:** Backend Spring Boot v3.3.3 / Java 21 LTS  

---

## 1. Diretrizes Técnicas, Arquitetura e Segurança

*   **Autenticação e Sessão:**
    *   **Login:** `POST /api/auth/login`
        *   **Payload de Envio:**
            ```json
            {
              "email": "advogado@escritorio.com",
              "senha": "senhaSegura123",
              "manterConectado": true
            }
            ```
            *(Nota: O campo `manterConectado` é um booleano opcional. Se enviado como `true`, estende a validade do token JWT gerado pelo backend de 2 horas para 7 dias, orientando também a persistência de sessão no front-end em `localStorage` vs. `sessionStorage` quando `false` ou omitido).*
        *   **Resposta (HTTP 200):**
            ```json
            {
              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            }
            ```
    *   **Resgate dos Dados do Usuário Logado (Perfil):**
        *   **Endpoint Oficial:** `GET /api/auth/me`
        *   **Descrição:** Retorna diretamente o `UsuarioDTO` da sessão ativa correspondente ao token JWT informado no header `Authorization`.
        *   **Resposta (HTTP 200 - `UsuarioDTO`):**
            ```json
            {
              "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "nome": "Dr. Carlos Eduardo",
              "email": "advogado@escritorio.com",
              "senha": null,
              "perfil": "ADVOGADO",
              "oab": "RS123456",
              "ativo": true
            }
            ```
            *(Nota de Segurança: O campo `senha` é retornado sempre como `null` para preservar o hash da credencial).*
    *   **Interceptor de Requisições:** Injetar automaticamente o header `Authorization: Bearer <token>` em todas as requisições autenticadas.
*   **Envelopamento de Paginação:**
    *   As rotas paginadas (`/api/clientes`, `/api/processos`, `/api/faturamentos`) retornam o envelope padrão do Spring Data:
        ```json
        {
          "content": [ ... ],
          "totalElements": 42,
          "totalPages": 5,
          "size": 10,
          "number": 0,
          "first": true,
          "last": false
        }
        ```
    *   Parâmetros de paginação padrão a serem enviados pelo front: `?page=0&size=10&sort=id,desc`.

---

## 2. Módulos do Sistema e Mapeamento Completo de Endpoints

### 2.1. Dashboard Executiva
Painel inicial com métricas consolidadas e atalhos estratégicos:
*   **Obter Dados Consolidados:** `GET /api/dashboard/{usuarioId}`
    *   **Retorno (`ResumoDashboardDTO`):**
        ```json
        {
          "totalClientesAtivos": 120,
          "totalProcessosAndamento": 45,
          "tarefasPendentesHoje": 3,
          "proximasTarefas": [ ... ],
          "totalReceberHoje": 3500.00,
          "proximasFaturasReceber": [ ... ],
          "audienciasHoje": 1,
          "proximasAudiencias": [ ... ]
        }
        ```

---

### 2.2. Gestão de Clientes, Emissão Documental e GED
Listagem com filtros, formulários de cadastro/edição em modal ou drawer, área nobre de emissão de PDFs oficiais e aba de documentos anexados.

#### Operações Cadastrais
*   **Listar Clientes (Paginado com Filtros):** `GET /api/clientes?page=0&size=10`
    *   **Query Parameters (Opcionais):**
        *   `page` (int, default = 0): Número da página.
        *   `size` (int, default = 10): Quantidade de itens por página.
        *   `termoBusca` (string): Termo para busca textual dinâmica que filtra por nome, CPF/CNPJ ou e-mail (ex: `?termoBusca=Silva`).
    *   **Retorno:** Envelope `Page<ClienteDTO>`.
*   **Buscar Cliente por ID:** `GET /api/clientes/{id}`
*   **Cadastrar Cliente:** `POST /api/clientes`
*   **Atualizar Cliente:** `PUT /api/clientes/{id}`

#### Emissão Automatizada de Documentos Oficiais (PDF com Fontes Embutidas)
Os endpoints abaixo geram o arquivo binário processado no servidor com fontes TrueType embutidas (`BaseFont.EMBEDDED`). A resposta deve ser tratada como `blob` no cliente (`responseType: 'blob'`) para download ou visualização imediata:

1.  **Gerar Procuração Ad Juditia & Declaração de Hipossuficiência:**
    *   **Rota:** `GET /api/clientes/{id}/procuracao`
    *   **Query Parameters (Opcionais):**
        *   `acao` (string): Nome da ação jurídica a ajuizar (ex: *"Ação Revisional de Benefício"*). Padrão se vazio: `"AÇÃO JUDICIAL"`.
        *   `varaCivel` (string): Identificação da vara (ex: *"1ª Vara Cível"*). Padrão se vazio: `"____ vara cível"`.
        *   `comarca` (string): Comarca correspondente (ex: *"Ijuí"*).
        *   `imprimirDeclaracao` (boolean, **default = true**): Se `false`, o PDF gerado conterá **apenas** a página 1 (Procuração), suprimindo a página 2 (Declaração de Hipossuficiência/AJG). Ideal para clientes que não terão assistência judiciária gratuita.

2.  **Gerar Contrato de Prestação de Serviços e Honorários:**
    *   **Rota:** `GET /api/clientes/{id}/contrato-honorarios`
    *   **Query Parameters (Opcionais):**
        *   `acao` (string): Ação que fundamenta a contratação.
        *   `vara` (string): Vara correspondente.
        *   `comarca` (string): Comarca de tramitação.
        *   `valorServicos` (string): Valor acordado ou percentual (ex: *"30% do proveito econômico obtido"* ou *"R$ 5.000,00"*).
        *   `objetivoDemanda` (string): Descrição sucinta da pretensão (ex: *"Restabelecimento de benefício por incapacidade temporária"*).
    *   *Nota:* O documento realiza concordância e flexões de gênero automáticas no preâmbulo e cláusulas com base no sexo do cliente cadastrado.

#### Gestão de Documentos Anexos do Cliente (GED)
*   **Listar Anexos do Cliente:** `GET /api/documentos/cliente/{clienteId}`
    *   *Retorno:* `List<DocumentoDTO>` com os documentos arquivados do cliente.
*   **Fazer Upload de Anexo:** `POST /api/documentos/upload` (`multipart/form-data`)
    *   *Form-Data:* `arquivo` (File), `titulo` (String), `clienteId` (UUID).
*   **Baixar / Visualizar Documento:** `GET /api/documentos/{id}/download`
    *   *Resposta:* Stream de bytes com headers `Content-Disposition: attachment; filename="..."` e `Content-Type` detectado automaticamente.
*   **Excluir Documento:** `DELETE /api/documentos/{id}`
    *   *Resposta:* HTTP 204 No Content (remove o registro no banco e o arquivo físico no disco).

---

### 2.3. Gestão de Processos, Histórico de Andamentos e Autos

#### Operações de Processo
*   **Listar Processos (Paginado com Filtros):** `GET /api/processos?page=0&size=10`
    *   **Query Parameters (Opcionais):**
        *   `page` (int, default = 0): Número da página.
        *   `size` (int, default = 10): Quantidade de itens por página.
        *   `termoBusca` (string): Busca textual por número CNJ, assunto ou nome do cliente vinculado (ex: `?termoBusca=0001234`).
        *   `arquivado` (boolean): Filtro de arquivamento (`true` para processos arquivados, `false` para ativos/em andamento, ou omitido para listar todos).
    *   **Retorno:** Envelope `Page<ProcessoDTO>`.
*   **Listar por Cliente (Paginado):** `GET /api/processos/cliente/{clienteId}?page=0&size=10`
*   **Buscar Detalhes por ID:** `GET /api/processos/{id}`
*   **Criar Processo:** `POST /api/processos`
*   **Editar Processo:** `PUT /api/processos/{id}`
*   **Arquivar Processo:** `PATCH /api/processos/{id}/arquivar`
*   **Desarquivar Processo:** `PATCH /api/processos/{id}/desarquivar`

#### Andamentos Processuais
*   **Listar Linha do Tempo:** `GET /api/processos/{processoId}/andamentos`
*   **Lançar Andamento Manual:** `POST /api/processos/{processoId}/andamentos`

#### Peças e Autos Anexados ao Processo (GED)
*   **Listar Documentos do Processo:** `GET /api/documentos/processo/{processoId}`
    *   *Retorno:* `List<DocumentoDTO>` contendo as petições, certidões e comprovantes do processo.
*   **Vincular Anexo ao Processo:** `POST /api/documentos/upload` (`multipart/form-data`)
    *   *Form-Data:* `arquivo` (File), `titulo` (String), `processoId` (UUID).
*   **Download de Peça:** `GET /api/documentos/{id}/download`
*   **Exclusão de Peça:** `DELETE /api/documentos/{id}`

#### Apoio a Seletores
*   **Listar Advogados Ativos:** `GET /api/usuarios/advogados` *(Alimenta o `<select>` de advogado responsável no cadastro/edição de processos).*

---

### 2.4. Agenda e Calendário Expansivo (Audiências e Tarefas)

A interface renderiza um calendário unificado, harmonizado com parâmetros idênticos em formato `LocalDate` para datas.

#### Endpoints de Consulta de Calendário
*   **Listar Audiências por Período:** `GET /api/audiencias/agenda?inicio={data}&fim={data}`
    *   *Formato obrigatório:* ISO Date (`YYYY-MM-DD`, ex: `2026-09-01`). O backend realiza a conversão automática para o início (`00:00:00`) e encerramento do dia (`23:59:59`).
*   **Listar Tarefas por Período:** `GET /api/tarefas/agenda?inicio={data}&fim={data}`
    *   *Formato obrigatório:* ISO Date (`YYYY-MM-DD`, ex: `2026-09-01`).
    *   *Regra de Sessão:* Retorna automaticamente as tarefas do usuário autenticado no token.

#### Gestão de Audiências
*   **Cadastrar Audiência:** `POST /api/audiencias` *(Data futura obrigatória).*
*   **Buscar Audiência por ID:** `GET /api/audiencias/{id}`
*   **Atualizar Audiência:** `PUT /api/audiencias/{id}`
*   **Excluir Audiência:** `DELETE /api/audiencias/{id}`
*   **Listar Audiências do Processo:** `GET /api/audiencias/processo/{processoId}`
*   **Alterar Status:** `PATCH /api/audiencias/{id}/status?status={STATUS}`
    *   *Atenção:* O status é enviado via **Query Parameter** (`status`: `AGENDADA`, `REALIZADA` ou `CANCELADA`).

#### Inteligência Artificial na Audiência (Resumo Estratégico)
*   **Gerar e Salvar no Registro da Audiência:** `POST /api/audiencias/{id}/gerar-resumo-ia`
    *   **Body:** `{ "conteudoPeca": "Texto completo copiado da petição/inicial..." }`
    *   *Retorno:* A entidade `AudienciaDTO` atualizada, já contendo o resumo gerado no campo `resumoPreparatorioIa`.
*   **Consulta Avulsa de Resumo IA:** `POST /api/ia/resumos/audiencia`
    *   **Body:** `{ "conteudoPeca": "..." }`
    *   *Retorno:* String textual com a síntese dos pontos controvertidos e provas.

---

### 2.5. Gestão de Tarefas (To-Do List)
*   **Listar Tarefas do Painel do Usuário:** `GET /api/tarefas/dashboard/{usuarioId}`
*   **Criar Tarefa:** `POST /api/tarefas`
*   **Editar Tarefa:** `PUT /api/tarefas/{id}`
*   **Excluir Tarefa:** `DELETE /api/tarefas/{id}`
*   **Concluir Tarefa:** `PATCH /api/tarefas/{id}/concluir`

---

### 2.6. Financeiro e Faturamento

O módulo financeiro possui dois níveis de informação: cards totalizadores de topo e tabela transacional detalhada.

#### Cards Totalizadores (Resumo em Tempo Real)
*   **Rota:** `GET /api/faturamentos/resumo`
*   **Retorno (`ResumoFinanceiroDTO`):**
    ```json
    {
      "totalReceber": 12500.00,
      "totalPagar": 3200.00,
      "saldoPrevisto": 9300.00,
      "totalVencido": 800.00
    }
    ```

#### Operações Transacionais
*   **Listar Faturas (Paginado com Filtros):** `GET /api/faturamentos?page=0&size=10`
    *   **Query Parameters (Opcionais):**
        *   `page` (int, default = 0): Número da página.
        *   `size` (int, default = 10): Quantidade de itens por página.
        *   `status` (string, enum `StatusFaturamentoEnum`): Filtro pelo status da fatura (`PENDENTE`, `PAGO`, `CANCELADO`). Ex: `?status=PENDENTE`.
        *   `natureza` (string, enum `NaturezaFaturamentoEnum`): Filtro pelo fluxo financeiro (`A_RECEBER`, `A_PAGAR`). Ex: `?natureza=A_RECEBER`.
    *   **Retorno:** Envelope `Page<FaturamentoDTO>`.
*   **Listar por Processo:** `GET /api/faturamentos/processo/{processoId}`
*   **Cadastrar Fatura / Honorário:** `POST /api/faturamentos`
*   **Registrar Pagamento / Baixa:** `PATCH /api/faturamentos/{id}/pagar`
    *   **ATENÇÃO — Payload Obrigatório:**
        ```json
        {
          "dataPagamento": "2026-09-02"
        }
        ```
        *(Requisições PATCH sem este corpo JSON retornarão HTTP 400 Bad Request).*

---

## 3. Requisitos de Usabilidade & Padrões de UX (Key User)

1.  **Modais de Contexto Rápido:**
    *   A emissão de Procuração e Contrato deve ocorrer em modal/drawer que já traga os campos pré-preenchidos (caso existentes), solicitando apenas os parâmetros variáveis (`acao`, `comarca`, `valorServicos`, checkbox de AJG) antes do disparo do download.
2.  **Tratamento de Arquivos Binários (PDFs e Anexos):**
    *   Utilizar `URL.createObjectURL(new Blob([response.data], { type: response.headers['content-type'] }))` para abrir o documento gerado em nova aba ou disparar download limpo com o nome do arquivo configurado no header `Content-Disposition`.
3.  **Feedback Visual de Estados:**
    *   **Financeiro:** Destacar visualmente faturas `PENDENTE` em aberto, `PAGO` em tom neutro/verde e vencidas em alerta.
    *   **Tarefas e Audiências:** Utilizar códigos de cores distintos na agenda para diferenciar prazos/diligências de audiências formais.
4.  **Resumo de IA:**
    *   Exibir estado de carregamento com *skeleton* ou *spinner* estilizado durante a chamada do `POST /api/audiencias/{id}/gerar-resumo-ia`, já que chamadas de LLM possuem latência natural (1 a 3 segundos).