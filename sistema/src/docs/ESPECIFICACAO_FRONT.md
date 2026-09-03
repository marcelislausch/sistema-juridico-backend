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
            *(Nota: O campo `manterConectado` orienta a persistência da sessão no `localStorage` vs. `sessionStorage`).*
        *   **Resposta (HTTP 200):**
            ```json
            {
              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            }
            ```
    *   **Captura dos Dados do Usuário Logado:**
        *   *Abordagem Atual:* Decodificar o payload do token JWT para extrair a claim `"id"` (UUID) e, em seguida, disparar `GET /api/usuarios/{id}` para resgatar `nome`, `email`, `perfil` (`ADMIN`, `ADVOGADO`, `SECRETARIA`) e `oab`.
        *   *Rota Recomendada (Alinhamento em desenvolvimento):* `GET /api/auth/me` (retornará diretamente o `UsuarioDTO` da sessão ativa).
    *   **Interceptor de Requisições:** Injetar automaticamente o header `Authorization: Bearer <token>` em todas as requisições autenticadas.
*   **Envelopamento de Paginação:**
    *   As rotas paginadas (`/api/clientes`, `/api/processos`) retornam o envelope padrão do Spring Data:
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
*   **Listar Clientes (Paginado):** `GET /api/clientes?page=0&size=10`
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
*   **Listar Todos (Paginado):** `GET /api/processos?page=0&size=10`
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

A interface deve renderizar um calendário unificado, diferenciando claramente os blocos de audiências e tarefas com alternância de filtros.

#### Endpoints de Consulta de Calendário
*   **Listar Audiências por Período:** `GET /api/audiencias/agenda?inicio={dataHora}&fim={dataHora}`
    *   *Formato obrigatório:* ISO Date-Time (`YYYY-MM-DDTHH:mm:ss`, ex: `2026-09-01T00:00:00`).
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
*   **Listar Todas as Faturas:** `GET /api/faturamentos`
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