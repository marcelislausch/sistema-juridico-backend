package com.sistemajuridico.backend.core.service;

import com.sistemajuridico.backend.core.domain.Escritorio;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.infrastructure.persistence.EscritorioRepository;
import com.sistemajuridico.backend.presentation.dtos.EscritorioDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EscritorioService {

    private final EscritorioRepository escritorioRepository;

    public EscritorioService(EscritorioRepository escritorioRepository) {
        this.escritorioRepository = escritorioRepository;
    }

    @Transactional(readOnly = true)
    public EscritorioDTO obterDadosEscritorio() {
        List<Escritorio> escritorios = this.escritorioRepository.findAll();
        if (escritorios.isEmpty()) {
            throw new RecursoNaoEncontradoException("Configurações do escritório não encontradas");
        }

        Escritorio escritorio = escritorios.get(0);
        return EscritorioDTO.fromEntity(escritorio);
    }

    @Transactional
    public EscritorioDTO atualizarDadosEscritorio(EscritorioDTO dto) {
        List<Escritorio> escritorios = this.escritorioRepository.findAll();
        Escritorio escritorio;
        if (!escritorios.isEmpty()) {
            escritorio = escritorios.get(0);
        } else {
            escritorio = new Escritorio();
        }

        String cnpjHigienizado = null;
        if (dto.cnpj() != null) {
            cnpjHigienizado = dto.cnpj().replaceAll("[^0-9]", "");
        }

        String telefoneHigienizado = null;
        if (dto.telefone() != null) {
            telefoneHigienizado = dto.telefone().replaceAll("[^0-9]", "");
        }

        String whatsappHigienizado = null;
        if (dto.whatsapp() != null) {
            whatsappHigienizado = dto.whatsapp().replaceAll("[^0-9]", "");
        }

        String cepHigienizado = null;
        if (dto.cep() != null) {
            cepHigienizado = dto.cep().replaceAll("[^0-9]", "");
        }

        escritorio.setRazaoSocial(dto.razaoSocial());
        escritorio.setNomeFantasia(dto.nomeFantasia());
        escritorio.setCnpj(cnpjHigienizado);
        escritorio.setRegistroOabSociedade(dto.registroOabSociedade());
        escritorio.setTelefone(telefoneHigienizado);
        escritorio.setWhatsapp(whatsappHigienizado);
        escritorio.setEmail(dto.email());
        escritorio.setCep(cepHigienizado);
        escritorio.setLogradouro(dto.logradouro());
        escritorio.setNumero(dto.numero());
        escritorio.setComplemento(dto.complemento());
        escritorio.setBairro(dto.bairro());
        escritorio.setCidade(dto.cidade());
        escritorio.setUf(dto.uf());

        Escritorio salvo = this.escritorioRepository.save(escritorio);
        return EscritorioDTO.fromEntity(salvo);
    }
}
