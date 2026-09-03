package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.usecases.AtualizarClienteUseCase;
import com.sistemajuridico.backend.core.usecases.BuscarClientePorIdUseCase;
import com.sistemajuridico.backend.core.usecases.CadastrarClienteUseCase;
import com.sistemajuridico.backend.core.usecases.GerarContratoHonorariosUseCase;
import com.sistemajuridico.backend.core.usecases.GerarProcuracaoClienteUseCase;
import com.sistemajuridico.backend.core.usecases.ListarClientesUseCase;
import com.sistemajuridico.backend.presentation.dtos.ClienteDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<ClienteDTO> criar(@RequestBody @Valid ClienteDTO dto) {
        Cliente clienteSalvo = cadastrarClienteUseCase.executar(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteDTO.fromEntity(clienteSalvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> atualizar(@PathVariable UUID id, @RequestBody @Valid ClienteDTO dto) {
        Cliente cliente = dto.toEntity();
        Cliente clienteAtualizado = atualizarClienteUseCase.executar(id, cliente);
        return ResponseEntity.ok(ClienteDTO.fromEntity(clienteAtualizado));
    }

    @GetMapping
    public ResponseEntity<Page<ClienteDTO>> listar(Pageable pageable) {
        Page<Cliente> paginaClientes = listarClientesUseCase.executar(pageable);
        List<ClienteDTO> dtoList = new ArrayList<>();
        for (Cliente cliente : paginaClientes.getContent()) {
            dtoList.add(ClienteDTO.fromEntity(cliente));
        }
        Page<ClienteDTO> response = new PageImpl<>(dtoList, pageable, paginaClientes.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> buscarPorId(@PathVariable UUID id) {
        Cliente cliente = this.buscarClientePorIdUseCase.executar(id);
        ClienteDTO dto = ClienteDTO.fromEntity(cliente);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/procuracao")
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
