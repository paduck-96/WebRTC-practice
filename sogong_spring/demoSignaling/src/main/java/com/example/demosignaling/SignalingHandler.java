package com.example.demosignaling;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
@AllArgsConstructor
public class SignalingHandler extends TextWebSocketHandler {
    //private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // 세션 아이디와 방 번호 연결시키기
    private Map<String, Room> membersToRoom;
    private final RoomService roomService;

    // json 변환
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 시그널링에서 사용되는 메시지 타입 정의
    private static final String MSG_TYPE_TEXT = "text";
    // SDP Offer message
    private static final String MSG_TYPE_OFFER = "offer";
    // SDP Answer message
    private static final String MSG_TYPE_ANSWER = "answer";
    // New ICE Candidate message
    private static final String MSG_TYPE_ICE = "ice";
    // join room data message
    private static final String MSG_TYPE_JOIN = "join";
    // leave room data message
    private static final String MSG_TYPE_LEAVE = "leave";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("입장된 session: "+session);
        // 사용자 입장 시 전달되는 값이 모두 참일 경우에 실행
        //아닐 경우 대기
        sendMessage(session, new WebSocketMessage("Server", MSG_TYPE_JOIN, Boolean.toString(!membersToRoom.isEmpty()), null, null));
        //sessions.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception{
        log.info("["+status+"]"+"leave: "+session);
        membersToRoom.remove(session.getId());
        //sessions.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
//        for (WebSocketSession webSocketSession : sessions.values()) {
//            if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
//                webSocketSession.sendMessage(message);
//            }
//        }
        // 클라이언트로 전달받은 메시지
        try{
            // 내가 받은 메시지 변환 - 값이랑 클래스
            WebSocketMessage message = objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class);
            log.info("message type: "+message.getType()+" message from: "+message.getFrom());
            String member = message.getFrom();
            String data = message.getData();
            // 방과 매핑
            Room room;
            // 클라이언트에서 전달받은 메시지 타입에 따라 코드 변경
            switch(message.getType()){
                // txt 전달
                case MSG_TYPE_TEXT:
                    log.info("텍스트 메시지: "+message.getData());
                            break;
                // peer 연결 위해 연결
                case MSG_TYPE_OFFER:
                case MSG_TYPE_ANSWER:
                case MSG_TYPE_ICE:
                    Object ice = message.getIce();
                    Object sdp = message.getSdp();
                    log.info("signal info: "+ice != null ? ice.toString().substring(0,64):sdp.toString().substring(0, 64));
                    // 해당 방에 데이터 전달
                    Room rm = membersToRoom.get(session.getId());
                    if( rm!= null){
                        Map<String, WebSocketSession> members = roomService.getClients(rm);
                        for(Map.Entry<String, WebSocketSession> client : members.entrySet())  {
                            // 나 빼고 메시지 보내기
                            if (!client.getKey().equals(member)) {
                                // 해당 타입 전송
                                sendMessage(client.getValue(),
                                        new WebSocketMessage(
                                                member,
                                                message.getType(),
                                                data,
                                                ice,
                                                sdp));
                            }
                        }
                    }
                    break;

                    //ice 정보
                case MSG_TYPE_JOIN:
                    // 데이터 값에 방 정보 있으니까 가져오기
                    log.info(member,"join room: ", message.getData());
                    room = roomService.findRoomByStringId(data)
                            .orElseThrow(() -> new IOException("방 번호 오류"));
                    // 방에 사용자 저장
                    roomService.addClient(room, member, session);
                    membersToRoom.put(session.getId(), room);
                    break;

                case MSG_TYPE_LEAVE:
                    log.info(member,"leave room: ", message.getData());
                    // 방에서 사용자 삭제
                    room = membersToRoom.get(session.getId());
                    Optional<String> client = roomService.getClients(room).entrySet().stream()
                            .filter(entry -> Objects.equals(entry.getValue().getId(), session.getId()))
                            .map(Map.Entry::getKey)
                            .findAny();
                    client.ifPresent(c -> roomService.removeClientByName(room, c));
                    break;

                // something should be wrong with the received message, since it's type is unrecognizable
                default:
                    log.info("error: ", message.getType());
                    // handle this if needed
            }
        }catch(IOException e){
            log.info("error: ", e.getMessage());
        }
    }




    private void sendMessage(WebSocketSession session, WebSocketMessage message){
        try{
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }catch(Exception e){
            log.info("error: ",e.getMessage());
        }
    }

}
