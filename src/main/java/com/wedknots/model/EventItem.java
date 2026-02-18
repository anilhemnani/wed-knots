package com.wedknots.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event_item_tbl")
public class EventItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit")
    private String unit; // e.g., "pieces", "kg", "boxes", etc.

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "needed_by_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate neededByDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ItemStatus status = ItemStatus.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Bidirectional relationship: Item is needed for a specific Activity/Ceremony
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "needed_for_activity_id")
    private EventActivity neededForActivity;

    @Column(name = "responsible")
    private String responsible; // Name of person responsible for sourcing this item

    // Bidirectional relationship: Item belongs to Supplier
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    // Bidirectional relationship: Item belongs to Event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private WeddingEvent event;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        calculateTotalPrice();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Helper method to get eventId
    public Long getEventId() {
        return event != null ? event.getId() : null;
    }

    // Helper method to get supplierId
    public Long getSupplierId() {
        return supplier != null ? supplier.getId() : null;
    }

    // Helper method to get supplier name
    public String getSupplierName() {
        return supplier != null ? supplier.getName() : "No Supplier";
    }

    // Helper method to get neededForActivityId
    public Long getNeededForActivityId() {
        return neededForActivity != null ? neededForActivity.getId() : null;
    }

    // Helper method to get activity name
    public String getNeededForActivityName() {
        return neededForActivity != null ? neededForActivity.getName() : null;
    }
}

