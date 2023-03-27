package com.example.demosignaling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@Log4j2
@Service
@AllArgsConstructor
public class RoomService {
    private final Optional<Long> parseId(String sid){
        Long id = null;
        try{
            id = Long.valueOf(sid);
        }catch(Exception e){
            log.info("error: "+e.getMessage());
        }
        return Optional.ofNullable(id);
    }

    private final Set<Room> rooms = new TreeSet<>(Comparator.comparing(Room::getId));

    public Set<Room> getRooms() {
        final TreeSet<Room> roomlist = new TreeSet<>(Comparator.comparing(Room::getId));
        roomlist.addAll(rooms);

        return roomlist;
    }

    public Boolean addRoom(Room room){
        return rooms.add(room);
    }

    public Optional<Room> findRoomByStringId(String sid){
        return rooms.stream().filter(r->r.getId().equals(parseId(sid).get())).findAny();
    }

    public Long getRoomId(Room room){
        return room.getId();
    }

    public Map<String, WebSocketSession> getClients( Room room) {
        return Optional.ofNullable(room)
                .map(r -> Collections.unmodifiableMap(r.getMembers()))
                .orElse(Collections.emptyMap());
    }

    public WebSocketSession addClient( Room room,  String name,  WebSocketSession session) {
        return room.getMembers().put(name, session);
    }

    public WebSocketSession removeClientByName( Room room,  String name) {
        return room.getMembers().remove(name);
    }
}
