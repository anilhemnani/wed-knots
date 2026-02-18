package com.wedknots.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event_activity_tbl")
public class EventActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location")
    private String location;

    @Column(name = "start_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;

    @Column(name = "activity_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ActivityType activityType = ActivityType.ACTIVITY;

    @Column(name = "visible_to_guests")
    @Builder.Default
    private Boolean visibleToGuests = true;

    @Column(name = "dress_code")
    private String dressCode;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "sort_order")
    private Integer sortOrder;

    // Bidirectional relationship: Activity belongs to Event
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
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to get eventId
    public Long getEventId() {
        return event != null ? event.getId() : null;
    }

    // Helper method to check if activity is ongoing
    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return startTime != null && endTime != null &&
               now.isAfter(startTime) && now.isBefore(endTime);
    }

    // Helper method to check if activity is upcoming
    public boolean isUpcoming() {
        return startTime != null && LocalDateTime.now().isBefore(startTime);
    }

    // Helper method to check if activity is completed
    public boolean isCompleted() {
        return endTime != null && LocalDateTime.now().isAfter(endTime);
    }
}

