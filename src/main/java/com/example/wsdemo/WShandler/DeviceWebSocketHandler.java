package com.example.wsdemo.WShandler;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeviceWebSocketHandler extends TextWebSocketHandler {
    static final ConcurrentHashMap<String, WebSocketSession> devSessions = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, WebSocketSession> conSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastHeartbeatTime = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long HEARTBEAT_TIMEOUT = 5000; // 5 seconds in milliseconds

    public DeviceWebSocketHandler() {
        scheduler.scheduleAtFixedRate(this::checkHeartbeats, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        devSessions.put(session.getId(), session);
        lastHeartbeatTime.put(session.getId(), System.currentTimeMillis());
        System.out.println("Device connected on /dev: " + session.getId());
        //session.sendMessage(new TextMessage("Connected to /dev"));  ---> sends a message to device
        notifyControllers("live");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("Received message on /dev: " + message.getPayload() + " from session: " + session.getId());

        lastHeartbeatTime.put(session.getId(), System.currentTimeMillis());

        // Forward other messages to all connected /con sessions
        DeviceWebSocketHandler.conSessions.values().forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage("Message from /dev: " + message.getPayload()));
                }
            } catch (IOException e) {
                System.out.println("Error forwarding message to /con session: " + s.getId() + " " + e.getMessage());
            }
        });
    }

    private void checkHeartbeats() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : lastHeartbeatTime.entrySet()) {
            if (currentTime - entry.getValue() > HEARTBEAT_TIMEOUT) {
                String sessionId = entry.getKey();
                WebSocketSession session = devSessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        session.close();
                        devSessions.remove(sessionId);
                        lastHeartbeatTime.remove(sessionId);
                        System.out.println("Device disconnected due to heartbeat timeout: " + sessionId);
                        notifyControllers("dead");
                    } catch (IOException e) {
                        System.out.println("Error closing timed out session: " + sessionId + " " + e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        devSessions.remove(session.getId());
        lastHeartbeatTime.remove(session.getId());
        System.out.println("Device disconnected on /dev: " + session.getId() + ", status: " + status);
        notifyControllers("dead");
    }

    static void notifyControllers(String status) {
        conSessions.values().forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage("Device status: " + status));
                }
            } catch (IOException e) {
                System.out.println("Error sending status to /con session: " + s.getId() + " " + e.getMessage());
            }
        });
    }
}