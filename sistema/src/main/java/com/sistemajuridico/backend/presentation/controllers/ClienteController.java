package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.enums.TipoClienteEnum;
import com.sistemajuridico.backend.core.usecases.AtualizarClienteUseCase;
import com.sistemajuridico.backend.core.usecases.BuscarClientePorIdUseCase;
import com.sistemajuridico.backend.core.usecases.CadastrarClienteUseCase;
import com.sistemajuridico.backend.core.usecases.GerarContratoHonorariosUseCase;
import com.sistemajuridico.backend.core.usecases.GerarProcuracaoClienteUseCase;
import com.sistemajuridico.backend.core.usecases.ListarClientesUseCase;
import com.sistemajuridico.backend.presentation.dtos.ClienteDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroPadraoDTO;
import com.sistemajuridico.backend.presentation.dtos.ErroValidacaoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Gestão cadastral de clientes, procurações e contratos de honorários")
public class ClienteController {

    private final CadastrarClienteUseCase cadastrarClienteUseCase;
    private final AtualizarClienteUseCase atualizarClienteUseCase;
    private final ListarClientesUseCase listarClientesUseCase;
    private final GerarProcuracaoClienteUseCase gerarProcuracaoClienteUseCase;
    private final BuscarClientePorIdUseCase buscarClientePorIdUseCase;
    private final GerarContratoHonorariosUseCase gerarContratoHonorariosUseCase;

    public ClienteController(CadastrarClienteUseCase cadastrarClienteUseCase,
                             AtualizarClienteUseCase atualizarClienteUseCase,
                             ListarClientesUseCase listarClientesUseCase,
                             GerarProcuracaoClienteUseCase gerarProcuracaoClienteUseCase,
                             BuscarClientePorIdUseCase buscarClientePorIdUseCase,
                             GerarContratoHonorariosUseCase gerarContratoHonorariosUseCase) {
        this.cadastrarClienteUseCase = cadastrarClienteUseCase;
        this.atualizarClienteUseCase = atualizarClienteUseCase;
        this.listarClientesUseCase = listarClientesUseCase;
        this.gerarProcuracaoClienteUseCase = gerarProcuracaoClienteUseCase;
        this.buscarClientePorIdUseCase = buscarClientePorIdUseCase;
        this.gerarContratoHonorariosUseCase = gerarContratoHonorariosUseCase;
    }

    @PostMapping
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
    public ResponseEntity<ClienteDTO> criar(@RequestBody @Valid ClienteDTO dto) {
        Cliente clienteSalvo = cadastrarClienteUseCase.executar(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteDTO.fromEntity(clienteSalvo));
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<ClienteDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid ClienteDTO dto) {
        Cliente cliente = dto.toEntity();
        Cliente clienteAtualizado = atualizarClienteUseCase.executar(id, cliente);
        return ResponseEntity.ok(ClienteDTO.fromEntity(clienteAtualizado));
    }

    @GetMapping
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
    public ResponseEntity<Page<ClienteDTO>> listar(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "termoBusca", required = false) String termoBusca,
            @RequestParam(name = "termo", required = false) String termoParam,
            @RequestParam(name = "tipo", required = false) TipoClienteEnum tipo,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        String termo = null;
        if (q != null && !q.trim().isEmpty()) {
            termo = q.trim();
        } else if (termoBusca != null && !termoBusca.trim().isEmpty()) {
            termo = termoBusca.trim();
        } else if (termoParam != null && !termoParam.trim().isEmpty()) {
            termo = termoParam.trim();
        }

        Page<Cliente> paginaClientes = this.listarClientesUseCase.executar(tipo, termo, pageable);
        List<ClienteDTO> dtos = new ArrayList<>();
        for (Cliente cliente : paginaClientes.getContent()) {
            dtos.add(ClienteDTO.fromEntity(cliente));
        }
        Page<ClienteDTO> pageDtos = new PageImpl<>(dtos, paginaClientes.getPageable(), paginaClientes.getTotalElements());
        return ResponseEntity.ok(pageDtos);
    }

    @GetMapping("/{id}")
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
    public ResponseEntity<ClienteDTO> buscarPorId(@PathVariable UUID id) {
        Cliente cliente = this.buscarClientePorIdUseCase.executar(id);
        ClienteDTO dto = ClienteDTO.fromEntity(cliente);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/procuracao")
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
    public ResponseEntity<byte[]> gerarProcuracao(
            @PathVariable UUID id,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String varaCivel,
            @RequestParam(required = false) String comarca,
            @RequestParam(defaultValue = "true") boolean imprimirDeclaracao) {
        byte[] arquivoBytes = this.gerarProcuracaoClienteUseCase.executar(id, acao, varaCivel, comarca, imprimirDeclaracao);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"procuracao.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(arquivoBytes);
    }

    @GetMapping("/{id}/contrato-honorarios")
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
    public ResponseEntity<byte[]> gerarContratoHonorarios(
            @PathVariable UUID id,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String vara,
            @RequestParam(required = false) String comarca,
            @RequestParam(required = false) String valorServicos,
            @RequestParam(required = false) String objetivoDemanda) {
        byte[] arquivoBytes = this.gerarContratoHonorariosUseCase.executar(id, acao, vara, comarca, valorServicos, objetivoDemanda);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contrato-honorarios.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(arquivoBytes);
    }
}
