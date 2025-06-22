package com.ppi.utility.importer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration; // ADDED
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration; // ADDED

import java.io.IOException;
import java.util.Objects;

/**
 * Main Spring Boot application class for the PPI Excel Importer.
 * This class also serves as the entry point for the JavaFX application.
 * It initializes the Spring application context and then launches the JavaFX UI.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class}) // MODIFIED: Exclude DB auto-configuration
public class PpiExcelImporterApplication extends Application {

	// Spring application context to manage beans and dependencies
	private ConfigurableApplicationContext applicationContext;

	/**
	 * Initializes the Spring application context.
	 * This method is called by the JavaFX Application thread before start().
	 * We use SpringApplicationBuilder to ensure no web server is started.
	 *
	 * @throws Exception if an error occurs during initialization.
	 */
	@Override
	public void init() throws Exception {
		// Build the Spring Boot application context.
		// WebApplicationType.NONE ensures no embedded web server is started,
		// making this a pure desktop application.
		applicationContext = new SpringApplicationBuilder(PpiExcelImporterApplication.class)
				.web(org.springframework.boot.WebApplicationType.NONE)
				.run(getParameters().getRaw().toArray(new String[0]));
	}

	/**
	 * The main entry point for the JavaFX application.
	 * This method is called after init() has completed.
	 * It sets up the primary stage and loads the FXML UI.
	 *
	 * @param primaryStage The primary stage for this application, onto which
	 * the application scene can be set.
	 * @throws Exception if an error occurs during application startup.
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			// Create an FXMLLoader and set its controller factory to allow Spring
			// to provide controller instances.
			FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/main-view.fxml")));
			fxmlLoader.setControllerFactory(applicationContext::getBean); // Let Spring manage controllers

			// Load the FXML file and get the root node.
			Parent root = fxmlLoader.load();

			// Get the controller instance managed by Spring (which was used by fxmlLoader.load())
			MainController mainController = fxmlLoader.getController();
			// Pass the primary stage to the controller
			mainController.setPrimaryStage(primaryStage);

			// Create a scene with the loaded FXML root and set its dimensions.
			Scene scene = new Scene(root, 600, 400); // Set initial window size
			// Apply the CSS stylesheet for branding and styling.
			scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

			// Set the scene to the primary stage.
			primaryStage.setTitle("PPI Utility - File Importer"); // Set window title
			primaryStage.setScene(scene);
			primaryStage.setResizable(false); // Make the window not resizable for simplicity in Phase 1
			primaryStage.show(); // Display the window
		} catch (Exception e) {
			// Print the full stack trace to the console for debugging
			System.err.println("Error during JavaFX application start:");
			e.printStackTrace();
			// Optionally, show an alert dialog if UI is already partially initialized
			Platform.exit(); // Ensure JavaFX thread exits
			System.exit(1); // Exit application with error code
		}
	}

	/**
	 * This method is called when the application should stop.
	 * It closes the Spring application context and exits the JavaFX platform.
	 */
	@Override
	public void stop() {
		// Close the Spring application context gracefully.
		if (applicationContext != null) {
			applicationContext.close();
		}
		// Exit the JavaFX platform.
		Platform.exit();
		// Terminate the JVM.
		System.exit(0);
	}

	/**
	 * Main method to launch the JavaFX application.
	 * This is the entry point when the JAR is executed.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		// Launch the JavaFX application.
		// This will internally call init(), start(), and stop() methods.
		launch(args);
	}
}