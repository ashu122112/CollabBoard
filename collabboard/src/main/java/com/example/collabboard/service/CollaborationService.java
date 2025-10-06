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
            // All UI updates must happen on the JavaFX Application Thread.
            // Platform.runLater ensures this.
            Platform.runLater(() -> onDataReceived.accept(data));
        }
    }

    // This is called by the WhiteboardController to register itself as a listener.
    public void setOnDataReceived(Consumer<String> listener) {
        this.onDataReceived = listener;
    }

    // This is called by the WhiteboardController to send drawing data out.
    public void send(String data) {
        if (host != null) {
            // If we are the host, broadcast to all clients.
            host.broadcast(data);
        } else if (client != null) {
            // If we are a client, send to the host.
            client.sendMessage(data);
        }
    }

    // Shuts down any active network connections.
    public void stop() {
        if (host != null) {
            host.shutdown();
            host = null;
        }
        if (client != null) {
            client.shutdown();
            client = null;
        }
        System.out.println("Collaboration service stopped.");
    }
}

