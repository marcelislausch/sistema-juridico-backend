package com.sistemajuridico.backend.infrastructure.storage;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import jakarta.annotation.PostConstruct;
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

    @Value("${google.oauth.client.id}")
    private String clientId;

    @Value("${google.oauth.client.secret}")
    private String clientSecret;

    @Value("${google.oauth.refresh.token}")
    private String refreshToken;

    @Value("${google.drive.folder.id}")
    private String folderId;

    private Drive driveService;

    public GoogleDriveStorageService() {
    }

    public GoogleDriveStorageService(String clientId, String clientSecret, String refreshToken, String folderId) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.folderId = folderId;
        inicializar();
    }

    @PostConstruct
    public void inicializar() {
        try {
            Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .setTokenServerEncodedUrl("https://oauth2.googleapis.com/token")
                    .setClientAuthentication(new ClientParametersAuthentication(this.clientId, this.clientSecret))
                    .build()
                    .setRefreshToken(this.refreshToken);

            credential.refreshToken();

            this.driveService = new Drive.Builder(
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
        if (this.driveService == null) {
            throw new RuntimeException("O serviço do Google Drive não foi inicializado corretamente.");
        }

        if (dados == null || dados.length == 0) {
            throw new RuntimeException("O conteúdo do arquivo para upload não pode ser nulo ou vazio.");
        }

        String nomeArquivo = "arquivo";
        if (nomeOriginal != null && !nomeOriginal.trim().isEmpty()) {
            nomeArquivo = nomeOriginal;
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
            InputStreamContent mediaContent = new InputStreamContent(null, inputStream);

            Drive.Files.Create createRequest = this.driveService.files().create(metadata, mediaContent);
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
        if (this.driveService == null) {
            throw new RuntimeException("O serviço do Google Drive não foi inicializado corretamente.");
        }

        if (idArquivo == null || idArquivo.trim().isEmpty()) {
            throw new RuntimeException("O ID do arquivo no Google Drive é obrigatório para download.");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Drive.Files.Get getRequest = this.driveService.files().get(idArquivo.trim());
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
        if (this.driveService == null) {
            throw new RuntimeException("O serviço do Google Drive não foi inicializado corretamente.");
        }

        if (idArquivo == null || idArquivo.trim().isEmpty()) {
            throw new RuntimeException("O ID do arquivo no Google Drive é obrigatório para exclusão.");
        }

        try {
            Drive.Files.Delete deleteRequest = this.driveService.files().delete(idArquivo.trim());
            deleteRequest.setSupportsAllDrives(true);
            deleteRequest.execute();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao excluir o arquivo de ID '" + idArquivo + "' do Google Drive: " + e.getMessage(), e);
        }
    }

    public Drive getDriveService() {
        return this.driveService;
    }
}
