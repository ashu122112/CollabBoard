package com.example.collabboard.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Host implements Runnable {
    private final int port;
    private ServerSocket serverSocket;
    private final Map<PrintWriter, String> clients = new ConcurrentHashMap<>();
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
                new Thread(new ClientHandler(clientSocket, this)).start();
            }
        } catch (IOException e) {
            System.out.println("Host server shut down.");
        }
    }

    public void addClient(PrintWriter writer, String username) {
        clients.put(writer, username);
        broadcastUserList();
    }

    private void broadcastUserList() {
        String userList = String.join(",", clients.values());
        broadcast("USER_LIST:" + userList);
    }

    public void broadcast(String message) {
        onDataReceived.accept(message);
        clients.keySet().forEach(writer -> writer.println(message));
    }

    public void forwardMessage(String message, PrintWriter sender) {
        onDataReceived.accept(message);
        clients.forEach((writer, username) -> {
            if (writer != sender) {
                writer.println(message);
            }
        });
    }

    public void kickUser(String usernameToKick) {
        PrintWriter writerToKick = null;
        for (Map.Entry<PrintWriter, String> entry : clients.entrySet()) {
            if (entry.getValue().equals(usernameToKick)) {
                writerToKick = entry.getKey();
                break;
            }
        }
        if (writerToKick != null) {
            writerToKick.println("YOU_WERE_KICKED");
            removeClient(writerToKick);
        }
    }

    public void removeClient(PrintWriter writer) {
        clients.remove(writer);
        broadcastUserList();
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

    // (shutdown method is the same)

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

                String identifyMessage = reader.readLine();
                if (identifyMessage != null && identifyMessage.startsWith("IDENTIFY:")) {
                    String username = identifyMessage.substring(9);
                    host.addClient(this.writer, username);
                } else {
                    return; // Invalid connection
                }

                String message;
                while ((message = reader.readLine()) != null) {
                    host.forwardMessage(message, this.writer);
                }
            } catch (IOException e) {
                // Client disconnected
            } finally {
                if (writer != null) {
                    host.removeClient(writer);
                }
            }
        }
    }
}