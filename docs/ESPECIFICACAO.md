# Documento de Especificação de Software (PRD) - v3.3

**Projeto:** Sistema de Gestão Jurídica Inteligente  
**Perfil:** Backend Corporativo / Portfólio  
**Padrão Arquitetural:** Clean Architecture (Arquitetura Hexagonal)  
**Versão do Spring Boot:** 3.3.3 | **Java:** 21 LTS  
**Paradigma Adotado:** Imperativo Puro (Zero Lambdas / Zero Streams)

---

## 1. Módulos do Sistema

O sistema é estruturado em módulos lógicos de alta coesão e baixo acoplamento:

*   **IAM (Identity & Access Management) & Segurança:**
    *   Controle de usuários e perfis de acesso (`ADMIN`, `ADVOGADO`, `SECRETARIA`).
    *   Autenticação via JWT *stateless* com assinatura HMAC256 e extração de claims de perfil e identificador.
    *   Suporte ao parâmetro opcional `manterConectado: boolean` no login para controle de persistência de sessão: se `true`, estende a validade do token JWT de 2 horas para 7 dias (expiração padrão: 2 horas).
    *   Endpoint oficial e centralizado para resgate dos dados do usuário logado (`GET /api/auth/me`), retornando o `UsuarioResponseDTO` da sessão ativa.
    *   **Blindagem de Credenciais (Segurança por Design):**
        *   Criação de DTOs segregados: `CriarUsuarioRequest` e `AtualizarUsuarioRequest` para operações de entrada; `UsuarioResponseDTO` para saídas.
        *   O campo `senha` nunca é exposto em nenhuma resposta da API, garantindo conformidade com a auditoria de segurança.
    *   **Listagem de Equipe Paginada (`GET /api/usuarios`):**
        *   Exclusivo para perfis `ADMIN` e `ADVOGADO` (`@PreAuthorize("hasAnyRole('ADMIN', 'ADVOGADO')")`).
        *   Suporte a paginação e filtros dinâmicos por status (`ativo=true/false`) e busca textual (`q` ou `termoBusca`) por nome, e-mail ou OAB.
    *   **Recuperação e Redefinição de Senha:**
        *   Solicitação com envio de e-mail SMTP via Google (`POST /api/auth/recuperar-senha`): geração de token atômico persistido na tabela `tb_password_reset_token` com validade de 15 minutos, regra anti-enumeração de contas e resposta padronizada `202 Accepted`. Envio de e-mail com instruções e formatação clara. Rollback transacional atômico integral em caso de falha no envio.
        *   Redefinição de senha com token (`POST /api/auth/redefinir-senha`): validação de existência, consumo prévio e expiração do token, com atualização de hash BCrypt e invalidação do token (HTTP 200 OK).
    *   **Alteração de Senha Autenticada:** Rota segura (`PATCH /api/auth/me/senha`) que valida a senha atual com BCrypt antes de atualizar para a nova credencial, retornando `204 No Content`.
    *   Tratamento defensivo do campo `oab`: campos vazios ou com espaços são persistidos como `null`, prevenindo violação de constraint de unicidade no PostgreSQL para múltiplos usuários sem registro na OAB.
    *   Filtro `JwtAuthenticationFilter` configurado com `shouldNotFilter` para liberar rotas públicas de autenticação (`/api/auth/recuperar-senha`, `/api/auth/redefinir-senha`, `/api/auth/login`) e documentação Swagger sem exigir token.
    *   Controle de acesso granular baseado em papéis (RBAC com `@PreAuthorize` e `@EnableMethodSecurity`).
    *   Listagem de advogados ativos (`GET /api/usuarios/advogados`) para vinculação em processos.
    *   Configuração global de CORS (`CorsConfig`) e liberação explícita de requisições `OPTIONS` preflight.

*   **Configurações Institucionais do Escritório:**
    *   Gestão cadastral centralizada do escritório (`GET /api/configuracoes/escritorio` e `PUT /api/configuracoes/escritorio`).
    *   Persistência em PostgreSQL na tabela `escritorios`: Razão Social, Nome Fantasia, CNPJ único (14 dígitos), Registro OAB (`OAB/RS 121.837`), contatos (telefone, WhatsApp, e-mail) e endereço oficial (`Rua Tiradentes, 676, Ijuí/RS`).
    *   Higienização imperativa de máscaras numéricas (CNPJ, telefone, WhatsApp, CEP) para persistência segura e padronizada.
    *   Inicialização automática via `DatabaseSeeder` com dados padrão caso a tabela esteja vazia.

*   **Core Legal & CRM:**
    *   **Gestão de Clientes:** Qualificação civil estendida (tipo de pessoa, estado civil, profissão, sexo, endereço completo e data de nascimento), com listagem paginada (`?page=0&size=10`), busca textual universal (`?q=` ou `?termoBusca=`) e filtro por tipo de cliente (`?tipo=FISICA|JURIDICA`).
    *   **Gestão de Processos:** Ciclo de vida completo (criação, edição, arquivamento e desarquivamento), qualificação estendida da lide (`parteAdversa`, `cpfCnpjParteAdversa`, `papelCliente` via enum `PapelClienteEnum`: `AUTOR`, `REU`, `TERCEIRO_INTERESSADO`, `valorCausa` e `comarca`), listagem paginada com filtros avançados no servidor por termo (`?q=`), fase processual (`?fase=`), cliente (`?clienteId=`), advogado responsável (`?advogadoId=`) e arquivamento (`?arquivado=true/false`).
    *   Histórico e linha do tempo de andamentos processuais com tipificação (`AUTOMATICO`, `MANUAL`, `IA`).

*   **Financeiro, Agenda & Produtividade:**
    *   **Faturamento & Fluxo de Caixa:** Controle de receitas e despesas (`A_RECEBER`, `A_PAGAR`), tipos (`HONORARIOS`, `CUSTAS`, `DESPESAS_ESCRITORIO`, `CONSULTA_AVULSA`), suporte a parcelamento (`numeroParcela` e `totalParcelas`) e gestão condicional de repasses a clientes para valores oriundos de sucumbência ou terceiros (`origemPagamento`: `DIRETO_CLIENTE`, `TERCEIRO_SUCUMBENCIA`; `valorHonorariosRetidos`, `valorRepasseCliente`, `statusRepasse`: `PENDENTE`, `REPASSADO`; `formaRepasse`, `dadosBancariosCliente` e `dataRepasse`). Suporte a lançamentos vinculados a processos ou diretamente a clientes (com vínculo processual anulável para consultas avulsas). Listagem paginada no servidor com filtros combinados: busca textual (`?q=`), status (`?status=`), natureza (`?natureza=`), tipo (`?tipo=`), intervalo de vencimento (`?vencimentoDe=` e `?vencimentoAte=`) e vínculo com processo (`?processoId=`).
    *   **Edição de Lançamento Financeiro (`PUT /api/faturamentos/{id}` e `PATCH /api/faturamentos/{id}`):**
        *   Permite a alteração completa ou parcial dos atributos do lançamento financeiro: `valor` (validação de valor estritamente positivo via `@Positive`), `descricao`, `dataVencimento`, `categoria`/`tipo` (`TipoFaturamentoEnum`), `status` (`StatusFaturamentoEnum`), `natureza` (`NaturezaFaturamentoEnum`), `dataPagamento` e reatribuição de `processoId`.
        *   Regra de transição inteligente de status: se o faturamento tiver seu status alterado para `PAGO` sem envio explícito da data de quitação, o sistema atribui automaticamente a data atual (`LocalDate.now()`).
        *   Processamento atômico e transacional imperativo puro via `EditarFaturamentoUseCase`.
    *   **Liquidação, Baixas e Repasses:**
        *   **Baixa Integral (`PATCH /api/faturamento/{id}/liquidar`):** Quitação total com registro de data efetiva de pagamento (`dataPagamento`) e atualização de status para `PAGO`.
        *   **Baixa Parcial (`PATCH /api/faturamento/{id}/liquidar-parcial`):** Recebimento parcial onde o sistema registra o valor pago, altera o status do título para `PARCIALMENTE_PAGO` e realiza a criação/desdobramento de um novo registro de faturamento (ou ajuste do saldo devedor e prorrogação da data) com a nova data de vencimento para cobrança da diferença.
        *   **Efetivação de Repasse (`PATCH /api/faturamento/{id}/repassar`):** Baixa e liquidação do repasse de valores ao cliente em receitas com origem `TERCEIRO_SUCUMBENCIA`, registrando `dataRepasse`, `formaRepasse` e alterando `statusRepasse` para `REPASSADO`.
        *   **Lançamento e Liquidação Rápida de Consulta Avulsa (`POST /api/faturamentos/consulta-avulsa`):** Fluxo financeiro expresso para recebimento de consultas jurídicas (ex: R$ 250,00) sem vínculo processual. O faturamento é associado unicamente ao `Cliente` (com `Processo` nulo), gerando o registro financeiro (`A_RECEBER`, tipo `CONSULTA_AVULSA`), liquidando o pagamento e transicionando o status para `PAGO` de forma atômica (`@Transactional`) via `RegistrarConsultaAvulsaUseCase`.
        *   **Assistente de Parcelamento (`POST /api/faturamento/parcelamento`):** Criação de múltiplos lançamentos financeiros simultâneos a partir da simulação e divisão contratual de parcelas.
    *   **Métricas Financeiras Consolidadas:** Rota de resumo financeiro (`GET /api/faturamentos/resumo`) com totalizadores de a receber, a pagar, saldo previsto e valores vencidos.
    *   **Gestão de Tarefas (To-Do List):** Tarefas vinculadas a usuários e processos (`DILIGENCIA`, `PRAZO`, `CONTATO`, `ATENDIMENTO`) com CRUD completo, marcação de conclusão, suporte a identificador externo do Google Calendar (`googleEventId`) para sincronização e consulta de prazos por período para o calendário (`GET /api/tarefas/agenda`) filtrando por início, fim, conclusão, tipo, processo e responsável.
    *   **Agenda de Audiências:** Agendamento com validação contra datas retroativas, mapeamento obrigatório de `responsavelId`, pauta global harmonizada por período em `LocalDate` (`/api/audiencias/agenda?inicio=&fim=&status=&processoId=&responsavelId=`), edição cadastral, exclusão e alteração de status (`AGENDADA`, `REALIZADA`, `CANCELADA`).
    *   **Dashboard Executiva:** Agregação de métricas em tempo real para o advogado logado (`GET /api/dashboard`) ou por ID específico (`GET /api/dashboard/{usuarioId}`), blindada com construtor defensivo contra valores nulos (números inicializados como `0` e listas como `ArrayList` vazias).

*   **Central de Notificações Operacionais:**
    *   Endpoint unificado `GET /api/notificacoes/resumo?data=YYYY-MM-DD`.
    *   Extração automática e segura do usuário logado via `Principal` do JWT.
    *   Agrupamento de audiências do dia para o advogado logado, tarefas pendentes com vencimento até a data e faturamentos a receber vencidos ou com vencimento no dia.
    *   Retorno tipado via Enums (`TipoNotificacaoEnum`, `DestinoNotificacaoEnum`, `TipoRecursoNotificacaoEnum`).

*   **Busca Global Multidomínio:**
    *   Endpoint unificado `GET /api/busca?q=&tipos=&limit=`.
    *   Pesquisa textual cross-domain que varre simultaneamente as tabelas de Processos (número CNJ e assunto), Clientes (nome, CPF/CNPJ e e-mail) e Usuários (nome e e-mail).
    *   Filtragem opcional pelo enum `TipoItemBuscaEnum` (`PROCESSO`, `CLIENTE`, `USUARIO`) e limitação de resultados configurável.

*   **Monitoramento, Automação & Integração com Tribunais (Novo Módulo):**
    *   **Telemetria de Conectividade:** Endpoint `GET /api/integracoes/tribunais/status` com monitoramento em tempo real da integridade dos serviços judiciais (TJRS, TRF4, TRT4, STJ, DataJud) via enum `StatusTribunalEnum` (`OPERACIONAL`, `DEGRADADO`, `INDISPONIVEL`).
    *   **Gestão de Certificados Digitais A1:** Upload seguro (`POST /api/certificados/upload`), consulta de vigência (`GET /api/certificados`) e exclusão (`DELETE /api/certificados/{id}`). Criptografia de chave privada e senha mestre com AES-256-GCM e mTLS para handshake HTTPS com tribunais.
    *   **Espelhamento Automático de Andamentos:** Varredura periódica e assíncrona dos autos processuais, cadastrando movimentações em `tb_andamento` com `tipo = AUTOMATICO` e hash antifalhas/antiduplicidade.
    *   **Sincronização Sob Demanda:** Rota REST `POST /api/integracoes/tribunais/processos/{id}/sincronizar` para atualização forçada de um processo pelo advogado.
    *   **Malha de Resiliência:** Padrões Circuit Breaker, Retry com Exponential Backoff e Jitter, e isolamento de falhas (Fail-Safe) para absorver quedas e instabilidades dos portais dos tribunais.
    *   **Auditoria de Varredura:** Rota paginada `GET /api/integracoes/tribunais/logs` com registro analítico de execuções, latência e eventuais falhas.

*   **Integração Google Cloud & Autenticação OAuth 2.0 Unificada:**
    *   **Gerenciador Central de Tokens (`GoogleOAuthTokenManager`):** Componente corporativo dedicado na camada de infraestrutura que centraliza as credenciais OAuth 2.0 (`client_id`, `client_secret`, `refresh_token`), executando a renovação automatizada de Access Token via chamada POST à API de autenticação do Google (`https://oauth2.googleapis.com/token`) sem replicação de código.
    *   **Sincronização One-Way do Google Calendar via Webhook (`POST /api/integracoes/google-calendar/webhook`):**
        *   Recepção de notificações push do Google Calendar com processamento assíncrono e padrão *Thin Payload* (consulta direta à Google Calendar API v3 utilizando o token renovado pelo `GoogleOAuthTokenManager`).
        *   **Fluxo Estritamente One-Way:** O sistema apenas importa eventos do Google Calendar. Tarefas, audiências e compromissos internos nunca são exportados para o Google.
        *   **Tratamento de Domínio como Tarefas:** Compromissos importados convertem-se em instâncias da entidade `Tarefa` (`tb_tarefa`) associadas ao advogado (`Usuario`) com tipo `TipoTarefaEnum.ATENDIMENTO`.
        *   **Idempotência e Versionamento (`googleEventId`):** Identificador externo indexado que garante operações de *upsert* transparentes e prevenção de duplicidades.
    *   **Integração com Google Drive (`GoogleDriveStorageService`):** Utiliza o mesmo `GoogleOAuthTokenManager` para geração e injeção do Access Token nas chamadas de persistência e download de arquivos em nuvem.

*   **GED (Gestão Eletrônica de Documentos) & Motor de Emissão:**
    *   Armazenamento primário de arquivos em nuvem via Google Drive (`GoogleDriveStorageService`) com suporte alternativo em disco local (`LocalStorageService`) vinculado a clientes e processos.
    *   Upload multipart (`POST /api/documentos/upload`), listagem de anexos por cliente (`GET /api/documentos/cliente/{clienteId}`), listagem por processo (`GET /api/documentos/processo/{processoId}`), download com detecção dinâmica de MediaType (`GET /api/documentos/{id}/download`) e exclusão física/lógica sincronizada (`DELETE /api/documentos/{id}`).
    *   **Motor Oficial de Geração de Documentos (`PdfDocumentGeneratorService`):** Emissão de PDFs com biblioteca iText, embutimento obrigatório de fontes TrueType (`Bookman Old Style` via `BaseFont.EMBEDDED`), cabeçalho e rodapé fixos automatizados via eventos de página:
        *   **Procuração Ad Judicia & Declaração de Hipossuficiência (AJG):** Emissão via `GET /api/clientes/{id}/procuracao` com parametrização dinâmica de ação, vara, comarca e toggle booleano para impressão condicional da página de declaração.
        *   **Contrato de Prestação de Serviços e Honorários Advocatícios:** Emissão via `GET /api/clientes/{id}/contrato-honorarios` com injeção de cláusulas de objeto, ação, vara, comarca, valor acordado e objetivo da demanda, com concordância e flexão gramatical por sexo do contratante.

*   **Inteligência Artificial & Automação (Spring AI):**
    *   Integração nativa com Spring AI (`spring-ai-openai-spring-boot-starter`).
    *   **Resumo Preparatório de Audiências:** Análise inteligente de peças processuais com persistência do resumo gerado diretamente no campo `resumoPreparatorioIa` da entidade `Audiencia` (`POST /api/audiencias/{id}/gerar-resumo-ia`).
    *   Endpoint dedicado para consultas avulsas de IA (`POST /api/ia/resumos/audiencia`).

---

## 2. Modelo de Domínio e Entidades

Todas as entidades de persistência herdam de `AuditableEntity` (ou possuem auditoria e UUID como identificador primário), garantindo rastreabilidade e integridade.

| Entidade | Atributos Principais | Relacionamentos / Tabela | Regras de Negócio e Endpoints |
| :--- | :--- | :--- | :--- |
| **Usuario** | `id`, `nome`, `email`, `senhaHash`, `perfil` (`ADMIN`, `ADVOGADO`, `SECRETARIA`), `oab`, `ativo` | 1:N Processos, 1:N Tarefas (`tb_usuario`) | E-mail único. Cadastro restrito a administradores. Tratamento de OAB vazia como `null`. A senha nunca é retornada nas saídas. Endpoints: `POST /api/usuarios`, `GET /api/usuarios` (paginado), `GET /api/usuarios/{id}`, `GET /api/usuarios/advogados`, `GET /api/auth/me`. |
| **PasswordResetToken** | `id`, `token`, `usuarioId`, `dataExpiracao`, `usado` | Tabela `tb_password_reset_token` | Token UUID único, expiração de 15 minutos, controle de uso único. Endpoints: `POST /api/auth/recuperar-senha`, `POST /api/auth/redefinir-senha`. |
| **Escritorio** | `id`, `razaoSocial`, `nomeFantasia`, `cnpj`, `registroOabSociedade`, `telefone`, `whatsapp`, `email`, `cep`, `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `uf` | Tabela `escritorios` (Singleton/Tenant) | CNPJ único (14 dígitos). Higienização de caracteres não numéricos. Endereço padrão: Rua Tiradentes, 676, Ijuí/RS. OAB: OAB/RS 121.837. Endpoints: `GET /api/configuracoes/escritorio`, `PUT /api/configuracoes/escritorio`. |
| **Cliente** | `id`, `nome`, `tipo` (`FISICA`, `JURIDICA`), `cpfCnpj`, `dataNascimento`, `estadoCivil`, `profissao`, `sexo`, `telefone`, `email`, endereço completo | 1:N Processos, 1:N Documentos (`tb_cliente`) | Validação estrita de CPF/CNPJ. Endpoints: `POST /api/clientes`, `GET /api/clientes` (paginado com `?q=` e `?tipo=`), `GET /api/clientes/{id}`, `PUT /api/clientes/{id}`, emissão de Procuração (`GET /api/clientes/{id}/procuracao`) e Contrato de Honorários (`GET /api/clientes/{id}/contrato-honorarios`). |
| **Processo** | `id`, `numeroCnj`, `assunto`, `faseAtual`, `parteAdversa`, `cpfCnpjParteAdversa`, `papelCliente` (`AUTOR`, `REU`, `TERCEIRO_INTERESSADO`), `valorCausa`, `comarca`, `dataCriacao`, `arquivado`, `dataUltimaSincronizacao`, `statusSincronizacao` (`SINCRONIZADO`, `PENDENTE`, `FALHA`, `EM_ANDAMENTO`), `tribunalOrigem` (`TJRS`, `TRF4`, `TRT4`, `STJ`, `STF`), `grauJurisdicao` | N:1 Cliente, N:1 Usuario, 1:N Documentos, 1:N Tarefas, 1:N Andamentos (`tb_processo`) | CNJ único. Qualificação da lide e rastreador de sincronização com tribunais. DTO aninha `ClienteResumoDTO cliente` e `UsuarioResumoDTO advogado` (`@JsonAlias`). Endpoints: `POST /api/processos`, `GET /api/processos` (paginado), `GET /api/processos/{id}`, `PUT /api/processos/{id}`, `PATCH /api/processos/{id}/arquivar`, `PATCH /api/processos/{id}/desarquivar`, `POST /api/integracoes/tribunais/processos/{id}/sincronizar`. |
| **Andamento** | `id`, `dataHora`, `descricao`, `tipo` (`AUTOMATICO`, `MANUAL`, `IA`), `codigoMovimentacaoTribunal`, `hashMovimentacao`, `linkPecaTribunal` | N:1 Processo, N:1 Documento (Opc) (`tb_andamento`) | Histórico cronológico processual. Prevenção de duplicidade por `hashMovimentacao` único em varreduras de tribunais. Endpoints: `POST /api/processos/{processoId}/andamentos`, `GET /api/processos/{processoId}/andamentos`. |
| **Tarefa** | `id`, `descricao`, `dataVencimento`, `concluida`, `tipo` (`DILIGENCIA`, `PRAZO`, `CONTATO`, `ATENDIMENTO`), `googleEventId` | N:1 Usuario, N:1 Processo (Opc) (`tb_tarefa`) | Alimenta To-Do list, Agenda e Dashboard. Armazena `googleEventId` (ID do compromisso externo no Google Calendar) para sincronização One-Way via webhook e garantia de idempotência. Compromissos importados recebem o tipo `ATENDIMENTO`. DTO aninha `UsuarioResumoDTO usuario` (`@JsonAlias({"usuarioId"})`), `ProcessoResumoDTO processo` e `googleEventId`. Endpoints: `POST /api/tarefas`, `PUT /api/tarefas/{id}`, `DELETE /api/tarefas/{id}`, `PATCH /api/tarefas/{id}/concluir`, `GET /api/tarefas/dashboard/{usuarioId}`, `GET /api/tarefas/agenda`, `POST /api/integracoes/google-calendar/webhook`. |
| **Faturamento**| `id`, `descricao`, `valor`, `tipo` (`HONORARIOS`, `CUSTAS`, `DESPESAS_ESCRITORIO`, `CONSULTA_AVULSA`), `status` (`PENDENTE`, `PAGO`, `PARCIALMENTE_PAGO`, `CANCELADO`), `natureza`, `dataVencimento`, `dataPagamento`, `numeroParcela`, `totalParcelas`, `origemPagamento` (`DIRETO_CLIENTE`, `TERCEIRO_SUCUMBENCIA`), `valorHonorariosRetidos`, `valorRepasseCliente`, `statusRepasse` (`PENDENTE`, `REPASSADO`), `formaRepasse`, `dadosBancariosCliente`, `dataRepasse` | N:1 Processo (Opc), N:1 Cliente (`tb_faturamento`) | Controle financeiro, parcelamento, consultas avulsas e edição cadastral. O relacionamento com `Processo` é anulável, vinculando-se unicamente ao `Cliente` nos lançamentos de `CONSULTA_AVULSA`. Processamento atômico de criação, liquidação e status `PAGO` via caso de uso em única transação. Suporte completo a edição cadastral imperativa via `EditarFaturamentoUseCase` (`EditarFaturamentoDTO`). DTO aninha `ProcessoResumoDTO processo` (opcional) e `ClienteResumoDTO cliente`. Endpoints: `GET /api/faturamentos/resumo`, `GET /api/faturamentos`, `POST /api/faturamentos`, `PUT /api/faturamentos/{id}`, `PATCH /api/faturamentos/{id}`, `POST /api/faturamento/parcelamento`, `POST /api/faturamentos/consulta-avulsa`, `PATCH /api/faturamento/{id}/liquidar`, `PATCH /api/faturamento/{id}/liquidar-parcial`, `PATCH /api/faturamento/{id}/repassar`. |
| **Audiencia** | `id`, `dataHora`, `local`, `observacoes`, `status`, `resumoPreparatorioIa` | N:1 Processo, N:1 Usuario (`tb_audiencia`) | Validação de data futura no agendamento. DTO aninha `ProcessoResumoDTO processo` e `UsuarioResumoDTO responsavel` (`@JsonAlias({"responsavelId"})`). Endpoints: `POST /api/audiencias`, `GET /api/audiencias/{id}`, `PUT /api/audiencias/{id}`, `DELETE /api/audiencias/{id}`, `PATCH /api/audiencias/{id}/status`, `GET /api/audiencias/agenda`, `POST /{id}/gerar-resumo-ia`. |
| **Documento** | `id`, `nomeArquivo`, `titulo`, `caminhoStorage`, `indexadoIA` | N:1 Processo (Opc), N:1 Cliente (Opc) (`tb_documento`) | GED e armazenamento seguro em nuvem ou disco. DTO aninha `ClienteResumoDTO cliente` (`@JsonAlias({"clienteId"})`) e `ProcessoResumoDTO processo` (`@JsonAlias({"processoId"})`). Upload (`POST /api/documentos/upload`), listagem por cliente (`GET /api/documentos/cliente/{clienteId}`), listagem por processo (`GET /api/documentos/processo/{processoId}`), download (`GET /api/documentos/{id}/download`) e exclusão física/lógica (`DELETE /api/documentos/{id}`). |
| **CertificadoDigital**| `id`, `nomeArquivo`, `alias`, `dataEmissao`, `dataExpiracao`, `emissor`, `titular`, `cpf`, `ativo`, `caminhoArquivoCriptografado`, `senhaCriptografada` | N:1 Usuario (`tb_certificado_digital`) | Gestão de certificados ICP-Brasil A1 (PKCS#12) para autenticação mTLS em tribunais. Criptografia AES-256-GCM para arquivo e senha em repouso. Endpoints: `POST /api/certificados/upload`, `GET /api/certificados`, `DELETE /api/certificados/{id}`. |
| **SincronizacaoTribunalLog** | `id`, `processoId`, `tribunal`, `tipoIntegracao`, `dataHoraInicio`, `dataHoraFim`, `duracaoMs`, `status` (`SUCESSO`, `FALHA_CONEXAO`, `FALHA_AUTENTICACAO`, `RATE_LIMITED`, `CIRCUITO_ABERTO`), `quantidadeMovimentacoesNovas`, `mensagemErro`, `detalhesTecnicos` | N:1 Processo (Opc) (`tb_sincronizacao_tribunal_log`) | Auditoria detalhada de varreduras automáticas e manuais nos portais de tribunais. Endpoint: `GET /api/integracoes/tribunais/logs`. |
| **TribunalConfig** | `id`, `sigla`, `nome`, `tipoIntegracao` (`DATAJUD_API`, `EPROC_SCRAPER`, `PJE_MNI`), `urlBase`, `ativo`, `intervaloMinimoRequisicoesMs`, `statusCircuito` (`FECHADO`, `ABERTO`, `SEMI_ABERTO`) | Singleton / Parametrização (`tb_tribunal_config`) | Configuração dinâmica de conexões com tribunais e parâmetros de rate limiting e Circuit Breaker. Endpoint: `GET /api/integracoes/tribunais/status`. |

---

## 3. Diretrizes Técnicas e Decisões Arquiteturais

### 3.1. Paradigma Java Puro e Imperativo
*   **Proibição Absoluta:** É expressamente vedado o uso de Expressões Lambda (`->`), Stream API (`.stream()`, `.filter()`, `.map()`, `.collect()`) e métodos funcionais encadeados de `Optional` (`.orElseThrow()`, `.map()`, `.ifPresent()`).
*   **Verificação Clássica de `Optional`:**
    ```java
    Optional<Entidade> opt = repository.findById(id);
    if (opt.isEmpty()) {
        throw new RecursoNaoEncontradoException("Recurso não encontrado!");
    }
    Entidade entidade = opt.get();
    ```
*   **Manipulação de Coleções:** Realizada exclusivamente com laços `for` tradicionais indexados ou *enhanced-for*, com instanciação explícita de coleções mutáveis (`List<DTO> lista = new ArrayList<>(); lista.add(...)`).

### 3.2. Consultas Dinâmicas em SQL Nativo no PostgreSQL (`nativeQuery = true`)
*   **Decisão Arquitetural:** O projeto descarta o uso da `CriteriaBuilder` API em prol de manutenibilidade direta e legibilidade pela equipe através de SQL nativo.
*   **Filtros Dinâmicos com Tratamento de Nulos:** As consultas de paginação e filtragem são declaradas diretamente nas interfaces principais dos Repositories utilizando `@Query(value = "...", countQuery = "...", nativeQuery = true)`.
*   **Prevenção Crítica do Erro `lower(bytea) does not exist`:**
    *   No PostgreSQL, quando um parâmetro nomeado no JPQL/SQL nativo recebe `null`, o driver JDBC não consegue inferir o tipo da coluna na comparação `(:termo IS NULL OR ...)`, convertendo o parâmetro para `bytea` e quebrando a execução com erro de função inexistente (`lower(bytea)`).
    *   **Regra Obrigatória:** Todo parâmetro opcional/anulável em queries nativas DEVE possuir cast explícito de tipo no SQL:
        ```sql
        WHERE (CAST(:termo AS text) IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', CAST(:termo AS text), '%')))
          AND (CAST(:tipo AS text) IS NULL OR c.tipo = CAST(:tipo AS text))
          AND (CAST(:clienteId AS uuid) IS NULL OR p.cliente_id = CAST(:clienteId AS uuid))
          AND (CAST(:ativo AS boolean) IS NULL OR u.ativo = CAST(:ativo AS boolean))
          AND (CAST(:vencimentoDe AS date) IS NULL OR f.data_vencimento >= CAST(:vencimentoDe AS date))
        ```
    *   As queries com suporte a paginação declaram obrigatoriamente a cláusula `countQuery` correspondente com os mesmos filtros e casts explícitos.

### 3.3. Padronização da Serialização de Paginação (Spring Boot 3.3+ `VIA_DTO`)
*   No Spring Boot 3.3+, a serialização padrão de instâncias de `PageImpl` foi descontinuada para evitar acoplamento interno com classes do framework.
*   O sistema adota a padronização oficial via Spring Data Web configurada na classe `WebConfig.java`:
    ```java
    @Configuration
    @EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
    public class WebConfig {
    }
    ```
*   Isso garante uma representação JSON estável e padronizada para o front-end (`content`, `page`: `{ size, number, totalElements, totalPages }`).

### 3.4. Tratamento Global de Exceções e Respostas de Erro Padronizadas
Centralizado na classe `GlobalExceptionHandler` (`@RestControllerAdvice`), retornando schemas rigorosamente tipados:
*   **`ErroPadraoDTO`:** Estrutura canônica contendo `timestamp` (ISO-8601), `status` (código HTTP numérico), `error` (descrição do erro HTTP) e `message` (mensagem de negócio legível).
    *   Retornado para `401 Unauthorized` (`BadCredentialsException`), `403 Forbidden` (`AccessDeniedException`), `404 Not Found` (`RecursoNaoEncontradoException`), `409 Conflict` (`DataIntegrityViolationException`) e `422 Unprocessable Entity` (`RegraNegocioException`).
*   **`ErroValidacaoDTO`:** Especialização do erro padrão que adiciona o campo `fieldErrors` com uma lista de `CampoErroDTO` (`campo`, `mensagem`).
    *   Retornado para `400 Bad Request` disparado em falhas de validação de argumentos anotados com `@Valid` (`MethodArgumentNotValidException`).

### 3.5. Documentação OpenAPI 3 (Swagger / Springdoc 2.6.0) e Segregação de Contratos
*   **Arquitetura de Contratos Segregados:** 100% das anotações de documentação OpenAPI (`@Tag`, `@Operation`, `@ApiResponses`, `@ParameterObject`) foram extraídas dos controladores e isoladas em interfaces contratuais no pacote `presentation.openapi` (`AndamentoControllerOpenApi`, `AudienciaControllerOpenApi`, `FaturamentoControllerOpenApi`, etc.).
*   **Benefícios Arquiteturais:**
    1. Os controladores Java ficam enxutos, legíveis e focados estritamente na orquestração web e invocação de casos de uso.
    2. **Isolamento de Validações Bean Validation (`HV000151`):** Anotações de validação como `@Valid` são mantidas exclusivamente nas interfaces contratuais, prevenindo a redefinição de restrições em métodos sobrescritos proibida pela especificação Jakarta Bean Validation.
    3. Suporte universal a `@ParameterObject` nos parâmetros `Pageable` para geração de parâmetros planos (`page`, `size`, `sort`) no Swagger UI.

### 3.6. DTOs de Resumo Aninhados e Retrocompatibilidade Total (Ondas 1, 2 e 3)
*   **Eliminação de IDs Crus nas Respostas:** Para evitar que o front-end precise realizar chamadas adicionais ("round-trips") para buscar nomes ou detalhes básicos de entidades relacionadas em listagens paginadas, todos os DTOs de resposta aninham objetos resumidos de domínio:
    *   `ProcessoResumoDTO`: Contém `id`, `numeroCnj`, `clienteId` e `nomeCliente`.
    *   `ClienteResumoDTO`: Contém `id`, `nome`, `cpfCnpj` e `tipo`.
    *   `UsuarioResumoDTO`: Contém `id`, `nome`, `email`, `oab` e `perfil`.
*   **Padronização nos DTOs Principais:**
    *   `ProcessoDTO`: Aninha `ClienteResumoDTO cliente` e `UsuarioResumoDTO advogado`.
    *   `FaturamentoDTO`: Aninha `ProcessoResumoDTO processo` (opcional) e `ClienteResumoDTO cliente`.
    *   `AudienciaDTO`: Aninha `ProcessoResumoDTO processo` e `UsuarioResumoDTO responsavel`.
    *   `TarefaDTO`: Aninha `UsuarioResumoDTO usuario`, `ProcessoResumoDTO processo` e expõe `googleEventId`.
    *   `DocumentoDTO`: Aninha `ClienteResumoDTO cliente` e `ProcessoResumoDTO processo`.
*   **Arquitetura Defensiva e Retrocompatibilidade Absoluta:**
    1.  **Anotações `@JsonAlias`:** Permitem que requisições legadas de clientes que enviem propriedades planas (`clienteId`, `advogadoId`, `usuarioId`, `processoId`, `responsavelId`) continuem sendo mapeadas com perfeição pelo Jackson.
    2.  **Métodos Acessores `@JsonIgnore`:** Disponibilizam getters como `.clienteId()`, `.advogadoId()`, `.processoId()`, `.usuarioId()` e `.responsavelId()` para que controladores e casos de uso mantenham sua assinatura sem refatorações destrutivas.
    3.  **Construtores Sobrecarregados:** Oferecem suporte direto a testes unitários e instanciações que passem apenas identificadores UUID.
    4.  **Desserializadores Delegados:** Os DTOs de resumo implementam `@JsonCreator(mode = DELEGATING)` permitindo que strings puras com UUID sejam automaticamente encapsuladas no respectivo record de resumo.
*   **`ResumoDashboardDTO`:** Implementa construtor defensivo que substitui valores nulos de contadores por `0`, valores monetários por `BigDecimal.ZERO` e coleções por listas vazias (`new ArrayList<>()`), garantindo que o dashboard nunca quebre o front-end por campos indefinidos (`undefined`/`null`).
*   **`UsuarioResponseDTO`:** Suprime qualquer atributo de credencial, retornando estritamente dados públicos e operacionais do usuário (`id`, `nome`, `email`, `perfil`, `oab`, `ativo`).

### 3.7. Precisão e Integridade Financeira
*   Todos os campos monetários mapeados no banco de dados (`valor`, `valorCausa`, `valorHonorariosRetidos`, `valorRepasseCliente`) utilizam rigorosamente `@Column(precision = 15, scale = 2) private BigDecimal ...` para garantir integridade contábil e evitar imprecisões de arredondamento.
*   Nas interfaces OpenAPI contratuais, a validação de restrição (`@Valid`) é mantida exclusivamente nas interfaces para evitar conflitos com o Bean Validation (`HV000151`).
*   **Edição Defensiva de Lançamentos:** A edição de lançamentos financeiros via `EditarFaturamentoUseCase` valida individualmente a presença dos campos alterados sem sobrescrever valores omitidos com nulo, assegura que o valor monetário permaneça estritamente positivo e automatiza a quitação quando o status transiciona para `PAGO`.

### 3.8. Sincronização One-Way do Google Calendar via Push Notifications (Webhooks)
*   **Fluxo Unidirecional (Google -> Sistema):** O backend atua estritamente como receptor de eventos do Google Calendar. Nenhuma tarefa, audiência ou compromisso criado internamente no sistema jurídico é transmitido para os servidores do Google.
*   **Processamento de Webhooks:** Endpoint dedicado (`POST /api/integracoes/google-calendar/webhook`) recebe notificações push disparadas pelo canal de sincronização da Google Calendar API v3.
*   **Mapeamento Unificado de Domínio como Tarefas:** Compromissos externos são transformados em instâncias de `Tarefa` associadas ao respectivo `Usuario` (advogado) e tipadas com `TipoTarefaEnum.ATENDIMENTO`.
*   **Idempotência e Versionamento (`googleEventId`):**
    1. A entidade `Tarefa` possui a coluna `google_event_id VARCHAR(255)` indexada.
    2. Ao processar o webhook via `SincronizarEventoGoogleCalendarUseCase`, o sistema consulta imperativamente `tarefaRepository.findByGoogleEventId(googleEventId)`.
    3. Caso o evento já exista (`opt.isPresent()`), realiza a atualização dos dados (descrição, data e horário de vencimento, ou tratamento de cancelamento caso o evento tenha sido excluído no Google).
    4. Caso seja novo (`opt.isEmpty()`), instancia e persiste uma nova tarefa vinculada ao advogado com `tipo = ATENDIMENTO` e `googleEventId`.
    5. Todo o processamento segue rigorosamente o Paradigma Imperativo Puro (zero lambdas e zero streams).

### 3.9. Lançamento e Liquidação Atômica de Consultas Avulsas (Sem Vínculo Processual)
*   **Desvinculação Processual:** Para consultas pontuais sem litígio (ex: R$ 250,00), a entidade `Faturamento` passa a ter o relacionamento `Processo` anulável (`processo_id NULL` em `tb_faturamento`), exigindo a vinculação direta à entidade `Cliente` (`cliente_id`).
*   **Novo Tipo de Faturamento:** Mapeamento de `TipoFaturamentoEnum.CONSULTA_AVULSA`.
*   **Transação Atômica via Use Case (`RegistrarConsultaAvulsaUseCase`):**
    1. Validação imperativa da existência do cliente via `ClienteRepository.findById` (`opt.isEmpty()`).
    2. Instanciação direta da entidade `Faturamento` configurando `natureza = NaturezaFaturamentoEnum.A_RECEBER`, `tipo = TipoFaturamentoEnum.CONSULTA_AVULSA`, valor recebido, `dataVencimento = dataPagamento` (ou data corrente) e `processo = null`.
    3. Atribuição imediata de `status = StatusFaturamentoEnum.PAGO` e data efetiva de quitação.
    4. Persistência atômica sob `@Transactional`, eliminando etapas manuais intermediárias de criação seguida de liquidação e prevenindo inconsistências de caixa.

### 3.10. Unificação da Gestão de Autenticação OAuth 2.0 (Google Cloud)
*   **Princípio da Responsabilidade Única (SRP):** Toda a lógica de renovação de credenciais OAuth 2.0 do Google foi centralizada no componente corporativo `GoogleOAuthTokenManager` (`infrastructure.security`).
*   **Desacoplamento de Controladores:** Removeu-se completamente qualquer injeção de credenciais globais (`client_id`, `client_secret`, `refresh_token`), chamadas HTTP de renovação via `RestTemplate` ou manipulação de JSON de tokens de controladores REST.
*   **Compartilhamento Transversal de Autenticação:** O `GoogleOAuthTokenManager` atende unificadamente:
    *   **Google Drive:** Injeção do Access Token nas requisições da biblioteca oficial do Google Drive no `GoogleDriveStorageService`.
    *   **Google Calendar:** Injeção do Access Token nas consultas à API v3 de eventos no `GoogleCalendarWebhookController`.
*   **Tolerância a Falhas na Inicialização:** As injeções utilizam valores padrão vazios (`@Value("${google.oauth.client.id:}")`, `@Value("${google.oauth.client.secret:}")`, `@Value("${google.oauth.refresh.token:}")`), prevenindo quebras no startup em ambientes de teste ou sem credenciais configuradas.

### 3.11. Blindagem de Encoding UTF-8 e Prevenção de Mojibake
*   **Padronização no Build Maven:** Configurado explicitamente no `pom.xml`:
    *   `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>`
    *   `<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>`
    *   `<encoding>UTF-8</encoding>` na configuração do `maven-compiler-plugin`.
*   **Forçamento de Encoding HTTP no Servlet:** Em `application.properties`:
    *   `server.servlet.encoding.charset=UTF-8`
    *   `server.servlet.encoding.force=true`
*   **Higienização Completa da Documentação OpenAPI:** Todas as 16 interfaces de documentação em `presentation.openapi` foram integralmente limpas de caracteres corrompidos (*mojibake* decorrentes de decodificação Latin-1/Windows-1252), garantindo tags limpas (ex: `"Configurações do Escritório"`, `"Notificações"`, `"Audiências"`, `"Autenticação"`, `"Usuários"`) e descrições técnicas em conformidade gramatical para renderização impecável no Swagger UI.

### 3.12. Gestão Segura de Certificados Digitais A1 (PKCS#12) e Autenticação mTLS
*   **Criptografia em Repouso:** Os certificados A1 (`.pfx` ou `.p12`) e suas senhas de desbloqueio NUNCA são armazenados em texto claro. O sistema emprega criptografia autenticada simétrica **AES-256-GCM** com chave de envelope configurada por variável de ambiente segura (`APP_CERTIFICATE_SECRET_KEY`).
*   **Carregamento e Validação X.509 em Memória:**
    *   Parsing defensivo via `KeyStore.getInstance("PKCS12")`.
    *   Inspeção estrita da cadeia de certificação e extração de atributos públicos (CN do titular, CN da Autoridade Certificadora emissora, período de vigência e CPF do advogado extraído da extensão ICP-Brasil OID `2.16.76.1.3.1`) sem expor nem persistir chaves privadas em logs.
*   **Fábrica Dinâmica de Conexão Segura (`CustomSSLContextFactory`):**
    *   Decripta o material criptográfico em memória sob demanda, inicializando instâncias de `SSLContext` configuradas com `KeyManagerFactory` e `TrustManagerFactory` para viabilizar handshake seguro mTLS em clientes HTTP direcionados aos portais judiciais.
*   **Monitoramento Ativo de Vigência:**
    *   Geração preventiva de avisos e notificações operacionais na régua de 30 dias e 7 dias antes do vencimento do certificado digital do advogado.

### 3.13. Arquitetura de Resiliência, Circuit Breaker e Tratamento de Falhas com Tribunais
*   **Padrão Circuit Breaker Aplicado a Serviços Judiciais:**
    *   Cada tribunal ou serviço integrado (TJRS, TRF4, TRT4, DataJud) possui controle de estado isolado: `FECHADO` (tráfego normal), `ABERTO` (serviço indisponível, requisições abortadas preventivamente) e `SEMI_ABERTO` (testes de contingência).
    *   Limiar de tolerância: acúmulo de 5 falhas consecutivas de conexão ou HTTP 5xx aciona a abertura do disjuntor por um período de cooldown (15 minutos), prevenindo exaustão de threads por timeouts e refletindo status `INDISPONIVEL` na telemetria da aplicação.
*   **Retry com Exponential Backoff e Jitter:**
    *   Retentativas com espaçamento progressivo exponencial ($1\text{s}, 2\text{s}, 4\text{s}$) e fator de dispersão (*jitter*) aplicadas exclusivamente a falhas de transporte transitórias (`SocketTimeoutException`, `ConnectException`, HTTP 502/503/504).
    *   Falhas de autenticação (`401 Unauthorized`), certificados revogados/vencidos ou números CNJ inválidos abortam no primeiro ciclo sem retentativa inútil.
*   **Isolamento Fail-Safe e Dead Letter Queue:**
    *   A varredura de cada processo em lote é executada dentro de bloco de isolamento transacional e captura de exceção. A falha de sincronização de um processo ou tribunal jamais interrompe a rotina dos demais processos da fila.
    *   Processos que falharem após o ciclo de retry são marcados com `status_sincronizacao = 'FALHA'`, registrados na tabela `tb_sincronizacao_tribunal_log` e redirecionados para reprocessamento na próxima janela de menor carga.

### 3.14. Agendamento Assíncrono com Spring Scheduler e Execução Imperativa Pura
*   **Pool Dedicado de Execução (`ThreadPoolTaskScheduler`):**
    *   Habilitação do agendamento via anotação `@EnableScheduling` em classe de configuração dedicada (`SchedulerConfig.java`) com pool dimensionado (`poolSize = 5`).
    *   Assegura total isolamento entre tarefas agendadas em segundo plano e as threads de atendimento de requisições HTTP da API Web.
*   **Estratégia de Varredura e Rate Limiting:**
    *   Cron Noturno (`0 0 2 * * *` — 02h00 da madrugada) percorre imperativamente todos os processos ativos cadastrados.
    *   Aplicação de atraso obrigatório (*throttling*) entre 1.500 ms e 3.000 ms entre requisições direcionadas ao mesmo tribunal para prevenir bloqueios de IP ou desafios de proteção anti-bot.
*   **Triagem Imperativa de Tarefas a partir de Intimações (Comunica PJe / DJEN):**
    *   Para cada lote de intimações inéditas persistidas no banco (`tb_intimacao_pje`), o sistema executa triagem imperativa (`if/else if` com `equalsIgnoreCase`):
        1. *Descarte de Informativos:* `"Lista de distribuição"` e `"Ata de sessão"` executam `continue` e não poluem a agenda do advogado com tarefas.
        2. *Alerta de Pauta (Diligência):* `"Pauta de julgamento"` gera tarefa de `tipo = DILIGENCIA` com descrição `"[DILIGÊNCIA - PAUTA] Proc. {numeroProcesso} ({siglaTribunal})"`.
        3. *Gestão de Prazos (Intimação/Citação):* Gera tarefa de `tipo = PRAZO` com prefixos categorizados pelo `tipoDocumento`:
           - `"Sentença"`: `"[URGENTE - SENTENÇA]"`
           - `"DESPACHO/DECISÃO"`: `"[URGENTE - DECISÃO]"`
           - `"Ato ordinatório"`: `"[PRAZO - ATO ORDINATÓRIO]"`
           - `"Notificação"`: `"[PRAZO - NOTIFICAÇÃO]"`
           - Demais tipos: `"[PRAZO - ATENÇÃO]"`
        4. Vinculação automática com a entidade `Processo` (quando localizado na base) e com o advogado titular da OAB consultada, com persistência em lote via `tarefaRepository.saveAll(...)`.
*   **Aderência ao Paradigma Imperativo:**
    *   Toda a orquestração de filas, repasses de lote e tratamento de contingência é codificada exclusivamente com laços `for` clássicos, verificações defensivas `opt.isEmpty()` e instanciação explícita de coleções mutáveis, mantendo 100% de aderência ao princípio arquitetural de Zero Lambdas e Zero Streams.

### 3.15. Sanitização Robusta e Enriquecimento de Andamentos Processuais do PJe
*   **Desafio dos Dados dos Portais Judiciais:** As publicações extraídas das rotas públicas do DJEN/Comunica PJe contêm fragmentos indesejados de marcação (blocos CSS inline `<style>`, tags `<br>`, entidades HTML como `&ccedil;`, `&ordm;` e `&nbsp;`, tabulações e abismos verticais de quebras repetitivas).
*   **Pipeline de Higienização com `HtmlUtils` e Regex Defensivo:**
    1. *Preservação Estrutural de Quebras:* Conversão insensível a maiúsculas/minúsculas de tags `<br>` (com ou sem barra) para quebra de linha simples (`\n`) e tags de fechamento `</p>` para quebra de parágrafo dupla (`\n\n`).
    2. *Expurgo Integral de CSS:* Remoção completa de blocos `<style.*?>.*?</style>` via regex dotall/case-insensitive (`(?is)`), prevenindo que declarações CSS inline poluam a visualização textual.
    3. *Eliminação de Tags Remanescentes:* Remoção de qualquer marcação HTML residual (`<[^>]*>`).
    4. *Decodificação Semântica com `HtmlUtils`:* Tradução de entidades HTML via `HtmlUtils.htmlUnescape(...)` e substituição explícita do caractere não-separável `\u00A0` (`&nbsp;`) por espaço em branco ASCII padrão.
    5. *Normalização Espacial:* Redução de múltiplos espaços horizontais, tabs (`\t`) e caracteres de controle para um único espaço (`[ \t\x0B\f\r]+` -> `" "`).
    6. *Normalização de Abismos Verticais:* Limitação de quebras de linha consecutivas a no máximo duas (`\n{3,}` -> `\n\n`), finalizando com `.trim()`.
*   **Enriquecimento Estruturado da Descrição do Andamento:**
    *   O andamento gerado automaticamente no processo concatena metadados institucionais com o teor integral limpo:
        `"[PJe - " + siglaTribunal + "] " + tipoComunicacao + " (" + tipoDocumento + ")\nÓrgão: " + nomeOrgao + "\n\n" + textoSanitizado`
*   **Persistência Ilimitada no Banco (`columnDefinition = "TEXT"`):**
    *   O atributo `descricao` na entidade `Andamento` (`tb_andamento`) possui a anotação `@Column(columnDefinition = "TEXT")`, garantindo suporte nativo a decisões e despachos extensos no PostgreSQL sem risco de truncamento ou estouro do limite de `character varying(255)`.

---

## 4. Estrutura de Pacotes

```text
└── src/main/java/com/sistemajuridico/backend/
    ├── core/
    │   ├── domain/
    │   │   ├── enums/                      # PerfilAcessoEnum, TipoTarefaEnum (ATENDIMENTO, PRAZO, DILIGENCIA), StatusAudienciaEnum, StatusTribunalEnum, PapelClienteEnum, OrigemPagamentoEnum, StatusRepasseEnum, TipoFaturamentoEnum (CONSULTA_AVULSA), StatusSincronizacaoEnum, TribunalOrigemEnum, StatusCircuitoEnum...
    │   │   ├── exceptions/                 # RegraNegocioException, RecursoNaoEncontradoException, IntegracaoTribunalException, CertificadoDigitalException...
    │   │   ├── validators/                 # DocumentoValidator (CPF/CNPJ)
    │   │   └── *.java                      # Usuario, Escritorio, PasswordResetToken, Cliente, Processo, Andamento, IntimacaoPje, Tarefa, Faturamento, CertificadoDigital, SincronizacaoTribunalLog, TribunalConfig...
    │   ├── service/                        # Serviços de domínio com lógica imperativa clássica
    │   │   ├── AuthService.java            # Recuperação SMTP, redefinição e alteração de senha
    │   │   └── EscritorioService.java       # Gestão cadastral e higienização dos dados do escritório
    │   └── usecases/                       # Casos de uso imperativos clássicos
    │       ├── AutenticarUsuarioUseCase.java
    │       ├── CadastrarUsuarioUseCase.java / ListarAdvogadosUseCase.java / BuscarUsuarioPorIdUseCase.java / ListarUsuariosUseCase.java
    │       ├── CadastrarClienteUseCase.java / AtualizarClienteUseCase.java / BuscarClientePorIdUseCase.java / ListarClientesUseCase.java
    │       ├── GerarProcuracaoClienteUseCase.java / GerarContratoHonorariosUseCase.java
    │       ├── CadastrarProcessoUseCase.java / AtualizarProcessoUseCase.java / ArquivarProcessoUseCase.java / DesarquivarProcessoUseCase.java / ListarProcessosUseCase.java
    │       ├── CadastrarAudienciaUseCase.java / AlterarStatusAudienciaUseCase.java / ListarAgendaGlobalUseCase.java
    │       ├── CriarTarefaUseCase.java / ConcluirTarefaUseCase.java / ListarTarefasPorPeriodoUseCase.java / ListarTarefasDashboardUseCase.java / SincronizarEventoGoogleCalendarUseCase.java
    │       ├── CadastrarFaturamentoUseCase.java / EditarFaturamentoUseCase.java / GerarParcelamentoUseCase.java / LiquidarFaturamentoUseCase.java / LiquidarParcialFaturamentoUseCase.java / RepassarFaturamentoUseCase.java / RegistrarConsultaAvulsaUseCase.java / ObterResumoFinanceiroUseCase.java / ListarFaturamentosUseCase.java
    │       ├── CadastrarCertificadoUseCase.java / ListarCertificadosUseCase.java / ExcluirCertificadoUseCase.java
    │       ├── SincronizarIntimacoesPjeUseCase.java / SincronizarProcessoTribunalUseCase.java / ObterLogsSincronizacaoUseCase.java / ObterStatusTribunaisUseCase.java
    │       ├── DashboardAdvogadoUseCase.java
    │       ├── ObterResumoNotificacoesUseCase.java
    │       ├── BuscaGlobalUseCase.java
    │       ├── GerarEAnexarResumoAudienciaUseCase.java / GerarResumoAudienciaUseCase.java
    │       ├── UploadDocumentoUseCase.java / DownloadDocumentoUseCase.java / ExcluirDocumentoUseCase.java
    │       └── ListarDocumentosPorClienteUseCase.java / ListarDocumentosPorProcessoUseCase.java / BuscarDocumentoPorIdUseCase.java
    │
    ├── infrastructure/
    │   ├── ai/                             # ResumoAIService, SpringAIResumoService, TextoChunkingService
    │   ├── config/                         # OpenApiConfig, WebConfig (@EnableSpringDataWebSupport VIA_DTO), SchedulerConfig (@EnableScheduling)
    │   ├── document/                       # DocumentGeneratorService, PdfDocumentGeneratorService, PdfExtractionService
    │   ├── integrations/
    │   │   ├── pje/                        # ComunicaPjeClient (DJEN / PJe Público por OAB com Throttling Defensivo)
    │   │   └── tribunais/                  # DataJudClient, EprocAdapter, PjeMniClient
    │   ├── persistence/                    # Repositories JPA com SQL Nativo (UsuarioRepository, ClienteRepository, ProcessoRepository, AndamentoRepository, IntimacaoPjeRepository, TarefaRepository...)
    │   ├── resilience/                     # TribunalCircuitBreakerRegistry, RetryExponentialBackoffService
    │   ├── scheduling/                     # VarreduraPjeScheduler (cron diário 02h00), VarreduraTribunaisScheduler, FilaSincronizacaoService
    │   ├── security/                       # SecurityConfig, CorsConfig, TokenService, JwtAuthenticationFilter, GoogleOAuthTokenManager
    │   │   └── certificados/               # CertificadoCryptoService (AES-256-GCM), CustomSSLContextFactory (mTLS)
    │   └── storage/                        # StorageService, LocalStorageService, GoogleDriveStorageService
    │
    └── presentation/
        ├── controllers/                    # REST Controllers implementando interfaces *OpenApi (inclui GoogleCalendarWebhookController, IntegracaoTribunalController, CertificadoDigitalController...)
        ├── openapi/                        # Interfaces de contrato OpenAPI 3 (@Tag, @Operation, @ApiResponses):
        │   ├── AndamentoControllerOpenApi.java
        │   ├── AudienciaControllerOpenApi.java
        │   ├── AuthControllerOpenApi.java
        │   ├── BuscaGlobalControllerOpenApi.java
        │   ├── CertificadoDigitalControllerOpenApi.java
        │   ├── ClienteControllerOpenApi.java
        │   ├── DashboardControllerOpenApi.java
        │   ├── DocumentoControllerOpenApi.java
        │   ├── EscritorioConfigControllerOpenApi.java
        │   ├── FaturamentoControllerOpenApi.java
        │   ├── GoogleCalendarWebhookControllerOpenApi.java
        │   ├── IntegracaoTribunalControllerOpenApi.java
        │   ├── NotificacaoControllerOpenApi.java
        │   ├── ProcessoControllerOpenApi.java
        │   ├── ResumoAudienciaControllerOpenApi.java
        │   ├── TarefaControllerOpenApi.java
        │   └── UsuarioControllerOpenApi.java
        └── dtos/                           # Records de entrada/saída (UsuarioResponseDTO, ErroPadraoDTO, EditarFaturamentoDTO, CertificadoDigitalDTO, SincronizacaoLogDTO, TribunalStatusDTO...)
```

---

## 5. Status de Implementação Backend

*Todas as metas anteriores de arquitetura, segurança, unificação OAuth 2.0 (Google Drive + Google Calendar), lançamento e liquidação de consultas avulsas, novas rotas de edição financeira, IA de resumos de audiência em dois estágios com chunking, blindagem de encoding UTF-8 em 100% da especificação OpenAPI, persistência no PostgreSQL via SQL nativo e conformidade com o Paradigma Imperativo Puro foram integralmente concluídas e validadas em produção.*

*O projeto inicia formalmente a Fase 3.3 focada no módulo corporativo de **Integração com Sistemas Judiciais dos Tribunais**, contemplando a gestão criptográfica de certificados A1, varredura periódica e sob demanda de processos, conectores DataJud e eproc/PJe, e malha de alta resiliência (Circuit Breaker e Exponential Backoff).*