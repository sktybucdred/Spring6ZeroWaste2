package projekt.zespolowy.zero_waste.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import projekt.zespolowy.zero_waste.dto.ChatMessageDTO;
import projekt.zespolowy.zero_waste.dto.ChatRoomDTO;
import projekt.zespolowy.zero_waste.entity.User;
import projekt.zespolowy.zero_waste.services.ChatMessageService;
import projekt.zespolowy.zero_waste.services.ChatRoomService;
import projekt.zespolowy.zero_waste.services.UserService;

import java.util.List;

@RestController
public class ChatController {
    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatRoomService chatRoomService;

    @GetMapping("/api/chat/rooms/{chatRoomId}/messages")
    public List<ChatMessageDTO> getMessagesByChatRoom(@PathVariable Long chatRoomId) {
        return chatMessageService.getMessagesByChatRoom(chatRoomId);
    }

    @GetMapping("/api/chat/rooms")
    public List<ChatRoomDTO> getChatRooms() {
        return chatMessageService.getAllChatRooms();
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO chatMessageDTO) {
        System.out.println(chatMessageDTO);
        User sender = userService.findByUsername((chatMessageDTO.getSender()));
        User receiver = userService.findByUsername((chatMessageDTO.getReceiver()));

        chatMessageService.saveMessage(chatMessageDTO, sender, receiver);

        messagingTemplate.convertAndSendToUser(receiver.getId().toString(), "/queue/messages", chatMessageDTO);
        messagingTemplate.convertAndSendToUser(sender.getId().toString(), "/queue/messages", chatMessageDTO);
    }
}
