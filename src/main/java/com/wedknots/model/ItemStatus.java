package com.wedknots.model;

/**
 * Status for event items that need to be sourced from suppliers
 */
public enum ItemStatus {
    PENDING,        // Item needs to be ordered
    ORDERED,        // Item has been ordered from supplier
    DELIVERED,      // Item has been delivered
    NOT_NEEDED      // Item is no longer needed
}

