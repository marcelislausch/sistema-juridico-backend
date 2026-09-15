package com.sistemajuridico.backend.infrastructure.integrations.pje;

import com.sistemajuridico.backend.presentation.dtos.pje.ComunicaPjeResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Collections;

@Component
public class ComunicaPjeClient {

    private static final Logger log = LoggerFactory.getLogger(ComunicaPjeClient.class);

    @Value("${comunica.pje.api.url:https://comunicaapi.pje.jus.br/api/v1/comunicacao}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public ComunicaPjeClient() {
        this.restTemplate = new RestTemplate();
    }

    public ComunicaPjeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ComunicaPjeClient(String apiUrl, RestTemplate restTemplate) {
        this.apiUrl = apiUrl;
        this.restTemplate = restTemplate;
    }

    /**
     * Realiza consulta publica a API do Comunica PJe (DJEN) por numero e UF da OAB.
     * Sem necessidade de certificados ou tokens mTLS (Acesso Publico).
     */
    public ComunicaPjeResponseDTO consultar(String numeroOab, String ufOab,
                                           LocalDate dataInicio, LocalDate dataFim,
                                           int pagina, int itensPorPagina) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.apiUrl)
                    .queryParam("numeroOab", numeroOab)
                    .queryParam("ufOab", ufOab)
                    .queryParam("pagina", pagina)
                    .queryParam("itensPorPagina", itensPorPagina);

            if (dataInicio != null) {
                builder.queryParam("dataDisponibilizacaoInicio", dataInicio.toString());
            }
            if (dataFim != null) {
                builder.queryParam("dataDisponibilizacaoFim", dataFim.toString());
            }

            String urlConstruida = builder.toUriString();
            log.info("Consultando Comunica PJe para OAB {}/{}: {}", numeroOab, ufOab, urlConstruida);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 SistemaJuridico/3.3");

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ComunicaPjeResponseDTO> response = this.restTemplate.exchange(
                    urlConstruida,
                    HttpMethod.GET,
                    requestEntity,
                    ComunicaPjeResponseDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Rate limit atingido (HTTP 429) no Comunica PJe ao consultar OAB {}/{}. Aplicando backoff defensivo de 10s...", numeroOab, ufOab);
            executarDelayDefensivo(10000L);
        } catch (HttpClientErrorException e) {
            log.error("Erro HTTP ({}) ao consultar Comunica PJe para OAB {}/{}: {}", e.getStatusCode(), numeroOab, ufOab, e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado de comunicacao com Comunica PJe para OAB {}/{}: {}", numeroOab, ufOab, e.getMessage(), e);
        }

        return null;
    }

    /**
     * Pausa defensiva (throttling anti-ban) entre requisicoes e paginacoes para proteger o IP da VPS.
     */
    public void executarDelayDefensivo(long milissegundos) {
        try {
            log.debug("Executando delay defensivo de throttling por {} ms...", milissegundos);
            Thread.sleep(milissegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread de espera defensiva do Comunica PJe foi interrompida.");
        }
    }
}
