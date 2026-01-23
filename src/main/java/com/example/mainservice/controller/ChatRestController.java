package com.example.mainservice.controller;

import com.example.mainservice.dto.ChatMessageDTO;
import com.example.mainservice.dto.ConversationDTO;
import com.example.mainservice.dto.DoctorSearchDTO;
import com.example.mainservice.entity.ChatMessage;
import com.example.mainservice.entity.Conversation;
import com.example.mainservice.entity.User;
import com.example.mainservice.repository.UserRepository;
import com.example.mainservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*") // Configure properly for production
@Slf4j
public class ChatRestController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    // ========== EXISTING ENDPOINTS (Keep as is) ==========

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(Authentication authentication) {
        Integer userId = getCurrentUserId(authentication);

        List<Conversation> conversations = chatService.getUserConversations(userId);
        List<ConversationDTO> conversationDTOs = conversations.stream()
                .map(conv -> mapToConversationDTO(conv, userId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(conversationDTOs);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(
            @PathVariable Long conversationId,
            Authentication authentication) {

        Integer userId = getCurrentUserId(authentication);

        List<ChatMessage> messages = chatService.getConversationMessages(conversationId);
        List<ChatMessageDTO> messageDTOs = messages.stream()
                .map(this::mapToChatMessageDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(messageDTOs);
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long conversationId,
            Authentication authentication) {

        Integer userId = getCurrentUserId(authentication);
        chatService.markMessagesAsRead(conversationId, userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestBody ConversationDTO conversationDTO,
            Authentication authentication) {

        Integer userId = getCurrentUserId(authentication);

        Integer patientId = conversationDTO.getPatient().getId();
        Integer doctorId = conversationDTO.getDoctor().getId();

        Conversation conversation = chatService.createConversation(patientId, Long.valueOf(doctorId));

        return ResponseEntity.ok(mapToConversationDTO(conversation, userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(Authentication authentication) {
        Integer userId = getCurrentUserId(authentication);
        Integer count = chatService.getTotalUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    // ========== NEW ENDPOINTS FOR DOCTOR SEARCH ==========

    /**
     * Search for doctors by name or registration number
     * GET /api/chat/doctors/search?query=john
     * GET /api/chat/doctors/search?query=D001
     */
    @GetMapping("/doctors/search")
    public ResponseEntity<List<DoctorSearchDTO>> searchDoctors(
            @RequestParam(required = false) String query) {

        log.info("Search request received with query: {}", query);
        List<DoctorSearchDTO> doctors = chatService.searchDoctors(query);

        return ResponseEntity.ok(doctors);
    }

    /**
     * Get all doctors (for initial display)
     * GET /api/chat/doctors
     */
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorSearchDTO>> getAllDoctors() {
        log.info("Fetching all doctors");
        List<DoctorSearchDTO> doctors = chatService.getAllDoctors();

        return ResponseEntity.ok(doctors);
    }

    /**
     * Get a specific doctor by ID
     * GET /api/chat/doctors/123
     */
    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<DoctorSearchDTO> getDoctorById(@PathVariable Long doctorId) {
        log.info("Fetching doctor with ID: {}", doctorId);
        DoctorSearchDTO doctor = chatService.getDoctorById(doctorId);

        return ResponseEntity.ok(doctor);
    }

    /**
     * Search doctors by hospital
     * GET /api/chat/doctors/hospital?name=Nawaloka
     */
    @GetMapping("/doctors/hospital")
    public ResponseEntity<List<DoctorSearchDTO>> searchDoctorsByHospital(
            @RequestParam String name) {

        log.info("Searching doctors by hospital: {}", name);
        List<DoctorSearchDTO> doctors = chatService.searchDoctorsByHospital(name);

        return ResponseEntity.ok(doctors);
    }

    /**
     * Start a conversation - works for both PATIENT→DOCTOR and DOCTOR→PATIENT
     * POST /api/chat/conversations/start?doctorId=123 (Patient starting chat)
     * POST /api/chat/conversations/start?patientId=456 (Doctor starting chat)
     */
    @PostMapping("/conversations/start")
    public ResponseEntity<?> startConversation(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            Authentication authentication) {

        try {
            log.info("=== START CONVERSATION REQUEST ===");
            log.info("doctorId parameter: {}", doctorId);
            log.info("patientId parameter: {}", patientId);

            // Validate that exactly one parameter is provided
            if (doctorId == null && patientId == null) {
                log.error("Both doctorId and patientId are null");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Either doctorId or patientId is required"));
            }

            if (doctorId != null && patientId != null) {
                log.error("Both doctorId and patientId provided");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Provide either doctorId OR patientId, not both"));
            }

            // Get current user ID from authentication
            Integer currentUserId;
            try {
                currentUserId = getCurrentUserId(authentication);
                log.info("Current authenticated user ID: {}", currentUserId);
            } catch (Exception e) {
                log.error("Authentication failed: {}", e.getMessage());
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Authentication required", "details", e.getMessage()));
            }

            // Determine final patient and doctor IDs
            Integer finalPatientId;
            Integer finalDoctorId;

            if (doctorId != null) {
                // Current user is PATIENT, starting chat with DOCTOR
                finalPatientId = currentUserId;
                finalDoctorId = doctorId.intValue();
                log.info("Patient {} starting chat with Doctor {}", finalPatientId, finalDoctorId);
            } else {
                // Current user is DOCTOR, starting chat with PATIENT
                finalDoctorId = currentUserId;
                finalPatientId = patientId.intValue();
                log.info("Doctor {} starting chat with Patient {}", finalDoctorId, finalPatientId);
            }

            // Create or retrieve existing conversation
            Conversation conversation = chatService.createConversation(finalPatientId, Long.valueOf(finalDoctorId));
            log.info("Conversation created/retrieved with ID: {}", conversation.getId());

            // Map to DTO
            ConversationDTO dto = mapToConversationDTO(conversation, currentUserId);

            log.info("=== CONVERSATION START SUCCESS ===");
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            log.error("=== ERROR STARTING CONVERSATION ===", e);
            e.printStackTrace();

            return ResponseEntity.status(500)
                    .body(Map.of(
                            "error", e.getMessage() != null ? e.getMessage() : "Unknown error",
                            "type", e.getClass().getSimpleName(),
                            "timestamp", LocalDateTime.now().toString()
                    ));
        }
    }

    // ========== HELPER METHODS ==========

    private Integer getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            return Integer.parseInt(authentication.getName());
        }
        throw new RuntimeException("User not authenticated");
    }

    private ConversationDTO mapToConversationDTO(Conversation conv, Integer currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conv.getId());
        dto.setLastMessage(conv.getLastMessage());
        dto.setTimestamp(conv.getTimestamp());

        User patient = userRepository.findById(conv.getPatientId())
                .orElse(null);

        User doctor = userRepository.findById(conv.getDoctorId())
                .orElse(null);

        if (patient != null) {
            dto.setPatient(new ConversationDTO.UserInfo(
                    patient.getId(),
                    patient.getName(),
                    patient.getProfilePicture(),
                    false,
                    "Patient"
            ));
        }

        if (doctor != null) {
            dto.setDoctor(new ConversationDTO.UserInfo(
                    doctor.getId(),
                    doctor.getName(),
                    doctor.getProfilePicture(),
                    false,
                    doctor.getRole() != null ? doctor.getRole().toString() : "Doctor"
            ));
        }

        Integer unreadCount = chatService.getUnreadCount(conv.getId(), currentUserId);
        dto.setUnreadCount(unreadCount);
        dto.setOnline(false);

        return dto;
    }

    private ChatMessageDTO mapToChatMessageDTO(ChatMessage msg) {
        return ChatMessageDTO.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .senderId(msg.getSenderId())
                .receiverId(msg.getReceiverId())
                .content(msg.getContent())
                .type(msg.getType())
                .timestamp(msg.getTimestamp())
                .read(msg.getRead())
                .build();
    }
}