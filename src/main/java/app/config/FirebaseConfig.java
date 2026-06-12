package app.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {
	
	@PostConstruct
    public void initialize() {
        try {      	
        	InputStream stream = new ClassPathResource("teste-firebase-service.json")
        	        .getInputStream();
        	
        
        	GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream);
        	
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Application inicializada com sucesso!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
