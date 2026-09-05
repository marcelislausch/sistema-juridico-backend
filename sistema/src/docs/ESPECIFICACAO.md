# Documento de Especificação de Software (PRD) - v2.8

**Projeto:** Sistema de Gestão Jurídica Inteligente  
**Perfil:** Backend Corporativo / Portfólio  
**Padrão Arquitetural:** Clean Architecture (Arquitetura Hexagonal)  
**Versão do Spring Boot:** 3.3.3 | **Java:** 21 LTS  

---

## 1. Módulos do Sistema

O sistema é estruturado em 5 grandes módulos lógicos de alta coesão e baixo acoplamento:

*   **IAM (Identity & Access Management) & Segurança:**
    *   Controle de usuários e perfis de acesso (`ADMIN`, `ADVOGADO`, `SECRETARIA`).
    *   Autenticação via JWT *stateless* com assinatura HMAC256 e extração de claims de perfil e identificador.
    *   Suporte ao parâmetro opcional `manterConectado: boolean` no login para controle de persistência de sessão: se `true`, estende a validade do token JWT de 2 horas para 7 dias (expiração padrão: 2 horas).
    *   Endpoint oficial e centralizado para resgate dos dados do usuário logado (`GET /api/auth/me`), retornando o `UsuarioDTO` da sessão ativa.
    *   Criptografia de senhas com BCrypt.
    *   Controle de acesso granular baseado em papéis (RBAC com `@PreAuthorize` e `@EnableMethodSecurity`).
    *   Listagem de advogados ativos (`GET /api/usuarios/advogados`) para vinculação em processos.
    *   Configuração global de CORS (`CorsConfig`) liberada para clientes web autorizados.
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

Todas as entidades de persistência herdam de `AuditableEntity`, possuindo rastreabilidade automática de auditoria (`criadoEm`, `criadoPor`, `atualizadoEm`, `atualizadoPor`).

| Entidade | Atributos Principais | Relacionamentos | Regras de Negócio e Endpoints |
| :--- | :--- | :--- | :--- |
| **Usuario** | `id`, `nome`, `email`, `senhaHash`, `perfil` (`ADMIN`, `ADVOGADO`, `SECRETARIA`), `oab`, `ativo` | 1:N Processos, 1:N Tarefas | E-mail único. Cadastro restrito a administradores. Endpoints: `POST /api/usuarios`, `GET /api/usuarios/{id}`, `GET /api/usuarios/advogados`, `GET /api/auth/me`. |
| **Cliente** | `id`, `nome`, `tipo` (`FISICA`, `JURIDICA`), `cpfCnpj`, `dataNascimento`, `estadoCivil`, `profissao`, `sexo`, `telefone`, `email`, endereço completo | 1:N Processos, 1:N Documentos | Validação estrita de CPF/CNPJ. Endpoints: `POST /api/clientes`, `GET /api/clientes` (paginado com `?termoBusca=`), `GET /api/clientes/{id}`, `PUT /api/clientes/{id}`, emissão de Procuração (`GET /api/clientes/{id}/procuracao`) e Contrato de Honorários (`GET /api/clientes/{id}/contrato-honorarios`). |
| **Processo** | `id`, `numeroCnj`, `assunto`, `faseAtual`, `dataCriacao`, `arquivado` | N:1 Cliente, N:1 Usuario, 1:N Documentos, 1:N Tarefas, 1:N Andamentos | CNJ único. Validação de pendência financeira para arquivamento. Endpoints: `POST /api/processos`, `GET /api/processos` (paginado com `?termoBusca=` e `?arquivado=`), `GET /api/processos/{id}`, `PUT /api/processos/{id}`, `PATCH /api/processos/{id}/arquivar`, `PATCH /api/processos/{id}/desarquivar`. |
| **Andamento** | `id`, `dataHora`, `descricao`, `tipo` (`AUTOMATICO`, `MANUAL`, `IA`) | N:1 Processo | Histórico cronológico processual. Endpoints: `POST /api/processos/{processoId}/andamentos`, `GET /api/processos/{processoId}/andamentos`. |
| **Tarefa** | `id`, `descricao`, `dataVencimento`, `concluida`, `tipo` (`DILIGENCIA`, `PRAZO`, `CONTATO`) | N:1 Usuario, N:1 Processo (Opc) | Alimenta To-Do list, Agenda e Dashboard. Endpoints: `POST /api/tarefas`, `PUT /api/tarefas/{id}`, `DELETE /api/tarefas/{id}`, `PATCH /api/tarefas/{id}/concluir`, `GET /api/tarefas/dashboard/{usuarioId}`, `GET /api/tarefas/agenda`. |
| **Faturamento**| `id`, `descricao`, `valor`, `tipo`, `status`, `natureza`, `dataVencimento`, `dataPagamento` | N:1 Processo (Opc) | Controle financeiro. Endpoints: `GET /api/faturamentos/resumo`, `GET /api/faturamentos` (paginado com `?status=` e `?natureza=`), `GET /api/faturamentos/processo/{processoId}`, `POST /api/faturamentos`, `PATCH /api/faturamentos/{id}/pagar`. |
| **Audiencia** | `id`, `dataHora`, `local`, `observacoes`, `status`, `resumoPreparatorioIa` | N:1 Processo | Validação de data futura no agendamento. Endpoints: `POST /api/audiencias`, `GET /api/audiencias/{id}`, `PUT /api/audiencias/{id}`, `DELETE /api/audiencias/{id}`, `PATCH /api/audiencias/{id}/status`, `GET /api/audiencias/agenda`, `POST /{id}/gerar-resumo-ia`. |
| **Documento** | `id`, `nomeArquivo`, `titulo`, `caminhoStorage`, `indexadoIA` | N:1 Processo (Opc), N:1 Cliente (Opc) | Upload (`POST /api/documentos/upload`), listagem por cliente (`GET /api/documentos/cliente/{clienteId}`), listagem por processo (`GET /api/documentos/processo/{processoId}`), download (`GET /api/documentos/{id}/download`) e exclusão física/lógica (`DELETE /api/documentos/{id}`). |

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

### 3.3. Segurança & CORS
*   Filtro `JwtAuthenticationFilter` interceptando requisições com validação de emissor, assinatura e expiração.
*   CORS centralizado em `CorsConfig` (`WebMvcConfigurer`) e registrado na cadeia de segurança via `.cors(Customizer.withDefaults())`.

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
    │   │   └── *.java                      # Cliente, Processo, Tarefa, Audiencia, Faturamento...
    │   └── usecases/                       # Casos de uso imperativos clássicos
    │       ├── AutenticarUsuarioUseCase.java
    │       ├── CadastrarClienteUseCase.java / AtualizarClienteUseCase.java
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
    │   ├── persistence/                    # Repositories JPA e DatabaseSeeder
    │   ├── security/                       # SecurityConfig, CorsConfig, TokenService, JwtFilter
    │   └── storage/                        # StorageService, LocalStorageService
    │
    └── presentation/
        ├── controllers/                    # REST Controllers (11 controladores implementados)
        └── dtos/                           # Records de entrada/saída com Bean Validation
```

---

## 5. Próximos Passos Backend (Backlog Técnico Prioritário)

*Todas as funcionalidades prioritárias do backend (IAM, Core Legal, Gestão Financeira com Paginação/Filtros, Agenda, GED e Inteligência Artificial) foram integralmente implementadas e validadas.*