package com.wedknots.delivery;

/**
 * Enumeration of supported delivery modes for messages and invitations
 */
public enum DeliveryMode {
    EMAIL("email", "Email"),
    SMS("sms", "SMS Text Message"),
    WHATSAPP_PERSONAL("whatsapp_personal", "Personal WhatsApp (Selenium)"),
    WHATSAPP_ADB("whatsapp_adb", "WhatsApp via ADB"),
    INTERNAL_MESSAGE("internal_message", "Internal Message"),
    EXTERNAL("external", "External (Manual)");

    private final String code;
    private final String displayName;

    DeliveryMode(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DeliveryMode fromCode(String code) {
        for (DeliveryMode mode : DeliveryMode.values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown delivery mode: " + code);
    }
}

