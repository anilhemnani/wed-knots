package com.wedknots.delivery;

import java.io.Serializable;

/**
 * Configuration for message delivery
 * Contains details for each delivery channel (email, SMS, WhatsApp, etc.)
 */
public class DeliveryConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    // Email Configuration
    private EmailConfig emailConfig;

    // SMS Configuration
    private SMSConfig smsConfig;

    // WhatsApp Configuration
    private WhatsAppConfig whatsAppConfig;

    // External Configuration (for third-party services)
    private ExternalConfig externalConfig;

    // Getters and Setters
    public EmailConfig getEmailConfig() {
        return emailConfig;
    }

    public void setEmailConfig(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    public SMSConfig getSmsConfig() {
        return smsConfig;
    }

    public void setSmsConfig(SMSConfig smsConfig) {
        this.smsConfig = smsConfig;
    }

    public WhatsAppConfig getWhatsAppConfig() {
        return whatsAppConfig;
    }

    public void setWhatsAppConfig(WhatsAppConfig whatsAppConfig) {
        this.whatsAppConfig = whatsAppConfig;
    }

    public ExternalConfig getExternalConfig() {
        return externalConfig;
    }

    public void setExternalConfig(ExternalConfig externalConfig) {
        this.externalConfig = externalConfig;
    }

    // Inner Classes for each delivery channel config
    public static class EmailConfig implements Serializable {
        private boolean enabled = true;
        private String senderEmail;
        private String senderName = "WedKnots";

        public EmailConfig() {}

        public EmailConfig(boolean enabled, String senderEmail, String senderName) {
            this.enabled = enabled;
            this.senderEmail = senderEmail;
            this.senderName = senderName;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getSenderEmail() {
            return senderEmail;
        }

        public void setSenderEmail(String senderEmail) {
            this.senderEmail = senderEmail;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }
    }

    public static class SMSConfig implements Serializable {
        private boolean enabled = false;
        private String provider;
        private String apiKey;
        private String apiSecret;
        private String senderId;
        private String apiUrl;

        public SMSConfig() {}

        public SMSConfig(boolean enabled, String provider, String apiKey, String apiSecret, String senderId) {
            this.enabled = enabled;
            this.provider = provider;
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
            this.senderId = senderId;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiSecret() {
            return apiSecret;
        }

        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }

        public String getSenderId() {
            return senderId;
        }

        public void setSenderId(String senderId) {
            this.senderId = senderId;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }
    }

    public static class WhatsAppConfig implements Serializable {
        private boolean enabled = false;
        private String apiKey;
        private String phoneNumberId;
        private String businessAccountId;

        public WhatsAppConfig() {}

        public WhatsAppConfig(boolean enabled, String apiKey, String phoneNumberId, String businessAccountId) {
            this.enabled = enabled;
            this.apiKey = apiKey;
            this.phoneNumberId = phoneNumberId;
            this.businessAccountId = businessAccountId;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getPhoneNumberId() {
            return phoneNumberId;
        }

        public void setPhoneNumberId(String phoneNumberId) {
            this.phoneNumberId = phoneNumberId;
        }

        public String getBusinessAccountId() {
            return businessAccountId;
        }

        public void setBusinessAccountId(String businessAccountId) {
            this.businessAccountId = businessAccountId;
        }
    }

    public static class ExternalConfig implements Serializable {
        private boolean enabled = false;
        private String serviceUrl;
        private String apiKey;

        public ExternalConfig() {}

        public ExternalConfig(boolean enabled, String serviceUrl, String apiKey) {
            this.enabled = enabled;
            this.serviceUrl = serviceUrl;
            this.apiKey = apiKey;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getServiceUrl() {
            return serviceUrl;
        }

        public void setServiceUrl(String serviceUrl) {
            this.serviceUrl = serviceUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
