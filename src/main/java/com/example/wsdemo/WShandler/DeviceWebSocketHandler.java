package com.example.wsdemo.WShandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(DeviceWebSocketHandler.class);
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
        logger.info("Device connected on /dev: {}", session.getId());
        session.sendMessage(new TextMessage("Connected to /dev"));
        notifyControllers("live");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        logger.info("Received message on /dev: {} from session: {}", message.getPayload(), session.getId());

        if ("ok".equals(message.getPayload())) {
            lastHeartbeatTime.put(session.getId(), System.currentTimeMillis());
            return;
        }

        // Forward other messages to all connected /con sessions
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
                        logger.info("Device disconnected due to heartbeat timeout: {}", sessionId);
                        notifyControllers("dead");
                    } catch (IOException e) {
                        logger.error("Error closing timed out session: {}", sessionId, e);
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        devSessions.remove(session.getId());
        lastHeartbeatTime.remove(session.getId());
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