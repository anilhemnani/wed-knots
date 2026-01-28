package com.wedknots.delivery;

import com.wedknots.model.Guest;
import com.wedknots.model.WeddingEvent;

/**
 * Request object for sending messages or invitations through the delivery service
 * Decouples the sender from delivery implementation details
 */
public class DeliveryRequest {
    private String messageId;
    private String messageType; // "INVITATION" or "MESSAGE"
    private String title;
    private String content;
    private Guest recipient;
    private WeddingEvent event;
    private DeliveryMode preferredMode;
    private String senderEmail;
    private String senderPhone;
    private String overridePhoneNumber;
    private String overrideContactName;

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Guest getRecipient() {
        return recipient;
    }

    public void setRecipient(Guest recipient) {
        this.recipient = recipient;
    }

    public WeddingEvent getEvent() {
        return event;
    }

    public void setEvent(WeddingEvent event) {
        this.event = event;
    }

    public DeliveryMode getPreferredMode() {
        return preferredMode;
    }

    public void setPreferredMode(DeliveryMode preferredMode) {
        this.preferredMode = preferredMode;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getOverridePhoneNumber() {
        return overridePhoneNumber;
    }

    public void setOverridePhoneNumber(String overridePhoneNumber) {
        this.overridePhoneNumber = overridePhoneNumber;
    }

    public String getOverrideContactName() {
        return overrideContactName;
    }

    public void setOverrideContactName(String overrideContactName) {
        this.overrideContactName = overrideContactName;
    }

    // Builder pattern for convenience
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String messageId;
        private String messageType;
        private String title;
        private String content;
        private Guest recipient;
        private WeddingEvent event;
        private DeliveryMode preferredMode;
        private String senderEmail;
        private String senderPhone;
        private String overridePhoneNumber;
        private String overrideContactName;

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder recipient(Guest recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder event(WeddingEvent event) {
            this.event = event;
            return this;
        }

        public Builder preferredMode(DeliveryMode preferredMode) {
            this.preferredMode = preferredMode;
            return this;
        }

        public Builder senderEmail(String senderEmail) {
            this.senderEmail = senderEmail;
            return this;
        }

        public Builder senderPhone(String senderPhone) {
            this.senderPhone = senderPhone;
            return this;
        }

        public Builder overridePhoneNumber(String overridePhoneNumber) {
            this.overridePhoneNumber = overridePhoneNumber;
            return this;
        }

        public Builder overrideContactName(String overrideContactName) {
            this.overrideContactName = overrideContactName;
            return this;
        }

        public DeliveryRequest build() {
            DeliveryRequest request = new DeliveryRequest();
            request.messageId = this.messageId;
            request.messageType = this.messageType;
            request.title = this.title;
            request.content = this.content;
            request.recipient = this.recipient;
            request.event = this.event;
            request.preferredMode = this.preferredMode;
            request.senderEmail = this.senderEmail;
            request.senderPhone = this.senderPhone;
            request.overridePhoneNumber = this.overridePhoneNumber;
            request.overrideContactName = this.overrideContactName;
            return request;
        }
    }
}
