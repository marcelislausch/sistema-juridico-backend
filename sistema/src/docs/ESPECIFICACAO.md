# Documento de Especificação de Software (PRD) - v2.2

**Projeto:** Sistema de Gestão Jurídica Inteligente (LegalTech)  
**Perfil:** Backend Corporativo / Portfólio  
**Padrão Arquitetural:** Clean Architecture (Arquitetura Hexagonal)

---

## 1. Módulos do Sistema

O sistema é estruturado em 4 grandes módulos lógicos:

*   **IAM (Identity & Access Management):** Controle de usuários, perfis de acesso (`ADMIN`, `ADVOGADO`, `SECRETARIA`), autenticação via JWT *stateless*, criptografia de senhas com BCrypt, controle de acesso baseado em papéis (RBAC) e inicialização automática e segura do usuário administrador (*DatabaseSeeder*).
*   **Core Legal:** Gestão completa de clientes (incluindo qualificação civil e endereço), processos judiciais vinculados a advogados responsáveis e andamentos processuais em linha do tempo.
*   **Financeiro & Agenda:** Gestão de faturamentos (honorários, custas e despesas do escritório), controle de fluxo financeiro com liquidação de faturas e agendamento de audiências judiciais.
*   **GED & IA:** Gestão Eletrônica de Documentos (upload e armazenamento desacoplado de PDFs) e motor de busca semântica e consulta inteligente via RAG (Retrieval-Augmented Generation).

---

## 2. Modelo de Domínio (Entidades e Regras de Negócio)

Todas as entidades herdam de `AuditableEntity`, possuindo rastreabilidade automática de auditoria (`criadoEm`, `criadoPor`, `atualizadoEm`, `atualizadoPor`).

| Entidade | Atributos Principais | Relacionamentos | Regras de Negócio e Endpoints |
| :--- | :--- | :--- | :--- |
| **Usuario** | `id`, `nome`, `email`, `senhaHash`, `perfil` (Enum: `ADMIN`, `ADVOGADO`, `SECRETARIA`), `oab`, `ativo` | 1:N Processos | O e-mail deve ser único. Cadastro (`POST /api/usuarios`) restrito a administradores (RBAC). Listagem de advogados em `GET /api/usuarios/advogados`. |
| **Cliente** | `id`, `nome`, `tipo` (Enum: `FISICA`, `JURIDICA`), `cpfCnpj`, `dataNascimento`, `telefone`, `email`, `cep`, `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `uf` | 1:N Processos | Validação de dígito verificador de CPF/CNPJ. Suporta criação (`POST /api/clientes`), edição completa (`PUT /api/clientes/{id}`) e listagem paginada (`GET /api/clientes`). |
| **Processo** | `id`, `numeroCnj`, `assunto`, `faseAtual`, `dataCriacao` | N:1 Cliente <br> N:1 Usuario (Advogado) | O processo deve estar associado a um cliente e, opcionalmente, a um advogado responsável. Não pode ser arquivado (`PATCH /api/processos/{id}/arquivar`) se possuir faturas com status `PENDENTE`. Apenas advogados ou administradores podem arquivar. |
| **Andamento** | `id`, `dataHora`, `descricao`, `tipo` (Enum: `AUTOMATICO`, `MANUAL`, `IA`) | N:1 Processo | Registro de histórico processual associado ao processo (`POST /api/processos/{processoId}/andamentos` e `GET /api/processos/{processoId}/andamentos`). |
| **Faturamento**| `id`, `descricao`, `valor`, `tipo` (Enum: `HONORARIOS`, `CUSTAS`, `DESPESAS_ESCRITORIO`), `natureza` (Enum: `A_RECEBER`, `A_PAGAR`), `dataVencimento`, `dataPagamento`, `status` (Enum: `PENDENTE`, `PAGO`, `CANCELADO`) | N:1 Processo (Opcional) | Ao liquidar a fatura (`PATCH /api/faturamentos/{id}/pagar`), o sistema exige obrigatoriamente a data efetiva do pagamento e atualiza o status para `PAGO`. |
| **Audiencia** | `id`, `dataHora`, `local`, `observacoes`, `status` (Enum: `AGENDADA`, `REALIZADA`, `CANCELADA`)| N:1 Processo | Não permite agendamento retroativo para novas audiências (`POST /api/audiencias`). Permite atualização de status em `PATCH /api/audiencias/{id}/status`. |
| **Documento** | `id`, `nomeArquivo`, `tipoDoc` (Enum: `PETICAO`, `SENTENCA`), `caminhoStorage`, `indexadoIA` | N:1 Processo | PDFs são armazenados em storage local ou S3, mantendo o caminho relativo no banco. A flag `indexadoIA` é atualizada após a vetorização assíncrona. |

---

## 3. Implementações Transversais e Arquitetura

### 3.1. Padronização de Exceções de Domínio (Clean Architecture)
A aplicação intercepta erros globalmente através da classe `GlobalExceptionHandler` (`@RestControllerAdvice`), padronizando o payload de resposta HTTP:
*   **HTTP 422 Unprocessable Entity:** Lançado para violações de regras de negócio (`RegraNegocioException`).
*   **HTTP 404 Not Found:** Lançado quando um recurso solicitado não é encontrado (`RecursoNaoEncontradoException`).
*   **HTTP 400 Bad Request:** Lançado para erros de validação de payload (`MethodArgumentNotValidException`), retornando a lista detalhada de campos inválidos.
*   **HTTP 409 Conflict:** Lançado para violações de integridade de dados/chaves únicas (`DataIntegrityViolationException`).

### 3.2. Bean Validation nos DTOs
Todas as entradas nos controladores utilizam a anotação `@Valid` combinada com validações declarativas do `jakarta.validation`:
*   `@NotBlank` e `@NotNull` para preenchimento de campos obrigatórios;
*   `@Email` para validação de formato de e-mail;
*   `@Past` para datas de nascimento;
*   `@Positive` para valores financeiros.

### 3.3. Paginação com Spring Data
Endpoints de listagem volumosos utilizam `Pageable` e retornam `Page<T>`:
*   `GET /api/clientes` (suporta `page`, `size`, `sort`);
*   `GET /api/processos/cliente/{clienteId}` (suporta `page`, `size`, `sort`).

### 3.4. Controle de Acesso Baseado em Perfis (RBAC)
*   Configurado via Spring Security com `@EnableMethodSecurity`.
*   A rota de criação de usuários (`POST /api/usuarios`) é estritamente restrita a usuários com perfil `ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`).

### 3.5. Inicialização Segura de Administrador (DatabaseSeeder)
*   Implementado via `CommandLineRunner` na inicialização do contexto.
*   Verifica a existência prévia de usuários administradores e injeta as credenciais iniciais a partir de variáveis de configuração (`api.admin.email` e `api.admin.senha`), evitando credenciais *hardcoded*.

### 3.6. Auditoria Automática JPA
*   Habilitada via `@EnableJpaAuditing`.
*   A classe `AuditorAwareImpl` extrai o e-mail do usuário autenticado no `SecurityContextHolder` para preenchimento automático de `criadoPor` e `atualizadoPor`.

---

## 4. Estrutura de Pastas (Clean Architecture)

```
└── src/main/java/com/sistemajuridico/backend/
    ├── core/                               # Domínio e Regras de Negócio (Java Puro)
    │   ├── domain/                         # Entidades e Superclasse de Auditoria
    │   │   ├── AuditableEntity.java
    │   │   ├── Cliente.java, Processo.java, Usuario.java...
    │   │   ├── enums/                      # PerfilAcessoEnum, TipoClienteEnum, StatusAudienciaEnum...
    │   │   ├── exceptions/                 # RegraNegocioException, RecursoNaoEncontradoException
    │   │   └── validators/                 # DocumentoValidator (CPF/CNPJ)
    │   └── usecases/                       # Casos de Uso com lógica imperativa pura
    │       ├── AutenticarUsuarioUseCase.java
    │       ├── CadastrarClienteUseCase.java, AtualizarClienteUseCase.java...
    │       ├── CadastrarProcessoUseCase.java, ArquivarProcessoUseCase.java...
    │       └── CadastrarAudienciaUseCase.java, LiquidarFaturamentoUseCase.java...
    │
    ├── infrastructure/                     # Camada Técnica e Integrações
    │   ├── persistence/                    # Repositories JPA e DatabaseSeeder
    │   ├── security/                       # SecurityConfig, JwtAuthenticationFilter, TokenService, AuditorAwareImpl
    │   ├── storage/                        # Serviço de armazenamento de PDFs
    │   └── ia/                             # Spring AI e Vetorização RAG
    │
    └── presentation/                       # Adaptadores de Entrada (REST API)
        ├── controllers/                    # Endpoints REST e GlobalExceptionHandler
        └── dtos/                           # Records de Entrada e Saída com Bean Validation
```