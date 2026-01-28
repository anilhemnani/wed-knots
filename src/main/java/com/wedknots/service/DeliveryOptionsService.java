package com.wedknots.service;

import com.wedknots.config.MessageDeliveryConfiguration;
import com.wedknots.delivery.provider.WhatsAppAdbProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to manage delivery options configuration
 */
@Service
public class DeliveryOptionsService {

    @Autowired
    private MessageDeliveryConfiguration.DeliveryProperties deliveryProperties;

    @Autowired(required = false)
    private WhatsAppAdbProvider whatsAppAdbProvider;

    /**
     * Get list of enabled delivery options
     */
    public List<DeliveryOption> getEnabledDeliveryOptions() {
        List<DeliveryOption> options = new ArrayList<>();

        if (deliveryProperties.getEmail().isEnabled()) {
            options.add(new DeliveryOption("email", "Email", "Send via email with subject, body and attachments", "bi-envelope"));
        }

        if (deliveryProperties.getSms().isEnabled()) {
            options.add(new DeliveryOption("sms", "SMS", "Send as text message", "bi-phone"));
        }

        if (deliveryProperties.getWhatsappPersonal() != null && deliveryProperties.getWhatsappPersonal().isEnabled()) {
            options.add(new DeliveryOption("whatsapp-personal", "WhatsApp (Browser)", "Send via WhatsApp Web using Selenium", "bi-whatsapp"));
        }

        // WhatsApp ADB - check both config AND device connectivity
        if (whatsAppAdbProvider != null && whatsAppAdbProvider.isConfigured()) {
            options.add(new DeliveryOption("whatsapp-adb", "WhatsApp (ADB)", "Send via WhatsApp using Android device", "bi-phone"));
        }

        if (deliveryProperties.getWhatsappBusiness() != null && deliveryProperties.getWhatsappBusiness().isEnabled()) {
            options.add(new DeliveryOption("whatsapp-business", "WhatsApp Business", "Send via WhatsApp Business API", "bi-whatsapp"));
        }

        if (deliveryProperties.getExternal().isEnabled()) {
            options.add(new DeliveryOption("external", "External/Manual", "Mark as sent externally", "bi-box-arrow-up-right"));
        }

        return options;
    }

    /**
     * Check if a delivery option is enabled
     */
    public boolean isDeliveryOptionEnabled(String deliveryMethod) {
        if (deliveryMethod == null) return false;

        switch (deliveryMethod.toLowerCase()) {
            case "email":
                return deliveryProperties.getEmail().isEnabled();
            case "sms":
                return deliveryProperties.getSms().isEnabled();
            case "whatsapp-personal":
                return deliveryProperties.getWhatsappPersonal() != null &&
                       deliveryProperties.getWhatsappPersonal().isEnabled();
            case "whatsapp-adb":
                // Check both config AND device connectivity
                return whatsAppAdbProvider != null && whatsAppAdbProvider.isConfigured();
            case "whatsapp-business":
                return deliveryProperties.getWhatsappBusiness() != null &&
                       deliveryProperties.getWhatsappBusiness().isEnabled();
            case "external":
                return deliveryProperties.getExternal().isEnabled();
            default:
                return false;
        }
    }

    /**
     * DTO for delivery option
     */
    public static class DeliveryOption {
        private String code;
        private String name;
        private String description;
        private String icon;

        public DeliveryOption(String code, String name, String description, String icon) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.icon = icon;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }
}

