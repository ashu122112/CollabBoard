package com.example.collabboard.controller;

import com.example.collabboard.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SignupController {

    @Autowired
    private UserService userService;

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    protected void handleSignupButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        try {
            userService.registerUser(username, email, password);
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Registration successful! Please login.");
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    protected void handleLoginLinkAction(ActionEvent event) {
        // TODO: Navigate back to the login scene
        System.out.println("Navigate to login");
    }
}
