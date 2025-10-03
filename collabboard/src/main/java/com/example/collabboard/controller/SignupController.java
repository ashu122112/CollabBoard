package com.example.collabboard.controller;

import com.example.collabboard.service.UserService;
import com.example.collabboard.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SignupController {

    private final UserService userService;
    private final ApplicationContext applicationContext;

    public SignupController(UserService userService, ApplicationContext applicationContext) {
        this.userService = userService;
        this.applicationContext = applicationContext;
    }

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    @FXML
    protected void handleSignupButtonAction(ActionEvent event) throws IOException { // <-- Add throws IOException
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
            // This line registers the user
            userService.registerUser(username, email, password);

            // *** THIS IS THE FIX ***
            // If registration is successful, automatically navigate to the login screen
            SceneManager.switchScene(
                event,
                "LoginView.fxml",
                "CollabBoard Login",
                applicationContext
            );

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    protected void handleLoginLinkAction(ActionEvent event) throws IOException {
        // This should already be working correctly
        SceneManager.switchScene(event, "LoginView.fxml", "CollabBoard Login", applicationContext);
    }
}