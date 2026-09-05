package com.sistemajuridico.backend.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.sistemajuridico.backend.core.domain.Usuario;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    private static final String SECRET = "sistemajuridico-secret-key";
    private static final String ISSUER = "sistema-juridico-backend";

    public String gerarToken(Usuario usuario, Boolean manterConectado) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            Instant dataExpiracao;
            if (Boolean.TRUE.equals(manterConectado)) {
                dataExpiracao = LocalDateTime.now().plusDays(7).toInstant(ZoneOffset.of("-03:00"));
            } else {
                dataExpiracao = LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
            }

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getEmail())
                    .withClaim("id", usuario.getId() != null ? usuario.getId().toString() : null)
                    .withClaim("perfil", usuario.getPerfil() != null ? usuario.getPerfil().name() : null)
                    .withExpiresAt(dataExpiracao)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String gerarToken(Usuario usuario) {
        return gerarToken(usuario, false);
    }

    public String validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public String extrairPerfil(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getClaim("perfil")
                    .asString();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }
}
