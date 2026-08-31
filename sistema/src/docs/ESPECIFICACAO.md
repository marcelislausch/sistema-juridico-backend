# Documento de Especificação de Software (PRD) - v2.5

**Projeto:** Sistema de Gestão Jurídica Inteligente (LegalTech)  
**Perfil:** Backend Corporativo / Portfólio  
**Padrão Arquitetural:** Clean Architecture (Arquitetura Hexagonal)  
**Versão do Spring Boot:** 3.3.3 | **Java:** 21 LTS

---

## 1. Módulos do Sistema

O sistema é estruturado em 5 grandes módulos lógicos:

*   **IAM (Identity & Access Management) & Segurança:**
    *   Controle de usuários e perfis de acesso (`ADMIN`, `ADVOGADO`, `SECRETARIA`).
    *   Autenticação via JWT *stateless* com assinatura HMAC256.
    *   Criptografia de senhas com BCrypt.
    *   Controle de acesso granular baseado em papéis (RBAC com `@PreAuthorize` e `@EnableMethodSecurity`).
    *   Inicialização automática e segura do usuário administrador via `DatabaseSeeder` com `@Value`.
    *   Configuração global de CORS (`CorsConfig`) habilitando integração com frontends modernos (como Lovable).
*   **Core Legal & CRM:**
    *   Gestão de clientes com qualificação civil estendida (estado civil, profissão, sexo, endereço completo e data de nascimento).
    *   Gestão de processos judiciais vinculados a advogados responsáveis e clientes.
    *   Histórico e linha do tempo de andamentos processuais.
*   **Financeiro, Agenda & Produtividade:**
    *   **Faturamento:** Controle de receitas e despesas (`A_RECEBER`, `A_PAGAR`), tipos (`HONORARIOS`, `CUSTAS`, `DESPESAS_ESCRITORIO`), liquidação com registro de data efetiva e status (`PENDENTE`, `PAGO`, `CANCELADO`).
    *   **Gestão de Tarefas (To-Do List):** Tarefas vinculadas a usuários e processos (`DILIGENCIA`, `PRAZO`, `CONTATO`) com acompanhamento de vencimento e conclusão.
    *   **Agenda de Audiências:** Agendamento com validação contra retroatividade, pauta global por período (`/api/audiencias/agenda`) e alteração de status (`AGENDADA`, `REALIZADA`, `CANCELADA`).
    *   **Dashboard Executiva:** Agregação de métricas em tempo real para o advogado (total de clientes ativos, processos em andamento, tarefas pendentes hoje, próximas 5 tarefas, montante financeiro a receber hoje e próximas faturas).
*   **GED (Gestão Eletrônica de Documentos):**
    *   Armazenamento físico de arquivos via `LocalStorageService` (`uploads/documentos`) vinculado a clientes e processos.
    *   **Motor de Geração de Documentos:** `DocumentGeneratorService` e `MockDocumentGeneratorService` emitindo Procuração oficial com preenchimento dinâmico dos dados do outorgante e download via endpoint `/api/clientes/{id}/procuracao`.
*   **Inteligência Artificial & Automação (Spring AI):**
    *   Integração nativa com Spring AI (`spring-ai-openai-spring-boot-starter`).
    *   **Resumo Preparatório de Audiências:** Caso de uso `GerarEAnexarResumoAudienciaUseCase` que sintetiza peças processuais volumosas e persiste o resultado diretamente no campo `resumoPreparatorioIa` da entidade `Audiencia` (`POST /api/audiencias/{id}/gerar-resumo-ia`).
    *   Endpoint dedicado para consultas avulsas de IA (`POST /api/ia/resumos/audiencia`).

---

## 2. Modelo de Domínio e Entidades

Todas as entidades herdam de `AuditableEntity`, possuindo rastreabilidade automática de auditoria (`criadoEm`, `criadoPor`, `atualizadoEm`, `atualizadoPor`).

| Entidade | Atributos Principais | Relacionamentos | Regras de Negócio e Endpoints |
| :--- | :--- | :--- | :--- |
| **Usuario** | `id`, `nome`, `email`, `senhaHash`, `perfil` (`ADMIN`, `ADVOGADO`, `SECRETARIA`), `oab`, `ativo` | 1:N Processos, 1:N Tarefas | E-mail único. Cadastro restrito a administradores (RBAC). |
| **Cliente** | `id`, `nome`, `tipo` (`FISICA`, `JURIDICA`), `cpfCnpj`, `dataNascimento`, `estadoCivil`, `profissao`, `sexo`, `telefone`, `email`, `cep`, `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `uf` | 1:N Processos, 1:N Documentos | Validação estrita de CPF/CNPJ. Emissão de procuração via `GET /api/clientes/{id}/procuracao`. |
| **Processo** | `id`, `numeroCnj`, `assunto`, `faseAtual`, `dataCriacao` | N:1 Cliente, N:1 Usuario, 1:N Documentos, 1:N Tarefas | CNJ único. Não pode ser arquivado se possuir faturas `PENDENTE`. |
| **Andamento** | `id`, `dataHora`, `descricao`, `tipo` (`AUTOMATICO`, `MANUAL`, `IA`) | N:1 Processo | Histórico cronológico processual. |
| **Tarefa** | `id`, `descricao`, `dataVencimento`, `concluida`, `tipo` (`DILIGENCIA`, `PRAZO`, `CONTATO`) | N:1 Usuario, N:1 Processo (Opc) | Alimenta o To-Do list e Dashboard. Endpoints: `POST /api/tarefas`, `PATCH /api/tarefas/{id}/concluir`, `GET /api/tarefas/dashboard/{usuarioId}`. |
| **Faturamento**| `id`, `descricao`, `valor`, `tipo`, `status`, `natureza`, `dataVencimento`, `dataPagamento` | N:1 Processo (Opc) | Alimenta fluxo de caixa e alertas da Dashboard. Endpoints: `POST /api/faturamentos`, `PATCH /api/faturamentos/{id}/pagar`, `GET /api/faturamentos/processo/{processoId}`. |
| **Audiencia** | `id`, `dataHora`, `local`, `observacoes`, `status`, `resumoPreparatorioIa` | N:1 Processo | Data futura obrigatória no agendamento. Pauta global via `/api/audiencias/agenda`. Resumo IA integrado via `POST /api/audiencias/{id}/gerar-resumo-ia`. |
| **Documento** | `id`, `nomeArquivo`, `titulo`, `caminhoStorage`, `indexadoIA` | N:1 Processo (Opc), N:1 Cliente (Opc) | Upload Multipart em `/api/documentos/upload`. Armazenamento em disco via `LocalStorageService`. |

---

## 3. Diretrizes Técnicas e Decisões Arquiteturais

### 3.1. Paradigma Java Puro e Imperativo
*   Proibição expressa de Expressões Lambda (`->`), Stream API (`.stream()`) e métodos funcionais de `Optional` (`.orElseThrow()`, `.map()`).
*   Verificação clássica de `Optional`:
    ```java
    Optional<Entidade> opt = repository.findById(id);
    if (opt.isEmpty()) {
        throw new RecursoNaoEncontradoException("Recurso não encontrado!");
    }
    Entidade entidade = opt.get();
    ```
*   Transformações de coleções utilizando laços `for` tradicionais e instanciação explícita de `new ArrayList<>()`.

### 3.2. Tratamento Global de Exceções
Padronizado no `GlobalExceptionHandler` (`@RestControllerAdvice`):
*   **HTTP 400 Bad Request:** Validação de DTOs (`MethodArgumentNotValidException`).
*   **HTTP 404 Not Found:** Recursos inexistentes (`RecursoNaoEncontradoException`).
*   **HTTP 409 Conflict:** Violação de unicidade (`DataIntegrityViolationException`).
*   **HTTP 422 Unprocessable Entity:** Regras de negócio violadas (`RegraNegocioException`).

### 3.3. Segurança & CORS
*   Autenticação JWT com filtro `JwtAuthenticationFilter`.
*   CORS centralizado em `CorsConfig` (`WebMvcConfigurer`) e habilitado na cadeia de segurança (`.cors(Customizer.withDefaults())`).

### 3.4. Resiliência e Integração com IA
*   Configuração do Spring AI com OpenAI/Gemini chat client.
*   Previsão de retry e fallback via **Spring Retry** (`@Retryable(value = {RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))`) para resiliência contra indisponibilidades transitórias (HTTP 503) das APIs de LLM.

---

## 4. Estrutura de Pacotes

```text
└── src/main/java/com/sistemajuridico/backend/
    ├── core/
    │   ├── domain/
    │   │   ├── enums/                      # PerfilAcessoEnum, TipoTarefaEnum, StatusAudienciaEnum...
    │   │   ├── exceptions/                 # RegraNegocioException, RecursoNaoEncontradoException...
    │   │   ├── validators/                 # DocumentoValidator (CPF/CNPJ)
    │   │   └── *.java                      # Cliente, Processo, Tarefa, Audiencia, Faturamento...
    │   └── usecases/                       # Casos de uso imperativos clássicos
    │       ├── AutenticarUsuarioUseCase.java
    │       ├── CadastrarClienteUseCase.java / AtualizarClienteUseCase.java
    │       ├── CriarTarefaUseCase.java / ConcluirTarefaUseCase.java
    │       ├── CadastrarFaturamentoUseCase.java / LiquidarFaturamentoUseCase.java
    │       ├── DashboardAdvogadoUseCase.java
    │       ├── GerarEAnexarResumoAudienciaUseCase.java
    │       └── GerarProcuracaoClienteUseCase.java / UploadDocumentoUseCase.java
    │
    ├── infrastructure/
    │   ├── ai/                             # ResumoAIService, SpringAIResumoService
    │   ├── document/                       # DocumentGeneratorService, MockDocumentGeneratorService
    │   ├── persistence/                    # Repositories JPA e DatabaseSeeder
    │   ├── security/                       # SecurityConfig, CorsConfig, TokenService, JwtFilter
    │   └── storage/                        # StorageService, LocalStorageService
    │
    └── presentation/
        ├── controllers/                    # REST Controllers
        └── dtos/                           # Records de entrada/saída com Bean Validation
```

---

## 5. Próximos Passos Backend (Backlog)

1. **Geração Automática de Contrato de Honorários:** Expandir `DocumentGeneratorService` para incluir o template do Contrato de Honorários Advocatícios.
2. **Resiliência Spring Retry:** Anotar as chamadas do serviço de IA (`SpringAIResumoService`) com `@Retryable` e `@Recover` para tratamento gracioso de falhas 503 da API de IA.
3. **Agendamento de Alertas Pré-Audiência (D-1):** Implementar rotina agendada com Spring `@Scheduled` para identificar audiências do dia seguinte e disparar a geração antecipada de resumos.
4. **Assistente RAG com Vetorização:** Configurar armazenamento vetorial (pgvector) e pipeline de ingestão de PDFs de processos para consultas contextuais do advogado.
5. **Relatórios Exportáveis:** Endpoints para exportação consolidada de relatórios financeiros e pauta de audiências em formato PDF/Excel.