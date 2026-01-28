package com.wedknots.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guest_phone_number_tbl")
public class GuestPhoneNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    @ToString.Exclude
    private Guest guest;

    // Denormalized event_id for efficient querying and unique constraint
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "phone_type")
    @Enumerated(EnumType.STRING)
    private PhoneType phoneType;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "contact_first_name")
    private String contactFirstName;

    @Column(name = "contact_last_name")
    private String contactLastName;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum PhoneType {
        PERSONAL,
        WORK,
        OTHER
    }
}
