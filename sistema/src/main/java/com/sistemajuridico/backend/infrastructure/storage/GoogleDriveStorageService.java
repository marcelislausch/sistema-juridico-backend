package com.sistemajuridico.backend.infrastructure.storage;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.sistemajuridico.backend.infrastructure.security.GoogleOAuthTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
@Primary
public class GoogleDriveStorageService implements StorageService {

    @Value("${google.drive.folder.id:}")
    private String folderId;

    private final GoogleOAuthTokenManager googleOAuthTokenManager;

    public GoogleDriveStorageService() {
        this.googleOAuthTokenManager = new GoogleOAuthTokenManager();
    }

    @Autowired
    public GoogleDriveStorageService(GoogleOAuthTokenManager googleOAuthTokenManager) {
        this.googleOAuthTokenManager = googleOAuthTokenManager;
    }

    public GoogleDriveStorageService(GoogleOAuthTokenManager googleOAuthTokenManager, String folderId) {
        this.googleOAuthTokenManager = googleOAuthTokenManager;
        this.folderId = folderId;
    }

    private Drive obterDriveService() {
        String token = this.googleOAuthTokenManager.obterAccessToken();
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Não foi possível obter o token de acesso do Google OAuth para o Google Drive.");
        }

        try {
            Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                    .build()
                    .setAccessToken(token.trim());

            return new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("Sistema Jurídico - GED")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Falha de E/S ao inicializar cliente OAuth do Google Drive: " + e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Falha de segurança ao inicializar cliente OAuth do Google Drive: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro inesperado ao inicializar cliente OAuth do Google Drive: " + e.getMessage(), e);
        }
    }

    @Override
    public String salvarArquivo(String nomeOriginal, byte[] dados) {
        return upload(nomeOriginal, dados);
    }

    public String upload(String nomeOriginal, byte[] dados) {
        Drive driveService = obterDriveService();

        if (dados == null || dados.length == 0) {
            throw new RuntimeException("O conteúdo do arquivo para upload não pode ser nulo ou vazio.");
        }

        if (nomeOriginal == null || nomeOriginal.trim().isEmpty()) {
            throw new IllegalArgumentException("Formato de arquivo não permitido. Apenas PDF, DOCX e imagens são aceitos.");
        }

        String nomeArquivo = nomeOriginal.trim();
        int pontoIndex = nomeArquivo.lastIndexOf('.');
        if (pontoIndex == -1 || pontoIndex == nomeArquivo.length() - 1) {
            throw new IllegalArgumentException("Formato de arquivo não permitido. Apenas PDF, DOCX e imagens são aceitos.");
        }

        String extensao = nomeArquivo.substring(pontoIndex).toLowerCase();
        boolean extensaoPermitida = false;
        String mimeType = "application/octet-stream";

        if (extensao.equals(".pdf")) {
            extensaoPermitida = true;
            mimeType = "application/pdf";
        } else if (extensao.equals(".docx")) {
            extensaoPermitida = true;
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (extensao.equals(".doc")) {
            extensaoPermitida = true;
            mimeType = "application/msword";
        } else if (extensao.equals(".jpg") || extensao.equals(".jpeg")) {
            extensaoPermitida = true;
            mimeType = "image/jpeg";
        } else if (extensao.equals(".png")) {
            extensaoPermitida = true;
            mimeType = "image/png";
        }

        if (!extensaoPermitida) {
            throw new IllegalArgumentException("Formato de arquivo não permitido. Apenas PDF, DOCX e imagens são aceitos.");
        }

        try {
            File metadata = new File();
            metadata.setName(nomeArquivo);

            if (this.folderId != null && !this.folderId.trim().isEmpty()) {
                List<String> parents = new ArrayList<String>();
                parents.add(this.folderId.trim());
                metadata.setParents(parents);
            }

            ByteArrayInputStream inputStream = new ByteArrayInputStream(dados);
            InputStreamContent mediaContent = new InputStreamContent(mimeType, inputStream);

            Drive.Files.Create createRequest = driveService.files().create(metadata, mediaContent);
            createRequest.setFields("id");
            createRequest.setSupportsAllDrives(true);
            File arquivoCriado = createRequest.execute();

            if (arquivoCriado == null || arquivoCriado.getId() == null || arquivoCriado.getId().trim().isEmpty()) {
                throw new RuntimeException("Falha ao salvar arquivo no Google Drive: nenhum ID foi retornado.");
            }

            return arquivoCriado.getId();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao realizar upload do arquivo '" + nomeArquivo + "' para o Google Drive: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadArquivo(String idArquivo) {
        return download(idArquivo);
    }

    public byte[] download(String idArquivo) {
        Drive driveService = obterDriveService();

        if (idArquivo == null || idArquivo.trim().isEmpty()) {
            throw new RuntimeException("O ID do arquivo no Google Drive é obrigatório para download.");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Drive.Files.Get getRequest = driveService.files().get(idArquivo.trim());
            getRequest.setSupportsAllDrives(true);
            getRequest.executeMediaAndDownloadTo(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao realizar download do arquivo de ID '" + idArquivo + "' do Google Drive: " + e.getMessage(), e);
        }
    }

    @Override
    public void excluirArquivo(String idArquivo) {
        excluir(idArquivo);
    }

    public void excluir(String idArquivo) {
        Drive driveService = obterDriveService();

        if (idArquivo == null || idArquivo.trim().isEmpty()) {
            throw new RuntimeException("O ID do arquivo no Google Drive é obrigatório para exclusão.");
        }

        try {
            Drive.Files.Delete deleteRequest = driveService.files().delete(idArquivo.trim());
            deleteRequest.setSupportsAllDrives(true);
            deleteRequest.execute();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao excluir o arquivo de ID '" + idArquivo + "' do Google Drive: " + e.getMessage(), e);
        }
    }

    public Drive getDriveService() {
        return obterDriveService();
    }
}
