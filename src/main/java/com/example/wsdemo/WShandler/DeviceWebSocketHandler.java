package com.example.wsdemo.WShandler;

import com.example.wsdemo.util.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Arrays;
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
    private static final long HEARTBEAT_TIMEOUT = 50000; // 5 seconds in milliseconds

    public DeviceWebSocketHandler() {
        scheduler.scheduleAtFixedRate(this::checkHeartbeats, 5, 5, TimeUnit.SECONDS);
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        String token = extractToken(query);
        String id = extractId(query);

        if (token == null || id == null || !Store.isValidToken(id, token)) {
            session.close(CloseStatus.POLICY_VIOLATION);
            System.out.println("Device connection rejected: Invalid token");
            return;
        }

        devSessions.put(session.getId(), session);
        lastHeartbeatTime.put(session.getId(), System.currentTimeMillis());
        System.out.println("Device connected on /dev [ id: "+id+" | session: " + session.getId()+" ]");
        //session.sendMessage(new TextMessage("Connected to /dev"));
        Store.saveDeviceStatusById(id, "live");
        Store.saveSessionId(id, session.getId());
        notifyControllers();
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


    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        devSessions.remove(session.getId());
        lastHeartbeatTime.remove(session.getId());
        System.out.println("Device disconnected on /dev: " + session.getId() + ", status: " + status);
        notifyControllers();
    }

    static void notifyControllers() {
        conSessions.values().forEach(s -> {
            try {
                if (s.isOpen()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String json = objectMapper.writeValueAsString(Store.getDeviceRecords());
                    s.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                System.out.println("Error sending status to /con session: " + s.getId() + " " + e.getMessage());
            }
        });
    }


    //=============================================
    // Helper methods
    //=============================================

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
                        String devId=Store.getDeviceIdBySessionId(sessionId);
                        System.out.println("Device "+devId+" disconnected due to heartbeat timeout ");
                        Store.saveDeviceStatusBySessionId(sessionId, "dead");
                        Store.saveSessionId(devId, null);
                        notifyControllers();
                    } catch (IOException e) {
                        System.out.println("Error closing timed out session: " + sessionId + " " + e.getMessage());
                    }
                }
            }
        }
    }

    private String extractToken(String query) {
        if (query == null) return null;
        return Arrays.stream(query.split("&"))
                .filter(param -> param.startsWith("token="))
                .map(param -> param.split("=")[1])
                .findFirst()
                .orElse(null);
    }

    private String extractId(String query) {
        if (query == null) return null;
        return Arrays.stream(query.split("&"))
                .filter(param -> param.startsWith("id="))
                .map(param -> param.split("=")[1])
                .findFirst()
                .orElse(null);
    }

}