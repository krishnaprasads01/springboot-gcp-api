package com.example.api.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {
    
    @Value("${spring.cloud.gcp.project-id:demo-project}")
    private String projectId;
    
    @Bean
    public Firestore firestore() {
        try {
            FirestoreOptions.Builder optionsBuilder = FirestoreOptions.getDefaultInstance().toBuilder();
            
            if (projectId != null && !projectId.isEmpty()) {
                optionsBuilder.setProjectId(projectId);
            } else {
                optionsBuilder.setProjectId("demo-project"); // Default for local development
            }
            
            return optionsBuilder.build().getService();
        } catch (Exception e) {
            // For local development without proper Firestore setup
            System.out.println("Warning: Could not initialize Firestore: " + e.getMessage());
            System.out.println("Using demo project configuration for local development");
            
            return FirestoreOptions.getDefaultInstance().toBuilder()
                    .setProjectId("demo-project")
                    .build()
                    .getService();
        }
    }
}
