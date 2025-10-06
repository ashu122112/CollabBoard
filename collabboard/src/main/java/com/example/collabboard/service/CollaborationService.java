package com.example.collabboard.service;

import com.example.collabboard.network.Client;
import com.example.collabboard.network.Host;
import javafx.application.Platform;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Consumer;

@Service
public class CollaborationService {

    private Host host;
    private Client client;
    private Consumer<String> onDataReceived;
    private String currentRoomIdentifier; // --- ADDED --- To store the current room's IP/code

    public boolean isHost() {
        return host != null;
    }

    public void startHost(int port) throws IOException {
        if (host != null || client != null) {
            stop(); // Stop any existing connection first
        }
        host = new Host(port, this::receiveDataFromServer);
        new Thread(host).start();
        System.out.println("Host started on port " + port);
    }

    public void connectToHost(String ipAddress, int port, Runnable onSuccess, Consumer<Exception> onFailure) {
        if (client != null || host != null) {
            stop(); // Stop any existing connection first
        }
        client = new Client(ipAddress, port, this::receiveDataFromServer, onSuccess, onFailure);
        new Thread(client).start();
        System.out.println("Attempting to connect to host at " + ipAddress + ":" + port);
    }

    private void receiveDataFromServer(String data) {
        if (onDataReceived != null) {
            Platform.runLater(() -> onDataReceived.accept(data));
        }
    }

    public void setOnDataReceived(Consumer<String> listener) {
        this.onDataReceived = listener;
    }

    public void send(String data) {
        if (host != null) {
            host.broadcast(data);
        } else if (client != null) {
            client.sendMessage(data);
        }
    }

    // --- ADDED --- Getter and Setter for the room identifier
    public void setCurrentRoomIdentifier(String identifier) {
        this.currentRoomIdentifier = identifier;
    }

    public String getCurrentRoomIdentifier() {
        return this.currentRoomIdentifier;
    }

    public void stop() {
        if (host != null) {
            host.shutdown();
            host = null;
        }
        if (client != null) {
            client.shutdown();
            client = null;
        }
        this.currentRoomIdentifier = null; // --- ADDED --- Clear the identifier on stop
        System.out.println("Collaboration service stopped.");
    }
}