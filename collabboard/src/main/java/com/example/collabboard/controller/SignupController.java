package com.example.collabboard.controller;

import com.example.collabboard.service.UserService;
import com.example.collabboard.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.context.ApplicationContext; // <-- Import ApplicationContext
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SignupController {

    private final UserService userService;
    private final ApplicationContext applicationContext; // <-- Add this

    // Use constructor injection
    public SignupController(UserService userService, ApplicationContext applicationContext) {
        this.userService = userService;
        this.applicationContext = applicationContext; // <-- Add this
    }

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    protected void handleSignupButtonAction(ActionEvent event) {
        // ... (your existing signup logic is fine)
    }

    @FXML
    protected void handleLoginLinkAction(ActionEvent event) throws IOException {
        // Pass the applicationContext to the SceneManager
        SceneManager.switchScene(event, "LoginView.fxml", "CollabBoard Login", applicationContext);
    }
}