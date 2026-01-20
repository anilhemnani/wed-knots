package com.wedknots.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Records which phone number(s) of a guest received an invitation.
 * Allows tracking which specific phone numbers were contacted when sending invitations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invitation_phone_record_tbl",
       indexes = {
           @Index(name = "idx_invitation_guest", columnList = "invitation_id, guest_id"),
           @Index(name = "idx_invitation_log_phone", columnList = "invitation_log_id, guest_phone_number_id")
       })
public class InvitationPhoneRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_log_id", nullable = false)
    private InvitationLog invitationLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_phone_number_id", nullable = false)
    private GuestPhoneNumber guestPhoneNumber;

    @Column(name = "phone_number")
    private String phoneNumber; // Denormalized for easy querying

    @Column(name = "is_primary")
    private Boolean isPrimary; // Was this the primary phone at time of sending?

    @Column(name = "phone_type")
    private String phoneType; // PERSONAL, WORK, OTHER

    @Column(name = "contact_method")
    private String contactMethod; // How was this number contacted? WHATSAPP, SMS, CALL, EMAIL, IN_PERSON

    @Column(name = "delivery_status")
    @Builder.Default
    private String deliveryStatus = "PENDING"; // PENDING, SENT, DELIVERED, FAILED, UNDELIVERABLE

    @Column(name = "delivery_timestamp")
    private LocalDateTime deliveryTimestamp;

    @Column(name = "error_message")
    private String errorMessage; // If delivery failed

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }
}

