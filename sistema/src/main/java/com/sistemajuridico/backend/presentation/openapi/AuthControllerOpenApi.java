package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.presentation.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "AutenticaÃ§Ã£o", description = "Login, perfil autenticado, recuperaÃ§Ã£o e alteraÃ§Ã£o de senha")
public interface AuthControllerOpenApi {

    @Operation(summary = "Efetuar login", description = "Autentica as credenciais do usuÃ¡rio e retorna o token JWT de acesso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AutenticaÃ§Ã£o bem-sucedida"),
            @ApiResponse(responseCode = "400", description = "Dados da requisiÃ§Ã£o invÃ¡lidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais invÃ¡lidas (e-mail ou senha incorretos)",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<TokenDTO> login(LoginDTO dto);

    @Operation(summary = "Obter dados do usuÃ¡rio logado", description = "Recupera os dados de perfil do usuÃ¡rio autenticado no token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil do usuÃ¡rio logado retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado ou token invÃ¡lido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "UsuÃ¡rio associado ao token nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<UsuarioResponseDTO> me();

    @Operation(summary = "Solicitar recuperaÃ§Ã£o de senha", description = "Inicia o fluxo de recuperaÃ§Ã£o de senha gerando token temporÃ¡rio enviado por e-mail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "SolicitaÃ§Ã£o aceita; se a conta existir, as instruÃ§Ãµes foram enviadas"),
            @ApiResponse(responseCode = "400", description = "E-mail invÃ¡lido ou malformatado",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Map<String, String>> recuperarSenha(RecuperarSenhaRequest request);

    @Operation(summary = "Redefinir senha com token", description = "Conclui a redefiniÃ§Ã£o de senha utilizando o token recebido por e-mail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisiÃ§Ã£o invÃ¡lidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Token nÃ£o encontrado ou expirado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "PolÃ­tica de senha nÃ£o atendida ou token invÃ¡lido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Map<String, String>> redefinirSenha(RedefinirSenhaRequest request);

    @Operation(summary = "Alterar senha autenticada", description = "Altera a senha do usuÃ¡rio logado mediante confirmaÃ§Ã£o da senha atual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senha atual invÃ¡lida ou dados incorretos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "UsuÃ¡rio nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "PolÃ­tica de senha nÃ£o atendida",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Void> alterarSenha(AlterarSenhaRequest request);
}
