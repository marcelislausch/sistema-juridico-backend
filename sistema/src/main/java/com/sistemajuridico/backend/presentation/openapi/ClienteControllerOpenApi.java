package com.sistemajuridico.backend.presentation.openapi;

import com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum;
import com.sistemajuridico.backend.presentation.dtos.ClienteDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Clientes", description = "GestÃ£o cadastral de clientes, procuraÃ§Ãµes e contratos de honorÃ¡rios")
public interface ClienteControllerOpenApi {

    @Operation(summary = "Cadastrar novo cliente", description = "Cadastra um novo cliente (pessoa fÃ­sica ou jurÃ­dica) no escritÃ³rio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisiÃ§Ã£o invÃ¡lidos ou campos nÃ£o atendem as validaÃ§Ãµes",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade (ex: CPF/CNPJ jÃ¡ cadastrado)",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<ClienteDTO> criar(ClienteDTO dto);

    @Operation(summary = "Atualizar dados do cliente", description = "Atualiza as informaÃ§Ãµes cadastrais de um cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisiÃ§Ã£o invÃ¡lidos ou campos nÃ£o atendem as validaÃ§Ãµes",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<ClienteDTO> atualizar(UUID id, ClienteDTO dto);

    @Operation(summary = "Listar clientes com paginaÃ§Ã£o e filtros", description = "Retorna lista paginada de clientes filtrando por termo de busca ou tipo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PÃ¡gina de clientes obtida com sucesso"),
            @ApiResponse(responseCode = "400", description = "ParÃ¢metros de consulta invÃ¡lidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Page<ClienteDTO>> listar(String q, String termoBusca, String termoParam, TipoClienteEnum tipo, Pageable pageable);

    @Operation(summary = "Buscar cliente por ID", description = "Recupera os detalhes de um cliente atravÃ©s do seu identificador Ãºnico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<ClienteDTO> buscarPorId(UUID id);

    @Operation(summary = "Gerar procuraÃ§Ã£o em PDF", description = "Gera o arquivo PDF da procuraÃ§Ã£o ad judicia com os dados do cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ProcuraÃ§Ã£o gerada com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada ao gerar documento",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<byte[]> gerarProcuracao(UUID id, String acao, String varaCivel, String comarca, boolean imprimirDeclaracao);

    @Operation(summary = "Gerar contrato de honorÃ¡rios em PDF", description = "Gera a minuta do contrato de prestaÃ§Ã£o de serviÃ§os advocatÃ­cios em PDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contrato de honorÃ¡rios gerado com sucesso"),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente nÃ£o encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negÃ³cio violada ao gerar documento",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<byte[]> gerarContratoHonorarios(UUID id, String acao, String vara, String comarca, String valorServicos, String objetivoDemanda);
}
