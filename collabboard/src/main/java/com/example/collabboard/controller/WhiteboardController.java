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
    @FXML private Label roomCodeLabel;

    private GraphicsContext graphicsContext;
    private final CollaborationService collaborationService;

    private double lastX, lastY;

    public WhiteboardController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    public void initData(String roomCode) {
        Platform.runLater(() -> roomCodeLabel.setText("Room Code: " + roomCode));
    }

    @FXML
    public void initialize() {
        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setLineWidth(2.0); // Set line width once
        colorPicker.setValue(Color.BLACK);

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

            // --- THIS IS THE FIX ---
            // Draw the line on the local canvas immediately.
            drawLine(lastX, lastY, x, y, color);

            // Create a data string for this drawing action
            String data = String.format("DRAW:%.2f,%.2f,%.2f,%.2f,%s", lastX, lastY, x, y, color.toString());
            
            // Send the data over the network to other users
            collaborationService.send(data);

            // Update the last known coordinates for the next drag event
            lastX = x;
            lastY = y;
        });
    }

    /**
     * This method is called when a data string is received from another user.
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

                // Draw the line received from the other user.
                drawLine(startX, startY, endX, endY, color);

            } catch (Exception e) {
                System.err.println("Could not parse incoming data: " + data);
                e.printStackTrace();
            }
        });
    }
    
    /**
     * A helper method to draw a line on the canvas.
     * This avoids code duplication between local and remote drawing.
     */
    private void drawLine(double startX, double startY, double endX, double endY, Color color) {
        graphicsContext.setStroke(color);
        graphicsContext.beginPath();
        graphicsContext.moveTo(startX, startY);
        graphicsContext.lineTo(endX, endY);
        graphicsContext.stroke();
    }
}

