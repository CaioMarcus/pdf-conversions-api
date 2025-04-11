package com.caio.pdf_conversions_api.Authentication.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FirebaseConfig {
    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            var options = FirebaseOptions.builder();
                options.setCredentials(GoogleCredentials.getApplicationDefault());

            if (System.getenv("GOOGLE_CLOUD_PROJECT") != null)
                options.setProjectId(System.getenv("GOOGLE_CLOUD_PROJECT"));

            var optionsBuilded = options.build();
            FirebaseApp.initializeApp(optionsBuilded);
        }
        return FirebaseAuth.getInstance();
    }
}
