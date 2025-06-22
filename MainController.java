package com.ppi.utility.importer; // *** CRITICAL: Ensure this package matches your folder structure ***

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Controller for the main application view (main-view.fxml).
 * This class handles UI interactions, such as file uploading.
 * It's annotated with @Component to be managed by Spring.
 */
@Component
public class MainController {

    // FXML elements injected by FXMLLoader
    @FXML
    private Label messageLabel; // Label to display messages to the user

    // The primary stage, will be set by the main application class after FXML loading
    private Stage primaryStage;

    /**
     * Initializes the controller. This method is automatically called by FXMLLoader
     * after the FXML file has been loaded and all @FXML annotated fields are injected.
     * It's a good place for initial setup of UI elements.
     */
    @FXML
    public void initialize() {
        // Initial message to the user, hidden until an action is performed.
        messageLabel.setText("");
        messageLabel.setVisible(false);
    }

    /**
     * Setter for the primary stage. This method will be called by the PpiExcelImporterApplication
     * after the MainController has been instantiated and the FXML loaded.
     * @param primaryStage The primary stage of the JavaFX application.
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Handles the "Upload File" button click event.
     * This method is automatically called when the button (fx:id="uploadButton") is clicked.
     */
    @FXML
    protected void onUploadButtonClick() {
        // Ensure primaryStage is set before using it
        if (primaryStage == null) {
            System.err.println("Error: Primary Stage is not set in MainController.");
            messageLabel.setText("Application error: Stage not ready.");
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setVisible(true);
            return;
        }

        // Create a FileChooser object to allow the user to select a file from their local machine.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Excel or CSV File"); // Set the title of the file chooser dialog

        // Add file extensions filters to only allow Excel and CSV files.
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Show the file open dialog and wait for the user to select a file.
        // primaryStage is needed to properly parent the dialog.
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        // Check if a file was selected.
        if (selectedFile != null) {
            // For Phase 1, just display a success message with the file name.
            String fileName = selectedFile.getName();
            messageLabel.setText("File '" + fileName + "' selected successfully!");
            messageLabel.setStyle("-fx-text-fill: green;"); // Set text color to green for success
            messageLabel.setVisible(true);

            // In future phases, you would pass 'selectedFile' to a service
            // to process its content and save to MySQL.
            // Example: fileProcessingService.processFile(selectedFile);
        } else {
            // If no file was selected, display a message indicating that.
            messageLabel.setText("File upload cancelled or no file selected.");
            messageLabel.setStyle("-fx-text-fill: red;"); // Set text color to red for cancellation
            messageLabel.setVisible(true);
        }
    }
}