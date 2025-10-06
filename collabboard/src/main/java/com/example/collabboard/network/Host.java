package com.example.collabboard.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Host implements Runnable {
    private final int port;
    private ServerSocket serverSocket;
    private final List<PrintWriter> clientWriters = Collections.synchronizedList(new ArrayList<>());
    private final Consumer<String> onDataReceived;

    public Host(int port, Consumer<String> onDataReceived) {
        this.port = port;
        this.onDataReceived = onDataReceived;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept(); // Blocks until a client connects
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.add(writer);

                // Create a new thread to handle messages from this client
                new Thread(new ClientHandler(clientSocket, this)).start();
            }
        } catch (IOException e) {
            System.out.println("Host server shut down.");
        }
    }

    public void broadcast(String message) {
        // First, process the message on the host's own UI
        onDataReceived.accept(message);
        // Then, send it to all connected clients
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }

    public void forwardMessage(String message, PrintWriter sender) {
        // Send the message to the host's UI
        onDataReceived.accept(message);
        // Forward the message to all other clients (except the original sender)
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                if (writer != sender) {
                    writer.println(message);
                }
            }
        }
    }

    public void removeClient(PrintWriter writer) {
        clientWriters.remove(writer);
    }

    public void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class to handle incoming messages from a single client
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final Host host;
        private PrintWriter writer;

        public ClientHandler(Socket socket, Host host) {
            this.clientSocket = socket;
            this.host = host;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
                String message;
                while ((message = reader.readLine()) != null) {
                    host.forwardMessage(message, this.writer);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            } finally {
                if (writer != null) {
                    host.removeClient(writer);
                }
            }
        }
    }
}