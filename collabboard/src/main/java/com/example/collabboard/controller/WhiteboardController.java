package com.example.collabboard.controller;

import com.example.collabboard.service.CollaborationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;

@Component
public class WhiteboardController {

    @FXML private Canvas canvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Label roomCodeLabel; // <-- This field is needed for the UI

    private GraphicsContext graphicsContext;
    private final CollaborationService collaborationService;

    private double lastX, lastY;

    public WhiteboardController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    /**
     * This method is called by the DashboardController to pass in the room code.
     * This is the method that was missing and causing the error.
     * @param roomCode The unique code for the collaboration room.
     */
    public void initData(String roomCode) {
        // We run this on the JavaFX thread to ensure UI updates are safe
        Platform.runLater(() -> {
            roomCodeLabel.setText("Room Code: " + roomCode);
            // Here you would connect to the host or start the host
            // collaborationService.connect(roomCode);
        });
    }

    @FXML
    public void initialize() {
        graphicsContext = canvas.getGraphicsContext2D();
        colorPicker.setValue(Color.BLACK);

        // Set up listener for incoming data from other users
        collaborationService.setOnDataReceived(this::parseAndDrawData);

        // --- Mouse Event Handlers for Drawing ---
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            lastX = event.getX();
            lastY = event.getY();
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double x = event.getX();
            double y = event.getY();
            Color color = colorPicker.getValue();

            // Format the drawing action into a simple string protocol
            String data = String.format("DRAW:%.2f,%.2f,%.2f,%.2f,%s", lastX, lastY, x, y, color.toString());

            // Send the data over the network via the service
            collaborationService.send(data);

            // Update the last known coordinates for the next drag event
            lastX = x;
            lastY = y;
        });
    }

    /**
     * This method is called when a data string is received from the CollaborationService.
     * It runs on the JavaFX Application Thread to safely update the UI.
     */
    private void parseAndDrawData(String data) {
        Platform.runLater(() -> {
            try {
                String[] parts = data.split(":");
                if (parts.length < 2 || !parts[0].equals("DRAW")) return;

                String[] params = parts[1].split(",");
                double startX = Double.parseDouble(params[0]);
                double startY = Double.parseDouble(params[1]);
                double endX = Double.parseDouble(params[2]);
                double endY = Double.parseDouble(params[3]);
                Color color = Color.valueOf(params[4]);

                // Draw the line received from another user on the local canvas
                graphicsContext.setStroke(color);
                graphicsContext.setLineWidth(2.0);
                graphicsContext.beginPath();
                graphicsContext.moveTo(startX, startY);
                graphicsContext.lineTo(endX, endY);
                graphicsContext.stroke();

            } catch (Exception e) {
                System.err.println("Could not parse incoming data: " + data);
                e.printStackTrace();
            }
        });
    }
}

