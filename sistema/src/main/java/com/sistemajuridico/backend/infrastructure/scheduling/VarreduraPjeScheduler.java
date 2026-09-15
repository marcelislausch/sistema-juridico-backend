package com.sistemajuridico.backend.infrastructure.scheduling;

import com.sistemajuridico.backend.core.usecases.SincronizarIntimacoesPjeUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class VarreduraPjeScheduler {

    private static final Logger log = LoggerFactory.getLogger(VarreduraPjeScheduler.class);

    private final SincronizarIntimacoesPjeUseCase sincronizarIntimacoesPjeUseCase;

    public VarreduraPjeScheduler(SincronizarIntimacoesPjeUseCase sincronizarIntimacoesPjeUseCase) {
        this.sincronizarIntimacoesPjeUseCase = sincronizarIntimacoesPjeUseCase;
    }

    /**
     * Rotina automatica agendada executada diariamente as 02h00 da madrugada.
     * Consulta publicacoes e intimacoes disponibilizadas nos ultimos 3 dias no DJEN/PJe
     * cobrindo finais de semana e feriados com rate limiting defensivo.
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "America/Sao_Paulo")
    public void executarVarreduraNoturna() {
        log.info("Iniciando varredura noturna agendada de intimacoes do Comunica PJe (02h00)...");

        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusDays(3);

        try {
            this.sincronizarIntimacoesPjeUseCase.executarVarreduraGeral(dataInicio, dataFim);
            log.info("Varredura noturna do Comunica PJe finalizada com exito.");
        } catch (Exception e) {
            log.error("Falha critica na execucao da varredura noturna do Comunica PJe: {}", e.getMessage(), e);
        }
    }
}
