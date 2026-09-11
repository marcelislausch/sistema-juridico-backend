-- ==============================================================================
-- Migration Manual - Atualização de Constraints de Domínio (PostgreSQL)
-- Funcionalidade: Consultas Avulsas e Integração Google Calendar
-- ==============================================================================

-- 1. Atualização da Check Constraint de Tipo em tb_faturamento
-- O Hibernate ddl-auto=update não atualiza constraints existentes ao adicionar valores em Enums Java.
ALTER TABLE tb_faturamento DROP CONSTRAINT IF EXISTS tb_faturamento_tipo_check;

ALTER TABLE tb_faturamento ADD CONSTRAINT tb_faturamento_tipo_check 
    CHECK (tipo IN ('HONORARIOS', 'CUSTAS', 'DESPESAS_ESCRITORIO', 'CONSULTA_AVULSA'));

-- 2. Garantir que processo_id permita valores nulos em tb_faturamento (Consultas sem vínculo processual)
ALTER TABLE tb_faturamento ALTER COLUMN processo_id DROP NOT NULL;

-- 3. Garantir a existência da coluna cliente_id em tb_faturamento com sua Foreign Key
ALTER TABLE tb_faturamento ADD COLUMN IF NOT EXISTS cliente_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_faturamento_cliente'
    ) THEN
        ALTER TABLE tb_faturamento 
            ADD CONSTRAINT fk_faturamento_cliente 
            FOREIGN KEY (cliente_id) REFERENCES tb_cliente(id);
    END IF;
END $$;

-- 4. [Preventivo] Atualização da Check Constraint de Tipo em tb_tarefa para inclusão de 'ATENDIMENTO'
ALTER TABLE tb_tarefa DROP CONSTRAINT IF EXISTS tb_tarefa_tipo_check;

ALTER TABLE tb_tarefa ADD CONSTRAINT tb_tarefa_tipo_check 
    CHECK (tipo IN ('DILIGENCIA', 'PRAZO', 'CONTATO', 'ATENDIMENTO'));

-- 5. [Preventivo] Garantir a existência da coluna google_event_id em tb_tarefa
ALTER TABLE tb_tarefa ADD COLUMN IF NOT EXISTS google_event_id VARCHAR(255);
