package com.example.collabboard.service;

import com.example.collabboard.JavaFxApplication.StageReadyEvent;
import com.example.collabboard.config.FxmlView;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class StageManager implements ApplicationListener<StageReadyEvent> {

    private Stage primaryStage;
    private final ApplicationContext applicationContext;

    public StageManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        this.primaryStage = event.getStage();
        primaryStage.setTitle("CollabBoard");
        switchScene(FxmlView.LOGIN); // This will call the void version
    }

    /**
     * Switches the scene and returns the controller of the new scene.
     * This is useful when you need to pass data to the new controller.
     * @param view The FxmlView enum constant for the scene to switch to.
     * @param <T> The type of the controller.
     * @return The controller instance for the new scene.
     */
    public <T> T switchScene(final FxmlView view) {
        FXMLLoader fxmlLoader = loadViewNodeHierarchy(view.getFxmlFile());
        show(fxmlLoader.getRoot());
        return fxmlLoader.getController();
    }

    private void show(final Parent rootnode) {
        Scene scene = primaryStage.getScene();

        if (scene == null) {
            scene = new Scene(rootnode);
            primaryStage.setScene(scene);
        } else {
            primaryStage.getScene().setRoot(rootnode);
        }
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();

        try {
            primaryStage.show();
        } catch (Exception exception) {
            System.err.println("Error showing the stage: " + exception.getMessage());
        }
    }

    /**
     * Loads the FXML file and returns the FXMLLoader instance.
     * This allows us to access both the root node and the controller.
     * @param fxmlFilePath The path to the FXML file.
     * @return The configured FXMLLoader instance.
     */
    private FXMLLoader loadViewNodeHierarchy(String fxmlFilePath) {
        FXMLLoader fxmlLoader = null;
        try {
            fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFilePath));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            fxmlLoader.load();
            Objects.requireNonNull(fxmlLoader.getRoot(), "A Root FXML node must not be null");
        } catch (IOException e) {
            System.err.println("Unable to load FXML view: " + fxmlFilePath);
            e.printStackTrace();
            Platform.exit();
        }
        return fxmlLoader;
    }
}

