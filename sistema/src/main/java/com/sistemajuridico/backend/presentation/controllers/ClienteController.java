package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.usecases.CadastrarClienteUseCase;
import com.sistemajuridico.backend.core.usecases.ListarClientesUseCase;
import com.sistemajuridico.backend.presentation.dtos.ClienteDTO;
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
    public ResponseEntity<ClienteDTO> criar(@RequestBody ClienteDTO dto) {
        Cliente clienteSalvo = cadastrarClienteUseCase.executar(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClienteDTO.fromEntity(clienteSalvo));
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listar() {
        List<Cliente> clientes = listarClientesUseCase.executar();

        // Abordagem clássica e explícita
        List<ClienteDTO> response = new ArrayList<>();
        for (Cliente cliente : clientes) {
            response.add(ClienteDTO.fromEntity(cliente));
        }

        return ResponseEntity.ok(response);
    }
}
