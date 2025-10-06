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
        if (host != null) return;
        host = new Host(port, this::receiveDataFromServer);
        new Thread(host).start();
        System.out.println("Host started on port " + port);
    }

    /**
     * Updated method to accept success and failure callbacks.
     * @param ipAddress The IP of the host to connect to.
     * @param port The port of the host.
     * @param onSuccess A Runnable to execute on successful connection.
     * @param onFailure A Consumer to handle any connection exceptions.
     */
    public void connectToHost(String ipAddress, int port, Runnable onSuccess, Consumer<Exception> onFailure) {
        if (client != null) return;
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
