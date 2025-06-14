package com.example.wsdemo.WShandler;

import com.example.wsdemo.model.DeviceCommand;
import com.example.wsdemo.model.DeviceInfo;
import com.example.wsdemo.util.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;


public class ControllerWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        DeviceWebSocketHandler.conSessions.put(session.getId(), session);
        System.out.println("Controller connected on /con: " + session.getId());
        // Send current device status on connection
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(Store.getDeviceRecords());
        session.sendMessage(new TextMessage(json));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        try {
            // Unwrap incoming payload
            ObjectMapper objectMapper = new ObjectMapper();
            DeviceCommand deviceCmd = objectMapper.readValue(message.getPayload(), DeviceCommand.class);

            String deviceId = deviceCmd.getId();
            if (!Store.isValidDevice(deviceId)) {
                System.out.println("Invalid device ID: " + deviceId);
                return;
            }

            String deviceSessionId = Store.getSessionIdByDeviceId(deviceId);
            if (deviceSessionId == null) {
                System.out.println("Device not connected: " + deviceId);
                return;
            }

            if(deviceCmd.getType().equals("ACTIVE")){
                Store.saveDeviceActive(deviceId, deviceCmd.getCommand());
                ObjectMapper objectMapper2 = new ObjectMapper();
                String json = objectMapper2.writeValueAsString(Store.getDeviceRecords());
                session.sendMessage(new TextMessage(json));//new updates back to controller
            }
            WebSocketSession deviceSession = DeviceWebSocketHandler.devSessions.get(deviceSessionId);
            if (deviceSession != null && deviceSession.isOpen()) {
                String commandJson = objectMapper.writeValueAsString(deviceCmd);
                deviceSession.sendMessage(new TextMessage(commandJson));
                System.out.println("Command sent to device " + deviceId + ": " + deviceCmd.getCommand());
            } else {
                System.out.println("Device session not active: " + deviceId);
            }
        } catch (Exception e) {
            System.out.println("Error processing command: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        DeviceWebSocketHandler.conSessions.remove(session.getId());
        System.out.println("Controller disconnected on /con: " + session.getId() + ", status: " + status);
    }
}