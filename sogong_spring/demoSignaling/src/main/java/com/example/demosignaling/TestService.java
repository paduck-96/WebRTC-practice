package com.example.demosignaling;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

public interface TestService {
    ModelAndView displayMainPage(Long id, String uuid);
    ModelAndView processRoomSelection(String sid, String uuid, BindingResult bindingResult);
    ModelAndView displaySelectedRoom(String sid, String uuid);
    ModelAndView processRoomExit(String sid, String uuid);
}
