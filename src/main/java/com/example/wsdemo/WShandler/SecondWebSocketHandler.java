package com.example.wsdemo.WShandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class SecondWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SecondWebSocketHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established for /ws2: {}", session.getId());
        session.sendMessage(new TextMessage("Connected to /ws2 WebSocket server"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Received message on /ws2: {} from session: {}", message.getPayload(), session.getId());
        session.sendMessage(new TextMessage("Echo from /ws2: " + message.getPayload()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        logger.info("WebSocket connection closed for /ws2: {}, status: {}", session.getId(), status);
    }
}