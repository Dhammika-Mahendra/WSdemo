package com.example.wsdemo.WShandler;

import com.example.wsdemo.model.DeviceInfo;
import com.example.wsdemo.util.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DeviceWebSocketHandler extends TextWebSocketHandler {

    static final ConcurrentHashMap<String, WebSocketSession> devSessions = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, WebSocketSession> conSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastHeartbeatTime = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final long HEARTBEAT_TIMEOUT = 60000; // 5 seconds in milliseconds

    public DeviceWebSocketHandler() {
        scheduler.scheduleAtFixedRate(this::checkHeartbeats, 5, 5, TimeUnit.SECONDS);
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        String token = extractToken(query);

        if (token == null || !Store.isValidToken(token)) {
            session.close(CloseStatus.POLICY_VIOLATION);
            System.out.println("Device connection rejected: Invalid token");
            return;
        }

        devSessions.put(session.getId(), session);
        lastHeartbeatTime.put(session.getId(), System.currentTimeMillis());
        String devId = Store.extractIdFromToken(token);
        System.out.println("Device connected on /dev [ id: "+devId+" | session: " + session.getId()+" ]");
        //session.sendMessage(new TextMessage("Connected to /dev"));
        Store.loginDevice(devId, session.getId(), token);
        notifyControllers();
        notifyUsers();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        try {

            // Unwrapping incoming payload
            ObjectMapper objectMapper = new ObjectMapper();
            DeviceInfo deviceInfo = objectMapper.readValue(message.getPayload(), DeviceInfo.class);

            // validation
            String query = session.getUri().getQuery();
            String token = extractToken(query);
            String id = deviceInfo.getId();

            if (token == null || id == null || !Store.isMatchingToken(id, token)) {
                System.out.println("Invalid token or id in message");
                return;
            }

            if(Objects.equals(deviceInfo.getType(), "USAGE")){
                //status updation
                System.out.println("Device Message [id: " + deviceInfo.getId() + " | status: " + deviceInfo.getStatus() + "]");
                Store.saveDeviceStatusById(deviceInfo.getId(), deviceInfo.getStatus());
                lastHeartbeatTime.put(session.getId(), System.currentTimeMillis());
                notifyControllers();
            }else if(Objects.equals(deviceInfo.getType(), "PING")){
                //only a heartbeat message
                System.out.println("Heartbeat received from device " + deviceInfo.getId());
                lastHeartbeatTime.put(session.getId(), System.currentTimeMillis());
            }

        } catch (Exception e) {
            System.out.println("Invalid message format: " + message.getPayload());
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        devSessions.remove(session.getId());
        lastHeartbeatTime.remove(session.getId());
        String devId= Store.getDeviceIdBySessionId(session.getId());
        Store.logoutDevice(devId);
        System.out.println("Device "+devId+" disconnected on /dev");
        notifyControllers();
        notifyUsers();
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

    static void notifyUsers() {
        UserWebSocketHandler.userSessions.values().forEach(s -> {
            try {
                if (s.isOpen()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String json = objectMapper.writeValueAsString(Store.getDeviceRecords().stream()
                            .filter(device -> "1".equals(device.active) && !"Dead".equals(device.status))
                            .collect(Collectors.toList()));
                    s.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                System.out.println("Error sending status to /user session: " + s.getId() + " " + e.getMessage());
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
                        String devId=Store.getDeviceIdBySessionId(sessionId);
                        System.out.println("Device "+devId+" out of heartbeat timeout ");
                        session.close();
                        devSessions.remove(sessionId);
                        lastHeartbeatTime.remove(sessionId);
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