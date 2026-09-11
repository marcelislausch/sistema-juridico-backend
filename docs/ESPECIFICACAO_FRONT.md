# Especificação Funcional e de Integração de API — Front-end (React)

**Projeto:** Sistema de Gestão Jurídica Inteligente  
**Perfil:** Guia de Integração e Contratos de API para a Equipe de Front-end  
**Alinhamento:** Backend Spring Boot v3.3.3 / Java 21 LTS  
**Versão:** 3.0 (Conformidade com Auditoria de Contratos e Swagger OpenAPI 3)

---

## 1. Diretrizes Técnicas, Arquitetura e Segurança

### 1.1. Autenticação, Sessão e Gestão de Credenciais
*   **Login:** `POST /api/auth/login`
    *   **Payload de Envio (`LoginDTO`):**
        ```json
        {
          "email": "advogado@escritorio.com",
          "senha": "senhaSegura123",
          "manterConectado": true
        }
        ```
        *(Nota: O campo `manterConectado` é opcional. Se `true`, estende o token JWT de 2 horas para 7 dias, orientando também a persistência em `localStorage` vs. `sessionStorage` quando `false` ou omitido).*
    *   **Resposta (HTTP 200 - `TokenDTO`):**
        ```json
        {
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        }
        ```
*   **Resgate dos Dados do Usuário Logado (Perfil Seguro):**
    *   **Endpoint Oficial:** `GET /api/auth/me`
    *   **Headers:** `Authorization: Bearer <token>`
    *   **Descrição:** Retorna as informações do usuário associado ao token da sessão ativa.
    *   **Resposta (HTTP 200 - `UsuarioResponseDTO`):**
        ```json
        {
          "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "nome": "Dr. Carlos Eduardo",
          "email": "advogado@escritorio.com",
          "perfil": "ADVOGADO",
          "oab": "RS121837",
          "ativo": true
        }
        ```
        > [!IMPORTANT]
        > **Blindagem de Segurança:** O contrato `UsuarioResponseDTO` **NÃO POSSUI** o campo `senha` (nem mesmo como `null`). As credenciais foram completamente expurgadas de todos os modelos de resposta da API.
*   **Recuperação de Senha (Público - `ForgotPasswordModal.tsx`):**
    *   **Rota:** `POST /api/auth/recuperar-senha`
    *   **Payload:** `{ "email": "usuario@escritorio.com.br" }`
    *   **Resposta (HTTP 202 Accepted):**
        ```json
        {
          "mensagem": "Se a conta existir, as instruções serão enviadas."
        }
        ```
    *   *(Ação no Front-end: Eliminar o mock com `setTimeout` e efetivar chamada com notificação amigável).*
*   **Redefinição de Senha com Token (Público):**
    *   **Rota:** `POST /api/auth/redefinir-senha`
    *   **Payload:** `{ "token": "uuid-do-token", "novaSenha": "novaSenhaSegura@2026" }`
    *   **Resposta (HTTP 200 OK):** `{ "mensagem": "Senha redefinida com sucesso." }`
*   **Alteração de Senha Autenticada (`SecurityTab.tsx`):**
    *   **Rota:** `PATCH /api/auth/me/senha`
    *   **Payload:** `{ "senhaAtual": "senhaAntiga123", "novaSenha": "novaSenhaSegura@2026" }`
    *   **Resposta (HTTP 204 No Content):** Sucesso sem corpo. Erros `400` para senha incorreta ou `422` para política não atendida.

### 1.2. Padronização de Paginação (Spring Boot 3.3+ `VIA_DTO`)
Todas as rotas paginadas da API adotam o modelo padronizado via Spring Data Web (`PageSerializationMode.VIA_DTO`):
```json
{
  "content": [ ... ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 42,
    "totalPages": 5
  }
}
```
*   **Parâmetros de Requisição Suportados (Planos no Swagger via `@ParameterObject`):**
    *   `page`: Índice da página baseado em zero (ex: `0`, `1`, `2`).
    *   `size`: Quantidade de itens por página (ex: `10`, `20`, `50`).
    *   `sort`: Critério e direção de ordenação (ex: `sort=nome,asc`, `sort=dataVencimento,desc`).

### 1.3. Padronização Canônica de Respostas de Erro
Todas as respostas de erro da API seguem schemas canônicos e consistentes:
*   **Erro Padrão (`ErroPadraoDTO`) — Códigos 401, 403, 404, 409, 422:**
    ```json
    {
      "timestamp": "2026-09-08T22:30:00",
      "status": 404,
      "error": "Not Found",
      "message": "Cliente não encontrado"
    }
    ```
*   **Erro de Validação de Campos (`ErroValidacaoDTO`) — Código 400 Bad Request:**
    ```json
    {
      "timestamp": "2026-09-08T22:30:00",
      "status": 400,
      "error": "Validation Error",
      "message": "Erro de validação nos campos informados.",
      "fieldErrors": [
        {
          "campo": "email",
          "mensagem": "E-mail inválido ou malformatado"
        },
        {
          "campo": "cpfCnpj",
          "mensagem": "CPF ou CNPJ deve possuir formato numérico válido"
        }
      ]
    }
    ```
    *(Ação no Front-end: Utilizar a lista `fieldErrors` para mapear erros diretamente abaixo dos respectivos inputs nos formulários).*

---

## 2. Módulos do Sistema e Mapeamento Completo de Endpoints

### 2.1. Dashboard Executiva
*   **Obter Dados Consolidados do Usuário Logado (Recomendado):** `GET /api/dashboard`
    *   *Nota:* O usuário é inferido automaticamente do token JWT, dispensando passagem de ID na rota.
*   **Obter Dados por ID (Compatibilidade):** `GET /api/dashboard/{usuarioId}`
*   **Retorno Blindado (`ResumoDashboardDTO`):**
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
    > [!TIP]
    > **Garantia de Não-Nulidade:** Todos os contadores e valores monetários retornam `0` ou `0.00` (nunca `null`), e todas as listas retornam arrays `[]` vazios quando não houver registros. O front-end pode consumir as propriedades sem receio de exceções `TypeError: Cannot read properties of undefined`.

---

### 2.2. Gestão de Clientes e CRM

#### Operações Cadastrais
*   **Listar Clientes (Paginado com Filtros no Servidor):** `GET /api/clientes`
    *   **Query Parameters:**
        *   `q` ou `termoBusca` (string): Busca textual universal por nome, CPF/CNPJ ou e-mail (ex: `?q=Silva`).
        *   `tipo` (Enum `TipoClienteEnum`): Filtra por pessoa física ou jurídica (`FISICA`, `JURIDICA`). Ex: `?tipo=FISICA`.
        *   `page` (int, default = 0), `size` (int, default = 10), `sort` (ex: `sort=nome,asc`).
    *   **Retorno:** Envelope paginado com `content: List<ClienteDTO>`.
*   **Buscar Cliente por ID:** `GET /api/clientes/{id}`
*   **Cadastrar Cliente:** `POST /api/clientes`
*   **Atualizar Cliente:** `PUT /api/clientes/{id}`

#### Emissão Automatizada de Documentos (PDF)
*   **Gerar Procuração e AJG:** `GET /api/clientes/{id}/procuracao?acao=&varaCivel=&comarca=&imprimirDeclaracao=true`
*   **Gerar Contrato de Honorários:** `GET /api/clientes/{id}/contrato-honorarios?acao=&vara=&comarca=&valorServicos=&objetivoDemanda=`
    *   *Nota:* Tratar a resposta no cliente como `responseType: 'blob'`.

---

### 2.3. Gestão de Processos Judiciais

#### Operações de Processo
*   **Listar Processos (Paginado com Filtros Avançados no Servidor):** `GET /api/processos`
    *   **Query Parameters:**
        *   `q` ou `termoBusca` (string): Busca por número CNJ, assunto ou nome do cliente.
        *   `fase` (Enum `FaseProcessualEnum`): Filtro de fase processual (`INICIAL`, `INSTRUCAO`, `RECURSAL`, `EXECUCAO`, `SUSPENSO`, `ARQUIVADO`).
        *   `arquivado` (boolean): Filtro de arquivamento (`true`, `false` ou omitido para todos).
        *   `clienteId` (UUID): Processos de um cliente específico.
        *   `advogadoId` (UUID): Processos sob responsabilidade de um advogado específico.
        *   `page` (int, default = 0), `size` (int, default = 10), `sort`.
    *   **Retorno:** Envelope paginado com `content: List<ProcessoDTO>`.
*   **Listar por Cliente:** `GET /api/processos/cliente/{clienteId}`
*   **Buscar Detalhes por ID:** `GET /api/processos/{id}`
*   **Criar Processo:** `POST /api/processos`
*   **Editar Processo:** `PUT /api/processos/{id}`
    *   **Payload do Processo (`ProcessoDTO`):**
        ```json
        {
          "numeroCnj": "5001234-88.2026.8.21.0016",
          "assunto": "Ação Revisional de Contrato Bancário",
          "faseAtual": "INICIAL",
          "parteAdversa": "Banco do Brasil S.A.",
          "cpfCnpjParteAdversa": "00.000.000/0001-91",
          "papelCliente": "AUTOR",
          "valorCausa": 75000.00,
          "comarca": "Ijuí/RS",
          "clienteId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "advogadoId": "7b8c9d0e-1234-5678-9abc-def012345678"
        }
        ```
    *   **Campos de Qualificação da Lide:**
        *   `parteAdversa` (string, opcional): Nome completo ou razão social da parte contrária.
        *   `cpfCnpjParteAdversa` (string, opcional): CPF ou CNPJ da parte adversa (com máscara no front, gravado limpo).
        *   `papelCliente` (Enum `PapelClienteEnum`): Posição jurídica do cliente: `AUTOR`, `REU`, `TERCEIRO_INTERESSADO`.
        *   `valorCausa` (number/decimal, opcional): Valor atribuído à causa na petição.
        *   `comarca` (string, opcional): Foro/comarca da ação (ex.: `"Ijuí/RS"`, `"Porto Alegre/RS"`).
*   **Arquivar Processo:** `PATCH /api/processos/{id}/arquivar`
*   **Desarquivar Processo:** `PATCH /api/processos/{id}/desarquivar`
*   **Andamentos do Processo:** `GET /api/processos/{processoId}/andamentos` e `POST /api/processos/{processoId}/andamentos`

---

### 2.4. Gestão Financeira e Fluxo de Caixa

#### Resumo Financeiro Consolidado
*   **Obter Totais:** `GET /api/faturamentos/resumo`
    *   Retorna `ResumoFinanceiroDTO` com `totalReceber`, `totalPagar`, `saldoPrevisto` e `totalVencido`.

#### Modelo de Dados da Fatura (`FaturamentoDTO`)
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "descricao": "Honorários Contratuais - Inicial",
  "valor": 3000.00,
  "tipo": "HONORARIOS",
  "status": "PENDENTE",
  "natureza": "A_RECEBER",
  "dataVencimento": "2026-10-15",
  "dataPagamento": null,
  "processoId": "8f3b49c1-5717-4562-b3fc-2c963f66afa6",
  "numeroParcela": 1,
  "totalParcelas": 3,
  "origemPagamento": "TERCEIRO_SUCUMBENCIA",
  "valorHonorariosRetidos": 1000.00,
  "valorRepasseCliente": 2000.00,
  "statusRepasse": "PENDENTE",
  "formaRepasse": "PIX",
  "dadosBancariosCliente": "Banco Banrisul - Ag 0240 CC 06.012345-0 (Chave PIX: 123.456.789-00)",
  "dataRepasse": null
}
```

#### Listagem e Lançamentos
*   **Listar Faturamentos (Paginado com Múltiplos Filtros no Servidor):** `GET /api/faturamentos`
    *   **Query Parameters:**
        *   `q` ou `termoBusca` (string): Busca textual na descrição da fatura.
        *   `status` (Enum `StatusFaturamentoEnum`): `PENDENTE`, `PAGO`, `PARCIALMENTE_PAGO`, `CANCELADO`.
        *   `natureza` (Enum `NaturezaFaturamentoEnum`): `A_RECEBER`, `A_PAGAR`.
        *   `tipo` (Enum `TipoFaturamentoEnum`): `HONORARIOS`, `CUSTAS`, `DESPESAS_ESCRITORIO`.
        *   `vencimentoDe` (date: `YYYY-MM-DD`): Data inicial do intervalo de vencimento.
        *   `vencimentoAte` (date: `YYYY-MM-DD`): Data final do intervalo de vencimento.
        *   `processoId` (UUID): Filtro por processo judicial.
        *   `page` (int, default = 0), `size` (int, default = 10), `sort`.
*   **Listar por Processo:** `GET /api/faturamentos/processo/{processoId}`
*   **Cadastrar Fatura Única:** `POST /api/faturamentos` (faturamento individual).
*   **Gerar Parcelamento:** `POST /api/faturamento/parcelamento` (gravação de múltiplas parcelas geradas pelo assistente).

#### Assistente de Parcelamento (Modal de Faturamento)
O modal de cadastro e lançamento financeiro dispõe do **Assistente de Parcelamento** para automação de contratos divididos:
1.  **Parâmetros de Entrada:** O operador informa o **Valor Total** (ex.: R$ 6.000,00), a quantidade de parcelas no campo **Total de Parcelas** (ex.: `3`) e a **Data do Primeiro Vencimento**.
2.  **Simulação em Grid Editável:**
    *   O sistema gera dinamicamente um grid interativo com as parcelas calculadas (`numeroParcela`, `totalParcelas`, `descricao`, `valor` e `dataVencimento`).
    *   O usuário possui flexibilidade para **editar individualmente** o valor e a data de vencimento de qualquer parcela no próprio grid (permitindo ajustes manuais de centavos ou parcelas com valores desiguais).
3.  **Trava Matemática Obrigatória (Validação de Integridade):**
    *   O botão **"Salvar"** do formulário permanece **estritamente desabilitado** caso a somatória dos valores de todas as linhas do grid seja diferente do valor total informado:
        $$\sum \text{parcelas} \neq \text{valorTotal}$$
    *   A interface deve exibir uma caixa de conferência em tempo real com:
        *   *Valor Total Informado*
        *   *Soma das Parcelas no Grid*
        *   *Diferença Pendente:* destacada em **vermelho** se houver divergência e em **verde** com mensagem de consistência quando a soma bater com exatidão (`Diferença: R$ 0,00`).
4.  **Persistência:** Ao salvar com a trava matemática validada, o front-end envia o array de faturamentos para a rota `POST /api/faturamento/parcelamento` para gravação de todas as parcelas no backend.

#### Regra de Exibição Condicional do Repasse
Em situações onde o escritório recebe valores de terceiros ou sucumbência processual em conta institucional:
*   **Gatilho de Renderização:** O bloco de campos de **Repasse ao Cliente** só deve ser renderizado na tela se o campo `origemPagamento` for selecionado como `TERCEIRO_SUCUMBENCIA`.
*   **Ocultamento para Pagamento Direto:** Se `origemPagamento` for igual a `DIRETO_CLIENTE` (ou nulo/omitido), o bloco de repasse **não deve ser exibido**, mantendo a interface enxuta.
*   **Campos do Bloco de Repasse (`TERCEIRO_SUCUMBENCIA`):**
    *   `valorHonorariosRetidos` (number): Valor retido a título de honorários advocatícios contratuais/sucumbenciais.
    *   `valorRepasseCliente` (number): Saldo líquido que deve ser transferido ao cliente (`valor - valorHonorariosRetidos`).
    *   `dadosBancariosCliente` (string): Dados da conta bancária ou chave PIX do cliente para repasse.
    *   `formaRepasse` (string): Modalidade do repasse (`PIX`, `TED`, `DOC`, `DINHEIRO`, `CHEQUE`).
    *   `statusRepasse` (Enum `StatusRepasseEnum`): Situação da transferência (`PENDENTE`, `REPASSADO`).
    *   `dataRepasse` (date): Data efetiva da realização da transferência bancária.

#### Fluxo de Liquidação (Baixa Integral e Baixa Parcial)
A tela de liquidação (ação de dar baixa na fatura) deve oferecer ao operador a seleção entre **Pagamento Total (Integral)** ou **Pagamento Parcial**:

1.  **Baixa Integral (Total):**
    *   **Rota:** `PATCH /api/faturamento/{id}/liquidar`
    *   **Payload de Envio:**
        ```json
        {
          "dataPagamento": "2026-09-10"
        }
        ```
    *   **Comportamento:** Quitação completa do título, atualizando o status para `PAGO`.

2.  **Baixa Parcial:**
    *   **Rota:** `PATCH /api/faturamento/{id}/liquidar-parcial`
    *   **Campos Solicitados na Modal:**
        *   `valorPago` (number): Valor parcial que está sendo recebido (deve ser $> 0$ e $<$ valor total da fatura).
        *   `dataPagamento` (date): Data do efetivo pagamento parcial.
        *   `novaDataVencimento` (date, obrigatório): Nova data de vencimento estipulada para a cobrança da diferença restante.
    *   **Payload de Envio:**
        ```json
        {
          "valorPago": 1500.00,
          "dataPagamento": "2026-09-10",
          "novaDataVencimento": "2026-10-10"
        }
        ```
    *   **Comportamento no Backend:** Registra o valor parcial pago, altera o status da fatura para `PARCIALMENTE_PAGO` e realiza a criação/desdobramento de uma nova fatura com o saldo devedor restante (`valorTotal - valorPago`) e a nova data de vencimento para controle da cobrança.

3.  **Efetivação de Repasse:**
    *   **Rota:** `PATCH /api/faturamento/{id}/repassar`
    *   **Payload de Envio:**
        ```json
        {
          "dataRepasse": "2026-09-10",
          "formaRepasse": "PIX"
        }
        ```
    *   **Comportamento:** Marca o repasse como `REPASSADO` e registra a data e forma do pagamento ao cliente.

---

### 2.5. Agenda Unificada (Audiências e Tarefas)

#### Audiências
*   **Listar Agenda Global:** `GET /api/audiencias/agenda`
    *   **Query Parameters:**
        *   `inicio` e `fim` (date: `YYYY-MM-DD`).
        *   `status` (Enum `StatusAudienciaEnum`): `AGENDADA`, `REALIZADA`, `CANCELADA`.
        *   `processoId` (UUID).
        *   `responsavelId` (UUID): Filtro por advogado responsável pela audiência.
*   **Retorno do DTO (`AudienciaDTO`):** Inclui o campo `responsavelId` mapeado para o responsável atribuído.
*   **Operações:** `POST /api/audiencias`, `GET /api/audiencias/{id}`, `PUT /api/audiencias/{id}`, `DELETE /api/audiencias/{id}`, `PATCH /api/audiencias/{id}/status?status=REALIZADA`.
*   **Resumo IA da Audiência:** `POST /api/audiencias/{id}/gerar-resumo-ia` com body `{ "conteudoPeca": "..." }`.

#### Tarefas
*   **Listar Agenda de Tarefas:** `GET /api/tarefas/agenda`
    *   **Query Parameters:**
        *   `inicio` e `fim` (date: `YYYY-MM-DD`).
        *   `concluida` (boolean): `true` para concluídas, `false` para pendentes.
        *   `status` (string, alias: `"CONCLUIDA"` ou `"PENDENTE"`).
        *   `tipo` (Enum `TipoTarefaEnum`): `DILIGENCIA`, `PRAZO`, `CONTATO`.
        *   `processoId` (UUID).
        *   `responsavelId` (UUID).
*   **Operações:** `POST /api/tarefas`, `PUT /api/tarefas/{id}`, `DELETE /api/tarefas/{id}`, `PATCH /api/tarefas/{id}/concluir`.

---

### 2.6. Central de Notificações do Dia (P1)
Substitui a agregação de múltiplas rotas no browser por um único endpoint do servidor:
*   **Rota:** `GET /api/notificacoes/resumo?data=YYYY-MM-DD`
*   **Headers:** `Authorization: Bearer <token>`
*   **Retorno (`NotificacaoResumoDTO`):**
    ```json
    {
      "notificacoes": [
        {
          "id": "c1f7a4b2-...",
          "tipo": "AUDIENCIA",
          "titulo": "Audiência de Instrução e Julgamento",
          "descricao": "Processo nº 5001234-88.2026.8.21.0016 - 1ª Vara Cível",
          "horario": "14:30",
          "destino": "audiencias",
          "recursoTipo": "AUDIENCIA",
          "recursoId": "8f3b49c1-..."
        }
      ],
      "quantidadeAgenda": 1,
      "quantidadeFinanceiro": 0
    }
    ```
*   **Tipagem Forte dos Campos:**
    *   `tipo`: Enum `TipoNotificacaoEnum` (`AUDIENCIA`, `TAREFA`, `FINANCEIRO`).
    *   `destino`: Enum `DestinoNotificacaoEnum` (`audiencias`, `agenda`, `financeiro`).
    *   `recursoTipo`: Enum `TipoRecursoNotificacaoEnum` (`AUDIENCIA`, `TAREFA`, `FATURAMENTO`).

---

### 2.7. Busca Global Unificada (P1)
Atende ao campo de busca textual no Topbar da aplicação:
*   **Rota:** `GET /api/busca?q={texto}&tipos={tipos}&limit={limit}`
*   **Exemplo:** `GET /api/busca?q=Menezes&tipos=PROCESSO,CLIENTE&limit=10`
*   **Retorno (`List<ItemBuscaDTO>`):**
    ```json
    [
      {
        "id": "4a7b9c1d-...",
        "tipo": "PROCESSO",
        "titulo": "5001234-88.2026.8.21.0016",
        "subtitulo": "Ação de Cobrança de Honorários",
        "rota": "/processos/4a7b9c1d-..."
      },
      {
        "id": "7b8c9d0e-...",
        "tipo": "CLIENTE",
        "titulo": "Cristhian Menezes",
        "subtitulo": "CPF: 123.456.789-00",
        "rota": "/clientes/7b8c9d0e-..."
      }
    ]
    ```
*   **Tipagem Forte:** O parâmetro `tipos` e o campo de retorno `tipo` utilizam o enum `TipoItemBuscaEnum` (`PROCESSO`, `CLIENTE`, `USUARIO`).

---

### 2.8. Status de Integração com Tribunais (P1)
Alimenta o badge/indicador "Tribunais Sincronizados" no Topbar:
*   **Rota:** `GET /api/integracoes/tribunais/status`
*   **Retorno (`TribunalStatusDTO`):**
    ```json
    {
      "status": "OPERACIONAL",
      "atualizadoEm": "2026-09-08T22:45:00",
      "mensagem": "Todos os serviços judiciais operando com sincronização regular.",
      "tribunaisSincronizados": [
        "TJRS - Tribunal de Justiça do Rio Grande do Sul",
        "TRF4 - Tribunal Regional Federal da 4ª Região",
        "TRT4 - Tribunal Regional do Trabalho da 4ª Região",
        "STJ - Superior Tribunal de Justiça"
      ]
    }
    ```
*   **Tipagem Forte:** O campo `status` é estritamente tipado com o enum `StatusTribunalEnum` (`OPERACIONAL`, `DEGRADADO`, `INDISPONIVEL`).

---

### 2.9. Gestão de Equipe e Usuários (`TeamTab.tsx`)
Atende à tela completa de "Equipe & Usuários" que engloba Admins, Advogados e Secretárias:
*   **Listagem Completa de Equipe (Paginada):** `GET /api/usuarios`
    *   **Headers:** `Authorization: Bearer <token>` (Permitido para `ADMIN` e `ADVOGADO`).
    *   **Query Parameters:**
        *   `q` ou `termoBusca` (string): Busca textual por nome, e-mail ou OAB.
        *   `ativo` (boolean): Filtra membros ativos (`true`) ou inativos (`false`).
        *   `page` (int, default = 0), `size` (int, default = 20), `sort` (default = `nome,asc`).
    *   **Retorno:** Envelope `Page<UsuarioResponseDTO>` contendo:
        ```json
        {
          "content": [
            {
              "id": "1a2b3c4d-...",
              "nome": "Mariana Santos",
              "email": "mariana.secretaria@escritorio.com",
              "perfil": "SECRETARIA",
              "oab": null,
              "ativo": true
            }
          ]
        }
        ```
*   **Cadastrar Novo Usuário:** `POST /api/usuarios`
    *   **Payload (`CriarUsuarioRequest`):**
        ```json
        {
          "nome": "Dra. Juliana Ribeiro",
          "email": "juliana@escritorio.com",
          "senha": "senhaForte@2026",
          "perfil": "ADVOGADO",
          "oab": "RS123456"
        }
        ```
    *   **Retorno (HTTP 201):** `UsuarioResponseDTO` (sem a senha).
*   **Seletor de Advogados Responsáveis:** Manter `GET /api/usuarios/advogados` exclusivo para alimentar os campos `<select>` de responsável em audiências e processos.

---

### 2.10. Configurações do Escritório (`OfficeTab.tsx`)
*   **Obter Dados:** `GET /api/configuracoes/escritorio`
*   **Salvar Dados:** `PUT /api/configuracoes/escritorio` com `EscritorioDTO` completo (CNPJ, Razão Social, WhatsApp, Endereço, etc.). O backend higieniza pontuações automaticamente.

---

## 3. Checklist de Integração e Eliminação de Mocks

| Componente Front-end | Situação Anterior | Integração Efetiva com a API |
| :--- | :--- | :--- |
| `ForgotPasswordModal.tsx` | `setTimeout` simulando envio | `POST /api/auth/recuperar-senha` |
| `SecurityTab.tsx` | `setTimeout` simulando troca de senha | `PATCH /api/auth/me/senha` |
| `OfficeTab.tsx` | State local com dados fictícios | `GET` e `PUT /api/configuracoes/escritorio` |
| `Topbar.tsx` (Notificações) | 4 requisições manuais e filtros no front | `GET /api/notificacoes/resumo` |
| `Topbar.tsx` (Busca) | Campo sem ação vinculada | `GET /api/busca?q=&tipos=&limit=` |
| `Topbar.tsx` (Tribunais) | Texto estático "Tribunais Sincronizados" | `GET /api/integracoes/tribunais/status` |
| `TeamTab.tsx` | Chamava `/usuarios/advogados` e mockava perfil | `GET /api/usuarios?page=0&size=20&ativo=true` |
| `ClientsPage.tsx` | `collectAllPages` e filtro em memória | `GET /api/clientes?q=&tipo=&page=&size=` |
| `ProcessesPage.tsx` | Download de 500 itens e filtro local | `GET /api/processos?q=&fase=&page=&size=` + cadastro/edição com qualificação da lide (`parteAdversa`, `cpfCnpjParteAdversa`, `papelCliente`, `valorCausa`, `comarca`) |
| `FinanceiroPage.tsx` | Paginação e somatórios no navegador | `GET /api/faturamentos?q=&status=&natureza=` + Assistente de Parcelamento (`/parcelamento` com trava matemática), Repasse condicional (`TERCEIRO_SUCUMBENCIA`), Baixa Integral (`/liquidar`), Baixa Parcial com desdobramento (`/liquidar-parcial`) e Repasse (`/repassar`) |
| `Dashboard.tsx` | Erro ao ler campos opcionais nulos | `GET /api/dashboard` (DTO com valores padrão garantidos) |