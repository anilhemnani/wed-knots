package com.wedknots.delivery.provider;

import com.wedknots.delivery.DeliveryRequest;
import com.wedknots.delivery.DeliveryResult;

/**
 * Interface for message delivery providers
 * Implementations handle actual delivery via different channels
 */
public interface MessageDeliveryProvider {
    /**
     * Check if this provider can handle the delivery
     */
    boolean canDeliver(DeliveryRequest request);

    /**
     * Deliver the message through this provider's channel
     */
    DeliveryResult deliver(DeliveryRequest request);

    /**
     * Get the delivery mode this provider handles
     */
    String getProviderName();

    /**
     * Validate that the provider is properly configured
     */
    boolean isConfigured();
}

