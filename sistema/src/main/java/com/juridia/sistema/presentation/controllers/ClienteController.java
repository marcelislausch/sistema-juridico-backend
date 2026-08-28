package com.juridia.sistema.presentation.controllers;

import com.juridia.sistema.core.domain.Cliente;
import com.juridia.sistema.core.usecases.CadastrarClienteUseCase;
import com.juridia.sistema.core.usecases.ListarClientesUseCase;
import com.juridia.sistema.presentation.dtos.ClienteDTO;
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