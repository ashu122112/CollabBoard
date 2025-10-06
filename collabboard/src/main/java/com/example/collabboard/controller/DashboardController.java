package com.example.collabboard.controller;

import com.example.collabboard.config.FxmlView;
import com.example.collabboard.model.User;
import com.example.collabboard.service.CollaborationService;
import com.example.collabboard.service.StageManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class DashboardController {

    // --- Dependencies ---
    @Lazy
    @Autowired
    private StageManager stageManager;

    @Autowired
    private CollaborationService collaborationService;

    // --- State ---
    private User loggedInUser;

    // --- FXML Fields ---
    @FXML
    private Label welcomeLabel;
    @FXML
    private TextField ipAddressField;
    @FXML
    private Label messageLabel;

    /**
     * Called from the LoginController to pass the current user's data.
     */
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        if (loggedInUser != null) {
            welcomeLabel.setText("Welcome, " + loggedInUser.getUsername() + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
    }

    /**
     * Handles the "Create New Room" button click. Starts a host server.
     */
    @FXML
    void handleCreateRoom(ActionEvent event) {
        try {
            // Start this user as the HOST on a default port
            collaborationService.startHost(12345);

            String roomCode = "UNKNOWN";
            // Display the host's local IP address so others can join
            try {
                roomCode = InetAddress.getLocalHost().getHostAddress();
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Room created! Share this IP with others: " + roomCode);
            } catch (UnknownHostException e) {
                messageLabel.setText("Room created! Could not determine your IP.");
            }

            // Navigate to the whiteboard and pass the room code
            WhiteboardController wc = stageManager.switchScene(FxmlView.WHITEBOARD);
            wc.initData(roomCode);

        } catch (Exception e) {
            // This catch block handles the IOException from startHost
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Failed to create room: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Join Room" button click. Connects as a client to a host.
     */
    @FXML
    void handleJoinRoom(ActionEvent event) {
        String ipAddress = ipAddressField.getText();
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please enter the Host's IP Address.");
            return;
        }
        
        messageLabel.setText("Connecting to " + ipAddress + "...");
        
        // --- THIS IS THE FIX ---
        // We now provide callbacks for success and failure.
        // The navigation to the whiteboard only happens on success.
        collaborationService.connectToHost(ipAddress.trim(), 12345,
            // On Success action:
            () -> Platform.runLater(() -> {
                WhiteboardController wc = stageManager.switchScene(FxmlView.WHITEBOARD);
                wc.initData(ipAddress.trim());
            }),
            // On Failure action:
            (exception) -> Platform.runLater(() -> {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Failed to connect to host: " + exception.getMessage());
            })
        );
    }

    /**
     * Handles the "Logout" button click.
     */
    @FXML
    void handleLogoutButtonAction(ActionEvent event) {
        loggedInUser = null;
        collaborationService.stop(); // Stop any active network connection
        stageManager.switchScene(FxmlView.LOGIN);
    }
}

