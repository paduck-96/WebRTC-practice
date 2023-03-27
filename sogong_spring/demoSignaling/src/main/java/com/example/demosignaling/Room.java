package com.example.demosignaling;

import lombok.*;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Data
@ToString
@EqualsAndHashCode
public class Room {
    // 방에 저장되는 사용자
    private final Long id;

    private final Map<String, WebSocketSession> members = new HashMap<>();
    Map<String, WebSocketSession> getMembers() {
        return members;
    }

}
