package com.example.collabboard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        // Initialize Spring Boot
        applicationContext = new SpringApplicationBuilder(CollabboardApplication.class).run();
    }

    @Override
    public void start(Stage stage) {
        // Tell Spring that the JavaFX Stage is ready
        applicationContext.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() {
        // Close the Spring context when the app is closed
        applicationContext.close();
        Platform.exit();
    }
}
