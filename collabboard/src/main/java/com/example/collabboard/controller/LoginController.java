package com.example.collabboard.controller;

import com.example.collabboard.config.FxmlView;
import com.example.collabboard.model.User;
import com.example.collabboard.service.StageManager;
import com.example.collabboard.service.UserService;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Random;
import javafx.util.Duration;

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
    private Pane animationPane;

    @FXML
    public void initialize() {
        startBackgroundAnimation();
    }

    private void startBackgroundAnimation() {
        if (animationPane == null) {
            return;
        }
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            Circle circle = new Circle();
            circle.setRadius(random.nextInt(40) + 20);
            circle.setFill(javafx.scene.paint.Color.rgb(33, 150, 243, 0.12 + random.nextDouble() * 0.15));
            circle.setCenterX(random.nextDouble() * 800);
            circle.setCenterY(random.nextDouble() * 600);
            animationPane.getChildren().add(circle);

            TranslateTransition translate = new TranslateTransition(Duration.seconds(10 + random.nextDouble() * 10), circle);
            translate.setByX(random.nextDouble() * 200 - 100);
            translate.setByY(random.nextDouble() * 200 - 100);
            translate.setCycleCount(TranslateTransition.INDEFINITE);
            translate.setAutoReverse(true);
            translate.play();

            ScaleTransition scale = new ScaleTransition(Duration.seconds(5 + random.nextDouble() * 5), circle);
            scale.setByX(0.5);
            scale.setByY(0.5);
            scale.setCycleCount(ScaleTransition.INDEFINITE);
            scale.setAutoReverse(true);
            scale.play();

            FadeTransition fade = new FadeTransition(Duration.seconds(3 + random.nextDouble() * 3), circle);
            fade.setFromValue(0.3);
            fade.setToValue(0.8);
            fade.setCycleCount(FadeTransition.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();
        }
    }

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

