package com.example.collabboard.controller;

import com.example.collabboard.service.CollaborationService;
import com.example.collabboard.service.SessionManager;
import com.example.collabboard.util.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WhiteboardController {

    private enum Tool {
        PEN, ERASER
    }


    @FXML private Canvas canvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Label roomCodeLabel;
    @FXML private ListView<String> chatListView;
    @FXML private TextField chatTextField;

  
    private final CollaborationService collaborationService;
    private final SessionManager sessionManager;
    private final ApplicationContext applicationContext;

 
    private GraphicsContext graphicsContext;
    private Tool currentTool = Tool.PEN;
    private double lastX, lastY;

    public WhiteboardController(CollaborationService collaborationService, SessionManager sessionManager, ApplicationContext applicationContext) {
        this.collaborationService = collaborationService;
        this.sessionManager = sessionManager;
        this.applicationContext = applicationContext;
    }

    @FXML
public void initialize() {
   
    AnchorPane parentPane = (AnchorPane) canvas.getParent();
    
    canvas.widthProperty().bind(parentPane.widthProperty());
    canvas.heightProperty().bind(parentPane.heightProperty());

    
    graphicsContext = canvas.getGraphicsContext2D();
    colorPicker.setValue(Color.BLACK);

    String roomIdentifier = collaborationService.getCurrentRoomIdentifier();
    if (roomIdentifier != null) {
        String labelPrefix = collaborationService.isHost() ? "Your IP: " : "Host IP: ";
        roomCodeLabel.setText(labelPrefix + roomIdentifier);
    }

    collaborationService.setOnDataReceived(this::parseData);

   

    canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
        if (currentTool == Tool.PEN) {
            graphicsContext.setStroke(colorPicker.getValue());
            graphicsContext.setLineWidth(2.0);
            graphicsContext.beginPath(); 
            graphicsContext.moveTo(event.getX(), event.getY());
            graphicsContext.stroke(); 
        }
        lastX = event.getX();
        lastY = event.getY();
    });

    canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
        double x = event.getX();
        double y = event.getY();
        String data = null;

        if (currentTool == Tool.PEN) {
            
            graphicsContext.lineTo(x, y);
            graphicsContext.stroke();
           
            data = String.format("DRAW:%.2f,%.2f,%.2f,%.2f,%s", lastX, lastY, x, y, colorPicker.getValue().toString());
        } else if (currentTool == Tool.ERASER) {
            double eraserSize = 15.0;
            
            eraseData(String.format("%.2f,%.2f,%.2f", x, y, eraserSize));
            
            data = String.format("ERASE:%.2f,%.2f,%.2f", x, y, eraserSize);
        }

        if (data != null) {
         
            collaborationService.send(data);
        }

        lastX = x;
        lastY = y;
    });
}

   

    @FXML
    private void selectPenTool() {
        currentTool = Tool.PEN;
    }

    @FXML
    private void selectEraserTool() {
        currentTool = Tool.ERASER;
    }

    @FXML
    private void handleExitRoom(ActionEvent event) throws IOException {
        collaborationService.stop();
        SceneManager.switchScene(event, "DashboardView.fxml", "CollabBoard - Dashboard", applicationContext);
    }

    @FXML
    void handleSendChatMessage(ActionEvent event) {
        String message = chatTextField.getText();
        if (message == null || message.trim().isEmpty()) return;

        String username = sessionManager.getCurrentUser().getUsername();
        String data = String.format("CHAT:%s: %s", username, message);
        
        
       parseData(data);
        collaborationService.send(data);
        
        chatTextField.clear();
    }



    private void parseData(String data) {
        Platform.runLater(() -> {
            try {
                String[] parts = data.split(":", 2);
                String command = parts[0];
                String content = parts[1];

                switch (command) {
                    case "DRAW":
                        drawData(content);
                        break;
                    case "ERASE":
                        eraseData(content);
                        break;
                    case "CHAT":
                        chatListView.getItems().add(content);
                        chatListView.scrollTo(chatListView.getItems().size() - 1);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Could not parse incoming data: " + data);
            }
        });
    }

    private void drawData(String content) {
        try {
            String[] params = content.split(",");
            double startX = Double.parseDouble(params[0]);
            double startY = Double.parseDouble(params[1]);
            double endX = Double.parseDouble(params[2]);
            double endY = Double.parseDouble(params[3]);
            Color color = Color.valueOf(params[4]);
            
           
            graphicsContext.setStroke(color);
            graphicsContext.setLineWidth(2.0);
            graphicsContext.beginPath();
            graphicsContext.moveTo(startX, startY);
            graphicsContext.lineTo(endX, endY);
            graphicsContext.stroke();
        } catch (Exception e) {
            System.err.println("Error drawing data: " + content);
        }
    }

    private void eraseData(String content) {
        try {
            String[] params = content.split(",");
            double x = Double.parseDouble(params[0]);
            double y = Double.parseDouble(params[1]);
            double size = Double.parseDouble(params[2]);
            graphicsContext.clearRect(x - size / 2, y - size / 2, size, size);
        } catch (Exception e) {
            System.err.println("Error erasing data: " + content);
        }
    }
}