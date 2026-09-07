# Documento de Especificação de Software (PRD) - v2.9

**Projeto:** Sistema de Gestão Jurídica Inteligente  
**Perfil:** Backend Corporativo / Portfólio  
**Padrão Arquitetural:** Clean Architecture (Arquitetura Hexagonal)  
**Versão do Spring Boot:** 3.3.3 | **Java:** 21 LTS  

---

## 1. Módulos do Sistema

O sistema é estruturado em módulos lógicos de alta coesão e baixo acoplamento:

*   **IAM (Identity & Access Management) & Segurança:**
    *   Controle de usuários e perfis de acesso (`ADMIN`, `ADVOGADO`, `SECRETARIA`).
    *   Autenticação via JWT *stateless* com assinatura HMAC256 e extração de claims de perfil e identificador.
    *   Suporte ao parâmetro opcional `manterConectado: boolean` no login para controle de persistência de sessão: se `true`, estende a validade do token JWT de 2 horas para 7 dias (expiração padrão: 2 horas).
    *   Endpoint oficial e centralizado para resgate dos dados do usuário logado (`GET /api/auth/me`), retornando o `UsuarioDTO` da sessão ativa.
    *   **Recuperação e Redefinição de Senha:**
        *   Solicitação com envio de e-mail SMTP via Google (`POST /api/auth/recuperar-senha`): geração de token atômico persistido na tabela `tb_password_reset_token` com validade de 15 minutos, regra anti-enumeração de contas e resposta padronizada `202 Accepted`. Envio de e-mail com instruções e formatação clara. Rollback transacional atômico integral em caso de falha no envio.
        *   Redefinição de senha com token (`POST /api/auth/redefinir-senha`): validação de existência, consumo prévio e expiração do token, com atualização de hash BCrypt e invalidação do token (HTTP 200 OK).
    *   **Alteração de Senha Autenticada:** Rota segura (`PATCH /api/auth/me/senha`) que valida a senha atual com BCrypt antes de atualizar para a nova credencial, retornando `204 No Content`.
    *   Criptografia de senhas com BCrypt.
    *   Tratamento defensivo do campo `oab`: campos vazios ou com espaços são persistidos como `null`, prevenindo violação de constraint de unicidade no PostgreSQL para múltiplos usuários sem registro na OAB.
    *   Filtro `JwtAuthenticationFilter` configurado com `shouldNotFilter` para liberar rotas públicas de autenticação (`/api/auth/recuperar-senha`, `/api/auth/redefinir-senha`, `/api/auth/login`) e documentação Swagger sem exigir token.
    *   Controle de acesso granular baseado em papéis (RBAC com `@PreAuthorize` e `@EnableMethodSecurity`).
    *   Listagem de advogados ativos (`GET /api/usuarios/advogados`) para vinculação em processos.
    *   Configuração global de CORS (`CorsConfig`) e liberação explícita de requisições `OPTIONS` preflight.
*   **Configurações Institucionais do Escritório:**
    *   Gestão cadastral centralizada do escritório (`GET /api/configuracoes/escritorio` e `PUT /api/configuracoes/escritorio`).
    *   Persistência em PostgreSQL na tabela `escritorios`: Razão Social, Nome Fantasia, CNPJ único (14 dígitos), Registro OAB (`OAB/RS 121.837`), contatos (telefone, WhatsApp, e-mail) e endereço oficial (`Rua Tiradentes, 676, Ijuí/RS`).
    *   Higienização imperativa de máscaras numéricas (CNPJ, telefone, WhatsApp, CEP) para persistência segura e padronizada.
*   **Core Legal & CRM:**
    *   Gestão de clientes com qualificação civil estendida (tipo de pessoa, estado civil, profissão, sexo, endereço completo e data de nascimento), com listagem paginada (`?page=0&size=10`) e busca textual dinâmica (`?termoBusca=`) por nome, CPF/CNPJ ou e-mail.
    *   Gestão de processos judiciais vinculados a advogados responsáveis e clientes, com suporte a ciclo de vida completo (criação, edição, arquivamento e desarquivamento), com listagem paginada (`?page=0&size=10`) e filtros dinâmicos por termo de busca (`?termoBusca=`) e status de arquivamento (`?arquivado=true/false`).
    *   Histórico e linha do tempo de andamentos processuais com tipificação (`AUTOMATICO`, `MANUAL`, `IA`).
*   **Financeiro, Agenda & Produtividade:**
    *   **Faturamento & Fluxo de Caixa:** Controle de receitas e despesas (`A_RECEBER`, `A_PAGAR`), tipos (`HONORARIOS`, `CUSTAS`, `DESPESAS_ESCRITORIO`), listagem paginada (`?page=0&size=10`) com suporte a filtros opcionais por status (`?status=PENDENTE`) e natureza (`?natureza=A_RECEBER`), liquidação com registro de data efetiva obrigatória (`PATCH /api/faturamentos/{id}/pagar`) e status (`PENDENTE`, `PAGO`, `CANCELADO`).
    *   **Métricas Financeiras Consolidadas:** Rota dedicada de resumo financeiro (`GET /api/faturamentos/resumo`) com totalizadores de a receber, a pagar, saldo previsto e valores vencidos.
    *   **Gestão de Tarefas (To-Do List):** Tarefas vinculadas a usuários e processos (`DILIGENCIA`, `PRAZO`, `CONTATO`) com CRUD completo, marcação de conclusão e consulta de prazos por período para o calendário (`GET /api/tarefas/agenda`).
    *   **Agenda de Audiências:** Agendamento com validação contra datas retroativas, pauta global harmonizada por período em `LocalDate` (`/api/audiencias/agenda?inicio=&fim=`), edição cadastral, exclusão e alteração de status (`AGENDADA`, `REALIZADA`, `CANCELADA`).
    *   **Dashboard Executiva:** Agregação de métricas em tempo real para o advogado (`GET /api/dashboard/{usuarioId}`) contemplando contadores de clientes, processos, tarefas, audiências do dia e fluxo financeiro imediato.
*   **GED (Gestão Eletrônica de Documentos) & Motor de Emissão:**
    *   Armazenamento físico de arquivos via `LocalStorageService` (`uploads/documentos`) vinculado a clientes e processos.
    *   **Ciclo Completo de Anexos:** Upload multipart (`POST /api/documentos/upload`), listagem de anexos por cliente (`GET /api/documentos/cliente/{clienteId}`), listagem de peças por processo (`GET /api/documentos/processo/{processoId}`), download com detecção dinâmica de MediaType (`GET /api/documentos/{id}/download`) e exclusão física/lógica sincronizada (`DELETE /api/documentos/{id}`).
    *   **Motor Oficial de Geração de Documentos (`PdfDocumentGeneratorService`):** Emissão de PDFs com biblioteca iText, embutimento obrigatório de fontes TrueType (`Bookman Old Style` via `BaseFont.EMBEDDED`) para garantir compatibilidade móvel e desktop, cabeçalho e rodapé fixos automatizados via eventos de página:
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
| **Usuario** | `id`, `nome`, `email`, `senhaHash`, `perfil` (`ADMIN`, `ADVOGADO`, `SECRETARIA`), `oab`, `ativo` | 1:N Processos, 1:N Tarefas (`tb_usuario`) | E-mail único. Cadastro restrito a administradores. Tratamento de OAB vazia como `null`. Endpoints: `POST /api/usuarios`, `GET /api/usuarios/{id}`, `GET /api/usuarios/advogados`, `GET /api/auth/me`. |
| **PasswordResetToken** | `id`, `token`, `usuarioId`, `dataExpiracao`, `usado` | Tabela `tb_password_reset_token` | Token UUID único, expiração de 15 minutos, controle de uso único. Endpoints: `POST /api/auth/recuperar-senha`, `POST /api/auth/redefinir-senha`. |
| **Escritorio** | `id`, `razaoSocial`, `nomeFantasia`, `cnpj`, `registroOabSociedade`, `telefone`, `whatsapp`, `email`, `cep`, `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `uf` | Tabela `escritorios` (Singleton/Tenant) | CNPJ único (14 dígitos). Higienização de caracteres não numéricos. Endereço padrão: Rua Tiradentes, 676, Ijuí/RS. OAB: OAB/RS 121.837. Endpoints: `GET /api/configuracoes/escritorio`, `PUT /api/configuracoes/escritorio`. |
| **Cliente** | `id`, `nome`, `tipo` (`FISICA`, `JURIDICA`), `cpfCnpj`, `dataNascimento`, `estadoCivil`, `profissao`, `sexo`, `telefone`, `email`, endereço completo | 1:N Processos, 1:N Documentos (`tb_cliente`) | Validação estrita de CPF/CNPJ. Endpoints: `POST /api/clientes`, `GET /api/clientes` (paginado com `?termoBusca=`), `GET /api/clientes/{id}`, `PUT /api/clientes/{id}`, emissão de Procuração (`GET /api/clientes/{id}/procuracao`) e Contrato de Honorários (`GET /api/clientes/{id}/contrato-honorarios`). |
| **Processo** | `id`, `numeroCnj`, `assunto`, `faseAtual`, `dataCriacao`, `arquivado` | N:1 Cliente, N:1 Usuario, 1:N Documentos, 1:N Tarefas, 1:N Andamentos (`tb_processo`) | CNJ único. Validação de pendência financeira para arquivamento. Endpoints: `POST /api/processos`, `GET /api/processos` (paginado com `?termoBusca=` e `?arquivado=`), `GET /api/processos/{id}`, `PUT /api/processos/{id}`, `PATCH /api/processos/{id}/arquivar`, `PATCH /api/processos/{id}/desarquivar`. |
| **Andamento** | `id`, `dataHora`, `descricao`, `tipo` (`AUTOMATICO`, `MANUAL`, `IA`) | N:1 Processo (`tb_andamento`) | Histórico cronológico processual. Endpoints: `POST /api/processos/{processoId}/andamentos`, `GET /api/processos/{processoId}/andamentos`. |
| **Tarefa** | `id`, `descricao`, `dataVencimento`, `concluida`, `tipo` (`DILIGENCIA`, `PRAZO`, `CONTATO`) | N:1 Usuario, N:1 Processo (Opc) (`tb_tarefa`) | Alimenta To-Do list, Agenda e Dashboard. Endpoints: `POST /api/tarefas`, `PUT /api/tarefas/{id}`, `DELETE /api/tarefas/{id}`, `PATCH /api/tarefas/{id}/concluir`, `GET /api/tarefas/dashboard/{usuarioId}`, `GET /api/tarefas/agenda`. |
| **Faturamento**| `id`, `descricao`, `valor`, `tipo`, `status`, `natureza`, `dataVencimento`, `dataPagamento` | N:1 Processo (Opc) (`tb_faturamento`) | Controle financeiro. Endpoints: `GET /api/faturamentos/resumo`, `GET /api/faturamentos` (paginado com `?status=` e `?natureza=`), `GET /api/faturamentos/processo/{processoId}`, `POST /api/faturamentos`, `PATCH /api/faturamentos/{id}/pagar`. |
| **Audiencia** | `id`, `dataHora`, `local`, `observacoes`, `status`, `resumoPreparatorioIa` | N:1 Processo (`tb_audiencia`) | Validação de data futura no agendamento. Endpoints: `POST /api/audiencias`, `GET /api/audiencias/{id}`, `PUT /api/audiencias/{id}`, `DELETE /api/audiencias/{id}`, `PATCH /api/audiencias/{id}/status`, `GET /api/audiencias/agenda`, `POST /{id}/gerar-resumo-ia`. |
| **Documento** | `id`, `nomeArquivo`, `titulo`, `caminhoStorage`, `indexadoIA` | N:1 Processo (Opc), N:1 Cliente (Opc) (`tb_documento`) | Upload (`POST /api/documentos/upload`), listagem por cliente (`GET /api/documentos/cliente/{clienteId}`), listagem por processo (`GET /api/documentos/processo/{processoId}`), download (`GET /api/documentos/{id}/download`) e exclusão física/lógica (`DELETE /api/documentos/{id}`). |

---

## 3. Diretrizes Técnicas e Decisões Arquiteturais

### 3.1. Paradigma Java Puro e Imperativo
*   Proibição expressa de Expressões Lambda (`->`), Stream API (`.stream()`) e métodos funcionais encadeados de `Optional` (`.orElseThrow()`, `.map()`).
*   Verificação clássica de `Optional`:
    ```java
    Optional<Entidade> opt = repository.findById(id);
    if (opt.isEmpty()) {
        throw new RecursoNaoEncontradoException("Recurso não encontrado!");
    }
    Entidade entidade = opt.get();
    ```
*   Transformações e iterações de coleções realizadas exclusivamente com laços `for` tradicionais e instanciação explícita de `new ArrayList<>()`.

### 3.2. Tratamento Global de Exceções
Padronizado no `GlobalExceptionHandler` (`@RestControllerAdvice`):
*   **HTTP 400 Bad Request:** Violações de validação de DTOs (`MethodArgumentNotValidException`) e parâmetros malformados.
*   **HTTP 404 Not Found:** Recursos inexistentes (`RecursoNaoEncontradoException`).
*   **HTTP 409 Conflict:** Violações de unicidade (`DataIntegrityViolationException`).
*   **HTTP 422 Unprocessable Entity:** Regras de negócio violadas (`RegraNegocioException`).

### 3.3. Segurança, CORS & Sessão
*   Filtro `JwtAuthenticationFilter` com `shouldNotFilter` dedicado para endpoints públicos (`/api/auth/login`, `/api/auth/recuperar-senha`, `/api/auth/redefinir-senha`, documentação).
*   Tratamento defensivo de token JWT impedindo interrupção de cadeia em caso de cabeçalhos corrompidos.
*   CORS centralizado em `CorsConfig` (`WebMvcConfigurer`) e registrado na cadeia de segurança via `.cors(Customizer.withDefaults())`, com liberação explícita de preflight `OPTIONS /**`.

### 3.4. Motor de Renderização de Documentos PDF
*   Utilização da biblioteca iText com configuração rigorosa de fidelidade gráfica.
*   Registro explícito e embutimento dos arquivos TrueType (`BOOKOS.TTF` e `BOOKOSB.TTF`) via `BaseFont.EMBEDDED` para sanar distorções em visualizadores nativos de dispositivos móveis.
*   Controle de margens estáticas (superior de 120pt a 140pt) para evitar sobreposição entre o corpo do texto e os cabeçalhos fixos injetados por eventos de página (`PdfPageEventHelper`).

---

## 4. Estrutura de Pacotes

```text
└── src/main/java/com/sistemajuridico/backend/
    ├── core/
    │   ├── domain/
    │   │   ├── enums/                      # PerfilAcessoEnum, TipoTarefaEnum, StatusAudienciaEnum...
    │   │   ├── exceptions/                 # RegraNegocioException, RecursoNaoEncontradoException...
    │   │   ├── validators/                 # DocumentoValidator (CPF/CNPJ)
    │   │   └── *.java                      # Usuario, Escritorio, PasswordResetToken, Cliente, Processo...
    │   ├── service/                        # Serviços de domínio com lógica imperativa clássica
    │   │   ├── AuthService.java            # Recuperação SMTP, redefinição e alteração de senha
    │   │   └── EscritorioService.java       # Gestão cadastral e higienização dos dados do escritório
    │   └── usecases/                       # Casos de uso imperativos clássicos
    │       ├── AutenticarUsuarioUseCase.java
    │       ├── CadastrarUsuarioUseCase.java / ListarAdvogadosUseCase.java / BuscarUsuarioPorIdUseCase.java
    │       ├── CadastrarClienteUseCase.java / AtualizarClienteUseCase.java / BuscarClientePorIdUseCase.java
    │       ├── GerarProcuracaoClienteUseCase.java / GerarContratoHonorariosUseCase.java
    │       ├── CadastrarProcessoUseCase.java / ArquivarProcessoUseCase.java / DesarquivarProcessoUseCase.java
    │       ├── CadastrarAudienciaUseCase.java / AlterarStatusAudienciaUseCase.java / ListarAgendaGlobalUseCase.java
    │       ├── CriarTarefaUseCase.java / ConcluirTarefaUseCase.java / ListarTarefasPorPeriodoUseCase.java
    │       ├── CadastrarFaturamentoUseCase.java / LiquidarFaturamentoUseCase.java / ObterResumoFinanceiroUseCase.java
    │       ├── DashboardAdvogadoUseCase.java
    │       ├── GerarEAnexarResumoAudienciaUseCase.java
    │       ├── UploadDocumentoUseCase.java
    │       ├── ListarDocumentosPorClienteUseCase.java / ListarDocumentosPorProcessoUseCase.java
    │       └── BuscarDocumentoPorIdUseCase.java / DownloadDocumentoUseCase.java / ExcluirDocumentoUseCase.java
    │
    ├── infrastructure/
    │   ├── ai/                             # ResumoAIService, SpringAIResumoService
    │   ├── document/                       # DocumentGeneratorService, PdfDocumentGeneratorService
    │   ├── persistence/                    # Repositories JPA (EscritorioRepository, PasswordResetTokenRepository...)
    │   ├── security/                       # SecurityConfig, CorsConfig, TokenService, JwtAuthenticationFilter
    │   └── storage/                        # StorageService, LocalStorageService
    │
    └── presentation/
        ├── controllers/                    # REST Controllers (AuthController, EscritorioConfigController, etc.)
        └── dtos/                           # Records de entrada/saída (RecuperarSenhaRequest, EscritorioDTO, etc.)
```

---

## 5. Status de Implementação Backend

*Todas as funcionalidades prioritárias do backend (IAM com Recuperação/Alteração de Senha, Configuração Institucional do Escritório, Core Legal, Gestão Financeira com Paginação/Filtros, Agenda, GED e Inteligência Artificial) foram integralmente implementadas, persistidas em banco de dados relacional e validadas sem dependência de mocks.*