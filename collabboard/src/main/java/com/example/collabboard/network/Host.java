package com.example.collabboard.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
                Socket clientSocket = serverSocket.accept(); 
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.add(writer);

                new Thread(new ClientHandler(clientSocket, this)).start();
            }
        } catch (IOException e) {
            System.out.println("Host server shut down.");
        }
    }

    /**
     * @param message 
     */
    public void broadcast(String message) {
        // The host's UI has already been updated by its own mouse events.
        // We do NOT need to call onDataReceived.accept(message) here.
        // This method's only job is to send the message to the clients.
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
    }

    /**
     * @param message 
     * @param sender  
     */
    public void forwardMessage(String message, PrintWriter sender) {
        // First, process the message on the host's own UI. This is correct.
        onDataReceived.accept(message);
        // Then, forward the message to all other clients (except the original sender).
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
           
            clientWriters.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

