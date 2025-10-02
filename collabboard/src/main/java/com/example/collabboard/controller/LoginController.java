    package com.example.collabboard.controller;

    import com.example.collabboard.service.UserService;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.scene.control.*;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Component;

    @Component
    public class LoginController {
        @Autowired
        private UserService userService;

        @FXML private TextField usernameField;
        @FXML private PasswordField passwordField;
        @FXML private Label errorLabel;

        @FXML
        protected void handleLoginButtonAction(ActionEvent event) {
            String username = usernameField.getText();
            String password = passwordField.getText();
            try {
                userService.loginUser(username, password);
                errorLabel.setText("Login Successful!");
                // TODO: Navigate to the main dashboard
            } catch (Exception e) {
                errorLabel.setText(e.getMessage());
            }
        }

        @FXML
        protected void handleSignupLinkAction(ActionEvent event) {
            // TODO: Navigate to the signup scene
            System.out.println("Navigate to signup");
        }
    }
    
