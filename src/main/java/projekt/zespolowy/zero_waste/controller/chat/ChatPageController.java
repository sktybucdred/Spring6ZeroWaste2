package projekt.zespolowy.zero_waste.controller.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import projekt.zespolowy.zero_waste.entity.User;
import projekt.zespolowy.zero_waste.services.UserService;

import java.security.Principal;

@Controller
public class ChatPageController {

    private final UserService userService;

    @Autowired
    public ChatPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/chat")
    public String chatPage(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("username", user.getUsername());
        return "/chat/chat";
    }

    @GetMapping("/chat-room")
    public String chatRoomPage(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("username", user.getUsername());
        return "/chat/chat-room";
    }
}
