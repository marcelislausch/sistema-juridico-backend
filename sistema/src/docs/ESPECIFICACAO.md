# Documento de Especificação de Software (PRD) - v2.0

**Projeto:** Sistema de Gestão Jurídica Inteligente (LegalTech)  
**Perfil:** Backend Corporativo / Portfólio  
**Padrão Arquitetural:** Clean Architecture (Arquitetura Hexagonal)

## 1. Módulos do Sistema

O sistema é dividido em 4 grandes módulos lógicos:

*   **IAM (Identity & Access Management):** Controle de usuários, perfis (Advogado, Secretária) e autenticação via JWT.
*   **Core Legal:** Gestão de clientes, processos judiciais e andamentos (linha do tempo).
*   **Financeiro & Agenda:** Controle de honorários, custas e audiências.
*   **GED & IA:** Gestão Eletrônica de Documentos e o motor de consulta inteligente via RAG.

## 2. Modelo de Domínio (Entidades e Regras de Negócio)

| Entidade | Atributos Principais | Relacionamentos | Regras de Negócio Essenciais |
| :--- | :--- | :--- | :--- |
| **Usuario** | `id`, `nome`, `email`, `senhaHash`, `perfil` (Enum: ADVOGADO, SECRETARIA), `oab`, `ativo` | 1:N Processos <br> 1:N Auditorias | O e-mail deve ser único. Apenas perfis ADVOGADO podem arquivar processos. |
| **Cliente** | `id`, `nome`, `tipo` (Enum: FISICA, JURIDICA), `cpfCnpj`, `telefone`, `email` | 1:N Processos | CPF/CNPJ deve passar por validação de dígito verificador na criação. |
| **Processo** | `id`, `numeroCnj`, `assunto`, `faseAtual`, `dataCriacao` | N:1 Cliente <br> N:1 Usuario | Não pode ser encerrado se houver fatura pendente. |
| **Andamento** | `id`, `dataHora`, `descricao`, `tipo` (Enum: AUTOMATICO, MANUAL, IA) | N:1 Processo | Registro imutável. Uma vez criado, não pode ser deletado, apenas retificado. |
| **Faturamento**| `id`, `tipo` (Enum: HONORARIO, CUSTA), `valor`, `vencimento`, `status` | N:1 Processo | Ao marcar como PAGO, o sistema deve exigir a data efetiva do pagamento. |
| **Audiencia** | `id`, `dataHora`, `local`, `observacoes`, `status` (Enum: AGENDADA, REALIZADA, CANCELADA)| N:1 Processo | Não permite agendamento retroativo para novas audiências. |
| **Documento** | `id`, `nomeArquivo`, `tipoDoc` (Enum: PETICAO, SENTENCA), `caminhoStorage`, `indexadoIA` | N:1 Processo | A flag `indexadoIA` só vira `true` após o sucesso da vetorização (assíncrono). |

> **Auditoria (Cross-cutting):** Todas as entidades possuem atributos de rastreabilidade: `criadoPor`, `criadoEm`, `atualizadoPor`, `atualizadoEm`.

## 3. Segurança e Infraestrutura

*   **Autenticação:** Baseada em Tokens (JWT - JSON Web Token). O frontend enviará o token no cabeçalho `Authorization: Bearer <token>`.
*   **Senhas:** Criptografia unidirecional usando o algoritmo BCrypt no momento do cadastro.
*   **Armazenamento de Arquivos:** PDFs não ficam no banco de dados. Serão salvos em storage (S3 ou disco local), e o banco guarda o `caminhoStorage`.

## 4. Estrutura de Pastas (Clean Architecture)

```plaintext
├── core/                       # Domínio e Casos de Uso (Java Puro)
│   ├── domain/                 # Classes: Usuario, Cliente, Processo...
│   │   ├── exceptions/         # RegraNegocioException
│   │   └── valueobjects/       # CpfCnpj, Email
│   └── usecases/               # AutenticarUsuarioUseCase, CadastrarAudienciaUseCase
│
├── infrastructure/             # Implementações Técnicas (Acoplado ao Framework)
│   ├── persistence/            # Entidades JPA, Repositories Spring Data
│   ├── security/               # Filtros JWT, Spring Security
│   ├── storage/                # Lógica de salvar PDF
│   └── ia/                     # Spring AI, VectorStore
│
└── presentation/               # Portas de Entrada
    ├── controllers/            # Endpoints REST (ProcessoController, etc)
    ├── dtos/                   # Requests e Responses (JSON)
    └── exceptions/             # GlobalExceptionHandler