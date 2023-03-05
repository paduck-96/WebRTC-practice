package com.example.demosignaling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class DemoSignalingApplication {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        SpringApplication.run(DemoSignalingApplication.class, args);
    }
}
