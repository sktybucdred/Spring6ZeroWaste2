package projekt.zespolowy.zero_waste.controller.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import projekt.zespolowy.zero_waste.dto.chat.ChatMessageDTO;
import projekt.zespolowy.zero_waste.dto.chat.ChatRoomDTO;
import projekt.zespolowy.zero_waste.entity.User;
import projekt.zespolowy.zero_waste.entity.chat.ChatRoom;
import projekt.zespolowy.zero_waste.repository.ChatRoomRepository;
import projekt.zespolowy.zero_waste.services.chat.ChatMessageService;
import projekt.zespolowy.zero_waste.services.chat.ChatRoomService;
import projekt.zespolowy.zero_waste.services.UserService;

import java.util.List;
import java.util.Optional;

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
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @GetMapping("/api/chat/rooms/{chatRoomId}/messages")
    public List<ChatMessageDTO> getMessagesByChatRoom(@PathVariable Long chatRoomId) {
        return chatMessageService.getMessagesByChatRoom(chatRoomId);
    }

    @GetMapping("/api/chat/rooms")
    public List<ChatRoomDTO> getChatRooms(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());

        return chatMessageService.getAllChatRoomsForUser(user);
    }

    @GetMapping("/api/chat/rooms/{roomId}")
    public ResponseEntity<ChatRoom> getChatRoom(@PathVariable Long roomId) {
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findById(roomId);
        return chatRoomOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO chatMessageDTO) {
        User sender = userService.findByUsername((chatMessageDTO.getSender()));
        User receiver = userService.findByUsername((chatMessageDTO.getReceiver()));

        chatMessageService.saveMessage(chatMessageDTO, sender, receiver);

        messagingTemplate.convertAndSendToUser(receiver.getId().toString(), "/queue/messages", chatMessageDTO);
        messagingTemplate.convertAndSendToUser(sender.getId().toString(), "/queue/messages", chatMessageDTO);

        // Send a notification to the receiver
        String notification = "User " + sender.getUsername() + " sent you a message.";
        messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/user/queue/notifications", notification);
    }
}
