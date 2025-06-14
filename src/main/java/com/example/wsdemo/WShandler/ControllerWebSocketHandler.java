package com.example.wsdemo.WShandler;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;


public class ControllerWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        DeviceWebSocketHandler.conSessions.put(session.getId(), session);
        System.out.println("Controller connected on /con: " + session.getId());
        session.sendMessage(new TextMessage("Connected to /con"));
        // Send current device status on connection
        String status = DeviceWebSocketHandler.devSessions.isEmpty() ? "dead" : "live";
        session.sendMessage(new TextMessage("Device status: " + status));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        System.out.println("Received message on /con: " + payload + " from session: " + session.getId());
        if (payload.equals("0") || payload.equals("1")) {
            DeviceWebSocketHandler.devSessions.values().forEach(s -> {
                try {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage( payload));
                    }
                } catch (IOException e) {
                    System.out.println("Error sending signal to /dev session: " + s.getId() + ", error: " + e.getMessage());
                }
            });
        } else {
            session.sendMessage(new TextMessage("Invalid signal: Use '0' or '1'"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        DeviceWebSocketHandler.conSessions.remove(session.getId());
        System.out.println("Controller disconnected on /con: " + session.getId() + ", status: " + status);
    }
}