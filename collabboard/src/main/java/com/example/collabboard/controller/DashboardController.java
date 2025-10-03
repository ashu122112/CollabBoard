package com.example.collabboard.controller;

import com.example.collabboard.model.User;
import com.example.collabboard.service.RoomService;
import org.springframework.stereotype.Component;
import javafx.fxml.FXML;
import javafx.scene.control.*;

@Component
public class DashboardController {

    // We'll need a way to know who is logged in. We'll add this later.
    private User loggedInUser;

    private final RoomService roomService;

    public DashboardController(RoomService roomService) {
        this.roomService = roomService;
    }

    @FXML
    private TextField roomCodeField;
    @FXML
    private Label messageLabel;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    @FXML
    void handleCreateRoom() {
        // For now, we'll just print. We need the logged-in user first.
        System.out.println("Create Room clicked!");
        // Room newRoom = roomService.createRoom(loggedInUser);
        // messageLabel.setText("New room created: " + newRoom.getRoomCode());
    }

    @FXML
    void handleJoinRoom() {
        String code = roomCodeField.getText();
        System.out.println("Attempting to join room: " + code);
        // Room room = roomService.findRoomByCode(code);
        // if (room != null) {
        //    // TODO: Navigate to the whiteboard view
        // } else {
        //    messageLabel.setText("Room not found.");
        // }
    }
}