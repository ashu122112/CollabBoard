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
    protected void handleLoginButtonAction(ActionEvent event) throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            userService.loginUser(username, password);

            // *** THIS IS THE FIX ***
            // Navigate to the new DashboardView instead of the old MainWindow
            SceneManager.switchScene(
                event,
                "DashboardView.fxml",           // <-- The only change is here
                "CollabBoard - Dashboard",      // You can update the title too
                applicationContext
            );

        } catch (Exception e) {
            errorLabel.setText("Invalid username or password");
        }
    }

    @FXML
    protected void handleSignupLinkAction(ActionEvent event) throws IOException {
        SceneManager.switchScene(event, "SignupView.fxml", "CollabBoard - Sign Up", applicationContext);
    }
}