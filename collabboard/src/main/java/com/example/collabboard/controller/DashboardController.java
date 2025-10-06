package com.example.collabboard.controller;

import com.example.collabboard.model.User;
import com.example.collabboard.service.CollaborationService;
import com.example.collabboard.service.RoomService;
import com.example.collabboard.service.SessionManager;
import com.example.collabboard.util.SceneManager;
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
    private final RoomService roomService;
    private final SessionManager sessionManager;
    private final ApplicationContext applicationContext;
    private final CollaborationService collaborationService;

    // --- FXML Fields ---
    @FXML private Label welcomeLabel;
    @FXML private TextField ipAddressField; // This should match the fx:id in your FXML
    @FXML private Label messageLabel;

    /**
     * Updated constructor to include the CollaborationService.
     */
    public DashboardController(RoomService roomService, SessionManager sessionManager, ApplicationContext applicationContext, CollaborationService collaborationService) {
        this.roomService = roomService;
        this.sessionManager = sessionManager;
        this.applicationContext = applicationContext;
        this.collaborationService = collaborationService;
    }

    /**
     * This method is called automatically after the FXML file is loaded.
     */
    @FXML
    public void initialize() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
    }

    /**
     * Handles the "Create New Room" button click. Starts a host server.
     */
    @FXML
    void handleCreateRoom(ActionEvent event) throws IOException {
        // Start this user as the HOST on a default port
        collaborationService.startHost(12345);

        // Display the host's local IP address so others can join
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Room created! Share this IP with others: " + localIp);
        } catch (UnknownHostException e) {
            messageLabel.setText("Room created! Could not determine your IP.");
        }

        // Navigate to the whiteboard
        SceneManager.switchScene(event, "WhiteboardView.fxml", "CollabBoard", applicationContext);
    }

    /**
     * Handles the "Join Room" button click. Connects as a client to a host.
     */
    @FXML
    void handleJoinRoom(ActionEvent event) throws IOException {
        String ipAddress = ipAddressField.getText();
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please enter the Host's IP Address.");
            return;
        }

        try {
            // Connect this user as a CLIENT to the host's IP
            collaborationService.connectToHost(ipAddress.trim(), 12345);
            // Navigate to the whiteboard
            SceneManager.switchScene(event, "WhiteboardView.fxml", "CollabBoard", applicationContext);
        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Failed to connect to host: " + ipAddress);
        }
    }

    /**
     * Handles the "Logout" button click.
     */
    @FXML
    void handleLogoutButtonAction(ActionEvent event) throws IOException {
        sessionManager.clearSession();
        collaborationService.stop(); // Stop any active network connection
        SceneManager.switchScene(event, "LoginView.fxml", "CollabBoard Login", applicationContext);
    }
}