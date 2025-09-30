package com.example.collabboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javafx.application.Application;

@SpringBootApplication
public class CollabboardApplication {

	public static void main(String[] args) {
		// SpringApplication.run(CollabboardApplication.class, args);
		Application.launch(JavaFxApplication.class, args);
	}
}