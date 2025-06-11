package com.example.wsdemo.WShandler;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final ConcurrentHashMap<String, WebSocketSession> wsSessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, WebSocketSession> ws2Sessions = new ConcurrentHashMap<>();
    private final String endpoint;

    public ChatWebSocketHandler(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (endpoint.equals("/chat")) {
            wsSessions.put(session.getId(), session);
            System.out.println("WebSocket connection established for /chat: " + session.getId());
            session.sendMessage(new TextMessage("Connected to /chat WebSocket server"));
        } else {
            ws2Sessions.put(session.getId(), session);
            System.out.println("WebSocket connection established for /chat2: " + session.getId());
            session.sendMessage(new TextMessage("Connected to /chat2 WebSocket server"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("Received message on " + endpoint + ": " + message.getPayload() + " from session: " + session.getId());
        String response = "From " + endpoint + ": " + message.getPayload();
        if (endpoint.equals("/chat")) {
            ws2Sessions.values().forEach(s -> {
                try {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(response));
                    }
                } catch (IOException e) {
                    System.out.println("Error sending message to /chat2 session: " + s.getId() + " " + e.getMessage());
                }
            });
        } else {
            wsSessions.values().forEach(s -> {
                try {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(response));
                    }
                } catch (IOException e) {
                    System.out.println("Error sending message to /chat session: " + s.getId() + " " + e.getMessage());
                }
            });
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        if (endpoint.equals("/chat")) {
            wsSessions.remove(session.getId());
            System.out.println("WebSocket connection closed for /chat: " + session.getId() + ", status: " + status);
        } else {
            ws2Sessions.remove(session.getId());
            System.out.println("WebSocket connection closed for /chat2: " + session.getId() + ", status: " + status);
        }
    }
}