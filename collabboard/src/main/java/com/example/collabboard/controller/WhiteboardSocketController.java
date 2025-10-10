package com.example.collabboard.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * This controller handles WebSocket messages for the whiteboard.
 * It's part of the backend server logic and acts as a relay for real-time communication.
 */
@Controller
public class WhiteboardSocketController {

    /**
     * This method is triggered when a client sends a message to a destination
     * like "/app/board/some-room-code".
     *
     * @MessageMapping("/board/{roomCode}"): This annotation maps the method to the specified destination.
     * The `{roomCode}` part is a dynamic variable captured from the URL.
     *
     * @SendTo("/topic/board/{roomCode}"): This annotation tells Spring to take the return value of this method
     * and broadcast it to all clients subscribed to the topic "/topic/board/{roomCode}".
     *
     * @param roomCode The unique identifier for the whiteboard room, extracted from the destination URL.
     * @param data     The message payload sent by the client (e.g., "DRAW:10,20,30,40,BLACK").
     * @return The same data, which is then broadcast to all subscribers of the topic.
     */
    @MessageMapping("/board/{roomCode}")
    @SendTo("/topic/board/{roomCode}")
    public String handleAction(@DestinationVariable String roomCode, String data) {
        // The server's job is simple: it receives the data and immediately sends it
        // back out to everyone in the same room. It acts as a message relay or broker.
        // You could add logic here to save the data to a database if you wanted to persist the drawings.
        return data;
    }
}

