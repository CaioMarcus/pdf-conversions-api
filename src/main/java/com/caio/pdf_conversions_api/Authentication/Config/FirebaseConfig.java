package com.caio.pdf_conversions_api.Authentication.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            try (InputStream serviceAccount = new FileInputStream("C:\\Users\\Caio\\Downloads\\conversoes-pdf-xls-01d5699244aa.json")) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }
        }
        return FirebaseAuth.getInstance();
    }
}
