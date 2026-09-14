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

@Tag(name = "Autenticação", description = "Login, perfil autenticado, recuperação e alteração de senha")
public interface AuthControllerOpenApi {

    @Operation(summary = "Efetuar login", description = "Autentica as credenciais do usuário e retorna o token JWT de acesso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (e-mail ou senha incorretos)",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<TokenDTO> login(LoginDTO dto);

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
    ResponseEntity<UsuarioResponseDTO> me();

    @Operation(summary = "Solicitar recuperação de senha", description = "Inicia o fluxo de recuperação de senha gerando token temporário enviado por e-mail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Solicitação aceita; se a conta existir, as instruções foram enviadas"),
            @ApiResponse(responseCode = "400", description = "E-mail inválido ou malformatado",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Map<String, String>> recuperarSenha(RecuperarSenhaRequest request);

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
    ResponseEntity<Map<String, String>> redefinirSenha(RedefinirSenhaRequest request);

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
    ResponseEntity<Void> alterarSenha(AlterarSenhaRequest request);
}
