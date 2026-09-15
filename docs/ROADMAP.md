# 🗺️ Roadmap & Ciclo de Vida do Sistema Jurídico

Este documento consolida o **cérebro externo** e o estado de maturidade arquitetural do backend do **Sistema Jurídico Inteligente**, servindo de fonte única de verdade para alinhamento entre sessões de desenvolvimento, auditorias técnicas e planejamento de evolução contínua.

---

## 🏛️ Visão Geral da Arquitetura & Stack Tecnológica

- **Linguagem & Plataforma:** Java 21 (LTS)
- **Framework Principal:** Spring Boot 3.3.3
- **Padrão Arquitetural:** Clean Architecture (Hexagonal) — segregação em `core` (domain, usecases, service), `infrastructure` (persistence, security, storage, ai, document) e `presentation` (controllers, dtos).
- **Paradigma Adotado:** **Imperativo Puro** (Zero Lambdas, Zero Streams API, checagens clássicas de `Optional` e laços de iteração clássicos para máxima previsibilidade e legibilidade técnica).
- **Banco de Dados:** PostgreSQL 15+ (Consultas otimizadas em SQL Nativo com binding tipado de parâmetros).
- **Inteligência Artificial:** Spring AI (OpenAI Starter conectado aos modelos Google Gemini via Google AI Studio).
- **Nuvem & Armazenamento:** Google Drive API v3 (OAuth 2.0) com fallback para armazenamento local.
- **Ambiente de Produção & DevOps:** VPS Ubuntu LTS (Oracle Cloud), Nginx com SSL Let's Encrypt, firewall UFW, Fail2Ban, CI/CD via GitHub Actions e gerenciamento de serviço systemd.

---

## 1. ✅ Concluído (Em Produção)

Módulos, fluxos e infraestruturas 100% implementados, testados, blindados contra falhas de segurança e em operação no ambiente de produção.

### 1.1. Armazenamento e Gestão Eletrônica de Documentos (GED)
- **Google Drive Storage Service:** Integração com a API do Google Drive (v3) utilizando credenciais OAuth 2.0 (`clientId`, `clientSecret`, `refreshToken`), salvando arquivos na pasta remota do escritório com persistência do File ID retornado.
- **Fallback Local:** Implementação de `LocalStorageService` para contingência e desenvolvimento local na pasta `uploads/documentos`.
- **Blindagem de Uploads:** Validação rigorosa de tamanho (limite de 10 MB via `spring.servlet.multipart`), validação imperativa de extensões autorizadas e sanitização de MIME types (`.pdf`, `.docx`, `.doc`, `.png`, `.jpg`, `.jpeg`).
- **Ciclo de Vida Documental:** Endpoints REST documentados para upload multipart (`POST /api/documentos/upload`), download com `MediaType` dinâmico (`GET /api/documentos/{id}/download`), listagem por cliente ou processo e exclusão física/lógica sincronizada (`DELETE /api/documentos/{id}`).

### 1.2. Consultas Avançadas em SQL Nativo (PostgreSQL)
- **Descarte de CriteriaBuilder:** Adoção unificada de consultas declarativas com `@Query(value = "...", countQuery = "...", nativeQuery = true)` nos repositórios JPA (`ProcessoRepository`, `ClienteRepository`, `FaturamentoRepository`, `TarefaRepository`, etc.).
- **Resolução da Anomalia `lower(bytea) does not exist`:** Aplicação sistemática de casting explícito de tipos nos parâmetros anuláveis (`CAST(:termo AS text)`, `CAST(:fase AS text)`, `CAST(:clienteId AS uuid)`, `CAST(:vencimentoDe AS date)`, `CAST(:arquivado AS boolean)`), prevenindo erros de tipagem do driver JDBC do PostgreSQL em filtros dinâmicos.
- **Paginação Padronizada no Servidor:** Paginação nativa com `Pageable`, serialização uniforme configurada via `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` (`WebConfig`) e parâmetros planos documentados no Swagger com `@ParameterObject`.

### 1.3. Documentação OpenAPI 3 (Swagger) & Tratamento Global de Exceções
- **Cobertura Integral dos Controllers:** Todos os 15+ controladores REST e suas dezenas de rotas foram formalmente documentados com anotações `@Tag`, `@Operation`, `@ParameterObject` e matriz completa de `@ApiResponses` (200, 201, 204, 400, 401, 403, 404, 409, 422).
- **Tratamento Centralizado de Erros:** `GlobalExceptionHandler` (`@RestControllerAdvice`) padronizando o payload JSON de qualquer exceção capturada:
  - `ErroPadraoDTO`: Estrutura canônica contendo `timestamp`, código HTTP `status`, `error` e `message` amigável (para `RecursoNaoEncontradoException`, `RegraNegocioException`, `BadCredentialsException`, `AccessDeniedException` e violações de integridade).
  - `ErroValidacaoDTO`: Especialização contendo lista de `CampoErroDTO` (`campo`, `mensagem`) para falhas de validação de payload `@Valid` (`MethodArgumentNotValidException`).

### 1.4. Autenticação, IAM & Blindagem de Segurança
- **JWT Stateless com Expiração Dinâmica:** Assinatura HMAC256 (`TokenService`) com suporte ao parâmetro opcional `manterConectado: boolean` no login (estendendo a sessão de 2 horas para 7 dias).
- **Endpoint de Sessão Ativa:** `GET /api/auth/me` para recuperação atômica do perfil do usuário autenticado a partir do token.
- **Blindagem de Credenciais (Security by Design):** Segregação entre DTOs de entrada (`CriarUsuarioRequest`, `AtualizarUsuarioRequest`) e saída (`UsuarioResponseDTO`), garantindo que o atributo `senha` ou seu hash BCrypt nunca seja exposto em requisições ou respostas da API.
- **Recuperação e Redefinição de Senha:** 
  - Solicitação via `POST /api/auth/recuperar-senha` com envio de e-mail formatado via SMTP Google (Gmail SSL/465). Geração atômica de token único na tabela `tb_password_reset_token` (expiração de 15 minutos), com resposta `202 Accepted` e política anti-enumeração de contas.
  - Consumo via `POST /api/auth/redefinir-senha` com validação de uso prévio, expiração e atualização segura de hash de senha.
- **Alteração de Senha Autenticada:** Rota segura `PATCH /api/auth/me/senha` com validação obrigatória da senha anterior antes de gravar o novo hash.
- **Controle de Acesso Baseado em Papéis (RBAC):** Proteção granular de rotas via `@PreAuthorize` para os perfis `ADMIN`, `ADVOGADO` e `SECRETARIA`.
- **Tratamento de Constraint de Unicidade:** Persistência defensiva do campo `oab` como `null` quando enviado em branco, prevenindo violação de constraint única no PostgreSQL para múltiplos usuários sem OAB.
- **Auditoria de Entidades:** Rastreabilidade de criação e modificação (`criadoEm`, `atualizadoEm`, `criadoPor`, `atualizadoPor`) via `AuditableEntity` e `AuditorAwareImpl`.

### 1.5. Pipeline de CI/CD & Automação DevOps
- **GitHub Actions (`deploy.yml`):** Esteira automatizada de entrega contínua acionada em pushes para o branch `master`.
- **Build Externo (Zero Sobrecarga de Servidor):** Compilação do `.jar` executada nos runners do GitHub (`mvn clean package -DskipTests` com Java 21 Temurin), poupando memória e CPU da VPS.
- **Deploy Seguro com SCP e SSH:** Cópia remota do artefato para `/opt/sistema-juridico/` e reinicialização segura do daemon `sistema-juridico.service` sob usuário restrito de sistema.
- **Hardening de Produção:** Nginx configurado com proxy reverso, SSL Let's Encrypt (HTTPS), rate limiting contra força-bruta nas rotas de autenticação, firewall UFW e bloqueio de endpoints perigosos do Actuator (apenas `/health` e `/info` liberados).

### 1.6. Gestão Cadastral e Configuração do Escritório
- **Tenant Institucional Centralizado:** Leitura (`GET /api/configuracoes/escritorio`) e atualização (`PUT /api/configuracoes/escritorio`) dos dados da banca (Razão Social, Nome Fantasia, CNPJ de 14 dígitos, Registro OAB, telefones, WhatsApp, e-mail e endereço físico completo).
- **Sanitização Imperativa de Strings:** Higienização de máscaras de pontuação de documentos e CEP antes da persistência.
- **Carga Inicial Autônoma:** `DatabaseSeeder` provisionando os dados oficiais do escritório e o usuário administrador padrão caso as tabelas estejam vazias.

### 1.7. CRM & Gestão de Clientes
- **Qualificação Civil Completa:** Cadastro e atualização de pessoas físicas e jurídicas (`FISICA`, `JURIDICA`), gênero (`MASCULINO`, `FEMININO`), estado civil, profissão, data de nascimento e endereço completo.
- **Validação de Documentos:** `DocumentoValidator` aplicando algoritmos oficiais de validação de dígito verificador para CPF e CNPJ.
- **Listagem Paginada Dinâmica:** Filtros avançados combinados por tipo e busca textual (`?q=` ou `?termoBusca=`).

### 1.8. Emissão Automatizada de Peças e Documentos em PDF
- **Motor iText Oficial (`PdfDocumentGeneratorService`):** Geração dinâmica de documentos jurídicos com tipografia padronizada em fonte Bookman Old Style embutida (`BaseFont.EMBEDDED`), cabeçalho e rodapé institucionais automáticos via eventos de página (`PdfPageEventHelper`).
- **Procuração Ad Judicia & AJG:** Emissão via `GET /api/clientes/{id}/procuracao` com interpolação de dados do cliente, poderes forenses e inclusão condicional da folha de Declaração de Hipossuficiência para Assistência Judiciária Gratuita.
- **Contrato de Honorários Advocatícios:** Emissão via `GET /api/clientes/{id}/contrato-honorarios` com injeção dinâmica de objeto, valor pactuado, comarca, vara e tratamento gramatical de concordância por sexo do contratante.

### 1.9. Processos Judiciais & Andamentos
- **Ciclo de Vida Processual:** Abertura, edição cadastral, atualização de fase processual (`INICIAL`, `INSTRUCAO`, `DECISAO`, `RECURSAL`, `EXECUCAO`, `ARQUIVADO`) e número CNJ unívoco.
- **Regras de Negócio de Arquivamento:** Verificação defensiva de débitos financeiros pendentes antes de autorizar o arquivamento (`PATCH /api/processos/{id}/arquivar`), com possibilidade de desarquivamento (`PATCH /api/processos/{id}/desarquivar`).
- **Linha do Tempo de Andamentos:** Registro cronológico de movimentações com classificação de origem (`AUTOMATICO`, `MANUAL`, `IA`).
- **[X] CONCLUÍDO - Ajustes na Gestão de Processos (Parte Adversa/Valor):**
  - Mapeamento e persistência da qualificação da lide: `parteAdversa`, `cpfCnpjParteAdversa`, `papelCliente` (Enum `PapelClienteEnum`: `AUTOR`, `REU`, `TERCEIRO_INTERESSADO`), `valorCausa` com precisão decimal (`BigDecimal(15,2)`) e `comarca`.
  - Propagação completa em DTOs (`ProcessoDTO`), mappers imperativos e Casos de Uso (`CadastrarProcessoUseCase`, `AtualizarProcessoUseCase`).

### 1.10. Financeiro, Fluxo de Caixa e Faturamento
- **Controle Bipolar de Lançamentos:** Gestão de receitas e despesas pela natureza (`A_RECEBER`, `A_PAGAR`) e classificação de despesa/honorários (`HONORARIOS`, `CUSTAS`, `DESPESAS_ESCRITORIO`).
- **Liquidação com Registro Histórico:** Quitação de títulos com data de pagamento obrigatória (`PATCH /api/faturamentos/{id}/pagar`) e controle de status (`PENDENTE`, `PAGO`, `CANCELADO`).
- **Resumo Financeiro Executivo:** Endpoint `GET /api/faturamentos/resumo` com cálculo em tempo real de saldo previsto, valores recebidos, a pagar e taxa de adimplência/inadimplência.
- **Filtros Financeiros Multicritério:** Listagem paginada no servidor cruzando intervalo de datas, status, natureza, tipo e vínculo com processo.
- **[X] CONCLUÍDO - Evolução do Módulo Financeiro (Parcelamentos, Liquidação Parcial e Repasses):**
  - **Assistente de Parcelamento Inteligente (`POST /api/faturamento/parcelamento`):** Criação e persistência em lote de lançamentos financeiros decorrentes da simulação de parcelas, via `GerarParcelamentoUseCase` em laços imperativos puros, com vínculo de numeração (`numeroParcela`, `totalParcelas`) e amarração com o processo judicial.
  - **Baixa Integral Expressa (`PATCH /api/faturamento/{id}/liquidar`):** Quitação total com registro de data de pagamento efetiva (`dataPagamento`) e transição de status para `PAGO` via `LiquidarFaturamentoUseCase`.
  - **Baixa Parcial com Desdobramento de Saldo (`PATCH /api/faturamento/{id}/liquidar-parcial`):** Recebimento parcial onde o título original é quitado no valor pago (`PAGO`) e o saldo remanescente é automaticamente clonado/desdobrado em uma nova fatura com status `PENDENTE` e nova data de vencimento via `LiquidarParcialFaturamentoUseCase`.
  - **Gestão de Repasses Sucumbenciais a Clientes (`PATCH /api/faturamento/{id}/repassar`):** Gestão condicional de receitas com origem `TERCEIRO_SUCUMBENCIA`, apuração de retenção de honorários (`valorHonorariosRetidos`) e saldo do cliente (`valorRepasseCliente`), controle de status (`statusRepasse: PENDENTE | REPASSADO`), forma de repasse e liquidação via `RepassarFaturamentoUseCase`.
  - **Padronização de Precisão Decimal:** Blindagem contábil de todas as colunas monetárias (`BigDecimal`) com `@Column(precision = 15, scale = 2)`.
  - **Validação OpenAPI Estrita:** Isolamento das anotações `@Valid` nos contratos OpenAPI (`FaturamentoControllerOpenApi`), mantendo a implementação concreta em conformidade com as regras do Bean Validation (`HV000151`).

### 1.11. Produtividade: Agenda, Tarefas, Notificações e Dashboard
- **Tarefas e Prazos Processuais:** CRUD completo de tarefas (`DILIGENCIA`, `PRAZO`, `CONTATO`), vinculadas a usuários e processos, com controle de conclusão (`PATCH /api/tarefas/{id}/concluir`) e consulta por período (`GET /api/tarefas/agenda`).
- **Agenda de Audiências:** Cadastro com validação impeditiva de datas passadas, vínculo de responsável, pauta harmonizada por período em `LocalDate` (`GET /api/audiencias/agenda`) e cancelamento/realização de pauta.
- **Central de Notificações do Dia:** Endpoint consolidado `GET /api/notificacoes/resumo?data=YYYY-MM-DD` que centraliza audiências do dia, prazos fatais de tarefas e faturamentos a receber vencidos para o usuário logado via JWT.
- **Dashboard do Advogado:** `GET /api/dashboard` com agregações métricas em tempo real, blindado por construtor defensivo que anula referências nulas para garantir estabilidade no frontend.
- **Busca Global Multidomínio:** `GET /api/busca` com varredura concorrente em Processos, Clientes e Usuários.
- **[X] CONCLUÍDO - Inteligência Artificial para Resumo de Audiências (Dois Estágios & Chunking):**
  - Integração com `gemini-3.6-flash` via Spring AI OpenAI Starter conectado ao Google AI Studio.
  - Engenharia de prompt especializada estruturada em seções táticas: Fatos Incontroversos, Fatos Controvertidos, Riscos Processuais/Preliminares, Roteiro de Perguntas e Parâmetros de Acordo.
  - Resposta tipada e estrita via `ResumoAudienciaEstruturadoDTO` (`core/domain/dto`) utilizando `BeanOutputConverter` do Spring AI.
  - Ingestão direta de PDFs dos autos via Apache PDFBox (`PdfExtractionService`) e `GoogleDriveStorageService` em laços imperativos clássicos, eliminando recorte e colagem manual de petições.
  - Controle de janela de contexto com `TextoChunkingService` (blocos de até 18.000 caracteres respeitando quebras de parágrafo e pontuações) e síntese progressiva em dois estágios no `GerarResumoAudienciaUseCase` e `GerarEAnexarResumoAudienciaUseCase` (resumo de chunks individuais e síntese tática final consolidada).

### 1.12. Integração Google Calendar (Sincronização One-Way via Webhooks Push)
- **Arquitetura Estritamente One-Way:** O sistema opera como receptor passivo de eventos da Google Calendar API v3 (Google -> Sistema Jurídico). Tarefas, audiências e prazos internos nunca são transmitidos para os servidores do Google.
- **Canal de Webhooks:** Endpoint dedicado `POST /api/integracoes/google-calendar/webhook` para recepção assíncrona de notificações push (headers `X-Goog-Resource-State`, `X-Goog-Channel-ID`, `X-Goog-Message-Number`).
- **Mapeamento Unificado de Domínio como Tarefas:** Compromissos externos são transformados em instâncias de `Tarefa` (`tb_tarefa`) associadas ao advogado (`Usuario`) com tipo `TipoTarefaEnum.ATENDIMENTO`.
- **Idempotência & Versionamento (`googleEventId`):** Coluna `google_event_id VARCHAR(255)` indexada em `tb_tarefa`. Lógica imperativa de upsert via `SincronizarEventoGoogleCalendarUseCase`: se existir, atualiza dados ou trata exclusão; se inédito, cria nova tarefa, prevenindo duplicações por disparos múltiplos.
- **Unificação OAuth 2.0 (`GoogleOAuthTokenManager`):** Componente centralizado em `infrastructure/security` que renova automaticamente Access Tokens para Google Drive e Google Calendar sem duplicidade de código.

### 1.13. Lançamento e Liquidação Atômica de Consultas Avulsas
- **Desvinculação Processual:** O relacionamento entre a entidade `Faturamento` e `Processo` tornou-se opcional (`processo_id` anulável em `tb_faturamento`), vinculando-se unicamente à entidade `Cliente` (`cliente_id`).
- **Novo Tipo de Faturamento:** Mapeamento do valor `TipoFaturamentoEnum.CONSULTA_AVULSA`.
- **Transação Atômica via Use Case (`RegistrarConsultaAvulsaUseCase`):**
  - Endpoint dedicado `POST /api/faturamentos/consulta-avulsa` (com suporte retrocompatível a `/api/faturamento/consulta-avulsa`).
  - Criação do faturamento com natureza `A_RECEBER`, tipo `CONSULTA_AVULSA`, `processo = null`, vínculo direto com `Cliente`, status `PAGO` e data de pagamento registrada na mesma transação sob `@Transactional`.
  - Elimina etapas manuais intermediárias de criação seguida de liquidação, garantindo consistência contábil instantânea.

### 1.14. Edição Cadastral de Lançamentos Financeiros & Unificação de Rotas
- **Endpoints Oficiais de Edição:** Suporte completo a `PUT /api/faturamentos/{id}` e `PATCH /api/faturamentos/{id}` (com alias `/api/faturamento/{id}`).
- **Eliminação de Duplicidades:** Unificação definitiva da nomenclatura das rotas financeiras, removendo inconsistências entre `/faturamento` e `/faturamentos`.
- **Edição Defensiva via `EditarFaturamentoUseCase`:**
  - Alteração flexível de valor, descrição, vencimento, categoria/tipo, status, natureza, data de pagamento e reatribuição de processo sem sobrescrever campos omitidos com nulo.
  - Validação estrita de valor estritamente positivo com Bean Validation `@Positive`.
  - Regra de transição inteligente: se o status for alterado para `PAGO` sem envio explícito da data de quitação, o sistema atribui automaticamente a data corrente (`LocalDate.now()`).

### 1.15. Segregação de Contratos OpenAPI 3, DTOs Aninhados e Blindagem UTF-8
- **Isolamento de Contratos OpenAPI:** 16 interfaces segregadas no pacote `presentation.openapi` (`*OpenApi.java`), isolando as anotações `@Tag`, `@Operation`, `@ApiResponses` e prevenindo o erro `HV000151` de validação em métodos sobrescritos.
- **DTOs de Resumo Aninhados:** Substituição de IDs planos por objetos resumidos (`ProcessoResumoDTO`, `ClienteResumoDTO`, `UsuarioResumoDTO`) em `ProcessoDTO`, `FaturamentoDTO`, `AudienciaDTO`, `TarefaDTO` e `DocumentoDTO`.
- **Retrocompatibilidade Absoluta:** Suporte a requisições legadas com propriedades planas via `@JsonAlias`, getters `@JsonIgnore` para preservação das assinaturas de use cases e desserializadores delegados `@JsonCreator`.
- **Construção Defensiva Anti-Nulo (`ResumoDashboardDTO`):** Contadores padronizados em `0`, valores monetários em `0.00` e coleções vazias instanciadas, blindando o front-end contra exceções de leitura de propriedades indefinidas.
- **Blindagem UTF-8:** Configuração estrita de encoding UTF-8 no build Maven e no servlet Spring Boot, com higienização completa de caracteres corrompidos (*mojibake*) na documentação do Swagger UI.

---

## 2. 🚧 Em Desenvolvimento / Novo Épico (WIP): Integração com Sistemas Judiciais dos Tribunais

Módulo corporativo para automação de captura, leitura e espelhamento de andamentos processuais e publicações do Diário de Justiça Eletrônico Nacional (DJEN / Comunica PJe) e tribunais onde a banca atua (TJRS, TRF4, TRT4, STJ, TST), eliminando a digitação manual de movimentações judiciais.

> [!IMPORTANT]
> **Decisão de Arquitetura — Restrição Absoluta Zero Certificado (Token A3):**  
> Como os advogados da banca utilizam certificado digital do tipo **A3 (Token USB físico)** conectado localmente às suas máquinas, está **estritamente vedado** o uso de mTLS, KeyStores no servidor ou qualquer fluxo dependente de certificado digital na VPS. A integração opera **100% sobre as rotas públicas de consulta por OAB do Comunica PJe** (`https://comunicaapi.pje.jus.br/api/v1/comunicacao`).

### 2.1. Cliente Comunica PJe e Throttling Anti-Ban (Rate Limiting)
- **Cliente REST (`ComunicaPjeClient`):** Consumo público dos endpoints do DJEN via `RestTemplate` filtrando por `numeroOab`, `ufOab`, `dataDisponibilizacaoInicio`, `dataDisponibilizacaoFim` e paginação (`pagina`, `itensPorPagina`).
- **Proteção de IP da VPS (Oracle Cloud) & Throttling Defensivo:**
  - Inserção de delays imperativos (`Thread.sleep` de 2.500 ms a 3.500 ms) entre páginas e entre diferentes OABs para prevenção de HTTP 429 (Too Many Requests) e banimento de IP.
  - Tratamento defensivo de erro 429 com backoff estendido de 10 segundos.
- **Modelagem de Intimações (`IntimacaoPje` / `tb_intimacao_pje`):**
  - Armazenamento completo dos metadados recebidos (`comunicacaoId`, `hash`, `numeroProcesso`, `numeroProcessoMascara`, `siglaTribunal`, `tipoComunicacao`, `tipoDocumento`, `nomeOrgao`, `dataDisponibilizacao`, `texto`, `link`, `numeroOab`, `ufOab`).
  - Prevenção de duplicatas via `existsByComunicacaoId`.
  - Vinculação automática a processo existente (`Processo`) e geração atômica de `Andamento` (`TipoAndamentoEnum.AUTOMATICO`).
- **Triagem Inteligente de Tarefas na Agenda (Opção B):**
  - Injeção direta de novas intimações na agenda do advogado como `Tarefa` com blindagem imperativa (`equalsIgnoreCase`):
    1. **Descarte de Informativos (`continue`):** `"Lista de distribuição"` e `"Ata de sessão"` (preservados no histórico sem gerar tarefas na agenda).
    2. **Alerta de Pauta:** `"Pauta de julgamento"` -> Tarefa `TipoTarefaEnum.DILIGENCIA`, descrição `"[DILIGÊNCIA - PAUTA] Proc. {numeroProcesso} ({siglaTribunal})"`.
    3. **Gestão de Prazos:** `"Intimação"` / `"Citação"` -> Tarefa `TipoTarefaEnum.PRAZO`, com prefixos conforme `tipoDocumento`:
       - `"Sentença"` -> `"[URGENTE - SENTENÇA] Proc. {numeroProcesso} ({siglaTribunal})"`
       - `"DESPACHO/DECISÃO"` -> `"[URGENTE - DECISÃO] Proc. {numeroProcesso} ({siglaTribunal})"`
       - `"Ato ordinatório"` -> `"[PRAZO - ATO ORDINATÓRIO] Proc. {numeroProcesso} ({siglaTribunal})"`
       - `"Notificação"` -> `"[PRAZO - NOTIFICAÇÃO] Proc. {numeroProcesso} ({siglaTribunal})"`
       - Outros -> `"[PRAZO - ATENÇÃO] Proc. {numeroProcesso} ({siglaTribunal})"`
    - Persistência em lote via `tarefaRepository.saveAll(...)` sob transação atômica.

### 2.2. Motor Híbrido de Sincronização (Automático + Manual)
- **Varredura Noturna Automática (`VarreduraPjeScheduler`):**
  - Agendamento cron diário às **02h00 da madrugada** (`0 0 2 * * *`).
  - Varredura em lote iterando imperativamente sobre todos os advogados com OAB ativa (`usuarioRepository.buscarAdvogadosComOabAtiva()`), consultando publicações dos últimos 3 dias para cobrir finais de semana e feriados.
- **Sincronização Sob Demanda (`POST /api/integracoes/pje/sincronizar`):**
  - Endpoint REST autenticado no `IntegracaoTribunalController` permitindo que o advogado force a atualização de suas próprias intimações a qualquer momento na interface.
- **Caso de Uso Centralizado (`SincronizarIntimacoesPjeUseCase`):**
  - Orquestração da paginação, parsing da OAB, delays de throttling e persistência atômica.
  - Implementado estritamente no **Paradigma Imperativo Puro** (Zero Lambdas, Zero Streams).

### 2.3. Modelagem de Dados e Espelhamento dos Autos Processuais
- **Extensão da Entidade `Processo` (`tb_processo`):**
  - Mapeamento das colunas de telemetria de sincronização:
    - `data_ultima_sincronizacao` (LocalDateTime, opcional).
    - `status_sincronizacao` (Enum `StatusSincronizacaoEnum`: `SINCRONIZADO`, `PENDENTE`, `FALHA`, `EM_ANDAMENTO`).
    - `tribunal_origem` (Enum `TribunalOrigemEnum`: `TJRS`, `TRF4`, `TRT4`, `STJ`, `STF`).
    - `grau_jurisdicao` (Enum: `PRIMEIRO_GRAU`, `SEGUNDO_GRAU`, `SUPERIOR`).
- **Enriquecimento da Entidade `Andamento` (`tb_andamento`):**
  - Preservação da coluna `tipo = 'AUTOMATICO'`.
  - Inclusão de `codigo_movimentacao_tribunal` (código oficial da tabela unificada do CNJ).
  - Inclusão de `hash_movimentacao` (SHA-256 gerado a partir de `numeroCnj + dataHora + descricao + codigo`), com constraint de unicidade para prevenção estrita de duplicidade de andamentos em sucessivas varreduras.
  - Vínculo opcional com `Documento` (`documento_id`) para peças, despachos e sentenças cujo PDF foi baixado pelo robô e arquivado automaticamente no GED (Google Drive).
- **Entidade de Auditoria `SincronizacaoTribunalLog` (`tb_sincronizacao_tribunal_log`):**
  - Rastreamento completo de cada tentativa de varredura: `id`, `processoId`, `tribunal`, `tipoIntegracao`, `dataHoraInicio`, `dataHoraFim`, `duracaoMs`, `status` (`SUCESSO`, `FALHA_CONEXAO`, `FALHA_AUTENTICACAO`, `RATE_LIMITED`, `CIRCUITO_ABERTO`), `quantidadeMovimentacoesNovas`, `mensagemErro` e `detalhesTecnicos`.
- **Entidade de Configuração `TribunalConfig` (`tb_tribunal_config`):**
  - Parametrizador de endpoints, credenciais de integração, tipo de conector (`DATAJUD_API`, `EPROC_SCRAPER`, `PJE_MNI`), intervalo mínimo entre requisições (`intervaloMinimoRequisicoesMs`) e toggle de ativação.

### 2.4. Agendamento de Tarefas & Varredura Assíncrona (Schedulers & Workers)
- **Configuração do Spring Scheduler (`@EnableScheduling`):**
  - Configuração de `ThreadPoolTaskScheduler` isolado com pool dedicado de threads de background (`poolSize = 5`), garantindo que rotinas pesadas de sincronização nunca disputem recursos nem degradem as threads HTTP do Tomcat.
- **Estratégias de Varredura:**
  1. **Varredura Noturna em Lote (Batch Cron):**
     - Execução diária na madrugada (ex: `0 0 2 * * *` — 02h00), varrendo em lotes controlados todos os processos ativos do escritório (`arquivado = false`).
  2. **Sincronização Sob Demanda (On-Demand):**
     - Endpoint REST `POST /api/integracoes/tribunais/processos/{id}/sincronizar` permitindo que o advogado force a atualização de um processo diretamente na tela do processo.
  3. **Varredura Prioritária (Near-Real-Time para Audiências e Prazos):**
     - Processos com audiências agendadas para os próximos 3 dias ou tarefas de prazo iminente são verificados com periodicidade reforçada.
- **Fila com Rate Limiting e Throttling:**
  - Aplicação de espaçamento imperativo mínimo entre chamadas sucessivas ao mesmo tribunal (ex: delay de 1.500 ms a 3.000 ms), evitando bloqueios de IP, acionamento de WAFs governamentais ou desafios de captcha.
- **Mecanismo de Lock Defensivo:**
  - Controle atômico via flag ou trava temporal na entidade `Processo` (`status_sincronizacao = 'EM_ANDAMENTO'`) para impedir que dois jobs simultâneos (cron noturno e disparo manual) acessem o mesmo processo concorrentemente.

### 2.5. Resiliência e Tratamento de Falhas com Serviços Externos
Portais de tribunais e web services judiciais sofrem de alta volatilidade, lentidões imprevisíveis, janelas de manutenção de fim de semana e erros HTTP 500/502/503/504 recorrentes. A arquitetura implementa uma malha defensiva de resiliência:
- **Padrão Circuit Breaker (Disjuntor de Falhas):**
  - Cada tribunal possui seu estado de circuito monitorado (`FECHADO`, `ABERTO`, `SEMI_ABERTO`).
  - Se um tribunal acumular consecutivamente $N$ falhas (ex: 5 falhas sucessivas de conexão), o circuito **abre** (`ABERTO`), suspendendo temporariamente novas requisições àquele tribunal por um período de cooldown configurável (ex: 15 minutos).
  - Atualização automática do status exposto no endpoint `GET /api/integracoes/tribunais/status` para `DEGRADADO` ou `INDISPONIVEL`, alertando a banca.
  - Após o cooldown, o circuito transiciona para `SEMI_ABERTO`, permitindo uma requisição de teste para avaliar se o serviço do tribunal foi restabelecido.
- **Retry com Exponential Backoff e Jitter:**
  - Retentativas com espaçamento exponencial progressivo ($1\text{s}, 2\text{s}, 4\text{s}$) e fator aleatório de dispersão (*jitter*) exclusivamente para falhas transitórias (`SocketTimeoutException`, `ConnectException`, HTTP 502/503/504).
  - Erros definitivos (ex: `401 Unauthorized` por certificado expirado, `404 Not Found` por processo inexistente ou `400 Bad Request` por CNJ inválido) **não sofrem retry**, abortando imediatamente e registrando a causa no log.
- **Dead Letter Queue / Fila de Contingência:**
  - Processos cuja sincronização falhou após as tentativas de retry são enfileirados com status `FALHA` e reagendados para processamento em janela de menor carga.
- **Isolamento de Falhas (Fail-Safe Isolation):**
  - O processamento de cada processo no lote ocorre em bloco `try/catch` isolado. A falha em um processo NUNCA interrompe a varredura dos demais processos da fila.

### 2.6. Adaptadores Complementares (DataJud CNJ e Portais)
- **Adaptador 1: API Pública do DataJud (CNJ):**
  - Consulta padronizada e autenticada via chave pública de API do DataJud/CNJ pelo número CNJ unificado.
  - Vantagem: Cobertura universal de metadados e histórico básico de movimentações sem necessidade de certificado digital.
- **Adaptador 2: eproc e PJe Externos:**
  - Consulta complementar direta em casos específicos autorizados.
- **Adaptador 3: PJe (TRT4):**
  - Conector de interoperabilidade via Modelo Nacional de Interoperabilidade (MNI) / Web Services do CNJ.

### 2.6. Telemetria, Observabilidade e Endpoints de Gestão
- **Monitoramento Ativo de Status (`GET /api/integracoes/tribunais/status`):**
  - Evolução do endpoint para refletir a telemetria real dos Circuit Breakers, latência média observada, status operacional por tribunal e data/hora da última sincronização bem-sucedida.
- **Sincronização Sob Demanda (`POST /api/integracoes/tribunais/processos/{id}/sincronizar`):**
  - Disparo manual de varredura atômica para o processo, retornando a quantidade de novos andamentos localizados.
- **Auditoria e Logs de Integração (`GET /api/integracoes/tribunais/logs`):**
  - Listagem paginada dos logs de sincronização com filtros por processo, tribunal, status e intervalo de datas.

---

## 3. 📅 Backlog Futuro (Próximos Passos)

Grandes iniciativas e automações planejadas para as próximas etapas de desenvolvimento do sistema:

### 3.1. Mensageria & Notificações Ativas para Clientes
- **Integração com WhatsApp:** Envio automatizado de lembretes de audiência para os clientes e notificações amigáveis de movimentação do seu processo via gateway de mensageria (ex: Evolution API ou Z-API).

### 3.2. Assinatura Eletrônica de Documentos
- **Plataformas de Assinatura:** Integração via webhook com plataformas de assinatura digital (ZapSign, Clicksign ou DocuSign) para envio e colheita de assinatura de Procurações e Contratos de Honorários gerados pelo sistema.

### 3.3. Download em Lote e OCR de Peças Históricas
- **Digitalização e OCR:** Processamento de cópias integrais de autos digitalizados com OCR em lote e indexação vetorial com Spring AI para pesquisa semântica nos autos.

---

## 📊 Matriz de Rastreabilidade das Camadas

| Domínio de Negócio | Controller Principal | Use Case / Service Central | Persistência (SQL Nativo) | Status de Entrega |
| :--- | :--- | :--- | :--- | :---: |
| **IAM / Segurança** | `AuthController`, `UsuarioController` | `AuthService`, `AutenticarUsuarioUseCase` | `UsuarioRepository`, `PasswordResetTokenRepository` | ✅ Produção |
| **Escritório (Tenant)**| `EscritorioConfigController` | `EscritorioService` | `EscritorioRepository` | ✅ Produção |
| **Clientes (CRM)** | `ClienteController` | `CadastrarClienteUseCase`, `DocumentoValidator`| `ClienteRepository` | ✅ Produção |
| **Documentos PDF** | `ClienteController` (Rotas PDF) | `PdfDocumentGeneratorService` (iText) | `ClienteRepository`, `EscritorioRepository` | ✅ Produção |
| **GED & Storage** | `DocumentoController` | `UploadDocumentoUseCase`, `GoogleDriveStorageService` | `DocumentoRepository` | ✅ Produção |
| **Processos** | `ProcessoController`, `AndamentoController` | `CadastrarProcessoUseCase`, `AtualizarProcessoUseCase`, `ArquivarProcessoUseCase` | `ProcessoRepository`, `AndamentoRepository` | ✅ Produção |
| **Financeiro & Parcelamento**| `FaturamentoController` | `GerarParcelamentoUseCase`, `LiquidarFaturamentoUseCase`, `LiquidarParcialFaturamentoUseCase`, `RepassarFaturamentoUseCase`, `ObterResumoFinanceiroUseCase` | `FaturamentoRepository` | ✅ Produção |
| **Edição Financeira** | `FaturamentoController` (`PUT/PATCH /{id}`) | `EditarFaturamentoUseCase` | `FaturamentoRepository` | ✅ Produção |
| **Consultas Avulsas** | `FaturamentoController` (`/consulta-avulsa`) | `RegistrarConsultaAvulsaUseCase` | `FaturamentoRepository`, `ClienteRepository` | ✅ Produção |
| **Agenda & Tarefas** | `AudienciaController`, `TarefaController` | `CadastrarAudienciaUseCase`, `CriarTarefaUseCase` | `AudienciaRepository`, `TarefaRepository` | ✅ Produção |
| **Google Calendar (Webhooks)** | `GoogleCalendarWebhookController` | `SincronizarEventoGoogleCalendarUseCase`, `GoogleOAuthTokenManager` | `TarefaRepository` | ✅ Produção |
| **Dashboard & Avisos**| `DashboardController`, `NotificacaoController`, `BuscaGlobalController` | `DashboardAdvogadoUseCase`, `ObterResumoNotificacoesUseCase`, `BuscaGlobalUseCase` | Múltiplos Repositories via SQL Nativo | ✅ Produção |
| **Resumos IA** | `ResumoAudienciaController` | `GerarResumoAudienciaUseCase`, `SpringAIResumoService`, `TextoChunkingService` | `AudienciaRepository` | ✅ Produção |
| **Comunica PJe (Zero Certificado A3)** | `IntegracaoTribunalController` (`/pje/sincronizar`) | `SincronizarIntimacoesPjeUseCase`, `VarreduraPjeScheduler` (cron 02h00) | `IntimacaoPjeRepository`, `TarefaRepository`, `AndamentoRepository` | ✅ Produção |
| **Certificados Digitais (A1/mTLS)** | `CertificadoDigitalController` | `CadastrarCertificadoUseCase`, `CustomSSLContextFactory` | `CertificadoDigitalRepository` | 🚧 Novo Épico (WIP) |
| **Sincronização Tribunais** | `IntegracaoTribunalController` | `SincronizarProcessoTribunalUseCase`, `DataJudClient`, `EprocAdapter` | `ProcessoRepository`, `AndamentoRepository` | 🚧 Novo Épico (WIP) |
| **Schedulers & Varredura** | *Execução em Background* | `VarreduraProcessosScheduler`, `FilaSincronizacaoService` | `ProcessoRepository`, `SincronizacaoTribunalLogRepository` | 🚧 Novo Épico (WIP) |
| **Resiliência & Circuit Breaker** | `IntegracaoTribunalController` (`/status`, `/logs`)| `TribunalCircuitBreakerRegistry`, `RetryExponentialBackoffService`| `SincronizacaoTribunalLogRepository`, `TribunalConfigRepository` | 🚧 Novo Épico (WIP) |
| **Mensageria (WhatsApp)** | *A implementar* | *A implementar (Evolution API / Z-API)* | *A implementar* | 📅 Backlog |
| **Assinatura Eletrônica** | *A implementar* | *A implementar (Clicksign / ZapSign)* | *A implementar* | 📅 Backlog |

---

*Última atualização: Setembro/2026 — Documento versionado e sincronizado com a base de código do repositório.*
