package com.example.collabboard.controller;

import com.example.collabboard.model.User;
import com.example.collabboard.service.CollaborationService;
import com.example.collabboard.service.SessionManager;
import com.example.collabboard.util.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class DashboardController {

    // --- Dependencies ---
    private final SessionManager sessionManager;
    private final CollaborationService collaborationService;
    private final ApplicationContext applicationContext;

    // --- FXML Fields ---
    @FXML private Label welcomeLabel;
    @FXML private TextField ipAddressField;
    @FXML private Label messageLabel;

    // Use constructor injection for all dependencies
    public DashboardController(SessionManager sessionManager, CollaborationService collaborationService, ApplicationContext applicationContext) {
        this.sessionManager = sessionManager;
        this.collaborationService = collaborationService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        // Pull the logged-in user from the central session manager
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
    }

    @FXML
    void handleCreateRoom(ActionEvent event) {
        try {
            collaborationService.startHost(12345); // Start the server
            String localIp = InetAddress.getLocalHost().getHostAddress();

            // Store the IP in the central service for the next screen to access
            collaborationService.setCurrentRoomIdentifier(localIp);

            // Navigate to the whiteboard
            SceneManager.switchScene(event, "WhiteboardView.fxml", "CollabBoard", applicationContext);
        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Failed to start host: " + e.getMessage());
        }
    }

    @FXML
    void handleJoinRoom(ActionEvent event) {
        String ipAddress = ipAddressField.getText();
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please enter the Host's IP Address.");
            return;
        }

        messageLabel.setText("Connecting to " + ipAddress + "...");

        // Use the improved connectToHost method with callbacks
        collaborationService.connectToHost(ipAddress.trim(), 12345,
                // On Success:
                () -> {
                    // Store the IP in the central service
                    collaborationService.setCurrentRoomIdentifier(ipAddress.trim());

                    collaborationService.send("IDENTIFY:" + sessionManager.getCurrentUser().getUsername());
                    // Navigate to the whiteboard on the JavaFX thread
                    Platform.runLater(() -> {
                        try {
                            SceneManager.switchScene(event, "WhiteboardView.fxml", "CollabBoard", applicationContext);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                },
                // On Failure:
                (exception) -> Platform.runLater(() -> {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("Failed to connect: " + exception.getMessage());
                })
        );
    }

    @FXML
    void handleLogoutButtonAction(ActionEvent event) throws IOException {
        sessionManager.clearSession();
        collaborationService.stop();
        SceneManager.switchScene(event, "LoginView.fxml", "CollabBoard Login", applicationContext);
    }
}