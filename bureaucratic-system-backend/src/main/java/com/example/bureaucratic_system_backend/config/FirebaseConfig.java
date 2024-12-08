package com.example.bureaucratic_system_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() throws Exception {
        // Use classloader to load the file from the resources folder
        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("key.json");

        if (serviceAccount == null) {
            throw new IllegalStateException("key.json file not found in the classpath");
        }

        // Initialize Firebase with the credentials
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase has been initialized successfully!");
        }
    }
}
