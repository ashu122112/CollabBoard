package com.example.collabboard;

import javafx.application.Application; 
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CollabboardApplication {

    public static void main(String[] args) {
        // with this line we launch the JavaFX application
        Application.launch(JavaFxApplication.class, args);
    }
}