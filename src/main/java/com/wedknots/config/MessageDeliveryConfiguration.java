package com.wedknots.config;

import com.wedknots.delivery.DeliveryConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for message delivery system
 * Loads settings from application.yml
 */
@Configuration
@EnableConfigurationProperties(MessageDeliveryConfiguration.DeliveryProperties.class)
public class MessageDeliveryConfiguration {

    /**
     * Create DeliveryConfig bean from application.yml properties
     */
    @Bean
    public DeliveryConfig deliveryConfig(DeliveryProperties props) {
        DeliveryConfig config = new DeliveryConfig();

        // Email config
        DeliveryConfig.EmailConfig emailConfig = new DeliveryConfig.EmailConfig();
        emailConfig.setEnabled(props.getEmail().isEnabled());
        emailConfig.setSenderEmail(props.getEmail().getSenderEmail());
        emailConfig.setSenderName(props.getEmail().getSenderName());
        config.setEmailConfig(emailConfig);

        // SMS config
        DeliveryConfig.SMSConfig smsConfig = new DeliveryConfig.SMSConfig();
        smsConfig.setEnabled(props.getSms().isEnabled());
        smsConfig.setProvider(props.getSms().getProvider());
        smsConfig.setApiKey(props.getSms().getApiKey());
        smsConfig.setApiSecret(props.getSms().getApiSecret());
        smsConfig.setSenderId(props.getSms().getSenderId());
        smsConfig.setApiUrl(props.getSms().getApiUrl());
        config.setSmsConfig(smsConfig);

        // WhatsApp config
        DeliveryConfig.WhatsAppConfig whatsAppConfig = new DeliveryConfig.WhatsAppConfig();
        whatsAppConfig.setEnabled(props.getWhatsapp().isEnabled());
        whatsAppConfig.setApiKey(props.getWhatsapp().getApiKey());
        whatsAppConfig.setPhoneNumberId(props.getWhatsapp().getPhoneNumberId());
        whatsAppConfig.setBusinessAccountId(props.getWhatsapp().getBusinessAccountId());
        config.setWhatsAppConfig(whatsAppConfig);

        // External config
        DeliveryConfig.ExternalConfig externalConfig = new DeliveryConfig.ExternalConfig();
        externalConfig.setEnabled(props.getExternal().isEnabled());
        externalConfig.setServiceUrl(props.getExternal().getServiceUrl());
        externalConfig.setApiKey(props.getExternal().getApiKey());
        config.setExternalConfig(externalConfig);

        return config;
    }

    /**
     * Properties class for delivery configuration
     */
    @ConfigurationProperties(prefix = "delivery")
    public static class DeliveryProperties {
        private EmailProperties email = new EmailProperties();
        private SMSProperties sms = new SMSProperties();
        private WhatsAppProperties whatsapp = new WhatsAppProperties();
        private WhatsAppPersonalProperties whatsappPersonal = new WhatsAppPersonalProperties();
        private WhatsAppAdbProperties whatsappAdb = new WhatsAppAdbProperties();
        private WhatsAppBusinessProperties whatsappBusiness = new WhatsAppBusinessProperties();
        private ExternalProperties external = new ExternalProperties();

        // Getters
        public EmailProperties getEmail() {
            return email;
        }

        public SMSProperties getSms() {
            return sms;
        }

        public WhatsAppProperties getWhatsapp() {
            return whatsapp;
        }

        public WhatsAppPersonalProperties getWhatsappPersonal() {
            return whatsappPersonal;
        }

        public WhatsAppAdbProperties getWhatsappAdb() {
            return whatsappAdb;
        }

        public WhatsAppBusinessProperties getWhatsappBusiness() {
            return whatsappBusiness;
        }

        public ExternalProperties getExternal() {
            return external;
        }

        // Inner classes
        public static class EmailProperties {
            private boolean enabled;
            private String senderEmail;
            private String senderName = "WedKnots";

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

        public static class SMSProperties {
            private boolean enabled;
            private String provider;
            private String apiKey;
            private String apiSecret;
            private String senderId;
            private String apiUrl;

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

        public static class WhatsAppProperties {
            private boolean enabled;
            private String apiKey;
            private String phoneNumberId;
            private String businessAccountId;

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

        public static class ExternalProperties {
            private boolean enabled;
            private String serviceUrl;
            private String apiKey;

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

        public static class WhatsAppPersonalProperties {
            private boolean enabled;
            private SeleniumProperties selenium = new SeleniumProperties();

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public SeleniumProperties getSelenium() {
                return selenium;
            }

            public void setSelenium(SeleniumProperties selenium) {
                this.selenium = selenium;
            }

            public static class SeleniumProperties {
                private String driverType;
                private String driverPath;
                private String profileDir;
                private String binary;
                private boolean headless;

                public String getDriverType() {
                    return driverType;
                }

                public void setDriverType(String driverType) {
                    this.driverType = driverType;
                }

                public String getDriverPath() {
                    return driverPath;
                }

                public void setDriverPath(String driverPath) {
                    this.driverPath = driverPath;
                }

                public String getProfileDir() {
                    return profileDir;
                }

                public void setProfileDir(String profileDir) {
                    this.profileDir = profileDir;
                }

                public String getBinary() {
                    return binary;
                }

                public void setBinary(String binary) {
                    this.binary = binary;
                }

                public boolean isHeadless() {
                    return headless;
                }

                public void setHeadless(boolean headless) {
                    this.headless = headless;
                }
            }
        }

        public static class WhatsAppAdbProperties {
            private boolean enabled;
            private String deviceId;
            private String adbPath;
            private int timeoutSeconds = 30;
            private int humanDelayMinMs = 500;
            private int humanDelayMaxMs = 2000;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getDeviceId() {
                return deviceId;
            }

            public void setDeviceId(String deviceId) {
                this.deviceId = deviceId;
            }

            public String getAdbPath() {
                return adbPath;
            }

            public void setAdbPath(String adbPath) {
                this.adbPath = adbPath;
            }

            public int getTimeoutSeconds() {
                return timeoutSeconds;
            }

            public void setTimeoutSeconds(int timeoutSeconds) {
                this.timeoutSeconds = timeoutSeconds;
            }

            public int getHumanDelayMinMs() {
                return humanDelayMinMs;
            }

            public void setHumanDelayMinMs(int humanDelayMinMs) {
                this.humanDelayMinMs = humanDelayMinMs;
            }

            public int getHumanDelayMaxMs() {
                return humanDelayMaxMs;
            }

            public void setHumanDelayMaxMs(int humanDelayMaxMs) {
                this.humanDelayMaxMs = humanDelayMaxMs;
            }
        }

        public static class WhatsAppBusinessProperties {
            private boolean enabled;
            private String apiKey;
            private String phoneNumberId;
            private String businessAccountId;
            private String apiUrl;

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

            public String getApiUrl() {
                return apiUrl;
            }

            public void setApiUrl(String apiUrl) {
                this.apiUrl = apiUrl;
            }
        }
    }
}
