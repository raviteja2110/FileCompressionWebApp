package com.raviteja2110.FileCompressionApp.FileCompression.Config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
@EnableAsync
public class GoogleDriveConfig {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${google.service.account.type}")
    private String type;

    @Value("${google.service.account.project_id}")
    private String projectId;

    @Value("${google.service.account.private_key_id}")
    private String privateKeyId;

    @Value("${google.service.account.private_key}")
    private String privateKey;

    @Value("${google.service.account.client_email}")
    private String clientEmail;

    @Value("${google.service.account.client_id}")
    private String clientId;

    @Value("${google.service.account.auth_uri}")
    private String authUri;

    @Value("${google.service.account.token_uri}")
    private String tokenUri;

    @Value("${google.service.account.auth_provider_x509_cert_url}")
    private String authProviderX509CertUrl;

    @Value("${google.service.account.client_x509_cert_url}")
    private String clientX509CertUrl;

    @Bean
    public Drive googleDrive() throws GeneralSecurityException, IOException {
        final var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        String formattedPrivateKey = privateKey.replace("\\n", "\n");

        String credentialsJson = String.format(
                "{\n" +
                        "  \"type\": \"%s\",\n" +
                        "  \"project_id\": \"%s\",\n" +
                        "  \"private_key_id\": \"%s\",\n" +
                        "  \"private_key\": \"%s\",\n" +
                        "  \"client_email\": \"%s\",\n" +
                        "  \"client_id\": \"%s\",\n" +
                        "  \"auth_uri\": \"%s\",\n" +
                        "  \"token_uri\": \"%s\",\n" +
                        "  \"auth_provider_x509_cert_url\": \"%s\",\n" +
                        "  \"client_x509_cert_url\": \"%s\"\n" +
                        "}",
                type,
                projectId,
                privateKeyId,
                formattedPrivateKey,
                clientEmail,
                clientId,
                authUri,
                tokenUri,
                authProviderX509CertUrl,
                clientX509CertUrl
        );

        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsJson.getBytes()))
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(projectId)
                .build();
    }
}
