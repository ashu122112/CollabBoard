package com.example.collabboard.controller;

import com.example.collabboard.service.UserService;
import com.example.collabboard.util.SceneManager; // <-- Make sure this is imported
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginController {

    private final UserService userService;
    private final ApplicationContext applicationContext;

    public LoginController(UserService userService, ApplicationContext applicationContext) {
        this.userService = userService;
        this.applicationContext = applicationContext;
    }

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) throws IOException { // <-- Add throws IOException
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            // This line checks the credentials
            userService.loginUser(username, password);

            // *** THIS IS THE FIX ***
            // If login is successful, navigate to the main window
            SceneManager.switchScene(
                event,
                "MainWindow.fxml",      // The FXML file for your main app screen
                "CollabBoard",          // The new window title
                applicationContext
            );

        } catch (Exception e) {
            errorLabel.setText("Invalid username or password"); // Provide a clear error
        }
    }

    @FXML
    protected void handleSignupLinkAction(ActionEvent event) throws IOException {
        // This should already be working correctly
        SceneManager.switchScene(event, "SignupView.fxml", "CollabBoard - Sign Up", applicationContext);
    }
}