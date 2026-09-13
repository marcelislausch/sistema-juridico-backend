package com.sistemajuridico.backend.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleOAuthTokenManager {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthTokenManager.class);

    @Value("${google.oauth.client.id:}")
    private String clientId;

    @Value("${google.oauth.client.secret:}")
    private String clientSecret;

    @Value("${google.oauth.refresh.token:}")
    private String refreshToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleOAuthTokenManager() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public GoogleOAuthTokenManager(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public GoogleOAuthTokenManager(String clientId, String clientSecret, String refreshToken,
                                  RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String obterAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
            body.add("client_id", this.clientId);
            body.add("client_secret", this.clientSecret);
            body.add("refresh_token", this.refreshToken);
            body.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);

            ResponseEntity<String> response = this.restTemplate.postForEntity(
                    "https://oauth2.googleapis.com/token",
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = this.objectMapper.readTree(response.getBody());
                JsonNode accessTokenNode = rootNode.get("access_token");
                if (accessTokenNode != null && !accessTokenNode.isNull()) {
                    return accessTokenNode.asText();
                }
            }
        } catch (Exception e) {
            log.error("Erro ao renovar token de acesso do Google OAuth: {}", e.getMessage(), e);
        }

        return null;
    }

    public String renovarToken() {
        return obterAccessToken();
    }

    public String executar() {
        return obterAccessToken();
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
