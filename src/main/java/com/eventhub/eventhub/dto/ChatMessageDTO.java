package com.eventhub.eventhub.dto;

import com.eventhub.eventhub.entity.ChatMessage;
import java.time.LocalDateTime;

public class ChatMessageDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String messageText;
    private LocalDateTime sentAt;
    private boolean isCurrentUser;
    private String senderProfileImageUrl;
    private String receiverProfileImageUrl;

    public ChatMessageDTO(ChatMessage message) {
        this.id = message.getId();
        this.senderId = message.getSender().getId();
        this.receiverId = message.getReceiver().getId();
        this.messageText = message.getMessageText();
        this.sentAt = message.getSentAt();
        this.senderProfileImageUrl = message.getSender().getProfileImageUrl();
        this.receiverProfileImageUrl = message.getReceiver().getProfileImageUrl();
    }

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean isCurrentUser) {
        this.isCurrentUser = isCurrentUser;
    }
    public String getSenderProfileImageUrl() {
        return senderProfileImageUrl;
    }

    public void setSenderProfileImageUrl(String senderProfileImageUrl) {
        this.senderProfileImageUrl = senderProfileImageUrl;
    }

    public String getReceiverProfileImageUrl() {
        return receiverProfileImageUrl;
    }

    public void setReceiverProfileImageUrl(String receiverProfileImageUrl) {
        this.receiverProfileImageUrl = receiverProfileImageUrl;
    }

}