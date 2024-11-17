package com.example.bureaucratic_system_backend;

import com.example.bureaucratic_system_backend.config.FirebaseConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BureaucraticSystemBackendApplication {

	public static void main(String[] args) throws Exception {
		FirebaseConfig firebaseConfig = new FirebaseConfig();
		firebaseConfig.initializeFirebase();
		SpringApplication.run(BureaucraticSystemBackendApplication.class, args);
		//System.out.println("miau");
	}
}
