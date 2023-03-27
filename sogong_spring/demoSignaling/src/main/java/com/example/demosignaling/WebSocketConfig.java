package com.example.demosignaling;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocket
@AllArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SignalingHandler signalingHandler;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        WebSocketHandShake  handshakeInterceptor = new WebSocketHandShake();

        registry.addHandler(signalingHandler, "/signal")
                .setHandshakeHandler(new DefaultHandshakeHandler())
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
