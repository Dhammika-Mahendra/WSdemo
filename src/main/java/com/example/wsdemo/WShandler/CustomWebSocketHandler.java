package com.example.wsdemo.WShandler;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class CustomWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket connection established: " + session.getId());
        session.sendMessage(new TextMessage("Connected to WebSocket server"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("Received message: " + message.getPayload() + " from session: " + session.getId());
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        System.out.println("WebSocket connection closed: " + session.getId() + ", status: " + status);
    }
}