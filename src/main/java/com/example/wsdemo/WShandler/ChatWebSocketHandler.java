package com.example.wsdemo.WShandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomWebSocketHandler.class);
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
            logger.info("WebSocket connection established for /chat: {}", session.getId());
            session.sendMessage(new TextMessage("Connected to /chat WebSocket server"));
        } else {
            ws2Sessions.put(session.getId(), session);
            logger.info("WebSocket connection established for /chat2: {}", session.getId());
            session.sendMessage(new TextMessage("Connected to /chat2 WebSocket server"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        logger.info("Received message on {}: {} from session: {}", endpoint, message.getPayload(), session.getId());
        String response = "From " + endpoint + ": " + message.getPayload();
        if (endpoint.equals("/chat")) {
            ws2Sessions.values().forEach(s -> {
                try {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(response));
                    }
                } catch (IOException e) {
                    logger.error("Error sending message to /chat2 session: {}", s.getId(), e);
                }
            });
        } else {
            wsSessions.values().forEach(s -> {
                try {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(response));
                    }
                } catch (IOException e) {
                    logger.error("Error sending message to /chat session: {}", s.getId(), e);
                }
            });
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        if (endpoint.equals("/chat")) {
            wsSessions.remove(session.getId());
            logger.info("WebSocket connection closed for /chat: {}, status: {}", session.getId(), status);
        } else {
            ws2Sessions.remove(session.getId());
            logger.info("WebSocket connection closed for /chat2: {}, status: {}", session.getId(), status);
        }
    }
}