package com.example.wsdemo.config;

import com.example.wsdemo.WShandler.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new CustomWebSocketHandler(), "/ws").setAllowedOrigins("*");
        registry.addHandler(new ChatWebSocketHandler("/chat"), "/chat").setAllowedOrigins("*");
        registry.addHandler(new ChatWebSocketHandler("/chat2"), "/chat2").setAllowedOrigins("*");
        registry.addHandler(new DeviceWebSocketHandler(), "/dev").setAllowedOrigins("*");
        registry.addHandler(new ControllerWebSocketHandler(), "/con").setAllowedOrigins("*");
        registry.addHandler(new UserWebSocketHandler(), "/user").setAllowedOrigins("*");
    }
}