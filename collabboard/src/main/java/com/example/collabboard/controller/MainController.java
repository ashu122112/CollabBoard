package com.example.collabboard.controller;

import com.example.collabboard.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MainController {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Button homeBtn;

    @FXML
    private Button collabBtn;

    @FXML
    private Button whiteboardBtn;

    @FXML
    private Button chatBtn;

    @FXML
    private Button analyticsBtn;

    @FXML
    private Button projectsBtn;

    @FXML
    private Button settingsBtn;

    @FXML
    private Button helpBtn;

    @Lazy
    @Autowired
    private ApplicationContext applicationContext;

    private User loggedInUser;
    private DashboardController dashboardController;

    @FXML
    public void initialize() {
        // Add icons to buttons programmatically
        homeBtn.setText("🏠 " + homeBtn.getText());
        collabBtn.setText("👥 " + collabBtn.getText());
        whiteboardBtn.setText("📊 " + whiteboardBtn.getText());
        chatBtn.setText("💬 " + chatBtn.getText());
        analyticsBtn.setText("📈 " + analyticsBtn.getText());
        projectsBtn.setText("📁 " + projectsBtn.getText());
        settingsBtn.setText("⚙️ " + settingsBtn.getText());
        helpBtn.setText("❓ " + helpBtn.getText());

        loadDashboard();
        setupNavigationHandlers();
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        if (dashboardController != null) {
            dashboardController.setLoggedInUser(user);
        }
    }

    private void loadDashboard() {
        try {
            System.out.println("Loading dashboard...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            BorderPane dashboard = loader.load();
            dashboardController = loader.getController();
            
            // Set the logged-in user on the dashboard controller
            if (loggedInUser != null && dashboardController != null) {
                dashboardController.setLoggedInUser(loggedInUser);
            }
            
            mainContainer.setCenter(dashboard);
            System.out.println("Dashboard loaded successfully!");
        } catch (Exception e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNavigationHandlers() {
        homeBtn.setOnAction(e -> {
            System.out.println("Home clicked");
            loadDashboard();
        });

        collabBtn.setOnAction(e -> {
            System.out.println("Collaboration Rooms clicked");
            // TODO: Load collaboration rooms view
        });

        whiteboardBtn.setOnAction(e -> {
            System.out.println("Whiteboard clicked");
            // TODO: Load whiteboard view
        });

        chatBtn.setOnAction(e -> {
            System.out.println("Chat clicked");
            // TODO: Load chat view
        });

        analyticsBtn.setOnAction(e -> {
            System.out.println("Analytics clicked");
            // TODO: Load analytics view
        });

        projectsBtn.setOnAction(e -> {
            System.out.println("Projects clicked");
            // TODO: Load projects view
        });

        settingsBtn.setOnAction(e -> {
            System.out.println("Settings clicked");
            // TODO: Load settings view
        });

        helpBtn.setOnAction(e -> {
            System.out.println("Help & Support clicked");
            // TODO: Load help view
        });
    }
}
