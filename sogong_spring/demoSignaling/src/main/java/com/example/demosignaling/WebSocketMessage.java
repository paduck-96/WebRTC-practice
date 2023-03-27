package com.example.demosignaling;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class WebSocketMessage {
    private String from;
    private String type;
    private String data;
    private Object ice;
    private Object sdp;


}
