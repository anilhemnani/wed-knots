package com.wedknots.delivery;

/**
 * Result of a delivery attempt
 * Contains information about success/failure and delivery details
 */
public class DeliveryResult {
    private boolean success;
    private DeliveryMode deliveryMode;
    private String deliveryId;
    private String status;
    private String errorMessage;
    private String timestamp;

    // Constructors
    public DeliveryResult() {}

    public DeliveryResult(boolean success, DeliveryMode deliveryMode, String deliveryId, String status) {
        this.success = success;
        this.deliveryMode = deliveryMode;
        this.deliveryId = deliveryId;
        this.status = status;
    }

    public DeliveryResult(boolean success, DeliveryMode deliveryMode, String errorMessage) {
        this.success = success;
        this.deliveryMode = deliveryMode;
        this.errorMessage = errorMessage;
        this.status = success ? "DELIVERED" : "FAILED";
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DeliveryMode getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(DeliveryMode deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

