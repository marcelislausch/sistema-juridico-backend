package com.sistemajuridico.backend.infrastructure.ai;

import com.sistemajuridico.backend.core.domain.dto.ResumoAudienciaEstruturadoDTO;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SpringAIResumoService implements ResumoAIService {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIResumoService.class);

    private final ChatClient chatClient;

    public SpringAIResumoService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public ResumoAudienciaEstruturadoDTO resumirPecas(String conteudo) {
        String systemPrompt = "Você é um especialista em estratégia contenciosa e direito processual brasileiro de alto nível. "
                + "Sua missão é analisar minuciosamente as peças processuais fornecidas e extrair um dossiê preparatório tático para a realização de uma audiência judicial.\n\n"
                + "Você deve ler atentamente a petição e extrair rigorosamente as seguintes categorias:\n"
                + "1. 'fatosIncontroversos': lista de fatos já admitidos, pacificados ou convergentes entre as partes.\n"
                + "2. 'fatosControvertidos': lista de pontos de conflito e divergência fática que demandam dilação probatória, especialmente prova oral ou depoimento pessoal.\n"
                + "3. 'riscosProcessuais': lista de preliminares processuais relevantes, eventuais fragilidades probatórias, prescrição/decadência e pontos de vulnerabilidade da tese.\n"
                + "4. 'roteiroPerguntas': lista de perguntas táticas e pontuais sugeridas para inquirição de testemunhas ou depoimento pessoal da parte contrária na audiência.\n"
                + "5. 'parametrosAcordo': diretrizes, cenários e estimativas táticas recomendadas para formulação ou avaliação de propostas de conciliação e acordo.";

        try {
            ResumoAudienciaEstruturadoDTO resumo = this.chatClient.prompt()
                    .system(systemPrompt)
                    .user(conteudo)
                    .call()
                    .entity(ResumoAudienciaEstruturadoDTO.class);

            if (resumo == null) {
                throw new RegraNegocioException("O modelo de inteligência artificial não retornou conteúdo estruturado para o resumo.");
            }

            return resumo;
        } catch (RegraNegocioException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RegraNegocioException("Falha ao gerar resumo estruturado pela inteligência artificial: " + ex.getMessage());
        }
    }

    @Override
    public String resumirChunk(String chunk, int parte, int totalPartes) {
        if (chunk == null || chunk.trim().isEmpty()) {
            return "";
        }

        String systemPrompt = "Você é um assistente jurídico sênior especializado em triagem e síntese processual. "
                + "Você está analisando a PARTE " + parte + " DE " + totalPartes + " dos autos de um processo judicial extenso.\n\n"
                + "Extraia objetivamente deste trecho:\n"
                + "- Fatos alegados, causas de pedir e teses defensivas;\n"
                + "- Pedidos formulados e preliminares processuais suscitadas;\n"
                + "- Provas documentais ou testemunhais mencionadas;\n"
                + "- Pontos controvertidos identificados.\n\n"
                + "Mantenha máxima fidelidade factual e jurídica, de forma concisa e direta.";

        try {
            String resposta = this.chatClient.prompt()
                    .system(systemPrompt)
                    .user(chunk)
                    .call()
                    .content();

            if (resposta == null) {
                return "";
            }
            return resposta.trim();
        } catch (Exception ex) {
            logger.error("Falha ao sintetizar o chunk {}/{}: {}", parte, totalPartes, ex.getMessage());
            return "";
        }
    }
}


