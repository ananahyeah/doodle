package com.ppi.utility.importer.config; // *** CRITICAL: Ensure this package matches your folder structure ***

import com.ppi.utility.importer.MainController; // *** CRITICAL: Ensure this import path is correct ***
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class for the application.
 * Defines beans that should be managed by the Spring container.
 */
@Configuration
public class AppConfig {

    /**
     * Defines a Spring bean for the MainController.
     * This allows Spring to manage the lifecycle and dependencies of the MainController,
     * enabling dependency injection into it if needed (e.g., a file processing service).
     *
     * @return An instance of MainController.
     */
    @Bean
    public MainController mainController() {
        return new MainController();
    }

    // Future beans for database interaction, services, etc., will be added here.
    // @Bean
    // public FileProcessingService fileProcessingService() {
    //     return new FileProcessingService(); // This service would handle Excel/CSV parsing and JPA saving
    // }
}