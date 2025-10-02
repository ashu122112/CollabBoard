// In: collabboard/src/main/java/com/example/collabboard/CollabboardApplication.java

package com.example.collabboard;

import javafx.application.Application; // <-- Make sure this is imported
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CollabboardApplication {

    public static void main(String[] args) {
        // This is the only line that should be in your main method now.
        // It tells JavaFX to take over the startup process.
        Application.launch(JavaFxApplication.class, args);
    }
}