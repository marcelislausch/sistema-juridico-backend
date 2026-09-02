package com.sistemajuridico.backend.presentation.dtos;

import java.math.BigDecimal;

public class ResumoFinanceiroDTO {

    private BigDecimal totalReceberPendente;
    private BigDecimal totalPagarPendente;
    private BigDecimal totalRecebidoMes;
    private BigDecimal totalPagoMes;

    public ResumoFinanceiroDTO() {
    }

    public ResumoFinanceiroDTO(BigDecimal totalReceberPendente, BigDecimal totalPagarPendente, BigDecimal totalRecebidoMes, BigDecimal totalPagoMes) {
        this.totalReceberPendente = totalReceberPendente;
        this.totalPagarPendente = totalPagarPendente;
        this.totalRecebidoMes = totalRecebidoMes;
        this.totalPagoMes = totalPagoMes;
    }

    public BigDecimal getTotalReceberPendente() {
        return totalReceberPendente;
    }

    public void setTotalReceberPendente(BigDecimal totalReceberPendente) {
        this.totalReceberPendente = totalReceberPendente;
    }

    public BigDecimal getTotalPagarPendente() {
        return totalPagarPendente;
    }

    public void setTotalPagarPendente(BigDecimal totalPagarPendente) {
        this.totalPagarPendente = totalPagarPendente;
    }

    public BigDecimal getTotalRecebidoMes() {
        return totalRecebidoMes;
    }

    public void setTotalRecebidoMes(BigDecimal totalRecebidoMes) {
        this.totalRecebidoMes = totalRecebidoMes;
    }

    public BigDecimal getTotalPagoMes() {
        return totalPagoMes;
    }

    public void setTotalPagoMes(BigDecimal totalPagoMes) {
        this.totalPagoMes = totalPagoMes;
    }
}
