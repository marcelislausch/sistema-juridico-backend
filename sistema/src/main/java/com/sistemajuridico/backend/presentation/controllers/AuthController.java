package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.service.AuthService;
import com.sistemajuridico.backend.core.usecases.AutenticarUsuarioUseCase;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login, perfil autenticado, recuperação e alteração de senha")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                          UsuarioRepository usuarioRepository,
                          AuthService authService) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Efetuar login", description = "Autentica as credenciais do usuário e retorna o token JWT de acesso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (e-mail ou senha incorretos)",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginDTO dto) {
        String token = autenticarUsuarioUseCase.executar(dto);
        return ResponseEntity.ok(new TokenDTO(token));
    }

    @GetMapping("/me")
    @Operation(summary = "Obter dados do usuário logado", description = "Recupera os dados de perfil do usuário autenticado no token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil do usuário logado retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário associado ao token não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> me() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = null;
        if (principal != null) {
            email = principal.toString();
        }

        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado");
        }

        Usuario usuario = optUsuario.get();
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuario));
    }

    @PostMapping("/recuperar-senha")
    @Operation(summary = "Solicitar recuperação de senha", description = "Inicia o fluxo de recuperação de senha gerando token temporário enviado por e-mail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Solicitação aceita; se a conta existir, as instruções foram enviadas"),
            @ApiResponse(responseCode = "400", description = "E-mail inválido ou malformatado",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<Map<String, String>> recuperarSenha(@RequestBody @Valid RecuperarSenhaRequest request) {
        this.authService.solicitarRecuperacaoSenha(request.email());
        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", "Se a conta existir, as instruções serão enviadas.");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resposta);
    }

    @PostMapping("/redefinir-senha")
    @Operation(summary = "Redefinir senha com token", description = "Conclui a redefinição de senha utilizando o token recebido por e-mail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Token não encontrado ou expirado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Política de senha não atendida ou token inválido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody @Valid RedefinirSenhaRequest request) {
        this.authService.redefinirSenha(request.token(), request.novaSenha());
        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", "Senha redefinida com sucesso.");
        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/me/senha")
    @Operation(summary = "Alterar senha autenticada", description = "Altera a senha do usuário logado mediante confirmação da senha atual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senha atual inválida ou dados incorretos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Política de senha não atendida",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    public ResponseEntity<Void> alterarSenha(@RequestBody @Valid AlterarSenhaRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = null;
        if (principal != null) {
            email = principal.toString();
        }

        this.authService.alterarSenhaAutenticada(email, request.senhaAtual(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }
}
