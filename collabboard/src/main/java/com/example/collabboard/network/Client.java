package com.example.collabboard.network;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class Client implements Runnable {
    private final String hostIp;
    private final int port;
    private Socket socket;
    private PrintWriter writer;
    private final Consumer<String> onDataReceived;
    private final Runnable onSuccess;
    private final Consumer<Exception> onFailure;

    
    public Client(String hostIp, int port, Consumer<String> onDataReceived, Runnable onSuccess, Consumer<Exception> onFailure) {
        this.hostIp = hostIp;
        this.port = port;
        this.onDataReceived = onDataReceived;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    @Override
    public void run() {
        try {
           
            socket = new Socket(hostIp, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            
            onSuccess.run();

            String message;
            while ((message = reader.readLine()) != null) {
                onDataReceived.accept(message);
            }
        } catch (IOException e) {
            
            System.err.println("Failed to connect to host: " + e.getMessage());
            onFailure.accept(e);
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public void shutdown() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
