# Documento de Especificação de Software (PRD) - v3.0

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
    *   **Gestão de Processos:** Ciclo de vida completo (criação, edição, arquivamento e desarquivamento), listagem paginada com filtros avançados no servidor por termo (`?q=`), fase processual (`?fase=`), cliente (`?clienteId=`), advogado responsável (`?advogadoId=`) e arquivamento (`?arquivado=true/false`).
    *   Histórico e linha do tempo de andamentos processuais com tipificação (`AUTOMATICO`, `MANUAL`, `IA`).

*   **Financeiro, Agenda & Produtividade:**
    *   **Faturamento & Fluxo de Caixa:** Controle de receitas e despesas (`A_RECEBER`, `A_PAGAR`), tipos (`HONORARIOS`, `CUSTAS`, `DESPESAS_ESCRITORIO`), listagem paginada no servidor com filtros combinados: busca textual (`?q=`), status (`?status=`), natureza (`?natureza=`), tipo (`?tipo=`), intervalo de vencimento (`?vencimentoDe=` e `?vencimentoAte=`) e vínculo com processo (`?processoId=`).
    *   **Liquidação:** Registro de data efetiva obrigatória (`PATCH /api/faturamentos/{id}/pagar`) e atualização de status (`PENDENTE`, `PAGO`, `CANCELADO`).
    *   **Métricas Financeiras Consolidadas:** Rota de resumo financeiro (`GET /api/faturamentos/resumo`) com totalizadores de a receber, a pagar, saldo previsto e valores vencidos.
    *   **Gestão de Tarefas (To-Do List):** Tarefas vinculadas a usuários e processos (`DILIGENCIA`, `PRAZO`, `CONTATO`) com CRUD completo, marcação de conclusão e consulta de prazos por período para o calendário (`GET /api/tarefas/agenda`) filtrando por início, fim, conclusão, tipo, processo e responsável.
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

*   **Monitoramento e Integração com Tribunais:**
    *   Endpoint `GET /api/integracoes/tribunais/status`.
    *   Exposição do status operacional das conexões e sincronizações eletrônicas com tribunais (TJRS, TRF4, TRT4, STJ) utilizando o enum `StatusTribunalEnum` (`OPERACIONAL`, `DEGRADADO`, `INDISPONIVEL`).

*   **GED (Gestão Eletrônica de Documentos) & Motor de Emissão:**
    *   Armazenamento físico de arquivos via `LocalStorageService` (`uploads/documentos`) vinculado a clientes e processos.
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
| **Processo** | `id`, `numeroCnj`, `assunto`, `faseAtual`, `dataCriacao`, `arquivado` | N:1 Cliente, N:1 Usuario, 1:N Documentos, 1:N Tarefas, 1:N Andamentos (`tb_processo`) | CNJ único. Validação de pendência financeira para arquivamento. Endpoints: `POST /api/processos`, `GET /api/processos` (paginado com `?q=`, `?fase=`, `?clienteId=`, `?advogadoId=` e `?arquivado=`), `GET /api/processos/{id}`, `PUT /api/processos/{id}`, `PATCH /api/processos/{id}/arquivar`, `PATCH /api/processos/{id}/desarquivar`. |
| **Andamento** | `id`, `dataHora`, `descricao`, `tipo` (`AUTOMATICO`, `MANUAL`, `IA`) | N:1 Processo (`tb_andamento`) | Histórico cronológico processual. Endpoints: `POST /api/processos/{processoId}/andamentos`, `GET /api/processos/{processoId}/andamentos`. |
| **Tarefa** | `id`, `descricao`, `dataVencimento`, `concluida`, `tipo` (`DILIGENCIA`, `PRAZO`, `CONTATO`) | N:1 Usuario, N:1 Processo (Opc) (`tb_tarefa`) | Alimenta To-Do list, Agenda e Dashboard. Endpoints: `POST /api/tarefas`, `PUT /api/tarefas/{id}`, `DELETE /api/tarefas/{id}`, `PATCH /api/tarefas/{id}/concluir`, `GET /api/tarefas/dashboard/{usuarioId}`, `GET /api/tarefas/agenda`. |
| **Faturamento**| `id`, `descricao`, `valor`, `tipo`, `status`, `natureza`, `dataVencimento`, `dataPagamento` | N:1 Processo (Opc) (`tb_faturamento`) | Controle financeiro. Endpoints: `GET /api/faturamentos/resumo`, `GET /api/faturamentos` (paginado com múltiplos filtros: `q`, `status`, `natureza`, `tipo`, `vencimentoDe`, `vencimentoAte`, `processoId`), `GET /api/faturamentos/processo/{processoId}`, `POST /api/faturamentos`, `PATCH /api/faturamentos/{id}/pagar`. |
| **Audiencia** | `id`, `dataHora`, `local`, `observacoes`, `status`, `resumoPreparatorioIa`, `responsavelId` | N:1 Processo, N:1 Usuario (`tb_audiencia`) | Validação de data futura no agendamento. Mapeamento de responsável. Endpoints: `POST /api/audiencias`, `GET /api/audiencias/{id}`, `PUT /api/audiencias/{id}`, `DELETE /api/audiencias/{id}`, `PATCH /api/audiencias/{id}/status`, `GET /api/audiencias/agenda`, `POST /{id}/gerar-resumo-ia`. |
| **Documento** | `id`, `nomeArquivo`, `titulo`, `caminhoStorage`, `indexadoIA` | N:1 Processo (Opc), N:1 Cliente (Opc) (`tb_documento`) | Upload (`POST /api/documentos/upload`), listagem por cliente (`GET /api/documentos/cliente/{clienteId}`), listagem por processo (`GET /api/documentos/processo/{processoId}`), download (`GET /api/documentos/{id}/download`) e exclusão física/lógica (`DELETE /api/documentos/{id}`). |

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

### 3.3. Padronização da Serialização de Paginação (Spring Boot 3.3+)
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

### 3.5. Documentação OpenAPI 3 (Swagger / Springdoc 2.6.0)
*   Todos os 15 Controllers da API são decorados com:
    *   `@Tag(name = "...", description = "...")`: Organização lógica e semântica por domínio de negócio.
    *   `@Operation(summary = "...", description = "...")`: Descrição minuciosa de cada endpoint.
    *   `@ApiResponses`: Mapeamento explícito dos cenários de sucesso (200, 201, 204) e dos cenários de erro (400, 401, 403, 404, 409, 422) com vínculo ao `@Schema(implementation = ErroPadraoDTO.class)` ou `@Schema(implementation = ErroValidacaoDTO.class)`.
*   **Anotação `@ParameterObject`:** Aplicada em **todos** os parâmetros do tipo `Pageable` em métodos de controller (`@ParameterObject @PageableDefault(...) Pageable pageable`), garantindo que o Swagger UI gere parâmetros de requisição planos (`page`, `size`, `sort`) sem colapso de objetos aninhados.

### 3.6. Blindagem de DTOs e Construtores Defensivos
*   **`ResumoDashboardDTO`:** Implementa construtor defensivo que substitui valores nulos de contadores por `0`, valores monetários por `BigDecimal.ZERO` e coleções por listas vazias (`new ArrayList<>()`), garantindo que o dashboard nunca quebre o front-end por campos indefinidos (`undefined`/`null`).
*   **`UsuarioResponseDTO`:** Suprime qualquer atributo de credencial, retornando estritamente dados públicos e operacionais do usuário (`id`, `nome`, `email`, `perfil`, `oab`, `ativo`).

---

## 4. Estrutura de Pacotes

```text
└── src/main/java/com/sistemajuridico/backend/
    ├── core/
    │   ├── domain/
    │   │   ├── enums/                      # PerfilAcessoEnum, TipoTarefaEnum, StatusAudienciaEnum, StatusTribunalEnum...
    │   │   ├── exceptions/                 # RegraNegocioException, RecursoNaoEncontradoException...
    │   │   ├── validators/                 # DocumentoValidator (CPF/CNPJ)
    │   │   └── *.java                      # Usuario, Escritorio, PasswordResetToken, Cliente, Processo, Faturamento...
    │   ├── service/                        # Serviços de domínio com lógica imperativa clássica
    │   │   ├── AuthService.java            # Recuperação SMTP, redefinição e alteração de senha
    │   │   └── EscritorioService.java       # Gestão cadastral e higienização dos dados do escritório
    │   └── usecases/                       # Casos de uso imperativos clássicos
    │       ├── AutenticarUsuarioUseCase.java
    │       ├── CadastrarUsuarioUseCase.java / ListarAdvogadosUseCase.java / BuscarUsuarioPorIdUseCase.java / ListarUsuariosUseCase.java
    │       ├── CadastrarClienteUseCase.java / AtualizarClienteUseCase.java / BuscarClientePorIdUseCase.java / ListarClientesUseCase.java
    │       ├── GerarProcuracaoClienteUseCase.java / GerarContratoHonorariosUseCase.java
    │       ├── CadastrarProcessoUseCase.java / ArquivarProcessoUseCase.java / DesarquivarProcessoUseCase.java / ListarProcessosUseCase.java
    │       ├── CadastrarAudienciaUseCase.java / AlterarStatusAudienciaUseCase.java / ListarAgendaGlobalUseCase.java
    │       ├── CriarTarefaUseCase.java / ConcluirTarefaUseCase.java / ListarTarefasPorPeriodoUseCase.java / ListarTarefasDashboardUseCase.java
    │       ├── CadastrarFaturamentoUseCase.java / LiquidarFaturamentoUseCase.java / ObterResumoFinanceiroUseCase.java / ListarFaturamentosUseCase.java
    │       ├── DashboardAdvogadoUseCase.java
    │       ├── ObterResumoNotificacoesUseCase.java
    │       ├── BuscaGlobalUseCase.java
    │       ├── GerarEAnexarResumoAudienciaUseCase.java / GerarResumoAudienciaUseCase.java
    │       ├── UploadDocumentoUseCase.java / DownloadDocumentoUseCase.java / ExcluirDocumentoUseCase.java
    │       └── ListarDocumentosPorClienteUseCase.java / ListarDocumentosPorProcessoUseCase.java / BuscarDocumentoPorIdUseCase.java
    │
    ├── infrastructure/
    │   ├── ai/                             # ResumoAIService, SpringAIResumoService
    │   ├── config/                         # OpenApiConfig, WebConfig (@EnableSpringDataWebSupport VIA_DTO)
    │   ├── document/                       # DocumentGeneratorService, PdfDocumentGeneratorService
    │   ├── persistence/                    # Repositories JPA com SQL Nativo (UsuarioRepository, ClienteRepository...)
    │   ├── security/                       # SecurityConfig, CorsConfig, TokenService, JwtAuthenticationFilter
    │   └── storage/                        # StorageService, LocalStorageService
    │
    └── presentation/
        ├── controllers/                    # REST Controllers documentados com @Tag, @Operation e @ApiResponses
        └── dtos/                           # Records de entrada/saída (UsuarioResponseDTO, ErroPadraoDTO, ErroValidacaoDTO...)
```

---

## 5. Status de Implementação Backend

*Todas as metas de arquitetura, segurança e funcionalidades das Etapas 1, 2, 3 e 4 do plano de ação da auditoria foram integralmente concluídas, com 100% de adesão ao Paradigma Imperativo Puro, persistência validada no PostgreSQL via SQL nativo, conformidade com OpenAPI 3 e cobertura de testes automatizados com BUILD SUCCESS.*