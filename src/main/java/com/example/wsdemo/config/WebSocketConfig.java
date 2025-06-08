package com.example.wsdemo.config;

import com.example.wsdemo.WShandler.CustomWebSocketHandler;
import com.example.wsdemo.WShandler.SecondWebSocketHandler;
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
        registry.addHandler(new SecondWebSocketHandler(), "/ws2").setAllowedOrigins("*");
    }
}