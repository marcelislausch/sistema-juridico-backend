# Especificação Funcional e de Integração de API — Front-end (React)

**Projeto:** Sistema de Gestão Jurídica Inteligente  
**Perfil:** Guia de Integração e Contratos de API para a Equipe de Front-end  
**Alinhamento:** Backend Spring Boot v3.3.3 / Java 21 LTS  
**Versão:** 3.3 (Conformidade com Swagger OpenAPI 3, Edição Financeira, Unificação Google Cloud, Blindagem UTF-8 e Módulo de Tribunais/Certificados Digitais)

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

### 1.4. Blindagem de Encoding UTF-8 e Contratos OpenAPI 3
*   **Encoding HTTP Padronizado em UTF-8:**
    *   Todas as respostas da API são emitidas com charset UTF-8 estrito (`Content-Type: application/json;charset=UTF-8`), configurado tanto no build Maven quanto nos servlets Spring.
    *   Eliminação integral de anomalias de codificação (*mojibake*, como `Ã§`, `Ã£`, `Ã©`, `â€"`).
    *   Textos jurídicos com acentuação, peças, andamentos, notificações, nomes de clientes e descrições financeiras transitam íntegros sem necessidade de decodificação manual no cliente.
*   **Contratos OpenAPI 3 Higienizados (Swagger):**
    *   A documentação OpenAPI é gerida por 16 interfaces dedicadas no pacote `presentation.openapi`, mantendo 100% de títulos, tags, resumos e schemas com ortografia técnica em português perfeitamente acentuada.
    *   Garante total previsibilidade e compatibilidade para geração automatizada de clientes e tipos TypeScript (ex: `openapi-typescript`, `@openapitools/openapi-generator-cli`) sem distorção de enums ou identificadores de propriedades.

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
    *   **Payload e Resposta do Processo (`ProcessoDTO`):**
        ```json
        {
          "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "numeroCnj": "5001234-88.2026.8.21.0016",
          "assunto": "Ação Revisional de Contrato Bancário",
          "faseAtual": "INICIAL",
          "parteAdversa": "Banco do Brasil S.A.",
          "cpfCnpjParteAdversa": "00.000.000/0001-91",
          "papelCliente": "AUTOR",
          "valorCausa": 75000.00,
          "comarca": "Ijuí/RS",
          "dataCriacao": "2026-09-10",
          "cliente": {
            "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            "nome": "Carlos Silva",
            "cpfCnpj": "123.456.789-00",
            "tipo": "FISICA"
          },
          "advogado": {
            "id": "7b8c9d0e-1234-5678-9abc-def012345678",
            "nome": "Dra. Marceli Lausch",
            "email": "marceli@escritorio.com",
            "oab": "RS123456",
            "perfil": "ADVOGADO"
          }
        }
        ```
    *   **Retrocompatibilidade:** O backend aceita tanto o objeto aninhado quanto IDs crus (`clienteId`, `advogadoId`) nas requisições via `@JsonAlias`. Nas respostas (`GET`, `POST`, `PUT`), sempre retorna o objeto resumido preenchido.
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
  "processo": {
    "id": "8f3b49c1-5717-4562-b3fc-2c963f66afa6",
    "numeroCnj": "5001234-88.2026.8.21.0016",
    "clienteId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "nomeCliente": "Carlos Silva"
  },
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
*   **Retrocompatibilidade:** O backend aceita `"processoId"` cru na criação via `@JsonAlias({"processoId"})`, e nas respostas retorna o objeto resumido `ProcessoResumoDTO` com os dados essenciais da causa vinculada e do cliente (`clienteId`).

#### Listagem e Lançamentos
*   **Listar Faturamentos (Paginado com Múltiplos Filtros no Servidor):** `GET /api/faturamentos`
    *   **Query Parameters:**
        *   `q` ou `termoBusca` (string): Busca textual na descrição da fatura.
        *   `status` (Enum `StatusFaturamentoEnum`): `PENDENTE`, `PAGO`, `PARCIALMENTE_PAGO`, `CANCELADO`.
        *   `natureza` (Enum `NaturezaFaturamentoEnum`): `A_RECEBER`, `A_PAGAR`.
        *   `tipo` (Enum `TipoFaturamentoEnum`): `HONORARIOS`, `CUSTAS`, `DESPESAS_ESCRITORIO`, `CONSULTA_AVULSA`.
        *   `vencimentoDe` (date: `YYYY-MM-DD`): Data inicial do intervalo de vencimento.
        *   `vencimentoAte` (date: `YYYY-MM-DD`): Data final do intervalo de vencimento.
        *   `processoId` (UUID): Filtro por processo judicial.
        *   `page` (int, default = 0), `size` (int, default = 10), `sort`.
*   **Listar por Processo:** `GET /api/faturamentos/processo/{processoId}`
*   **Cadastrar Fatura Única:** `POST /api/faturamentos` (faturamento individual com vínculo opcional a processo).
*   **Editar Faturamento:** `PUT /api/faturamentos/{id}` e `PATCH /api/faturamentos/{id}` (alteração de valor, categoria, vencimento, status e processo).
*   **Gerar Parcelamento:** `POST /api/faturamento/parcelamento` (gravação de múltiplas parcelas geradas pelo assistente).
*   **Registrar Consulta Avulsa:** `POST /api/faturamentos/consulta-avulsa` (lançamento e liquidação imediata sem processo judicial).

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

4.  **Edição de Lançamento Financeiro (`EditarFaturamentoModal.tsx`):**
    *   **Rotas Oficiais:** `PUT /api/faturamentos/{id}` e `PATCH /api/faturamentos/{id}` (suporta também `/api/faturamento/{id}`).
    *   **Payload de Envio (`EditarFaturamentoDTO`):**
        ```json
        {
          "descricao": "Honorários Contratuais - Reajuste de Parcela",
          "valor": 3500.00,
          "categoria": "HONORARIOS",
          "status": "PENDENTE",
          "natureza": "A_RECEBER",
          "dataVencimento": "2026-10-25",
          "dataPagamento": null,
          "processoId": "8f3b49c1-5717-4562-b3fc-2c963f66afa6"
        }
        ```
    *   **Regras e Flexibilidade de Parâmetros:**
        *   `valor` (number/decimal, opcional): Validado com `@Positive(message = "O valor deve ser positivo")`. Deve ser estritamente maior que zero.
        *   `descricao` (string, opcional): Texto explicativo do faturamento.
        *   `categoria` ou `tipo` (Enum `TipoFaturamentoEnum`): Aceita `HONORARIOS`, `CUSTAS`, `DESPESAS_ESCRITORIO` ou `CONSULTA_AVULSA`. O backend aceita indistintamente as chaves `"categoria"` ou `"tipo"` via anotação `@JsonAlias`.
        *   `status` (Enum `StatusFaturamentoEnum`): `PENDENTE`, `PAGO`, `PARCIALMENTE_PAGO`, `CANCELADO`.
        *   `natureza` (Enum `NaturezaFaturamentoEnum`): `A_RECEBER`, `A_PAGAR`.
        *   `dataVencimento` (date `YYYY-MM-DD`, opcional): Atualização da data de vencimento.
        *   `dataPagamento` (date `YYYY-MM-DD`, opcional): Atualização da data em que o pagamento foi realizado.
        *   `processoId` (UUID, opcional): Permite vincular, desvincular ou transferir o processo associado.
    *   **Regra Inteligente de Liquidação Automática:**
        *   Caso o operador altere o `status` para `PAGO` e **não envie** o campo `dataPagamento` (ou passe `null`), o backend atribui automaticamente a data atual (`LocalDate.now()`).
        *   Se o status for revertido para `PENDENTE` ou `CANCELADO`, a `dataPagamento` é desfeita no banco de dados.
    *   **Resposta (HTTP 200 OK):** `FaturamentoDTO` com os dados atualizados.

5.  **Lançamento Rápido de Consulta Jurídica Avulsa (`ConsultaAvulsaModal.tsx`):**
    *   **Rota Oficial:** `POST /api/faturamentos/consulta-avulsa` (suporta também `/api/faturamento/consulta-avulsa`).
    *   **Objetivo de UX:** Permite aos advogados registrarem honorários de consultas individuais sem a necessidade burocrática de abrir uma ficha de processo judicial para o cliente.
    *   **Payload de Envio (`ConsultaAvulsaDTO`):**
        ```json
        {
          "clienteId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "valor": 250.00,
          "descricao": "Consulta Jurídica Trabalhista Inicial",
          "dataPagamento": "2026-09-13",
          "formaPagamento": "PIX"
        }
        ```
    *   **Comportamento Transacional no Backend:**
        *   Cria um faturamento de natureza `A_RECEBER` e categoria `CONSULTA_AVULSA`.
        *   Associa o faturamento unicamente ao `Cliente` (mantendo `processo = null`).
        *   Realiza a liquidação atômica imediata definindo `status = StatusFaturamentoEnum.PAGO` e data de pagamento (se omitida, assume `LocalDate.now()`).
    *   **Resposta (HTTP 201 Created):** `FaturamentoDTO` liquidado.

---

### 2.5. Agenda Unificada (Audiências e Tarefas)

#### Audiências
*   **Listar Agenda Global:** `GET /api/audiencias/agenda`
    *   **Query Parameters:**
        *   `inicio` e `fim` (date: `YYYY-MM-DD`).
        *   `status` (Enum `StatusAudienciaEnum`): `AGENDADA`, `REALIZADA`, `CANCELADA`.
        *   `processoId` (UUID).
        *   `responsavelId` (UUID): Filtro por advogado responsável pela audiência.
*   **Modelo de Retorno do DTO (`AudienciaDTO`):**
    ```json
    {
      "id": "4b2e6f80-...",
      "dataHora": "2026-09-20T14:30:00",
      "local": "1ª Vara Cível de Ijuí",
      "observacoes": "Audiência de Instrução e Julgamento",
      "status": "AGENDADA",
      "resumoPreparatorioIa": null,
      "processo": {
        "id": "8f3b49c1-...",
        "numeroCnj": "5001234-88.2026.8.21.0016",
        "clienteId": "3fa85f64-...",
        "nomeCliente": "Carlos Silva"
      },
      "responsavel": {
        "id": "7b8c9d0e-...",
        "nome": "Dra. Marceli Lausch",
        "email": "marceli@escritorio.com",
        "oab": "RS123456",
        "perfil": "ADVOGADO"
      }
    }
    ```
    *   **Retrocompatibilidade:** `@JsonAlias({"processoId"})` e `@JsonAlias({"responsavelId"})` aceitam IDs crus no envio. O getter `@JsonIgnore responsavelId()` é mantido.
*   **Operações:** `POST /api/audiencias`, `GET /api/audiencias/{id}`, `PUT /api/audiencias/{id}`, `DELETE /api/audiencias/{id}`, `PATCH /api/audiencias/{id}/status?status=REALIZADA`.
*   **Resumo IA da Audiência:** `POST /api/audiencias/{id}/gerar-resumo-ia` com body `{ "conteudoPeca": "..." }`.

#### Tarefas
*   **Listar Agenda de Tarefas:** `GET /api/tarefas/agenda`
    *   **Query Parameters:**
        *   `inicio` e `fim` (date: `YYYY-MM-DD`).
        *   `concluida` (boolean): `true` para concluídas, `false` para pendentes.
        *   `status` (string, alias: `"CONCLUIDA"` ou `"PENDENTE"`).
        *   `tipo` (Enum `TipoTarefaEnum`): `DILIGENCIA`, `PRAZO`, `CONTATO`, `ATENDIMENTO`.
        *   `processoId` (UUID).
        *   `responsavelId` (UUID).
*   **Modelo de Retorno do DTO (`TarefaDTO`):**
    ```json
    {
      "id": "9a8b7c6d-...",
      "descricao": "Atendimento ao Cliente - Reunião Google Meet",
      "dataVencimento": "2026-09-22",
      "concluida": false,
      "tipo": "ATENDIMENTO",
      "googleEventId": "_60q30c1g60o30c1g60o32c1g60o30c1g",
      "usuario": {
        "id": "7b8c9d0e-...",
        "nome": "Dra. Marceli Lausch",
        "email": "marceli@escritorio.com",
        "oab": "RS123456",
        "perfil": "ADVOGADO"
      },
      "processo": {
        "id": "8f3b49c1-...",
        "numeroCnj": "5001234-88.2026.8.21.0016",
        "clienteId": "3fa85f64-...",
        "nomeCliente": "Carlos Silva"
      }
    }
    ```
    *   **Retrocompatibilidade:** `@JsonAlias({"usuarioId"})` e `@JsonAlias({"processoId"})` aceitam IDs crus no envio. O getter `@JsonIgnore usuarioId()` é mantido.
    *   **Sincronização Google Calendar (One-Way):** Compromissos e atendimentos agendados no Google Calendar do advogado são sincronizados automaticamente via webhook para a agenda de tarefas do sistema com `tipo: "ATENDIMENTO"` e o identificador externo `googleEventId`. Na interface, recomenda-se exibir um badge/ícone do Google Calendar para diferenciar esses compromissos de prazos e diligências internas do escritório.
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

### 2.8. Status e Gestão de Integração com Tribunais (Novo Módulo)

#### 2.8.1. Status de Conectividade com Tribunais
Alimenta o badge/indicador "Tribunais Sincronizados" no Topbar e o modal de telemetria de conectividade:
*   **Rota:** `GET /api/integracoes/tribunais/status`
*   **Retorno (`TribunalStatusDTO`):**
    ```json
    {
      "status": "OPERACIONAL",
      "atualizadoEm": "2026-09-14T21:30:00",
      "mensagem": "Todos os serviços judiciais operando com sincronização regular.",
      "tribunaisSincronizados": [
        "TJRS - Tribunal de Justiça do Rio Grande do Sul",
        "TRF4 - Tribunal Regional Federal da 4ª Região",
        "TRT4 - Tribunal Regional do Trabalho da 4ª Região",
        "STJ - Superior Tribunal de Justiça",
        "DataJud - Conselho Nacional de Justiça"
      ]
    }
    ```
*   **Tipagem Forte:** O campo `status` é estritamente tipado com o enum `StatusTribunalEnum` (`OPERACIONAL`, `DEGRADADO`, `INDISPONIVEL`).

#### 2.8.2. Sincronização Sob Demanda de Processo
Permite ao advogado acionar o botão "Sincronizar com Tribunal" diretamente no cabeçalho ou aba de andamentos da tela de Detalhes do Processo (`ProcessDetailsModal.tsx`):
*   **Rota:** `POST /api/integracoes/tribunais/processos/{processoId}/sincronizar`
*   **Headers:** `Authorization: Bearer <token>`
*   **Resposta (HTTP 200 OK - `SincronizacaoProcessoResponseDTO`):**
    ```json
    {
      "processoId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "numeroCnj": "5001234-88.2026.8.21.0016",
      "novasMovimentacoes": 3,
      "status": "SINCRONIZADO",
      "mensagem": "Sincronização concluída com sucesso. 3 novos andamentos foram importados.",
      "dataHora": "2026-09-14T21:35:00"
    }
    ```
*   *(Ação no Front-end: Exibir feedback via toast de sucesso e disparar automaticamente o refetch da lista de andamentos do processo).*

#### 2.8.3. Histórico e Logs de Auditoria de Sincronização
Exibe o histórico de varreduras na aba de integrações ou no histórico do processo:
*   **Rota:** `GET /api/integracoes/tribunais/logs`
*   **Query Parameters:**
    *   `processoId` (UUID, opcional): Filtro por processo específico.
    *   `tribunal` (Enum `TribunalOrigemEnum`, opcional): `TJRS`, `TRF4`, `TRT4`, `STJ`.
    *   `status` (Enum `StatusSincronizacaoLogEnum`, opcional): `SUCESSO`, `FALHA_CONEXAO`, `FALHA_AUTENTICACAO`, `RATE_LIMITED`, `CIRCUITO_ABERTO`.
    *   `page` (int, default = 0), `size` (int, default = 10).
*   **Retorno:** Envelope paginado com `content: List<SincronizacaoLogDTO>`:
    ```json
    {
      "content": [
        {
          "id": "7b8c9d0e-...",
          "processoId": "3fa85f64-...",
          "numeroCnj": "5001234-88.2026.8.21.0016",
          "tribunal": "TJRS",
          "tipoIntegracao": "EPROC_SCRAPER",
          "dataHoraInicio": "2026-09-14T02:00:15",
          "dataHoraFim": "2026-09-14T02:00:18",
          "duracaoMs": 3200,
          "status": "SUCESSO",
          "quantidadeMovimentacoesNovas": 2,
          "mensagemErro": null
        }
      ]
    }
    ```

#### 2.8.4. Gestão de Certificados Digitais A1 (Aba Configurações / Segurança do Advogado)
Permite aos advogados o upload do arquivo `.pfx`/`.p12` para viabilizar consultas autenticadas (mTLS) em tribunais que exigem assinatura:
*   **Listar Certificados do Usuário Logado:** `GET /api/certificados`
    *   **Retorno (`List<CertificadoDigitalDTO>`):**
        ```json
        [
          {
            "id": "2c3d4e5f-...",
            "nomeArquivo": "certificado_advogado.pfx",
            "alias": "DR CARLOS SILVA:12345678900",
            "titular": "CARLOS EDUARDO DA SILVA",
            "cpf": "123.456.789-00",
            "emissor": "AC VALID BRASIL v5",
            "dataEmissao": "2026-01-10",
            "dataExpiracao": "2027-01-10",
            "diasRestantes": 118,
            "ativo": true
          }
        ]
        ```
    *   *(Nota de Segurança: O backend nunca expõe a chave privada nem a senha criptografada).*
*   **Upload de Certificado A1:** `POST /api/certificados/upload` (`multipart/form-data`)
    *   **Form Parameters:**
        *   `arquivo` (MultipartFile, obrigatório): Arquivo com extensão `.pfx` ou `.p12`.
        *   `senha` (string, obrigatório): Senha do certificado informada pelo usuário.
    *   **Retorno (HTTP 201 Created):** `CertificadoDigitalDTO` com dados validados.
*   **Revogar / Excluir Certificado:** `DELETE /api/certificados/{id}` (HTTP 204 No Content).

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

### 2.11. Gestão Eletrônica de Documentos (GED)
Atende às operações de upload, visualização e download de arquivos e peças jurídicas:
*   **Upload de Documento:** `POST /api/documentos/upload` (`multipart/form-data`)
    *   **Form Parameters:**
        *   `arquivo` (MultipartFile, obrigatório): Arquivo PDF, DOCX ou imagem.
        *   `titulo` (string, opcional): Título amigável da peça.
        *   `clienteId` (UUID, opcional): Vínculo direto ao cliente.
        *   `processoId` (UUID, opcional): Vínculo direto ao processo judicial.
*   **Listar por Cliente:** `GET /api/documentos/cliente/{clienteId}`
*   **Listar por Processo:** `GET /api/documentos/processo/{processoId}`
*   **Download de Arquivo:** `GET /api/documentos/{id}/download` (Stream binário `application/octet-stream`).
*   **Excluir Documento:** `DELETE /api/documentos/{id}`
*   **Modelo de Retorno do DTO (`DocumentoDTO`):**
    ```json
    {
      "id": "5c6d7e8f-...",
      "nomeArquivo": "procuracao_assinada.pdf",
      "titulo": "Procuração Ad Judicia",
      "caminhoStorage": "uploads/ged/2026/09/procuracao_assinada.pdf",
      "indexadoIA": true,
      "cliente": {
        "id": "3fa85f64-...",
        "nome": "Carlos Silva",
        "cpfCnpj": "123.456.789-00",
        "tipo": "FISICA"
      },
      "processo": {
        "id": "8f3b49c1-...",
        "numeroCnj": "5001234-88.2026.8.21.0016",
        "clienteId": "3fa85f64-...",
        "nomeCliente": "Carlos Silva"
      }
    }
    ```
    *   **Retrocompatibilidade:** `@JsonAlias({"clienteId"})` e `@JsonAlias({"processoId"})` aceitam IDs crus no envio. Os métodos `@JsonIgnore clienteId()` e `@JsonIgnore processoId()` mantêm compatibilidade nos acessos legados.

---

### 2.12. Integração Comunica PJe / DJEN (Zero Certificado A3)
Módulo público de captura de intimações judiciais e injeção automática de prazos na agenda do advogado:
*   **Sincronização Sob Demanda:** `POST /api/integracoes/pje/sincronizar`
    *   **Headers:** `Authorization: Bearer <token_jwt>`
    *   **Query Params Opcionais:** `dataInicio=YYYY-MM-DD` e `dataFim=YYYY-MM-DD` (default: últimos 3 dias).
    *   **Comportamento:** O backend extrai a OAB cadastrada no perfil do advogado logado (`Usuario.oab`), consulta a API oficial do Comunica PJe com rate limiting defensivo, persiste as publicações em `tb_intimacao_pje`, vincula processos existentes e gera tarefas triadas na agenda.
    *   **Payload de Resposta (`SincronizacaoPjeResultadoDTO`):**
        ```json
        {
          "totalEncontradas": 12,
          "novasIntimacoes": 4,
          "andamentosCriados": 3,
          "numeroOabConsultada": "121837",
          "ufOabConsultada": "RS",
          "mensagem": "Sincronização concluída para OAB 121837/RS. Total consultado: 12, novas intimações: 4, andamentos vinculados: 3.",
          "executadoEm": "2026-09-15T00:05:00"
        }
        ```
*   **Reflexos na Agenda (`AgendaPage.tsx`):**
    *   As intimações geram tarefas com triagem inteligente automática:
        *   `DILIGENCIA`: Pautas de julgamento (`[DILIGÊNCIA - PAUTA] Proc. {cnj} ({tribunal})`).
        *   `PRAZO`: Intimações e citações com prefixos claros de prioridade (`[URGENTE - SENTENÇA]`, `[URGENTE - DECISÃO]`, `[PRAZO - ATO ORDINATÓRIO]`, `[PRAZO - NOTIFICAÇÃO]`, `[PRAZO - ATENÇÃO]`).
        *   *Informativos:* Listas de distribuição e atas de sessão são descartadas da agenda para evitar poluição visual.
*   **Reflexos na Linha do Tempo do Processo (`ProcessDetailsModal.tsx`):**
    *   Os andamentos automáticos vinculados exibem a descrição estruturada e limpa de tags HTML/CSS inline:
        ```text
        [PJe - TJRS] Intimação (DESPACHO/DECISÃO)
        Órgão: 1ª Vara Cível de Ijuí

        Fica intimada a parte autora para manifestação no prazo legal.
        ```

---

## 3. Checklist de Integração e Eliminação de Mocks

| Componente Front-end | Situação Anterior | Integração Efetiva com a API |
| :--- | :--- | :--- |
| `ForgotPasswordModal.tsx` | `setTimeout` simulando envio | `POST /api/auth/recuperar-senha` |
| `SecurityTab.tsx` | `setTimeout` simulando troca de senha | `PATCH /api/auth/me/senha` |
| `OfficeTab.tsx` | State local com dados fictícios | `GET` e `PUT /api/configuracoes/escritorio` |
| `Topbar.tsx` (Notificações) | 4 requisições manuais e filtros no front | `GET /api/notificacoes/resumo` |
| `Topbar.tsx` (Busca) | Campo sem ação vinculada | `GET /api/busca?q=&tipos=&limit=` |
| `Topbar.tsx` (Tribunais) | Texto estático "Tribunais Sincronizados" | `GET /api/integracoes/tribunais/status` (telemetria em tempo real) + `POST /api/integracoes/pje/sincronizar` (sincronização de intimações PJe/DJEN) |
| `ProcessDetailsModal.tsx` | Andamentos cadastrados apenas manualmente | `POST /api/integracoes/tribunais/processos/{processoId}/sincronizar` e andamentos automáticos PJe com descrição estruturada e teor limpo de HTML |
| `ProcessHistoryTab.tsx` | Sem visualização de status de varredura | `GET /api/integracoes/tribunais/logs?processoId=` (histórico e status de auditoria) |
| `CertificatesTab.tsx` | Sem suporte a certificado digital | `GET /api/certificados`, `POST /api/certificados/upload` e `DELETE /api/certificados/{id}` (gestão de certificados A1 ICP-Brasil) |
| `TeamTab.tsx` | Chamava `/usuarios/advogados` e mockava perfil | `GET /api/usuarios?page=0&size=20&ativo=true` |
| `ClientsPage.tsx` | `collectAllPages` e filtro em memória | `GET /api/clientes?q=&tipo=&page=&size=` |
| `ProcessesPage.tsx` | Download de 500 itens e filtro local | `GET /api/processos?q=&fase=&page=&size=` + cadastro/edição com qualificação da lide (`parteAdversa`, `cpfCnpjParteAdversa`, `papelCliente`, `valorCausa`, `comarca`) |
| `FinanceiroPage.tsx` | Paginação e somatórios no navegador | `GET /api/faturamentos?q=&status=&natureza=` + Assistente de Parcelamento (`/parcelamento` com trava matemática), Repasse condicional (`TERCEIRO_SUCUMBENCIA`), Baixa Integral (`/liquidar`), Baixa Parcial com desdobramento (`/liquidar-parcial`) e Repasse (`/repassar`) |
| `EditarFaturamentoModal.tsx` | Lançamento com valor ou dados incorretos sem ação de ajuste | `PUT` e `PATCH /api/faturamentos/{id}` (`EditarFaturamentoDTO` com validação `@Positive` e quitação inteligente automática) |
| `ConsultaAvulsaModal.tsx` | Cobrança de atendimento avulso exigia criação de processo fictício | `POST /api/faturamentos/consulta-avulsa` (`ConsultaAvulsaDTO` com vínculo direto ao cliente, `processo = null` e liquidação atômica) |
| `AgendaPage.tsx` / Tarefas | Compromissos externos do Google Calendar não eram exibidos | Visualização de tarefas sincronizadas com `tipo = "ATENDIMENTO"` (Google Calendar) e prazos processuais automáticos triados (`DILIGENCIA`, `PRAZO` do Comunica PJe) |
| `Dashboard.tsx` | Erro ao ler campos opcionais nulos | `GET /api/dashboard` (DTO com valores padrão garantidos) |