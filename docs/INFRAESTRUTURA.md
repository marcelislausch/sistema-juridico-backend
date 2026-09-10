# Plano de Infraestrutura e Deploy - Sistema Jurídico

Este documento detalha a arquitetura de produção, segurança e automação do sistema jurídico, com foco em alta disponibilidade e proteção de dados sensíveis.

## 1. Servidor e Sistema Operacional
* **Provedor:** Hostinger (Plano VPS)
* **Sistema Operacional:** Ubuntu 22.04 LTS ou 24.04 LTS.
* **Acesso Administrativo:** SSH estrito com chaves criptográficas (sem autenticação por senha).
* **Atualizações:** Pacote `unattended-upgrades` configurado para aplicar patches de segurança do Ubuntu automaticamente de forma silenciosa.

## 2. Domínio e Roteamento (DNS)
* **Domínio Principal:** `cristhianmenezesadvocacia.com.br` (HostGator).
* **URL do Sistema:** `https://sistema.cristhianmenezesadvocacia.com.br`
* **Apontamento:** Registro tipo "A" na HostGator apontando para o IP da VPS.

## 3. Segurança de Rede e Proxy (Nginx)
* **UFW (Uncomplicated Firewall):** Modo Drop ativo. Portas 80, 443 e 22 abertas. Porta 5432 (Banco) restrita ao `localhost`.
* **Fail2Ban:** Ativo para bloquear IPs com múltiplas falhas de autenticação SSH.
* **Rate Limiting (Antiforça-Bruta):** Nginx configurado com `limit_req` nas rotas de `/login` para bloquear tentativas de adivinhação de senhas em massa.
* **Headers de Segurança (Nginx):** `Strict-Transport-Security` (HSTS), `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY` e `Content-Security-Policy` básico.
* **Criptografia Web:** Certificado SSL/TLS via Let's Encrypt / Certbot.

## 4. Stack de Aplicação, Privilégios Mínimos e Validações
* **Usuários de Sistema:** O Spring Boot roda sob um usuário Linux restrito (`appuser`) via `systemd`.
* **Gerenciamento de Segredos (Secrets):** Nenhuma credencial fica no repositório. O banco e o Google OAuth são alimentados por um arquivo oculto `.env` no servidor, com permissão `chmod 600` (legível apenas pelo `appuser`), injetado via `EnvironmentFile` no serviço.
* **Segurança do Spring Boot:** Endpoints sensíveis do `Actuator` bloqueados. Apenas `/health` e `/info` expostos.
* **Validação de Uploads (GED):** O backend realiza checagem estrita de extensão e MIME type, permitindo exclusivamente `.pdf`, `.docx`, `.doc`, `.png`, `.jpg` e `.jpeg` antes de enviar ao Google Drive.
* **Banco de Dados:** PostgreSQL 15+. Conexão via usuário restrito (`lex_user`), sem privilégios de superusuário.

## 5. Rotina de Backup (Disaster Recovery)
* **Ferramenta:** Script Shell (`pg_dump`) agendado no Cron.
* **Segurança e Retenção:** O backup é criptografado localmente via GPG, sincronizado externamente com o Google Drive via Rclone, e os arquivos com mais de 30 dias são expurgados automaticamente.

## 6. Integração e Entrega Contínuas (CI/CD)
* **Ferramenta:** GitHub Actions.
* **Fluxo de Compilação Externa:** Para poupar CPU/RAM do servidor de produção, o build (`mvn clean package`) ocorre nos servidores do GitHub (Runner). O servidor de produção exige apenas o JRE instalado.
* **Segurança do Deploy:** O GitHub utiliza `scp` (Secure Copy) para transferir o `.jar` final para a VPS utilizando um usuário de sistema dedicado (`deployer`). A chave SSH deste usuário possui restrição `command=` no `authorized_keys`, permitindo exclusivamente a transferência do arquivo e o restart do serviço, sem privilégios de administrador.