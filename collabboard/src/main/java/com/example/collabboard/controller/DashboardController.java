package com.example.collabboard.controller;

<<<<<<< HEAD
import com.example.collabboard.model.User;
import com.example.collabboard.service.CollaborationService;
import com.example.collabboard.service.RoomService;
import com.example.collabboard.service.SessionManager;
import com.example.collabboard.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
=======
import com.example.collabboard.model.DashboardStats;
import com.example.collabboard.model.SessionData;
import com.example.collabboard.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;
>>>>>>> fd5b9a1b80998d3273c5392d4323a9969290664e

@Component
public class DashboardController {

<<<<<<< HEAD
    // --- Dependencies ---
    private final RoomService roomService;
    private final SessionManager sessionManager;
    private final ApplicationContext applicationContext;
    private final CollaborationService collaborationService;

    // --- FXML Fields ---
    @FXML private Label welcomeLabel;
    @FXML private TextField ipAddressField; // This should match the fx:id in your FXML
    @FXML private Label messageLabel;

    /**
     * Updated constructor to include the CollaborationService.
     */
    public DashboardController(RoomService roomService, SessionManager sessionManager, ApplicationContext applicationContext, CollaborationService collaborationService) {
        this.roomService = roomService;
        this.sessionManager = sessionManager;
        this.applicationContext = applicationContext;
        this.collaborationService = collaborationService;
    }

    /**
     * This method is called automatically after the FXML file is loaded.
     */
    @FXML
    public void initialize() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
    }

    /**
     * Handles the "Create New Room" button click. Starts a host server.
     */
    @FXML
    void handleCreateRoom(ActionEvent event) throws IOException {
        // Start this user as the HOST on a default port
        collaborationService.startHost(12345);

        // Display the host's local IP address so others can join
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Room created! Share this IP with others: " + localIp);
        } catch (UnknownHostException e) {
            messageLabel.setText("Room created! Could not determine your IP.");
=======
    @FXML
    private Label activeSessionsLabel;

    @FXML
    private Label totalProjectsLabel;

    @FXML
    private Label teamMembersLabel;

    @FXML
    private Label hoursCollaboratedLabel;

    @FXML
    private VBox sessionsContainer;

    @FXML
    private VBox activityContainer;

    @FXML
    private VBox onlineUsersContainer;

    @FXML
    private Button createSessionBtn;

    @FXML
    private Button joinSessionBtn;

    @FXML
    private Button browseTemplatesBtn;

    private User loggedInUser;

    @FXML
    public void initialize() {
        loadDashboardData();
        loadActivityFeed();
        loadOnlineUsers();
        setupActionHandlers();
    }

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        updateDashboard();
    }

    private void loadDashboardData() {
        // MOCK DATA FOR UI TESTING
        DashboardStats stats = new DashboardStats(12, 47, 28, 156);

        activeSessionsLabel.setText(String.valueOf(stats.getActiveSessions()));
        totalProjectsLabel.setText(String.valueOf(stats.getTotalProjects()));
        teamMembersLabel.setText(String.valueOf(stats.getTeamMembers()));
        hoursCollaboratedLabel.setText(String.valueOf(stats.getHoursCollaborated()));

        // MOCK SESSIONS DATA
        List<SessionData> sessions = new ArrayList<>();
        sessions.add(new SessionData("1", "Product Design Workshop", "Last active 2 hours ago", 8, "#5e35b1"));
        sessions.add(new SessionData("2", "Brainstorming Session", "Last active 1 day ago", 5, "#00acc1"));
        sessions.add(new SessionData("3", "Q1 Planning Meeting", "Last active 3 days ago", 12, "#43a047"));

        displaySessions(sessions);
    }

    private void loadActivityFeed() {
        // Activity Feed Items
        activityContainer.getChildren().addAll(
                createActivityItem("John Doe", "added a new comment to Product Design Workshop", "2 minutes ago"),
                createActivityItem("Emma Wilson", "started a new whiteboard session", "5 minutes ago"),
                createActivityItem("Mike Johnson", "completed the API integration project", "12 minutes ago"),
                createActivityItem("Lisa Chen", "invited 3 new members to the team", "1 hour ago"),
                createActivityItem("Alex Turner", "updated the brainstorming template", "2 hours ago"),
                createActivityItem("David Kim", "shared a new resource in the library", "3 hours ago")
        );
    }

    private VBox createActivityItem(String userName, String action, String time) {
        VBox item = new VBox(5);
        item.setPadding(new Insets(10));
        item.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8;");

        Label nameLabel = new Label(userName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label actionLabel = new Label(action);
        actionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        actionLabel.setWrapText(true);

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

        item.getChildren().addAll(nameLabel, actionLabel, timeLabel);
        return item;
    }

    private void loadOnlineUsers() {
        // Online Users
        onlineUsersContainer.getChildren().addAll(
                createOnlineUserItem("John Doe", "in Design Workshop"),
                createOnlineUserItem("Emma Wilson", "Available"),
                createOnlineUserItem("Mike Johnson", "Busy")
        );
    }

    private HBox createOnlineUserItem(String name, String status) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8));

        // Avatar
        Region avatar = new Region();
        avatar.setStyle("-fx-background-color: #9e9e9e; -fx-background-radius: 20;");
        avatar.setPrefSize(40, 40);
        avatar.setMinSize(40, 40);
        avatar.setMaxSize(40, 40);

        // User Info
        VBox info = new VBox(2);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label statusLabel = new Label(status);
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        info.getChildren().addAll(nameLabel, statusLabel);

        // Online Indicator
        Region indicator = new Region();
        indicator.setStyle("-fx-background-color: #4caf50; -fx-background-radius: 6;");
        indicator.setPrefSize(12, 12);
        indicator.setMinSize(12, 12);
        indicator.setMaxSize(12, 12);

        item.getChildren().addAll(avatar, info, indicator);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        return item;
    }

    private void displaySessions(List<SessionData> sessions) {
        sessionsContainer.getChildren().clear();
        for (SessionData session : sessions) {
            HBox sessionCard = createSessionCard(session);
            sessionsContainer.getChildren().add(sessionCard);
        }
    }

    private HBox createSessionCard(SessionData session) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Color indicator
        Region colorBox = new Region();
        colorBox.setStyle("-fx-background-color: " + session.getColorCode() + "; -fx-background-radius: 8;");
        colorBox.setPrefSize(50, 50);
        colorBox.setMinSize(50, 50);
        colorBox.setMaxSize(50, 50);

        // Session info
        VBox info = new VBox(5);
        Label titleLabel = new Label(session.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label timeLabel = new Label(session.getLastActive());
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

        info.getChildren().addAll(titleLabel, timeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label participantsLabel = new Label(session.getParticipantCount() + " participants");
        participantsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

        Button joinBtn = new Button("Join");
        joinBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 20;");
        joinBtn.setOnAction(e -> joinSession(session.getId()));

        card.getChildren().addAll(colorBox, info, spacer, participantsLabel, joinBtn);
        return card;
    }

    private void setupActionHandlers() {
        createSessionBtn.setOnAction(e -> handleCreateSession());
        joinSessionBtn.setOnAction(e -> handleJoinSession());
        browseTemplatesBtn.setOnAction(e -> handleBrowseTemplates());
    }

    @FXML
    private void handleCreateSession() {
        System.out.println("Create Session clicked - UI WORKING!");
    }

    @FXML
    private void handleJoinSession() {
        System.out.println("Join Session clicked - UI WORKING!");
    }

    @FXML
    private void handleBrowseTemplates() {
        System.out.println("Browse Templates clicked - UI WORKING!");
    }

    private void joinSession(String sessionId) {
        System.out.println("Joining session: " + sessionId + " - UI WORKING!");
    }

    @FXML
    private void handleViewAllSessions() {
        System.out.println("View All Sessions clicked - UI WORKING!");
    }

    private void updateDashboard() {
        // Update dashboard with user-specific data if needed
        if (loggedInUser != null) {
            // Update any user-specific elements here
            System.out.println("Dashboard updated for user: " + loggedInUser.getUsername());
>>>>>>> fd5b9a1b80998d3273c5392d4323a9969290664e
        }

        // Navigate to the whiteboard
        SceneManager.switchScene(event, "WhiteboardView.fxml", "CollabBoard", applicationContext);
    }
<<<<<<< HEAD

    /**
     * Handles the "Join Room" button click. Connects as a client to a host.
     */
    @FXML
    void handleJoinRoom(ActionEvent event) throws IOException {
        String ipAddress = ipAddressField.getText();
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please enter the Host's IP Address.");
            return;
        }

        try {
            // Connect this user as a CLIENT to the host's IP
            collaborationService.connectToHost(ipAddress.trim(), 12345);
            // Navigate to the whiteboard
            SceneManager.switchScene(event, "WhiteboardView.fxml", "CollabBoard", applicationContext);
        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Failed to connect to host: " + ipAddress);
        }
    }

    /**
     * Handles the "Logout" button click.
     */
    @FXML
    void handleLogoutButtonAction(ActionEvent event) throws IOException {
        sessionManager.clearSession();
        collaborationService.stop(); // Stop any active network connection
        SceneManager.switchScene(event, "LoginView.fxml", "CollabBoard Login", applicationContext);
    }
=======
>>>>>>> fd5b9a1b80998d3273c5392d4323a9969290664e
}