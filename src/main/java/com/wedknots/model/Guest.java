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


    @Column(name = "contact_first_name")
    private String contactFirstName;

    @Column(name = "contact_last_name")
    private String contactLastName;

    @Column(name = "contact_email")
    private String contactEmail;

    // Primary phone number stored directly in Guest table
    // Contact name for primary phone is same as guest's contactFirstName/contactLastName
    @Column(name = "primary_phone_number")
    private String primaryPhoneNumber;


    // Additional phone numbers managed via GuestPhoneNumber entity (one-to-many relationship)
    @OneToMany(mappedBy = "guest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<GuestPhoneNumber> phoneNumbers = new java.util.ArrayList<>();

    @Column(name = "side")
    private String side;

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

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

    @Version
    private Long version;

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

    // Helper method for backward compatibility - get contact phone (for views)
    public String getContactPhone() {
        return primaryPhoneNumber;
    }

    // Helper method for backward compatibility - set contact phone
    public void setContactPhone(String phoneNumber) {
        this.primaryPhoneNumber = phoneNumber;
    }

    // Get all phone numbers (primary + additional)
    public java.util.List<String> getAllPhoneNumbers() {
        java.util.List<String> allNumbers = new java.util.ArrayList<>();
        if (primaryPhoneNumber != null && !primaryPhoneNumber.trim().isEmpty()) {
            allNumbers.add(primaryPhoneNumber);
        }
        phoneNumbers.forEach(phone -> {
            if (phone.getPhoneNumber() != null && !phone.getPhoneNumber().trim().isEmpty()) {
                allNumbers.add(phone.getPhoneNumber());
            }
        });
        return allNumbers;
    }
}
