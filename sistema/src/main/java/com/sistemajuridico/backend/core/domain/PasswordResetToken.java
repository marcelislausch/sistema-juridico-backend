package com.sistemajuridico.backend.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(nullable = false)
    private boolean usado = false;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String token, UUID usuarioId, LocalDateTime dataExpiracao) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.dataExpiracao = dataExpiracao;
        this.usado = false;
    }

    public PasswordResetToken(UUID id, String token, UUID usuarioId, LocalDateTime dataExpiracao, boolean usado) {
        this.id = id;
        this.token = token;
        this.usuarioId = usuarioId;
        this.dataExpiracao = dataExpiracao;
        this.usado = usado;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }
}
