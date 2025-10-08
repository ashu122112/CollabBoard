package com.example.collabboard.controller;

import com.example.collabboard.service.CollaborationService;
import com.example.collabboard.service.SessionManager;
import com.example.collabboard.util.SceneManager;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Component
public class WhiteboardController {

    private enum Tool { PEN, ERASER, RECTANGLE, OVAL, STICKY_NOTE }

    // Use Stacks for efficient push/pop operations for undo/redo
    private final Stack<String> drawingHistory = new Stack<>();
    private final Stack<String> redoHistory = new Stack<>();

    private GraphicsContext graphicsContext;
    private Tool currentTool = Tool.PEN;
    private double startX, startY;
    private double lastX, lastY;

    @FXML private AnchorPane canvasPane;
    @FXML private Canvas canvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Label roomCodeLabel;
    @FXML private ListView<String> chatListView;
    @FXML private TextField chatTextField;

    private final CollaborationService collaborationService;
    private final SessionManager sessionManager;
    private final ApplicationContext applicationContext;

    public WhiteboardController(CollaborationService collaborationService, SessionManager sessionManager, ApplicationContext applicationContext) {
        this.collaborationService = collaborationService;
        this.sessionManager = sessionManager;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        // Bind the canvas size to its parent AnchorPane to make it resizable
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
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    // --- FXML ACTION HANDLERS ---

    @FXML private void selectPenTool() { currentTool = Tool.PEN; }
    @FXML private void selectEraserTool() { currentTool = Tool.ERASER; }
    @FXML private void selectRectangleTool() { currentTool = Tool.RECTANGLE; }
    @FXML private void selectOvalTool() { currentTool = Tool.OVAL; }
    @FXML private void selectStickyNoteTool() { currentTool = Tool.STICKY_NOTE; }

    @FXML private void handleClearCanvas(ActionEvent event) { collaborationService.send("CLEAR"); }

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
        collaborationService.send(data);
        chatTextField.clear();
    }

    @FXML
    void handleExportAsImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
            } catch (IOException e) {
                System.err.println("Error saving image: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleSaveBoard(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Board State");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CollabBoard File", "*.collab"));
        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                for (String action : drawingHistory) {
                    writer.println(action);
                }
            } catch (IOException e) {
                System.err.println("Error saving board state: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleLoadBoard(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Board State");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CollabBoard File", "*.collab"));
        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                collaborationService.send("CLEAR");
                List<String> actions = Files.readAllLines(file.toPath());
                for (String action : actions) {
                    collaborationService.send(action);
                }
            } catch (IOException e) {
                System.err.println("Error loading board state: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUndo(ActionEvent event) {
        if (!drawingHistory.isEmpty()) {
            collaborationService.send("UNDO");
        }
    }

    @FXML
    private void handleRedo(ActionEvent event) {
        if (!redoHistory.isEmpty()) {
            collaborationService.send("REDO");
        }
    }

    // --- MOUSE AND DATA PROCESSING ---

    private void handleMousePressed(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();
        lastX = startX; // Initialize lastX/Y here
        lastY = startY;
        if (currentTool == Tool.PEN) {
            graphicsContext.setStroke(colorPicker.getValue());
            graphicsContext.setLineWidth(2.0);
            graphicsContext.beginPath();
            graphicsContext.moveTo(startX, startY);
            graphicsContext.stroke();
        } else if (currentTool == Tool.STICKY_NOTE) {
            createTemporaryTextArea(startX, startY);
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (currentTool != Tool.PEN && currentTool != Tool.ERASER) return;
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
    }
    
    private void handleMouseReleased(MouseEvent event) {
        if (currentTool != Tool.RECTANGLE && currentTool != Tool.OVAL) return;
        double endX = event.getX();
        double endY = event.getY();
        Color color = colorPicker.getValue();
        String data = null;
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double width = Math.abs(startX - endX);
        double height = Math.abs(startY - endY);
        if (currentTool == Tool.RECTANGLE) {
            data = String.format("RECTANGLE:%.2f,%.2f,%.2f,%.2f,%s", x, y, width, height, color.toString());
        } else if (currentTool == Tool.OVAL) {
            data = String.format("OVAL:%.2f,%.2f,%.2f,%.2f,%s", x, y, width, height, color.toString());
        }
        if (data != null) {
            collaborationService.send(data);
        }
    }
    
    private void parseData(String data) {
        // When a new drawing action occurs, clear the redo history
        if (data.startsWith("DRAW:") || data.startsWith("ERASE:") || data.startsWith("RECTANGLE:") || data.startsWith("OVAL:") || data.startsWith("STICKY_NOTE:")) {
            drawingHistory.push(data);
            redoHistory.clear();
        } else if (data.equals("CLEAR")) {
            drawingHistory.clear();
            redoHistory.clear();
        }

        Platform.runLater(() -> {
            try {
                if (data.equals("CLEAR")) {
                    graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    return;
                }
                if ("UNDO".equals(data)) {
                    if (!drawingHistory.isEmpty()) {
                        redoHistory.push(drawingHistory.pop());
                        redrawCanvas();
                    }
                    return;
                }
                if ("REDO".equals(data)) {
                    if (!redoHistory.isEmpty()) {
                        drawingHistory.push(redoHistory.pop());
                        redrawCanvas();
                    }
                    return;
                }

                String[] parts = data.split(":", 2);
                String command = parts[0];
                String content = parts[1];
                switch (command) {
                    case "DRAW": drawData(content); break;
                    case "ERASE": eraseData(content); break;
                    case "RECTANGLE": drawRectangle(content); break;
                    case "OVAL": drawOval(content); break;
                    case "STICKY_NOTE": drawStickyNote(content); break;
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

    // --- HELPER METHODS ---

    private void redrawCanvas() {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // Iterate through a copy to draw from oldest to newest
        for (String action : new ArrayList<>(drawingHistory)) {
            String[] parts = action.split(":", 2);
            String command = parts[0];
            String content = parts[1];
            switch (command) {
                case "DRAW": drawData(content); break;
                case "ERASE": eraseData(content); break;
                case "RECTANGLE": drawRectangle(content); break;
                case "OVAL": drawOval(content); break;
                case "STICKY_NOTE": drawStickyNote(content); break;
            }
        }
    }

    private void createTemporaryTextArea(double x, double y) {
        TextArea textArea = new TextArea();
        textArea.setLayoutX(x);
        textArea.setLayoutY(y);
        textArea.setPrefSize(150, 100);
        textArea.setStyle("-fx-font-size: 14px; -fx-background-color: #FFFFE0;");
        textArea.setWrapText(true);
        textArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                String text = textArea.getText().replace(":", "").replace(",", "");
                String data = String.format("STICKY_NOTE:%.2f,%.2f,%s", x, y, text);
                collaborationService.send(data);
                canvasPane.getChildren().remove(textArea);
                event.consume();
            }
        });
        canvasPane.getChildren().add(textArea);
        textArea.requestFocus();
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
    
    private void drawRectangle(String content) {
        try {
            String[] params = content.split(",");
            double x = Double.parseDouble(params[0]);
            double y = Double.parseDouble(params[1]);
            double width = Double.parseDouble(params[2]);
            double height = Double.parseDouble(params[3]);
            Color color = Color.valueOf(params[4]);
            graphicsContext.setStroke(color);
            graphicsContext.setLineWidth(2.0);
            graphicsContext.strokeRect(x, y, width, height);
        } catch (Exception e) {
            System.err.println("Error drawing rectangle: " + content);
        }
    }
    
    private void drawOval(String content) {
        try {
            String[] params = content.split(",");
            double x = Double.parseDouble(params[0]);
            double y = Double.parseDouble(params[1]);
            double width = Double.parseDouble(params[2]);
            double height = Double.parseDouble(params[3]);
            Color color = Color.valueOf(params[4]);
            graphicsContext.setStroke(color);
            graphicsContext.setLineWidth(2.0);
            graphicsContext.strokeOval(x, y, width, height);
        } catch (Exception e) {
            System.err.println("Error drawing oval: " + content);
        }
    }

    private void drawStickyNote(String content) {
        try {
            String[] params = content.split(",", 3);
            double x = Double.parseDouble(params[0]);
            double y = Double.parseDouble(params[1]);
            String text = params[2];
            double width = 150;
            double height = 100;
            graphicsContext.setFill(Color.web("#FFFFE0"));
            graphicsContext.setStroke(Color.DARKGRAY);
            graphicsContext.setLineWidth(1.0);
            graphicsContext.fillRect(x, y, width, height);
            graphicsContext.strokeRect(x, y, width, height);
            graphicsContext.setFill(Color.BLACK);
            graphicsContext.setFont(new Font("System", 14));
            String[] lines = text.split("\n");
            for(int i = 0; i < lines.length; i++) {
                if (i < 5) {
                    graphicsContext.fillText(lines[i], x + 5, y + 20 + (i * 18));
                }
            }
        } catch (Exception e) {
            System.err.println("Error drawing sticky note: " + content);
        }
    }
}