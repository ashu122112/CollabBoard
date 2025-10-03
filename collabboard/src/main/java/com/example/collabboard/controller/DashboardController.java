package com.example.collabboard.controller;

import com.example.collabboard.config.FxmlView;
import com.example.collabboard.model.User;
import com.example.collabboard.service.RoomService;
import com.example.collabboard.service.StageManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class DashboardController {

    private final RoomService roomService;
    private User loggedInUser;

    @Lazy
    @Autowired
    private StageManager stageManager;

    @FXML
    private TextField roomCodeField;

    @FXML
    private Label messageLabel;

    @FXML
    private Label welcomeLabel;

    // Use constructor injection for required dependencies like RoomService
    @Autowired
    public DashboardController(RoomService roomService) {
        this.roomService = roomService;
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        // Set the welcome message when the user is set
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        }
    }

    @FXML
    void handleCreateRoom(ActionEvent event) {
        // We will implement the actual room creation logic later.
        // For now, we'll just show a message.
        if (loggedInUser != null) {
            System.out.println("Create Room clicked by: " + loggedInUser.getUsername());
            // String newRoomCode = roomService.createRoom(loggedInUser);
            // messageLabel.setText("New room created: " + newRoomCode);
            messageLabel.setText("Room creation feature coming soon!");
        }
    }

    @FXML
    void handleJoinRoom(ActionEvent event) {
        String code = roomCodeField.getText();
        if (code == null || code.trim().isEmpty()) {
            messageLabel.setText("Please enter a room code.");
            return;
        }
        System.out.println("Attempting to join room: " + code);
        // We will implement the actual room joining logic later.
        messageLabel.setText("Room joining feature coming soon!");
        // Room room = roomService.findRoomByCode(code);
        // if (room != null) {
        //     // TODO: Navigate to the whiteboard view
        // } else {
        //     messageLabel.setText("Room not found.");
        // }
    }

    @FXML
    void handleLogoutButtonAction(ActionEvent event) {
        // Clear the logged-in user and switch back to the Login scene
        loggedInUser = null;
        stageManager.switchScene(FxmlView.LOGIN);
    }
}

