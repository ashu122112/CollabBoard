package com.example.collabboard.controller;

import com.example.collabboard.service.SessionManager;
import com.example.collabboard.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MainController {

    // --- Dependencies ---
    private final ApplicationContext applicationContext;
    private final SessionManager sessionManager;

    // --- FXML Fields ---
    @FXML private BorderPane mainContainer;
    @FXML private Button homeBtn;
    @FXML private Button whiteboardBtn;
    // Add other buttons from your FXML as needed

    // Use constructor injection for all required dependencies
    public MainController(ApplicationContext applicationContext, SessionManager sessionManager) {
        this.applicationContext = applicationContext;
        this.sessionManager = sessionManager;
    }

    @FXML
    public void initialize() {
        // Load the initial view (dashboard) when the main controller starts
        loadView("/fxml/DashboardView.fxml");
    }

    /**
     * Handles the Home button click, loading the dashboard.
     */
    @FXML
    private void handleHomeButtonAction(ActionEvent event) {
        loadView("/fxml/DashboardView.fxml");
    }

    /**
     * Handles the Whiteboard button click.
     */
    @FXML
    private void handleWhiteboardButtonAction(ActionEvent event) {
        loadView("/fxml/WhiteboardView.fxml");
    }
    
    /**
     * Handles the Logout button click.
     */
    @FXML
    void handleLogoutButtonAction(ActionEvent event) throws IOException {
        // Clear the user's session
        sessionManager.clearSession();
        // Navigate the entire window back to the login screen
        SceneManager.switchScene(event, "LoginView.fxml", "CollabBoard Login", applicationContext);
    }

    /**
     * A reusable helper method to load different FXML views into the center of the main BorderPane.
     * @param fxmlPath The path to the .fxml file in the resources folder.
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            // Use Spring to create the controller, ensuring dependencies are injected
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            mainContainer.setCenter(view);
        } catch (IOException e) {
            System.err.println("Error loading FXML view: " + fxmlPath);
            e.printStackTrace();
        }
    }
}