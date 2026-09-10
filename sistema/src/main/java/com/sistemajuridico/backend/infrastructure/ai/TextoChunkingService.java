package com.sistemajuridico.backend.infrastructure.ai;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextoChunkingService {

    public static final int TAMANHO_MAXIMO_CHUNK = 18000;
    public static final int TAMANHO_MINIMO_CHUNK = 2000;

    public List<String> dividirEmChunks(String texto) {
        List<String> chunks = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            return chunks;
        }

        String textoLimpo = texto.trim();
        if (textoLimpo.length() <= TAMANHO_MAXIMO_CHUNK) {
            chunks.add(textoLimpo);
            return chunks;
        }

        int inicio = 0;
        int totalCaracteres = textoLimpo.length();

        while (inicio < totalCaracteres) {
            int limite = inicio + TAMANHO_MAXIMO_CHUNK;

            if (limite >= totalCaracteres) {
                String pedacoFinal = textoLimpo.substring(inicio).trim();
                if (!pedacoFinal.isEmpty()) {
                    chunks.add(pedacoFinal);
                }
                break;
            }

            // Tenta quebra de parágrafo duplo (\n\n) próxima do limite
            int pontoDeCorte = textoLimpo.lastIndexOf("\n\n", limite);

            // Se não encontrou quebra dupla após a margem mínima, tenta quebra de linha simples (\n)
            if (pontoDeCorte <= inicio + TAMANHO_MINIMO_CHUNK) {
                pontoDeCorte = textoLimpo.lastIndexOf("\n", limite);
            }

            // Se não encontrou quebra simples, tenta ponto final com espaço (". ")
            if (pontoDeCorte <= inicio + TAMANHO_MINIMO_CHUNK) {
                pontoDeCorte = textoLimpo.lastIndexOf(". ", limite);
                if (pontoDeCorte != -1) {
                    pontoDeCorte += 1; // Mantém o ponto junto da frase anterior
                }
            }

            // Caso não haja pontuação no intervalo, força o corte no limite de caracteres
            if (pontoDeCorte <= inicio + TAMANHO_MINIMO_CHUNK) {
                pontoDeCorte = limite;
            }

            String pedaco = textoLimpo.substring(inicio, pontoDeCorte).trim();
            if (!pedaco.isEmpty()) {
                chunks.add(pedaco);
            }

            inicio = pontoDeCorte;
        }

        return chunks;
    }
}
