package com.wedknots.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guest_tbl")
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_name")
    private String familyName;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_email")
    private String contactEmail;

    // Phone numbers managed via GuestPhoneNumber entity (one-to-many relationship)
    @OneToMany(mappedBy = "guest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<GuestPhoneNumber> phoneNumbers = new java.util.ArrayList<>();

    @Column(name = "side")
    private String side;

    @Column(name = "address")
    private String address;

    @Column(name = "max_attendees")
    private int maxAttendees;

    @Column(name = "expected_arrival_date")
    private LocalDate expectedArrivalDate;

    @Column(name = "expected_departure_date")
    private LocalDate expectedDepartureDate;

    @Column(name = "expected_attendance")
    @Enumerated(EnumType.STRING)
    private ExpectedAttendance expectedAttendance;

    // Bidirectional relationship: Guest belongs to Event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private WeddingEvent event;

    // Aggregation: Guest owns RSVP - cascade all operations, orphan removal enabled
    @OneToOne(mappedBy = "guest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RSVP rsvp;

    // Travel information for this guest
    @OneToOne(mappedBy = "guest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TravelInfo travelInfo;

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

    // Helper method to get eventId for backward compatibility
    public Long getEventId() {
        return event != null ? event.getId() : null;
    }

    // Helper method to set eventId for backward compatibility
    public void setEventId(Long eventId) {
        // This is handled through setEvent() method
        // Kept for backward compatibility but does nothing
    }

    // Helper method to get primary phone number
    public GuestPhoneNumber getPrimaryPhoneNumber() {
        return phoneNumbers.stream()
                .filter(phone -> Boolean.TRUE.equals(phone.getIsPrimary()))
                .findFirst()
                .orElse(phoneNumbers.isEmpty() ? null : phoneNumbers.get(0));
    }

    // Helper method to get primary phone number as string
    public String getPrimaryPhoneNumberString() {
        GuestPhoneNumber primary = getPrimaryPhoneNumber();
        return primary != null ? primary.getPhoneNumber() : null;
    }

    // Helper method for backward compatibility - get contact phone (for views)
    public String getContactPhone() {
        return getPrimaryPhoneNumberString();
    }

    // Helper method for backward compatibility - set contact phone
    public void setContactPhone(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            // Only for migration/setting single phone - actual management via phoneNumbers collection
            if (phoneNumbers.isEmpty()) {
                GuestPhoneNumber primary = GuestPhoneNumber.builder()
                        .guest(this)
                        .phoneNumber(phoneNumber)
                        .phoneType(GuestPhoneNumber.PhoneType.PERSONAL)
                        .isPrimary(true)
                        .build();
                phoneNumbers.add(primary);
            } else {
                // Update existing primary
                GuestPhoneNumber primary = getPrimaryPhoneNumber();
                if (primary != null) {
                    primary.setPhoneNumber(phoneNumber);
                }
            }
        }
    }
}
