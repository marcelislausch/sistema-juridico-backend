# Auditoria de contratos Frontend → Backend

**Data:** 07/09/2026  
**Escopo:** `src/` do frontend React/TypeScript, confrontado com o OpenAPI local.  
**Fonte de verdade consultada:** `http://localhost:8080/v3/api-docs` (Sistema Jurídico API v1.0).  
**Objetivo:** registrar comportamentos simulados e diferenças comprovadas entre o frontend e o contrato documentado, para orientar a evolução do backend.

> A revisão foi feita contra **37 rotas e 21 schemas** expostos pelo OpenAPI local. Propostas de endpoint marcadas como “ausente no Swagger” não fazem parte do contrato atual; são demandas para o backend decidir e documentar.

## Prioridade de implementação

| Prioridade | Demanda | Situação atual no frontend | Referência |
|---|---|---|---|
| P0 | Recuperação de senha | Simulada com `setTimeout`; mostra sucesso sem enviar e-mail. **Ausente no Swagger.** | `src/components/Login/ForgotPasswordModal.tsx:24` |
| P0 | Alteração de senha | Simulada com `setTimeout`; a senha nunca é persistida nem validada. **Ausente no Swagger.** | `src/components/Settings/SecurityTab.tsx:61` |
| P0 | Configuração do escritório | Formulário é preenchido com dados fictícios fixos e “salvo” localmente. **Ausente no Swagger.** | `src/components/Settings/OfficeTab.tsx:13-34` |
| P1 | Notificações do dia | O frontend reúne e filtra quatro fontes; regra de autorização fica duplicada no navegador. | `src/services/notificacaoService.ts:48-106` |
| P1 | Lista completa de equipe | A tela representa admins, advogados e secretarias, mas consulta só `/usuarios/advogados`. | `src/components/Settings/TeamTab.tsx:22`; `src/services/usuarioService.ts:25` |
| P1 | Busca global e status de tribunais | Campo de busca não executa ação e “Tribunais Sincronizados” é texto fixo. | `src/components/Layout/Topbar.tsx:31-61` |
| P2 | Filtros e paginação no servidor | Algumas telas baixam todas as páginas e filtram no browser. | `ClientsPage.tsx:65-82`, `ProcessesPage.tsx:98-113`, `FinanceiroPage.tsx:73-86` |

---

## 1. Estruturas hoje simuladas que devem vir da API

### 1.1 Recuperação de senha — P0

**Situação no OpenAPI:** não existe rota de recuperação ou redefinição de senha.

Implementar:

```http
POST /api/auth/recuperar-senha
Content-Type: application/json

{ "email": "usuario@escritorio.com.br" }
```

Resposta recomendada: `202 Accepted` sem informar se o e-mail existe, para evitar enumeração de contas.

```json
{ "mensagem": "Se a conta existir, as instruções serão enviadas." }
```

Também é necessário o fluxo de conclusão via token:

```http
POST /api/auth/redefinir-senha

{ "token": "token-de-recuperacao", "novaSenha": "..." }
```

O backend deve definir expiração, uso único e invalidação do token. Enquanto esse endpoint não existir, o modal atual induz o usuário a acreditar que o e-mail foi enviado.

### 1.2 Alteração de senha autenticada — P0

**Situação no OpenAPI:** não existe rota de alteração de senha autenticada.

Implementar uma operação autenticada, por exemplo:

```http
PATCH /api/auth/me/senha
Authorization: Bearer <jwt>

{
  "senhaAtual": "...",
  "novaSenha": "..."
}
```

Resposta recomendada: `204 No Content`; retornar `400` para senha atual inválida e `422` para política de senha não atendida. A política deve ser centralizada no backend; o cliente hoje só exige seis caracteres, embora exiba um medidor mais rigoroso.

### 1.3 Dados institucionais do escritório — P0

**Situação no OpenAPI:** não há schema nem rota de configuração/escritório.

Implementar leitura e atualização:

```http
GET /api/configuracoes/escritorio
PUT /api/configuracoes/escritorio
```

DTO sugerido, usado tanto na resposta quanto no `PUT`:

```json
{
  "razaoSocial": "Cristhian Menezes Sociedade de Advogados",
  "nomeFantasia": "Cristhian Menezes Advocacia",
  "cnpj": "34567890000112",
  "registroOabSociedade": "OAB/SP nº 42.890",
  "telefone": "1134567890",
  "whatsapp": "11987654321",
  "email": "contato@escritorio.com.br",
  "cep": "01310100",
  "logradouro": "Avenida Paulista",
  "numero": "1842",
  "complemento": "Conjunto 142",
  "bairro": "Bela Vista",
  "cidade": "São Paulo",
  "uf": "SP"
}
```

Regras importantes:

- Guardar documentos e telefones sem máscara; o frontend já remove máscara em alguns fluxos e deve fazê-lo também aqui.
- A entidade deve ser única por escritório/tenant, não associada ao usuário que editou.
- `EscritorioConfig` atual está incompleto e divergente: tem `oabSecao`, mas a tela usa `oabSociedade`; não possui `whatsapp` e não é utilizada (`src/types/usuario.ts:13`). Substituí-lo por um DTO canônico próprio, por exemplo `EscritorioDTO`.

### 1.4 Central de notificações do dia — P1

**Situação no OpenAPI:** não existe rota de notificações. A implementação atual é inteiramente uma composição no frontend, apesar de usar dados reais das rotas de agenda e faturamento.

Hoje o navegador faz requisições para audiências, tarefas, processos e faturamentos, decide permissões por perfil e monta os objetos de notificação. Isso gera custo, resultados incompletos por paginação e regras de autorização duplicadas.

Implementar:

```http
GET /api/notificacoes/resumo?data=2026-09-07
Authorization: Bearer <jwt>
```

O usuário deve ser obtido exclusivamente do token no backend. Resposta que preserva a UI atual:

```json
{
  "notificacoes": [
    {
      "id": "uuid",
      "tipo": "AUDIENCIA",
      "titulo": "Audiência agendada",
      "descricao": "Sala 2",
      "horario": "14:00",
      "destino": "audiencias",
      "recursoTipo": "AUDIENCIA",
      "recursoId": "uuid"
    }
  ],
  "quantidadeAgenda": 1,
  "quantidadeFinanceiro": 0
}
```

`tipo` deve aceitar `AUDIENCIA | TAREFA | FINANCEIRO`; `destino` pode ser mantido temporariamente para a navegação atual. Incluir `recursoTipo` e `recursoId` permite posteriormente abrir o item correto, em vez de apenas navegar para uma página.

### 1.5 Equipe/usuários — P1

**Situação no OpenAPI:** existem somente `POST /api/usuarios`, `GET /api/usuarios/{id}` e `GET /api/usuarios/advogados`; `GET /api/usuarios` não está documentado.

Criar `GET /api/usuarios` ou `GET /api/usuarios/equipe` paginado. O endpoint atual `/api/usuarios/advogados` é adequado para *selects* de responsável, mas não para a tela “Equipe & Usuários”, que inclui `ADMIN`, `ADVOGADO` e `SECRETARIA`.

```http
GET /api/usuarios?page=0&size=20&ativo=true&sort=nome,asc
```

O retorno precisa expor `id`, `nome`, `email`, `perfil`, `oab` e `ativo`. A tela atualmente sempre mostra “Ativo”, ignorando `UsuarioDTO.ativo` (`src/components/Settings/TeamTab.tsx:132`). Se a administração de equipe estiver no escopo, prever também atualização, ativação/desativação e, conforme regra de negócio, redefinição de senha administrativa.

### 1.6 Busca global e sincronização de tribunais — P1

**Situação no OpenAPI:** não existem rotas para busca global nem para a integração/status de tribunais.

Não existe chamada para o campo de busca do topo e o texto “Tribunais Sincronizados” é estático. Se ambas as promessas permanecerem no produto, implementar:

```http
GET /api/busca?q=<texto>&tipos=PROCESSO,CLIENTE,USUARIO&limit=10
GET /api/integracoes/tribunais/status
```

O primeiro deve devolver itens tipados, com `id`, `tipo`, `titulo`, `subtitulo` e rota/recurso. O segundo deve retornar ao menos `status`, `atualizadoEm` e eventual mensagem de indisponibilidade. Caso não entrem no escopo, remover/ocultar essas indicações no frontend.

---

## 2. Contratos existentes confirmados pelo Swagger

### Convenções transversais

- Base URL configurada por `VITE_API_URL`, com fallback `/api` (`src/services/api.ts:1`).
- Todas as rotas autenticadas recebem `Authorization: Bearer <token>`.
- Listas paginadas de clientes, processos e faturamentos são documentadas no formato Spring `Page`: `content`, `totalElements`, `totalPages`, `size`, `number`, `first`, `last` e `pageable`.
- O OpenAPI documenta um parâmetro obrigatório `pageable` nesses três endpoints, enquanto o frontend envia `page`, `size` e `sort`. Em Spring isso normalmente funciona, mas a especificação deve expor esses três parâmetros individualmente para refletir a chamada real.
- Erros devem ter ao menos uma das chaves `message`, `error`, `mensagem`, `msg` ou `detail`; a preferência é padronizar em `{ "message": "...", "code": "...", "fieldErrors": [] }`.
- IDs são tratados como `string`; dashboard e ações rápidas pressupõem UUID. Confirmar UUID como padrão para **todas** as entidades.
- Datas sem hora são esperadas como `YYYY-MM-DD`; data/hora como ISO-8601. Definir timezone do escritório e devolver timestamps com offset (`2026-09-07T14:00:00-03:00`) ou UTC (`Z`) de forma consistente.

### Rotas já consumidas e presentes no Swagger

| Domínio | Chamadas esperadas pelo frontend |
|---|---|
| Autenticação | `POST /auth/login`, `GET /auth/me` |
| Clientes | `POST/GET /clientes`, `GET/PUT /clientes/{id}`, `GET /clientes/{id}/procuracao`, `GET /clientes/{id}/contrato-honorarios` |
| Processos | `POST/GET /processos`, `GET/PUT /processos/{id}`, `GET /processos/cliente/{clienteId}`, `PATCH /processos/{id}/arquivar`, `PATCH /processos/{id}/desarquivar`, `GET/POST /processos/{id}/andamentos` |
| Audiências | `POST /audiencias`, `GET /audiencias/agenda`, `GET /audiencias/processo/{id}`, `GET/PUT/DELETE /audiencias/{id}`, `PATCH /audiencias/{id}/status`, `POST /audiencias/{id}/gerar-resumo-ia` |
| Tarefas | `POST /tarefas`, `PUT/DELETE /tarefas/{id}`, `PATCH /tarefas/{id}/concluir`, `GET /tarefas/agenda` |
| Financeiro | `GET/POST /faturamentos`, `PATCH /faturamentos/{id}/pagar`, `GET /faturamentos/resumo`, `GET /faturamentos/processo/{id}` |
| Documentos | `POST /documentos/upload`, `GET /documentos/processo/{id}`, `GET /documentos/cliente/{id}`, `GET /documentos/{id}/download`, `DELETE /documentos/{id}` |
| Usuários | `POST /usuarios`, `GET /usuarios/{id}`, `GET /usuarios/advogados` |
| Dashboard/IA | `GET /dashboard/{usuarioId}`, `POST /ia/resumos/audiencia` |

### Ausências confirmadas no contrato publicado

Não há no OpenAPI: recuperação/redefinição/alteração de senha, configuração do escritório, notificações, busca global, status de integração com tribunais, listagem paginada de todos os usuários, atualização/ativação de usuário e endpoint de edição do perfil autenticado.

As rotas documentadas também só descrevem respostas genéricas `200 OK`, sem erros `400`, `401`, `403`, `404`, `409` ou `422`. Esses cenários precisam entrar na especificação para o frontend tratar erros de domínio de forma confiável.

---

## 3. DTOs e possíveis divergências a resolver

| Domínio/DTO | O que o frontend pressupõe | Decisão necessária no backend |
|---|---|---|
| `TokenDTO` | O Swagger confirma `{ token: string }` para o login e `LoginDTO` com `email`, `senha`, `manterConectado?`. | O frontend está aderente. Se houver refresh token ou expiração, adicioná-los deliberadamente ao contrato; não renomear `token` sem migrar o cliente. |
| `UsuarioDTO` | O schema atual confirma `id?`, `nome`, `email`, `senha?`, `perfil`, `oab?`, `ativo?`. | O mesmo `UsuarioDTO` é usado como resposta de `GET /auth/me` e `GET /usuarios/{id}` e expõe o campo `senha` na documentação. Criar `CriarUsuarioRequest`, `UsuarioResponse` e `AtualizarUsuarioRequest`; `senha` não pode aparecer em nenhuma resposta. |
| `ClienteDTO` | Endereço e dados civis são campos planos; CPF/CNPJ é enviado sem máscara no drawer principal. | Confirmar enum `FISICA | JURIDICA`, campos aplicáveis por tipo e normalização de CPF/CNPJ, CEP e telefone. Se existir representante legal de PJ, falta estrutura no DTO. |
| `ProcessoDTO` | `numeroCnj`, `assunto`, `faseAtual`, `dataCriacao`, `clienteId`, `advogadoId`. | Há dois conceitos aparentes de arquivamento: `faseAtual = ARQUIVADO` e query `arquivado=true/false`. Definir uma única fonte de verdade e expor filtros de fase no servidor. |
| `AudienciaDTO` | O Swagger contém apenas `id`, `dataHora`, `local`, `observacoes`, `status`, `resumoPreparatorioIa` e `processoId`. O tipo do frontend acrescenta `usuarioId`, `responsavelId` e `advogadoId`. | **Divergência confirmada:** remover os três campos do frontend ou adicionar um único `responsavelId` (recomendado) ao schema e às respostas do backend. O comentário do tipo diz UTC (`Z`), mas o formulário envia data local sem timezone (`YYYY-MM-DDTHH:mm:ss`). Padronizar. |
| `TarefaDTO` | `usuarioId` obrigatório, `processoId?`, `concluida?`, `tipo?`. | Definir se conclusão deve ser alterável por `PUT` ou apenas por ação; no aninhamento por processo o backend pode derivar `processoId`, evitando duplicação no corpo. Prever filtro por responsável, situação, tipo e período. |
| `FaturamentoDTO` | `valor: number`, enum de tipo/natureza/status e `processoId?`. | Confirmar escala monetária/decimal e se o lançamento deve trazer dados resumidos do processo/cliente para listagem e busca. O cliente hoje busca globalmente para filtrar texto. |
| `DocumentoDTO` | O schema publicado confirma somente `id`, `nomeArquivo`, `titulo`, `caminhoStorage`, `indexadoIA`, `clienteId` e `processoId`. | O frontend está aderente. Para evoluir o módulo, incluir `mimeType`, `tamanhoBytes`, `criadoEm`, `criadoPorId` e, se pertinente, URL/tempo de expiração. Nunca expor caminho interno de storage como contrato público. |
| `AndamentoDTO` | Em `POST /processos/{id}/andamentos` envia também `processoId` e `dataHora` gerada no cliente. | O backend deve derivar `processoId` da rota e registrar `dataHora` no servidor; definir se `AUTOMATICO` e `IA` podem ser criados por usuários. |
| `ResumoDashboardDTO` | O Swagger confirma os oito campos esperados, porém todos opcionais; o frontend assume zero/lista vazia quando ausentes. | Devolver campos não nulos: números como `0` e coleções como `[]`. Definir se o usuário vem do token, permitindo migrar de `/dashboard/{usuarioId}` para `GET /dashboard`. |
| `NotificacaoDTO` | É DTO de tela criado no frontend, não proveniente da API. | Adotar o contrato da seção 1.4 ou retirar esse DTO do frontend quando a API fornecer um modelo definitivo. |
| `EscritorioConfig` | Tipo isolado, incompleto e sem uso. | Substituir pelo `EscritorioDTO` da seção 1.3 para que modelo, tela e API usem os mesmos nomes. |

### Observações de comportamento que afetam o contrato

1. O Swagger define `AudienciaDTO.dataHora` como `date-time`, mas não determina offset/timezone; o tipo do frontend comenta UTC e o formulário envia data local sem offset (`src/components/Dashboard/CreateHearingDrawer.tsx:161-170`). Documentar e devolver sempre um padrão único para evitar deslocamento por fuso.
2. O Swagger também deixa `status` opcional em audiência e faturamento; o frontend assume respectivamente `AGENDADA`/`PENDENTE`. O backend deve sempre devolvê-lo em respostas de leitura.
3. O Swagger confirma `termoBusca` em clientes/processos, `arquivado` em processos e `status`/`natureza` em faturamentos. Ainda faltam filtros documentados para `tipo`, `fase`, intervalo e busca de faturamentos.
4. O Swagger confirma a assinatura atual de `POST /documentos/upload`: arquivo multipart chamado `arquivo`; `titulo` obrigatório e `clienteId`/`processoId` opcionais na query string. É compatível com o frontend; como melhoria futura, os metadados podem migrar para partes multipart explícitas.
5. O Swagger confirma que procuração e contrato são `GET` com parâmetros de dados pessoais em query string e resposta binária. Para reduzir exposição em URL/logs, considerar uma versão `POST` com body e resposta binária.

---

## 4. O que pode deixar de ser filtrado/agregado no frontend

Para o volume crescer sem baixar centenas de itens para o navegador, adicionar filtros aos endpoints de listagem:

| Recurso | Filtros/pesquisa recomendados |
|---|---|
| `GET /clientes` | `q`, `tipo`, `page`, `size`, `sort` e, se as métricas forem necessárias, `GET /clientes/resumo` com total/PF/PJ. |
| `GET /processos` | `q`, `fase`, `arquivado` (se mantido), `clienteId`, `advogadoId`, `page`, `size`, `sort`. |
| `GET /audiencias/agenda` | `inicio`, `fim`, `status`, `processoId`, `responsavelId`; retorno ordenado por `dataHora`. |
| `GET /tarefas/agenda` | `inicio`, `fim`, `status`, `tipo`, `processoId`, `responsavelId`; deixar o backend aplicar autorização. |
| `GET /faturamentos` | `q`, `status`, `natureza`, `tipo`, `vencimentoDe`, `vencimentoAte`, `processoId`, `page`, `size`, `sort`. |

Isso elimina padrões como `size=500`, `collectAllPages(...)` e filtro por texto/data no browser.

---

## 5. Duplicações e riscos de manutenção no frontend

- Há dois formulários de cliente (`CreateClientDrawer` e `CreateClientModal`) e dois de usuário (`CreateUserDrawer` e `CreateUserModal`). Os modais não estão importados/usados. Mesmo inativos, eles mantêm payloads paralelos e podem divergir do contrato real.
- `processoService.listarAdvogados()` e `usuarioService.listarAdvogados()` chamam a mesma rota, porém retornam tipos diferentes (`AdvogadoDTO` e `UsuarioDTO`). Unificar o DTO de usuário resumido para selects.
- O dashboard consulta `/dashboard/{usuarioId}` **e** separadamente agenda/processos recentes. Definir se o dashboard é o agregado oficial; se for, ele deve retornar todos os blocos necessários e as chamadas extras devem ser removidas.
- `dashboardService` troca erro/ID inválido por um dashboard com zeros. Isso não é mock de conteúdo, mas mascara indisponibilidade do backend como “não há dados”; o endpoint deve ser confiável e a UI deve receber o erro.

## Critério de aceite para o backend

1. Publicar OpenAPI 3 com enums, campos obrigatórios, formatos de data, paginação e respostas de erro.
2. Implementar os três fluxos P0 sem retornos simulados no cliente.
3. Retornar DTOs completos, com campos não nulos onde a tela exige valor padrão.
4. Centralizar autorização de dashboard, notificações, agenda e financeiro no backend a partir do JWT.
5. Confirmar os nomes de query params e os contratos de upload/download em testes de integração.
6. Após a confirmação, o frontend deve separar `Create...Request` de `...Response` e remover os componentes duplicados e os fallbacks que fingem sucesso.
