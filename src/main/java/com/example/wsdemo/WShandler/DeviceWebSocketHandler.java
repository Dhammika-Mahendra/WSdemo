package com.example.wsdemo.WShandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeviceWebSocketHandler.class);
    static final ConcurrentHashMap<String, WebSocketSession> devSessions = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, WebSocketSession> conSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        devSessions.put(session.getId(), session);
        logger.info("Device connected on /dev: {}", session.getId());
        session.sendMessage(new TextMessage("Connected to /dev"));
        notifyControllers("live");
    }

    //Echoing the messages back to the dev (no forwarding to cons)----------------------------------
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
//        logger.info("Received message on /dev: {} from session: {}", message.getPayload(), session.getId());
//        // Devices don't send messages in this setup, but log if they do
//        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
//    }

    //Forwarding messages to cons-----------------------------------------------------------------
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        logger.info("Received message on /dev: {} from session: {}", message.getPayload(), session.getId());
        // Forward the message to all connected /con sessions
        DeviceWebSocketHandler.conSessions.values().forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage("Message from /dev: " + message.getPayload()));
                }
            } catch (IOException e) {
                logger.error("Error forwarding message to /con session: {}", s.getId(), e);
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        devSessions.remove(session.getId());
        logger.info("Device disconnected on /dev: {}, status: {}", session.getId(), status);
        notifyControllers("dead");
    }

    static void notifyControllers(String status) {
        conSessions.values().forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage("Device status: " + status));
                }
            } catch (IOException e) {
                logger.error("Error sending status to /con session: {}", s.getId(), e);
            }
        });
    }
}
