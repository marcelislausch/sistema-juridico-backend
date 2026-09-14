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

@Tag(name = "Clientes", description = "Gestão cadastral de clientes, procurações e contratos de honorários")
public interface ClienteControllerOpenApi {

    @Operation(summary = "Cadastrar novo cliente", description = "Cadastra um novo cliente (pessoa física ou jurídica) no escritório")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou campos não atendem as validações",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade (ex: CPF/CNPJ já cadastrado)",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<ClienteDTO> criar(ClienteDTO dto);

    @Operation(summary = "Atualizar dados do cliente", description = "Atualiza as informações cadastrais de um cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos ou campos não atendem as validações",
                    content = @Content(schema = @Schema(implementation = ErroValidacaoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<ClienteDTO> atualizar(UUID id, ClienteDTO dto);

    @Operation(summary = "Listar clientes com paginação e filtros", description = "Retorna lista paginada de clientes filtrando por termo de busca ou tipo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de clientes obtida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<Page<ClienteDTO>> listar(String q, String termoBusca, String termoParam, TipoClienteEnum tipo, Pageable pageable);

    @Operation(summary = "Buscar cliente por ID", description = "Recupera os detalhes de um cliente através do seu identificador único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<ClienteDTO> buscarPorId(UUID id);

    @Operation(summary = "Gerar procuração em PDF", description = "Gera o arquivo PDF da procuração ad judicia com os dados do cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Procuração gerada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao gerar documento",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<byte[]> gerarProcuracao(UUID id, String acao, String varaCivel, String comarca, boolean imprimirDeclaracao);

    @Operation(summary = "Gerar contrato de honorários em PDF", description = "Gera a minuta do contrato de prestação de serviços advocatícios em PDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contrato de honorários gerado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acesso proibido",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada ao gerar documento",
                    content = @Content(schema = @Schema(implementation = ErroPadraoDTO.class)))
    })
    ResponseEntity<byte[]> gerarContratoHonorarios(UUID id, String acao, String vara, String comarca, String valorServicos, String objetivoDemanda);
}
