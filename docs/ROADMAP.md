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
- **Ambiente de Produção & DevOps:** VPS Ubuntu LTS (Hostinger), Nginx com SSL Let's Encrypt, firewall UFW, Fail2Ban, CI/CD via GitHub Actions e gerenciamento de serviço systemd.

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

### 1.10. Financeiro, Fluxo de Caixa e Faturamento
- **Controle Bipolar de Lançamentos:** Gestão de receitas e despesas pela natureza (`A_RECEBER`, `A_PAGAR`) e classificação de despesa/honorários (`HONORARIOS`, `CUSTAS`, `DESPESAS_ESCRITORIO`).
- **Liquidação com Registro Histórico:** Quitação de títulos com data de pagamento obrigatória (`PATCH /api/faturamentos/{id}/pagar`) e controle de status (`PENDENTE`, `PAGO`, `CANCELADO`).
- **Resumo Financeiro Executivo:** Endpoint `GET /api/faturamentos/resumo` com cálculo em tempo real de saldo previsto, valores recebidos, a pagar e taxa de adimplência/inadimplência.
- **Filtros Financeiros Multicritério:** Listagem paginada no servidor cruzando intervalo de datas, status, natureza, tipo e vínculo com processo.

### 1.11. Produtividade: Agenda, Tarefas, Notificações e Dashboard
- **Tarefas e Prazos Processuais:** CRUD completo de tarefas (`DILIGENCIA`, `PRAZO`, `CONTATO`), vinculadas a usuários e processos, com controle de conclusão (`PATCH /api/tarefas/{id}/concluir`) e consulta por período (`GET /api/tarefas/agenda`).
- **Agenda de Audiências:** Cadastro com validação impeditiva de datas passadas, vínculo de responsável, pauta harmonizada por período em `LocalDate` (`GET /api/audiencias/agenda`) e cancelamento/realização de pauta.
- **Central de Notificações do Dia:** Endpoint consolidado `GET /api/notificacoes/resumo?data=YYYY-MM-DD` que centraliza audiências do dia, prazos fatais de tarefas e faturamentos a receber vencidos para o usuário logado via JWT.
- **Dashboard do Advogado:** `GET /api/dashboard` com agregações métricas em tempo real, blindado por construtor defensivo que anula referências nulas para garantir estabilidade no frontend.
- **Busca Global Multidomínio:** `GET /api/busca` com varredura concorrente em Processos, Clientes e Usuários.

---

## 2. 🚧 Em Refinamento (WIP)

Funcionalidades cujo fluxo inicial e arquitetura base já foram codificados e testados tecnicamente, mas que necessitam de evolução estrutural e de produto para entregar valor prático ao advogado.

### 2.1. Inteligência Artificial para Resumo e Preparação de Audiências
- **Situação Atual do Código:**
  - O fluxo base está implementado através de `ResumoAudienciaController`, `GerarResumoAudienciaUseCase`, `GerarEAnexarResumoAudienciaUseCase` e `SpringAIResumoService`.
  - Conexão ativa com o modelo `gemini-3.6-flash` através do starter OpenAI do Spring AI apontando para o Google AI Studio.
  - O endpoint `POST /api/audiencias/{id}/gerar-resumo-ia` aceita um texto de peça e anexa o resultado à coluna `resumo_preparatorio_ia` da audiência.
- **Diagnóstico das Limitações Atuais:**
  - *Prompt Simplista e Monolítico:* O system prompt atual solicita apenas um resumo genérico dos pontos da lide, sem divisão tática ou direcionamento para a condução da audiência.
  - *Saída Não Estruturada:* Retorna texto corrido simples (Markdown não tipado), o que impede o frontend de exibir blocos interativos (cards separados por tópicos).
  - *Dependência de Recorte Manual:* O endpoint atual exige que o advogado copie e cole manualmente o texto da petição inicial ou contestação (`conteudoPeca`), em vez de ler automaticamente os arquivos PDF já anexados ao processo no Google Drive.
- **Plano de Evolução e Refinamento:**
  1. **✅ Engenharia de Prompt Especializada (Concluído):**
     - Estruturação em seções táticas claras: Fatos Incontroversos, Fatos Controvertidos, Riscos Processuais/Preliminares, Roteiro de Perguntas para testemunhas/depoimento e Parâmetros de Acordo.
  2. **✅ Estruturação de Resposta com JSON Schema / DTO (Concluído):**
     - Criação do record `ResumoAudienciaEstruturadoDTO` (`core/domain/dto`) e integração com `BeanOutputConverter` do Spring AI em `SpringAIResumoService`, propagado para `GerarResumoAudienciaUseCase`, `GerarEAnexarResumoAudienciaUseCase` e `ResumoAudienciaController`.
  3. **✅ Ingestão Automática de Documentos dos Autos (Concluído):**
     - Extração de texto de arquivos PDF via Apache PDFBox (`PdfExtractionService`), download de documentos via `GoogleDriveStorageService` em laços imperativos clássicos e concatenação estruturada de peças antes do envio ao modelo LLM, eliminando a dependência de recorte e colagem manual de texto.
  4. **✅ Controle de Janela de Contexto & Tokenização (Concluído):**
     - Fatiamento inteligente de peças volumosas através do `TextoChunkingService` com cortes defensivos respeitando parágrafos (`\n\n`, `\n`) e pontuação final (`. `) em blocos de até 18.000 caracteres.
     - Resumo progressivo em dois estágios no `GerarResumoAudienciaUseCase` e `GerarEAnexarResumoAudienciaUseCase`: blocos múltiplos são sintetizados individualmente via `resumoAIService.resumirChunk` e consolidados em um dossiê preliminar antes da estruturação final no `ResumoAudienciaEstruturadoDTO`.
     - Execução direta com zero overhead para documentos que cabem em um único chunk. Tudo implementado no paradigma imperativo estrito (zero streams, zero lambdas).

---

## 3. 📅 Backlog (Próximos Passos)

Grandes iniciativas e automações planejadas para as próximas etapas de desenvolvimento do sistema.

### 3.1. Web Scraping & Integração com Sistemas Judiciais dos Tribunais
- **Objetivo:** Automatizar a coleta de andamentos e autos processuais diretamente dos portais dos tribunais onde o Dr. Cristhian atua, eliminando a digitação manual de andamentos.
- **Escopo Inicial dos Tribunais:**
  - **TJRS:** Portais Themis / eproc estadual.
  - **TRF4:** Sistema eproc da Justiça Federal da 4ª Região.
  - **TRT4:** Sistema PJe da Justiça do Trabalho.
  - **Tribunais Superiores:** STJ e STF.
- **Funcionalidades a Desenvolver:**
  - **Robô Extrator de Andamentos:** Varredura periódica pelo número CNJ ou OAB para identificar novas movimentações e registrar automaticamente em `tb_andamento` com `tipo = 'AUTOMATICO'`.
  - **Leitor e Downloader de Peças dos Autos:** Extração de cópias integrais de decisões, sentenças, despachos e notas de expediente, salvando automaticamente os arquivos no Google Drive na pasta do respectivo processo.
  - **Evolução do Status de Tribunais:** Transformar o endpoint `GET /api/integracoes/tribunais/status` (hoje baseado em lista controlada) em um monitor com telemetria real via healthcheck ativo ou integração oficial com a API Pública do **DataJud / CNJ**.

### 3.2. Cron Jobs (`@Scheduled`) & Automações em Segundo Plano
- **Objetivo:** Transformar o backend em uma plataforma proativa, executando rotinas automáticas sem dependência de interação humana.
- **Tarefas Agendadas no Backlog:**
  - **Robôs de Varredura Noturna:**
    - Agendamento cron (ex: `0 0 2 * * *` — 02h00 da madrugada) para acionar os scrapers judiciais e sincronizar movimentações ocorridas no dia anterior.
  - **Sentinela de Prazos Fatais & Alertas Preventivos:**
    - Job executado no início da manhã (ex: `0 0 7 * * *`) para identificar tarefas pendentes com vencimento nas próximas 24/48 horas e audiências agendadas para o dia seguinte.
    - Disparo de e-mails de alerta com prioridade alta para os advogados responsáveis.
  - **Monitor de Inadimplência e Faturamentos Vencidos:**
    - Job diário que identifica títulos de clientes com `dataVencimento < hoje` e status `PENDENTE`.
    - Atualização do status ou flag de cobrança pendente e envio de relatório consolidado para o financeiro do escritório.

### 3.3. Mensageria & Notificações Ativas para Clientes
- **Integração com WhatsApp:** Envio automatizado de lembretes de audiência para os clientes e notificações amigáveis de movimentação do seu processo via gateway de mensageria (ex: Evolution API ou Z-API).
- **Assinatura Eletrônica de Documentos:** Integração via webhook com plataformas de assinatura digital (ZapSign, Clicksign ou DocuSign) para envio e colheita de assinatura de Procurações e Contratos de Honorários gerados pelo sistema.

---

## 📊 Matriz de Rastreabilidade das Camadas

| Domínio de Negócio | Controller Principal | Use Case / Service Central | Persistência (SQL Nativo) | Status de Entrega |
| :--- | :--- | :--- | :--- | :---: |
| **IAM / Segurança** | `AuthController`, `UsuarioController` | `AuthService`, `AutenticarUsuarioUseCase` | `UsuarioRepository`, `PasswordResetTokenRepository` | ✅ Produção |
| **Escritório (Tenant)**| `EscritorioConfigController` | `EscritorioService` | `EscritorioRepository` | ✅ Produção |
| **Clientes (CRM)** | `ClienteController` | `CadastrarClienteUseCase`, `DocumentoValidator`| `ClienteRepository` | ✅ Produção |
| **Documentos PDF** | `ClienteController` (Rotas PDF) | `PdfDocumentGeneratorService` (iText) | `ClienteRepository`, `EscritorioRepository` | ✅ Produção |
| **GED & Storage** | `DocumentoController` | `UploadDocumentoUseCase`, `GoogleDriveStorageService` | `DocumentoRepository` | ✅ Produção |
| **Processos** | `ProcessoController`, `AndamentoController` | `CadastrarProcessoUseCase`, `ArquivarProcessoUseCase` | `ProcessoRepository`, `AndamentoRepository` | ✅ Produção |
| **Financeiro** | `FaturamentoController` | `LiquidarFaturamentoUseCase`, `ObterResumoFinanceiroUseCase` | `FaturamentoRepository` | ✅ Produção |
| **Agenda & Tarefas** | `AudienciaController`, `TarefaController` | `CadastrarAudienciaUseCase`, `CriarTarefaUseCase` | `AudienciaRepository`, `TarefaRepository` | ✅ Produção |
| **Dashboard & Avisos**| `DashboardController`, `NotificacaoController`, `BuscaGlobalController` | `DashboardAdvogadoUseCase`, `ObterResumoNotificacoesUseCase`, `BuscaGlobalUseCase` | Múltiplos Repositories via SQL Nativo | ✅ Produção |
| **Resumos IA** | `ResumoAudienciaController` | `GerarResumoAudienciaUseCase`, `SpringAIResumoService` | `AudienciaRepository` | 🚧 Em Refinamento |
| **Robôs / Scraping** | `IntegracaoTribunalController` | *A implementar (Scrapers / DataJud CNJ)* | *A implementar* | 📅 Backlog |
| **Jobs Agendados** | *Não exposto via HTTP* | *A implementar (`@Scheduled` Cron Services)* | *A implementar* | 📅 Backlog |

---

*Última atualização: Setembro/2026 — Documento versionado e sincronizado com a base de código do repositório.*
