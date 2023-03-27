package com.example.demosignaling;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@AllArgsConstructor
@Log4j2
@Service
public class TestServiceImpl implements TestService{

    private final Optional<Long> parseId(String sid){
        Long id = null;
        try{
            id = Long.valueOf(sid);
        }catch(Exception e){
            log.info("error: "+e.getMessage());
        }
        return Optional.ofNullable(id);
    }
    private static final String REDIRECT = "redirect:/";

    private final RoomService roomService;

    @Override
    public ModelAndView displayMainPage(Long id, String uuid) {
        final ModelAndView modelAndView = new ModelAndView("main");
        modelAndView.addObject("id", id);
        modelAndView.addObject("rooms", roomService.getRooms());
        modelAndView.addObject("uuid", uuid);

        return modelAndView;
    }

    @Override
    public ModelAndView processRoomSelection(String sid, String uuid, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // simplified version, no errors processing
            return new ModelAndView(REDIRECT);
        }
        Optional<Long> optionalId = parseId(sid);
        optionalId.ifPresent(id -> Optional.ofNullable(uuid).ifPresent(name -> roomService.addRoom(new Room(id))));

        return this.displayMainPage(optionalId.orElse(null), uuid);
    }

    @Override
    public ModelAndView displaySelectedRoom(String sid, String uuid) {
        // redirect to main page if provided data is invalid
        ModelAndView modelAndView = new ModelAndView(REDIRECT);

        if (parseId(sid).isPresent()) {
            Room room = roomService.findRoomByStringId(sid).orElse(null);
            if(room != null && uuid != null && !uuid.isEmpty()) {
                log.info(uuid,"join room: ", sid);
                // open the chat room
                modelAndView = new ModelAndView("chat_room", "id", sid);
                modelAndView.addObject("uuid", uuid);
            }
        }

        return modelAndView;
    }

    @Override
    public ModelAndView processRoomExit(String sid, String uuid) {
        if(sid != null && uuid != null) {
            log.debug(uuid,"left room: ", sid);
            // implement any logic you need
        }
        return new ModelAndView(REDIRECT);
    }
}
