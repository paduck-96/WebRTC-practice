package com.example.demosignaling;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketHandShake implements HandshakeInterceptor{
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            HttpHeaders headers = request.getHeaders();
            headers.add("Connection","upgrade");
            headers.add("Upgrade", "websocket");
            headers.add("Sec-Websocket-Version", "13");
            headers.add("Sec-Websocket-Key", "Rk84UEVDMjIxREVDMDhGMzY0M0M3OUY5");
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

        }
    }
