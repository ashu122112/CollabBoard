package com.example.collabboard.service;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Service;

import com.example.collabboard.network.Host;
import com.example.collabboard.network.Client;
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
        // If already running, do nothing
        if (host != null) return;

        // Create and start the host server on a new thread
        host = new Host(port, this::receiveDataFromServer);
        new Thread(host).start();
        System.out.println("Host started on port " + port);
    }

    public void connectToHost(String ipAddress, int port) throws IOException {
        // If already connected, do nothing
        if (client != null) return;

        // Create and connect the client on a new thread
        client = new Client(ipAddress, port, this::receiveDataFromServer);
        new Thread(client).start();
        System.out.println("Connected to host at " + ipAddress + ":" + port);
    }

    // Called by Host or Client when a message arrives
    private void receiveDataFromServer(String data) {
        if (onDataReceived != null) {
            // Run on JavaFX thread to safely update the UI
            Platform.runLater(() -> onDataReceived.accept(data));
        }
    }

    // This allows the WhiteboardController to "listen" for new data
    public void setOnDataReceived(Consumer<String> listener) {
        this.onDataReceived = listener;
    }

    // Send a message out to the network
    public void send(String data) {
        if (host != null) {
            host.broadcast(data);
        } else if (client != null) {
            client.sendMessage(data);
        }
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
        System.out.println("Collaboration service stopped.");
    }
}