package com.wedknots.model;

public enum RSVPStatus {
    ATTENDING,
    NOT_ATTENDING,
    PENDING,
    MAYBE;

    public static RSVPStatus fromString(String value) {
        for (RSVPStatus status : RSVPStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No RSVPStatus with value: " + value);
    }
}
