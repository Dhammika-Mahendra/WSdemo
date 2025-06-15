package com.example.wsdemo.WShandler;

import com.example.wsdemo.model.DeviceInfo;
import com.example.wsdemo.util.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserWebSocketHandler extends TextWebSocketHandler {

    static final ConcurrentHashMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        String token = extractToken(query);

        // Validate the token
        if (token == null || token.isEmpty()) {
            session.close(CloseStatus.POLICY_VIOLATION);
            System.out.println("User connection rejected: Missing token");
            return;
        }

        // validating user
        char lastLetter = token.charAt(token.length() - 1);
        boolean isValidUser = Store.isUserExists(String.valueOf(lastLetter));
        if (!isValidUser) {
            session.close(CloseStatus.POLICY_VIOLATION);
            System.out.println("User connection rejected: Invalid token");
            return;
        }

        // Proceed with connection if token is valid
        userSessions.put(session.getId(), session);
        System.out.println("User connected on /user: " + session.getId());

        // Filter and send active devices
        var activeDevices = Store.getDeviceRecords().stream()
                .filter(device -> "1".equals(device.active) && !"Dead".equals(device.status))
                .collect(Collectors.toList());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(activeDevices);
        session.sendMessage(new TextMessage(json));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        DeviceInfo deviceInfo = objectMapper.readValue(message.getPayload(), DeviceInfo.class);

        // Extract token from query
        String query = session.getUri().getQuery();
        String token = extractToken(query);

        if (token == null || token.isEmpty()) {
            System.out.println("Invalid token in message");
            return;
        }

        // Extract user (last letter of the token)
        char lastLetter = token.charAt(token.length() - 1);
        String user = String.valueOf(lastLetter);
        if (!Store.isUserExists(user)) {
            System.out.println("Invalid user in token");
            return;
        }

        // Get current device status
        String currentStatus = Store.getDeviceRecords().stream()
                .filter(device -> device.id.equals(deviceInfo.getId()))
                .map(device -> device.status)
                .findFirst()
                .orElse(null);

        // Validate status transition
        boolean validTransition = false;
        if ("Charging".equals(deviceInfo.getStatus()) && "Live".equals(currentStatus)) {
            validTransition = true;
        } else if ("Live".equals(deviceInfo.getStatus()) && "Charging".equals(currentStatus)) {
            validTransition = true;
        }

        if (validTransition) {
            Store.useDevice(deviceInfo.getId(), deviceInfo.getStatus(), user);

            var activeDevices = Store.getDeviceRecords().stream()
                    .filter(device -> "1".equals(device.active) && !"Dead".equals(device.status))
                    .collect(Collectors.toList());

            String updatedJson = objectMapper.writeValueAsString(activeDevices);
            for (WebSocketSession userSession : userSessions.values()) {
                if (userSession.isOpen()) {
                    userSession.sendMessage(new TextMessage(updatedJson));
                }
            }
        } else {
            System.out.println("Invalid status transition from " + currentStatus + " to " + deviceInfo.getStatus());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        userSessions.remove(session.getId());
        System.out.println("User disconnected from /user: " + session.getId());
    }

    private String extractToken(String query) {
        if (query == null) return null;
        return Arrays.stream(query.split("&"))
                .filter(param -> param.startsWith("token="))
                .map(param -> param.split("=")[1])
                .findFirst()
                .orElse(null);
    }

}