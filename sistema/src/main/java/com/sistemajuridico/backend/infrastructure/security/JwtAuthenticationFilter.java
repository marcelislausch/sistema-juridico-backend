package com.sistemajuridico.backend.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public JwtAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
        }

        if (path != null) {
            if (path.startsWith("/api/auth/recuperar-senha")
                    || path.startsWith("/api/auth/redefinir-senha")
                    || path.startsWith("/api/auth/login")
                    || path.startsWith("/api/integracoes/google-calendar/webhook")
                    || path.startsWith("/v3/api-docs")
                    || path.startsWith("/swagger-ui")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extrairToken(request);

            if (token != null) {
                String email = tokenService.validarToken(token);
                if (email != null && !email.isEmpty()) {
                    String perfil = tokenService.extrairPerfil(token);
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (perfil != null && !perfil.isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + perfil));
                        authorities.add(new SimpleGrantedAuthority(perfil));
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            // Em caso de qualquer falha na leitura ou validação de token, limpa o contexto e prossegue
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extrairToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
