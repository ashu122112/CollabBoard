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


@Component
public class DashboardController {

   
    private final SessionManager sessionManager;
    private final CollaborationService collaborationService;
    private final ApplicationContext applicationContext;

    
    @FXML private Label welcomeLabel;
    @FXML private TextField ipAddressField;
    @FXML private Label messageLabel;

    
    public DashboardController(SessionManager sessionManager, CollaborationService collaborationService, ApplicationContext applicationContext) {
        this.sessionManager = sessionManager;
        this.collaborationService = collaborationService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        
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
            collaborationService.startHost(12345); 
            String localIp = InetAddress.getLocalHost().getHostAddress();

            collaborationService.setCurrentRoomIdentifier(localIp);

           
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

        
        collaborationService.connectToHost(ipAddress.trim(), 12345,
            // On Success:
            () -> {
                
                collaborationService.setCurrentRoomIdentifier(ipAddress.trim());
                
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