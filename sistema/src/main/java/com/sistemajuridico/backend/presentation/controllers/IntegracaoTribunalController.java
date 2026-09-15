package com.sistemajuridico.backend.presentation.controllers;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.StatusTribunalEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.core.usecases.SincronizarIntimacoesPjeUseCase;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.TribunalStatusDTO;
import com.sistemajuridico.backend.presentation.dtos.pje.SincronizacaoPjeResultadoDTO;
import com.sistemajuridico.backend.presentation.openapi.IntegracaoTribunalControllerOpenApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/integracoes")
public class IntegracaoTribunalController implements IntegracaoTribunalControllerOpenApi {

    private final UsuarioRepository usuarioRepository;
    private final SincronizarIntimacoesPjeUseCase sincronizarIntimacoesPjeUseCase;

    public IntegracaoTribunalController(UsuarioRepository usuarioRepository,
                                        SincronizarIntimacoesPjeUseCase sincronizarIntimacoesPjeUseCase) {
        this.usuarioRepository = usuarioRepository;
        this.sincronizarIntimacoesPjeUseCase = sincronizarIntimacoesPjeUseCase;
    }

    @Override
    @GetMapping("/tribunais/status")
    public ResponseEntity<TribunalStatusDTO> verificarStatusTribunais() {
        List<String> tribunais = new ArrayList<>();
        tribunais.add("TJRS - Tribunal de Justiça do Rio Grande do Sul");
        tribunais.add("TRF4 - Tribunal Regional Federal da 4ª Região");
        tribunais.add("TRT4 - Tribunal Regional do Trabalho da 4ª Região");
        tribunais.add("STJ - Superior Tribunal de Justiça");
        tribunais.add("Comunica PJe - Diário de Justiça Eletrônico Nacional (DJEN)");

        TribunalStatusDTO status = new TribunalStatusDTO(
                StatusTribunalEnum.OPERACIONAL,
                LocalDateTime.now(),
                "Todos os serviços judiciais operando com sincronização regular.",
                tribunais
        );
        return ResponseEntity.ok(status);
    }

    @Override
    @PostMapping("/pje/sincronizar")
    public ResponseEntity<SincronizacaoPjeResultadoDTO> sincronizarIntimacoesPje(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new RegraNegocioException("Usuário não autenticado.");
        }

        String email = principal.getName();
        Optional<Usuario> optUsuario = this.usuarioRepository.findByEmail(email);
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário autenticado não localizado no sistema.");
        }

        Usuario advogado = optUsuario.get();
        if (advogado.getOab() == null || advogado.getOab().trim().isEmpty()) {
            throw new RegraNegocioException("O usuário autenticado (" + advogado.getNome() + ") não possui registro de OAB cadastrado.");
        }

        LocalDate dataFim = LocalDate.now();
        LocalDate dataInicio = dataFim.minusDays(5);

        SincronizacaoPjeResultadoDTO resultado = this.sincronizarIntimacoesPjeUseCase.executarParaAdvogado(
                advogado, dataInicio, dataFim
        );

        return ResponseEntity.ok(resultado);
    }
}
