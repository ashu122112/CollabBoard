package com.example.collabboard.controller;

import com.example.collabboard.config.FxmlView;
import com.example.collabboard.model.User;
import com.example.collabboard.service.StageManager;
import com.example.collabboard.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LoginController {

    // Inject the new StageManager for navigation
    @Lazy
    @Autowired
    private StageManager stageManager;

    @Autowired
    private UserService userService;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel; // Changed from messageLabel to match your FXML

    @FXML
    void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // The updated userService.loginUser returns an Optional
        Optional<User> userOptional = userService.loginUser(username, password);

        if (userOptional.isPresent()) {
            errorLabel.setText("Login Successful!");
            // Switch to the dashboard and pass the logged-in user's data
            DashboardController dashboardController = stageManager.switchScene(FxmlView.DASHBOARD);
            dashboardController.setLoggedInUser(userOptional.get());
        } else {
            errorLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    void handleSignupLinkAction(ActionEvent event) {
        // Use the StageManager to switch to the signup view
        stageManager.switchScene(FxmlView.SIGNUP);
    }

    // Add this new method to handle the "Forgot Password" link
    @FXML
    void handleForgotPasswordLinkAction(ActionEvent event) {
        stageManager.switchScene(FxmlView.FORGOT_PASSWORD);
    }
}

