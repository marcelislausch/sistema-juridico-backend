package com.sistemajuridico.backend.core.usecases;

import com.sistemajuridico.backend.core.domain.Andamento;
import com.sistemajuridico.backend.core.domain.IntimacaoPje;
import com.sistemajuridico.backend.core.domain.Processo;
import com.sistemajuridico.backend.core.domain.Tarefa;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.TipoAndamentoEnum;
import com.sistemajuridico.backend.core.domain.enums.TipoTarefaEnum;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.integrations.pje.ComunicaPjeClient;
import com.sistemajuridico.backend.infrastructure.persistence.AndamentoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.IntimacaoPjeRepository;
import com.sistemajuridico.backend.infrastructure.persistence.ProcessoRepository;
import com.sistemajuridico.backend.infrastructure.persistence.TarefaRepository;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import com.sistemajuridico.backend.presentation.dtos.pje.ComunicaPjeItemDTO;
import com.sistemajuridico.backend.presentation.dtos.pje.ComunicaPjeResponseDTO;
import com.sistemajuridico.backend.presentation.dtos.pje.SincronizacaoPjeResultadoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SincronizarIntimacoesPjeUseCase {

    private static final Logger log = LoggerFactory.getLogger(SincronizarIntimacoesPjeUseCase.class);

    private final ComunicaPjeClient comunicaPjeClient;
    private final UsuarioRepository usuarioRepository;
    private final ProcessoRepository processoRepository;
    private final AndamentoRepository andamentoRepository;
    private final IntimacaoPjeRepository intimacaoPjeRepository;
    private final TarefaRepository tarefaRepository;

    public SincronizarIntimacoesPjeUseCase(ComunicaPjeClient comunicaPjeClient,
                                          UsuarioRepository usuarioRepository,
                                          ProcessoRepository processoRepository,
                                          AndamentoRepository andamentoRepository,
                                          IntimacaoPjeRepository intimacaoPjeRepository,
                                          TarefaRepository tarefaRepository) {
        this.comunicaPjeClient = comunicaPjeClient;
        this.usuarioRepository = usuarioRepository;
        this.processoRepository = processoRepository;
        this.andamentoRepository = andamentoRepository;
        this.intimacaoPjeRepository = intimacaoPjeRepository;
        this.tarefaRepository = tarefaRepository;
    }

    /**
     * Sincroniza intimacoes do Comunica PJe para um advogado especifico.
     * Trata paginacao com delays defensivos para evitar bloqueio de IP da VPS.
     */
    @Transactional
    public SincronizacaoPjeResultadoDTO executarParaAdvogado(Usuario advogado, LocalDate dataInicio, LocalDate dataFim) {
        if (advogado == null) {
            throw new RegraNegocioException("Advogado informado e nulo.");
        }

        String oabRaw = advogado.getOab();
        if (oabRaw == null || oabRaw.trim().isEmpty()) {
            throw new RegraNegocioException("O advogado " + advogado.getNome() + " nao possui numero de OAB cadastrado.");
        }

        String numeroOab = extrairNumeroOab(oabRaw);
        String ufOab = extrairUfOab(oabRaw);

        if (numeroOab.isEmpty()) {
            throw new RegraNegocioException("Nao foi possivel extrair digitos numericos validos da OAB: " + oabRaw);
        }

        log.info("Iniciando sincronizacao de intimacoes do PJe para {} (OAB {}/{}) no periodo de {} a {}",
                advogado.getNome(), numeroOab, ufOab, dataInicio, dataFim);

        int totalEncontradas = 0;
        int novasIntimacoes = 0;
        int andamentosCriados = 0;

        int pagina = 1;
        int itensPorPagina = 50;
        boolean temProximaPagina = true;

        while (temProximaPagina) {
            ComunicaPjeResponseDTO resposta = this.comunicaPjeClient.consultar(
                    numeroOab, ufOab, dataInicio, dataFim, pagina, itensPorPagina
            );

            if (resposta == null || resposta.items() == null || resposta.items().isEmpty()) {
                log.info("Nenhuma comunicacao retornada para OAB {}/{} na pagina {}.", numeroOab, ufOab, pagina);
                break;
            }

            List<ComunicaPjeItemDTO> itens = resposta.items();
            totalEncontradas += itens.size();

            List<IntimacaoPje> intimacoesRecemSalvas = new ArrayList<>();

            for (int i = 0; i < itens.size(); i++) {
                ComunicaPjeItemDTO item = itens.get(i);
                if (item.id() == null) {
                    continue;
                }

                // Evita duplicidade de registro de intimacao
                boolean jaExiste = this.intimacaoPjeRepository.existsByComunicacaoId(item.id());
                if (jaExiste) {
                    log.debug("Comunicacao ID {} ja persistida previamente. Pulando...", item.id());
                    continue;
                }

                IntimacaoPje intimacao = new IntimacaoPje();
                intimacao.setComunicacaoId(item.id());
                intimacao.setHash(item.hash());
                intimacao.setNumeroProcesso(item.numeroProcesso());
                intimacao.setNumeroProcessoMascara(item.numeroProcessoComMascara());
                intimacao.setSiglaTribunal(item.siglaTribunal());
                intimacao.setTipoComunicacao(item.tipoComunicacao());
                intimacao.setTipoDocumento(item.tipoDocumento());
                intimacao.setNomeOrgao(item.nomeOrgao());
                intimacao.setDataDisponibilizacao(item.obterDataDisponibilizacaoAsLocalDate());
                intimacao.setTexto(item.texto());
                intimacao.setLink(item.link());
                intimacao.setNumeroOab(numeroOab);
                intimacao.setUfOab(ufOab);
                intimacao.setAdvogado(advogado);
                intimacao.setLida(false);

                // Vinculacao com Processo existente na base de dados
                Processo processoVinculado = localizarProcessoCorrespondente(item);
                if (processoVinculado != null) {
                    intimacao.setProcesso(processoVinculado);

                    // Cria andamento automatico no processo com texto sanitizado da publicacao
                    Andamento andamento = new Andamento();
                    andamento.setProcesso(processoVinculado);
                    andamento.setTipo(TipoAndamentoEnum.AUTOMATICO);
                    andamento.setDataHora(LocalDateTime.now());

                    String textoSanitizado = "";
                    if (intimacao.getTexto() != null) {
                        String limpo = intimacao.getTexto();
                        // 1. Preserva quebras de linha: converte <br> para \n e </p> para \n\n
                        limpo = limpo.replaceAll("(?i)<br\\s*/?>", "\n");
                        limpo = limpo.replaceAll("(?i)</p>", "\n\n");
                        // 2. Remove blocos CSS vazados
                        limpo = limpo.replaceAll("(?is)<style.*?>.*?</style>", "");
                        // 3. Remove demais tags HTML
                        limpo = limpo.replaceAll("<[^>]*>", "");
                        // 4. Decodifica entidades HTML (&ccedil;, &ordm;, &nbsp;, etc.)
                        limpo = HtmlUtils.htmlUnescape(limpo).replace('\u00A0', ' ');
                        // 5. Normaliza excesso de espacos horizontais (tabs/espacos multiplos)
                        limpo = limpo.replaceAll("[ \\t\\x0B\\f\\r]+", " ");
                        // 6. Normaliza quebras de linha consecutivas para no maximo duas
                        limpo = limpo.replaceAll("\\n{3,}", "\n\n");
                        // 7. Remove espacos residuais nas bordas
                        textoSanitizado = limpo.trim();
                    }

                    String siglaTribunal = intimacao.getSiglaTribunal();
                    if (siglaTribunal == null) {
                        siglaTribunal = "";
                    }

                    String tipoComunicacao = intimacao.getTipoComunicacao();
                    if (tipoComunicacao == null) {
                        tipoComunicacao = "";
                    }

                    String tipoDocumento = intimacao.getTipoDocumento();
                    if (tipoDocumento == null) {
                        tipoDocumento = "";
                    }

                    String nomeOrgao = intimacao.getNomeOrgao();
                    if (nomeOrgao == null) {
                        nomeOrgao = "";
                    }

                    String descricaoAndamento = "[PJe - " + siglaTribunal + "] " + tipoComunicacao
                            + " (" + tipoDocumento + ")\nÓrgão: " + nomeOrgao + "\n\n" + textoSanitizado;

                    andamento.setDescricao(descricaoAndamento);
                    this.andamentoRepository.save(andamento);
                    andamentosCriados++;
                }

                this.intimacaoPjeRepository.save(intimacao);
                intimacoesRecemSalvas.add(intimacao);
                novasIntimacoes++;
            }

            // Injeta as novas intimacoes diretamente na agenda do advogado como Tarefas triadas
            List<Tarefa> tarefasParaSalvar = new ArrayList<>();
            for (int i = 0; i < intimacoesRecemSalvas.size(); i++) {
                IntimacaoPje intimacao = intimacoesRecemSalvas.get(i);

                String tipoCom = intimacao.getTipoComunicacao();
                if (tipoCom == null) {
                    tipoCom = "";
                }

                // 1. Descarte de Informativos: nao cria tarefa, mantem apenas no historico
                if (tipoCom.equalsIgnoreCase("Lista de distribuição")
                        || tipoCom.equalsIgnoreCase("Lista de distribuicao")
                        || tipoCom.equalsIgnoreCase("Ata de sessão")
                        || tipoCom.equalsIgnoreCase("Ata de sessao")) {
                    continue;
                }

                String numeroProcesso = intimacao.getNumeroProcesso();
                if (numeroProcesso == null || numeroProcesso.trim().isEmpty()) {
                    numeroProcesso = intimacao.getNumeroProcessoMascara();
                }
                if (numeroProcesso == null || numeroProcesso.trim().isEmpty()) {
                    numeroProcesso = "Sem número";
                }

                String siglaTribunal = intimacao.getSiglaTribunal();
                if (siglaTribunal == null) {
                    siglaTribunal = "";
                }

                Tarefa tarefa = new Tarefa();

                // 2. Alerta de Pauta (Diligência)
                if (tipoCom.equalsIgnoreCase("Pauta de julgamento")) {
                    tarefa.setTipo(TipoTarefaEnum.DILIGENCIA);
                    tarefa.setDescricao("[DILIGÊNCIA - PAUTA] Proc. " + numeroProcesso + " (" + siglaTribunal + ")");
                } else {
                    // 3. Gestão de Prazos (Intimação/Citação e demais comunicações)
                    tarefa.setTipo(TipoTarefaEnum.PRAZO);

                    String tipoDoc = intimacao.getTipoDocumento();
                    if (tipoDoc == null) {
                        tipoDoc = "";
                    }

                    String prefixo;
                    if (tipoDoc.equalsIgnoreCase("Sentença") || tipoDoc.equalsIgnoreCase("Sentenca")) {
                        prefixo = "[URGENTE - SENTENÇA]";
                    } else if (tipoDoc.equalsIgnoreCase("DESPACHO/DECISÃO")
                            || tipoDoc.equalsIgnoreCase("DESPACHO/DECISAO")
                            || tipoDoc.equalsIgnoreCase("Despacho")
                            || tipoDoc.equalsIgnoreCase("Decisão")
                            || tipoDoc.equalsIgnoreCase("Decisao")) {
                        prefixo = "[URGENTE - DECISÃO]";
                    } else if (tipoDoc.equalsIgnoreCase("Ato ordinatório")
                            || tipoDoc.equalsIgnoreCase("Ato ordinatorio")) {
                        prefixo = "[PRAZO - ATO ORDINATÓRIO]";
                    } else if (tipoDoc.equalsIgnoreCase("Notificação")
                            || tipoDoc.equalsIgnoreCase("Notificacao")) {
                        prefixo = "[PRAZO - NOTIFICAÇÃO]";
                    } else {
                        prefixo = "[PRAZO - ATENÇÃO]";
                    }

                    tarefa.setDescricao(prefixo + " Proc. " + numeroProcesso + " (" + siglaTribunal + ")");
                }

                LocalDate dataVencimento = intimacao.getDataDisponibilizacao();
                if (dataVencimento == null) {
                    dataVencimento = LocalDate.now();
                }
                tarefa.setDataVencimento(dataVencimento);
                tarefa.setConcluida(false);
                tarefa.setUsuario(advogado);

                if (intimacao.getProcesso() != null) {
                    tarefa.setProcesso(intimacao.getProcesso());
                }

                tarefasParaSalvar.add(tarefa);
            }

            if (!tarefasParaSalvar.isEmpty()) {
                this.tarefaRepository.saveAll(tarefasParaSalvar);
            }

            // Controle de paginacao
            if (itens.size() < itensPorPagina) {
                temProximaPagina = false;
            } else {
                pagina++;
                // Pausa defensiva de 2,5 segundos entre paginas para proteger o IP da VPS
                this.comunicaPjeClient.executarDelayDefensivo(2500L);
            }
        }

        String mensagem = String.format("Sincronização concluída para OAB %s/%s. Total consultado: %d, novas intimações: %d, andamentos vinculados: %d.",
                numeroOab, ufOab, totalEncontradas, novasIntimacoes, andamentosCriados);

        log.info(mensagem);

        return new SincronizacaoPjeResultadoDTO(
                totalEncontradas,
                novasIntimacoes,
                andamentosCriados,
                numeroOab,
                ufOab,
                mensagem,
                LocalDateTime.now()
        );
    }

    /**
     * Varredura geral noturna iterando imperativamente sobre todos os advogados ativos com OAB.
     */
    public void executarVarreduraGeral(LocalDate dataInicio, LocalDate dataFim) {
        log.info("Iniciando varredura geral de intimacoes do PJe...");

        List<Usuario> advogados = this.usuarioRepository.buscarAdvogadosComOabAtiva();
        log.info("Localizados {} advogados com OAB ativa para varredura.", advogados.size());

        for (int i = 0; i < advogados.size(); i++) {
            Usuario adv = advogados.get(i);
            try {
                executarParaAdvogado(adv, dataInicio, dataFim);
            } catch (Exception e) {
                log.error("Falha ao sincronizar intimacoes do advogado {} (OAB: {}): {}", adv.getNome(), adv.getOab(), e.getMessage(), e);
            }

            // Pausa defensiva de 3,5 segundos entre advogados para anti-ban de IP
            if (i < advogados.size() - 1) {
                this.comunicaPjeClient.executarDelayDefensivo(3500L);
            }
        }

        log.info("Varredura geral de intimacoes do PJe finalizada.");
    }

    /**
     * Localiza processo na base de dados comparando numero com mascara ou limpo de pontuacao.
     */
    private Processo localizarProcessoCorrespondente(ComunicaPjeItemDTO item) {
        if (item.numeroProcessoComMascara() != null && !item.numeroProcessoComMascara().trim().isEmpty()) {
            Optional<Processo> optPorMascara = this.processoRepository.findByNumeroCnjExato(item.numeroProcessoComMascara().trim());
            if (!optPorMascara.isEmpty()) {
                return optPorMascara.get();
            }
        }

        if (item.numeroProcesso() != null && !item.numeroProcesso().trim().isEmpty()) {
            Optional<Processo> optPorLimpo = this.processoRepository.findByNumeroCnjLimpo(item.numeroProcesso().trim());
            if (!optPorLimpo.isEmpty()) {
                return optPorLimpo.get();
            }
        }

        return null;
    }

    /**
     * Extrai digitos numericos da OAB de forma estritamente imperativa.
     *
     */
    private String extrairNumeroOab(String oabRaw) {
        if (oabRaw == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < oabRaw.length(); i++) {
            char c = oabRaw.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Extrai a UF da OAB de forma imperativa (default RS caso nao especificada).
     */
    private String extrairUfOab(String oabRaw) {
        if (oabRaw == null) {
            return "RS";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < oabRaw.length(); i++) {
            char c = oabRaw.charAt(i);
            if (Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c));
            }
        }
        String letras = sb.toString();
        letras = letras.replace("OAB", "").trim();

        if (letras.length() >= 2) {
            return letras.substring(0, 2);
        }

        return "RS";
    }
}
