package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.usecases.CadastrarClienteUseCase;
import com.sistemajuridico.backend.core.usecases.ListarClientesUseCase;
import com.sistemajuridico.backend.presentation.dtos.ClienteDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final CadastrarClienteUseCase cadastrarClienteUseCase;
    private final ListarClientesUseCase listarClientesUseCase;

    public ClienteController(CadastrarClienteUseCase cadastrarClienteUseCase, ListarClientesUseCase listarClientesUseCase) {
        this.cadastrarClienteUseCase = cadastrarClienteUseCase;
        this.listarClientesUseCase = listarClientesUseCase;
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> criar(@RequestBody @Valid ClienteDTO dto) {
        Cliente clienteSalvo = cadastrarClienteUseCase.executar(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteDTO.fromEntity(clienteSalvo));
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
}
