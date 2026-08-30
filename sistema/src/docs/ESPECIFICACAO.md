# Documento de Especificação de Software (PRD) - v2.4

**Projeto:** Sistema de Gestão Jurídica Inteligente (LegalTech)  
**Perfil:** Backend Corporativo / Portfólio  
**Padrão Arquitetural:** Clean Architecture (Arquitetura Hexagonal)

---

## 1. Módulos do Sistema

O sistema é estruturado em 5 grandes módulos lógicos:

*   **IAM (Identity & Access Management):** Controle de usuários, perfis de acesso (`ADMIN`, `ADVOGADO`, `SECRETARIA`), autenticação via JWT *stateless*, criptografia de senhas com BCrypt, controle de acesso baseado em papéis (RBAC) e inicialização automática e segura do usuário administrador (*DatabaseSeeder*).
*   **Core Legal & CRM:** Gestão completa de clientes (incluindo qualificação civil estendida e endereço), processos judiciais vinculados a advogados responsáveis e andamentos processuais em linha do tempo.
*   **Financeiro, Agenda & Produtividade:** Gestão de faturamentos (com alertas de vencimentos na Dashboard), controle de fluxo financeiro com liquidação de faturas, agendamento de audiências judiciais e **Gestão de Tarefas (To-Do List diário)** para acompanhamento de prazos e diligências.
*   **GED (Gestão Eletrônica de Documentos):** Armazenamento de arquivos vinculados a processos (Petições, Sentenças) e clientes (CNH, Comprovante de Residência). Inclui motor de **Geração Automática de Documentos**, emitindo Procurações e Contratos de Honorários a partir de templates, substituindo as tags de qualificação pelos dados do cliente.
*   **Inteligência Artificial & Automação (Módulo Avançado):**
    *   **Assistente RAG:** Chatbot alimentado pelo Spring AI que utiliza os PDFs do próprio processo como base de conhecimento exclusiva.
    *   **Resumos Inteligentes:** Geração automática de relatórios pré-audiência (alerta D-1) e extração de resumos processuais simplificados para envio via WhatsApp aos clientes.
    *   **RPA/Crawlers (Visão de Futuro):** Robôs autônomos para varredura do sistema e-Proc (TJRS), realizando leitura automática de novos andamentos e download de intimações/petições diretamente para o Storage.

---

## 2. Modelo de Domínio (Entidades e Regras de Negócio)

Todas as entidades herdam de `AuditableEntity`, possuindo rastreabilidade automática de auditoria (`criadoEm`, `criadoPor`, `atualizadoEm`, `atualizadoPor`).

| Entidade | Atributos Principais | Relacionamentos | Regras de Negócio e Endpoints |
| :--- | :--- | :--- | :--- |
| **Usuario** | `id`, `nome`, `email`, `senhaHash`, `perfil` (`ADMIN`, `ADVOGADO`, `SECRETARIA`), `oab`, `ativo` | 1:N Processos, 1:N Tarefas | O e-mail deve ser único. Cadastro estrito a administradores (RBAC). |
| **Cliente** | `id`, `nome`, `tipo` (`FISICA`, `JURIDICA`), `cpfCnpj`, `dataNascimento`, `estadoCivil`, `profissao`, `sexo`, `telefone`, `email`, `cep`, `logradouro`... | 1:N Processos, 1:N Documentos | Validação de CPF/CNPJ. Fornece a base de dados para preenchimento automático das tags nos templates de Procuração e Contratos. |
| **Processo** | `id`, `numeroCnj`, `assunto`, `faseAtual`, `dataCriacao` | N:1 Cliente, N:1 Usuario | Não pode ser arquivado se possuir faturas com status `PENDENTE`. |
| **Andamento** | `id`, `dataHora`, `descricao`, `tipo` (`AUTOMATICO`, `MANUAL`, `IA`) | N:1 Processo | Registro de histórico processual. Base de dados para extração de resumos de IA para WhatsApp. |
| **Tarefa** *(Novo)* | `id`, `descricao`, `dataVencimento`, `concluida` (Boolean), `tipo` (`DILIGENCIA`, `PRAZO`, `CONTATO`) | N:1 Usuario, N:1 Processo (Opc) | Alimenta a "To-Do List" diária do advogado (ex: buscar matrícula, redigir petição, contatar cliente). |
| **Faturamento**| `id`, `descricao`, `valor`, `tipo`, `natureza`, `dataVencimento`, `dataPagamento`, `status` | N:1 Processo (Opcional) | Liquidação exige data efetiva e atualiza status para `PAGO`. Gera alertas na Dashboard para valores vencendo hoje ou atrasados. |
| **Audiencia** | `id`, `dataHora`, `local`, `observacoes`, `status` | N:1 Processo | Pauta acessada via `/api/audiencias/agenda`. Dispara gatilho automático 1 dia útil antes (D-1) com resumo do processo. |
| **Documento** | `id`, `nomeArquivo`, `tipoDoc` (`PETICAO`, `SENTENCA`, `CNH`, `COMPROVANTE_RESIDENCIA`, `OUTROS`), `caminhoStorage`, `indexadoIA` | N:1 Processo (Opc), N:1 Cliente (Opc) | PDFs salvos via `StorageService`. Vinculado a clientes para scans pessoais e a processos para peças processuais. |

---

## 3. Implementações Transversais e Arquitetura

### 3.1. Padronização de Exceções de Domínio (Clean Architecture)
A aplicação intercepta erros globalmente através da classe `GlobalExceptionHandler` (`@RestControllerAdvice`), padronizando o payload de resposta HTTP:
*   **HTTP 422 Unprocessable Entity:** Lançado para violações de regras de negócio (`RegraNegocioException`).
*   **HTTP 404 Not Found:** Lançado quando um recurso solicitado não é encontrado (`RecursoNaoEncontradoException`).
*   **HTTP 400 Bad Request:** Lançado para erros de validação de payload (`MethodArgumentNotValidException`).
*   **HTTP 409 Conflict:** Lançado para violações de integridade de dados/chaves únicas (`DataIntegrityViolationException`).

### 3.2. Bean Validation nos DTOs
Todas as entradas nos controladores utilizam a anotação `@Valid` combinada com validações declarativas do `jakarta.validation`.

### 3.3. Paginação com Spring Data
Endpoints de listagem volumosos utilizam `Pageable` e retornam `Page<T>`.

### 3.4. Controle de Acesso Baseado em Perfis (RBAC)
Configurado via Spring Security com `@EnableMethodSecurity`.

### 3.5. Inicialização Segura de Administrador (DatabaseSeeder)
Implementado via `CommandLineRunner` na inicialização do contexto utilizando variáveis de ambiente, evitando credenciais *hardcoded*.

### 3.6. Auditoria Automática JPA
Habilitada via `@EnableJpaAuditing` interceptando o `SecurityContextHolder`.

### 3.7. Serviço de Geração de Documentos (Módulo GED)
O sistema contará com um `DocumentGeneratorService` responsável por ler os templates base (Procuração e Contrato de Honorários), localizar as tags de qualificação destacadas em vermelho/XXX, e substituir iterativamente pelos dados do `Cliente`, gerando o arquivo final.
*   **Template Procuração:** [Link Google Docs](https://docs.google.com/document/d/16AW-2cO6NQX7p7mK8iHfhQc_wimWcbeO/edit?usp=sharing&ouid=103994287021892020300&rtpof=true&sd=true)
*   **Template Contrato:** [Link Google Docs](https://docs.google.com/document/d/16OXIdms-sYaEJvpaaOTvvqBP9eCtpESV/edit?usp=sharing&ouid=103994287021892020300&rtpof=true&sd=true)

---

## 4. Estrutura de Pastas (Clean Architecture)

```text
└── src/main/java/com/sistemajuridico/backend/
    ├── core/                               # Domínio e Regras de Negócio (Java Puro)
    │   ├── domain/                         # Entidades e Superclasse de Auditoria
    │   │   ├── AuditableEntity.java
    │   │   ├── Cliente.java, Processo.java, Tarefa.java...
    │   │   ├── enums/                      # TipoDocumentoEnum, StatusAudienciaEnum...
    │   │   ├── exceptions/                 # RegraNegocioException, RecursoNaoEncontradoException
    │   │   └── validators/                 # DocumentoValidator (CPF/CNPJ)
    │   └── usecases/                       # Casos de Uso com lógica imperativa pura
    │       ├── AutenticarUsuarioUseCase.java
    │       ├── CadastrarClienteUseCase.java...
    │       └── GerarProcuracaoClienteUseCase.java...
    │
    ├── infrastructure/                     # Camada Técnica e Integrações
    │   ├── persistence/                    # Repositories JPA e DatabaseSeeder
    │   ├── security/                       # SecurityConfig, JwtAuthenticationFilter, TokenService
    │   ├── storage/                        # StorageService e LocalStorageService
    │   ├── document/                       # DocumentGeneratorService (Parse de templates)
    │   └── ia/                             # Spring AI e Vetorização RAG
    │
    └── presentation/                       # Adaptadores de Entrada (REST API)
        ├── controllers/                    # Endpoints REST e GlobalExceptionHandler
        └── dtos/                           # Records de Entrada e Saída com Bean Validation