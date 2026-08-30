package com.sistemajuridico.backend.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SpringAIResumoService implements ResumoAIService {

    private final ChatClient chatClient;

    public SpringAIResumoService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String resumirPecas(String conteudo) {
        String systemPrompt = "Você é um assistente jurídico sênior especializado em análise e síntese de peças processuais. "
                + "Seu objetivo é extrair de forma clara, objetiva e estruturada os pontos principais da lide, os pedidos formulados, "
                + "os principais fundamentos fáticos e jurídicos, as provas apresentadas e os pontos controvertidos, visando preparar o advogado para uma audiência judicial.";

        return this.chatClient.prompt()
                .system(systemPrompt)
                .user(conteudo)
                .call()
                .content();
    }
}
