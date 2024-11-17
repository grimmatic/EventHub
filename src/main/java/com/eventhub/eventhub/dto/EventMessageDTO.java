package com.eventhub.eventhub.dto;

import com.eventhub.eventhub.entity.EventMessage;

import java.time.LocalDateTime;

public class EventMessageDTO {
    private Long id;
    private Long senderId;
    private Long eventId;
    private String message;
    private LocalDateTime timestamp;
    private boolean isCurrentUser;
    private String firstName;
    private String lastName;


    public EventMessageDTO(EventMessage message) {
        this.id = message.getId();
        this.senderId = message.getSender().getId();
        this.eventId = message.getEvent().getId();
        this.message = message.getMessage();
        this.timestamp = message.getTimestamp();
        this.firstName = message.getSender().getFirstName();
        this.lastName = message.getSender().getLastName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}