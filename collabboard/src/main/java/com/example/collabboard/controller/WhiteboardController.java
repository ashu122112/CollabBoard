package com.example.collabboard.controller;

import com.example.collabboard.config.FxmlView;
import com.example.collabboard.service.CollaborationService;
import com.example.collabboard.service.StageManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class WhiteboardController {

    // Enum to represent the available tools
    private enum Tool {
        PEN,
        ERASER
    }

    // --- FXML Fields ---
    @FXML private Canvas canvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Label roomCodeLabel;

    // --- Dependencies ---
    @Lazy
    @Autowired
    private StageManager stageManager;

    @Autowired
    private CollaborationService collaborationService;

    // --- State ---
    private GraphicsContext graphicsContext;
    private Tool currentTool = Tool.PEN; // Default tool
    private double lastX, lastY;

    public void initData(String roomCode) {
        Platform.runLater(() -> roomCodeLabel.setText("Room Code: " + roomCode));
    }

    @FXML
    public void initialize() {
        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setLineWidth(2.0);
        colorPicker.setValue(Color.BLACK);

        collaborationService.setOnDataReceived(this::parseAndExecuteAction);

        // --- Mouse Event Handlers for Drawing/Erasing ---
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            lastX = event.getX();
            lastY = event.getY();
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double x = event.getX();
            double y = event.getY();
            String data = null;

            if (currentTool == Tool.PEN) {
                Color color = colorPicker.getValue();
                drawLine(lastX, lastY, x, y, color); // Draw locally
                data = String.format("DRAW:%.2f,%.2f,%.2f,%.2f,%s", lastX, lastY, x, y, color.toString());
            } else if (currentTool == Tool.ERASER) {
                double eraserSize = 10.0;
                eraseArea(x, y, eraserSize); // Erase locally
                data = String.format("ERASE:%.2f,%.2f,%.2f", x, y, eraserSize);
            }

            if (data != null) {
                collaborationService.send(data); // Send action to others
            }

            lastX = x;
            lastY = y;
        });
    }

    // --- FXML Action Handlers for Buttons ---

    @FXML
    private void selectPenTool() {
        currentTool = Tool.PEN;
    }

    @FXML
    private void selectEraserTool() {
        currentTool = Tool.ERASER;
    }

    @FXML
    private void handleExitRoom(ActionEvent event) {
        collaborationService.stop(); // Stop the host or client
        stageManager.switchScene(FxmlView.DASHBOARD); // Navigate back to the dashboard
    }

    /**
     * Parses data received from other users and executes the corresponding action.
     */
    private void parseAndExecuteAction(String data) {
        Platform.runLater(() -> {
            try {
                String[] parts = data.split(":");
                String command = parts[0];
                String[] params = parts[1].split(",");

                if ("DRAW".equals(command)) {
                    double startX = Double.parseDouble(params[0]);
                    double startY = Double.parseDouble(params[1]);
                    double endX = Double.parseDouble(params[2]);
                    double endY = Double.parseDouble(params[3]);
                    Color color = Color.valueOf(params[4]);
                    drawLine(startX, startY, endX, endY, color);
                } else if ("ERASE".equals(command)) {
                    double x = Double.parseDouble(params[0]);
                    double y = Double.parseDouble(params[1]);
                    double size = Double.parseDouble(params[2]);
                    eraseArea(x, y, size);
                }
            } catch (Exception e) {
                System.err.println("Could not parse incoming data: " + data);
            }
        });
    }

    /**
     * Helper method to draw a line on the canvas.
     */
    private void drawLine(double startX, double startY, double endX, double endY, Color color) {
        graphicsContext.setStroke(color);
        graphicsContext.setLineWidth(2.0);
        graphicsContext.beginPath();
        graphicsContext.moveTo(startX, startY);
        graphicsContext.lineTo(endX, endY);
        graphicsContext.stroke();
    }

    /**
     * Helper method to erase a portion of the canvas.
     */
    private void eraseArea(double x, double y, double size) {
        graphicsContext.clearRect(x - size / 2, y - size / 2, size, size);
    }
}

